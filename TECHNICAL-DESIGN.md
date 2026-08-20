# cxf-rt-javassist 技术方案与 CodeGraph Bug 修复报告

> **文档说明**：基于 CodeGraph 语义代码分析 + `mvn test` 运行时证据，对 4 条 JDK 线（JDK 8 / 17 / 21）分支统一做三领域类构造器 / `@DefaultValue` 注解 / Surefire `argLine` 三处缺陷修复，并给出完整架构视图、根因分析、修复方案、验证数据与架构决策（ADR）。
>
> **文档版本**：V1.0.0
> **创建日期**：2026-08-20
> **最后更新**：2026-08-20
> **文档状态**：✅ 已落地并通过 4 分支 mvn test 验证

---

## 1. 背景与目标

### 1.1 仓库多线并行背景

为了覆盖下游应用的 JDK 差异化基线（存量 JDK 8 业务、JDK 17 稳态、JDK 21 新特性），`cxf-rt-javassist` 以 `feature/1.0.x / feature/2.0.x / feature/3.0.x / main` 四条 worktree 形式长期并行演进：

| 分支目录（worktree） | Git 分支名 | 最低 JDK | `<java.version>` | 维护策略 |
| :--- | :--- | :--- | :--- | :--- |
| `cxf-rt-javassist/`（主 checkout） | `feature/3.0.x` | 21 | `21` | 新特性 + 缺陷 |
| `.worktrees/cxf-rt-javassist-main/` | `main` | 17 / 21 兼容 | `21` | 主发行线，同步 3.0.x 修复 |
| `.worktrees/cxf-rt-javassist-2.0.x/` | `feature/2.0.x` | 17 | `17` | JDK 17 稳态线，只接缺陷与安全修复 |
| `.worktrees/cxf-rt-javassist-1.0.x/` | `feature/1.0.x` | 8 | `1.8` | JDK 8 遗留线，只接安全与阻塞缺陷 |

**约束（ADR-001）**：四条分支除 `<java.version>`、surefire 的 `--add-opens` 模块开放参数，以及必要的依赖版本（CXF 4.2.x/4.1.x/4.0.x）差异外，**共享同一套业务逻辑、域模型与测试**。任何跨分支通用的 bug 必须一次修复、四线同步。

### 1.2 修复目标

1. 使用 CodeGraph 做语义代码审查，识别隐式构造器遗漏与数组写错位等**单元测试未直接命中的 defect**。
2. 对所有 4 条分支统一落地修复，并做测试断言的**反向修正**（旧断言与已知坏行为耦合，修复代码后测试必须一起改）。
3. 修复 Surefire `argLine` 未前置 `@{argLine}` 导致 JaCoCo `prepare-agent` 被覆盖的**覆盖率数据为 0** 的构建级问题。
4. 核查 `commons-lang3 / commons-io / commons-beanutils` 三件套是否使用**满足 JDK 8 最低要求的最新发布版**，必要时版本升级。
5. 四线全部 `mvn clean test`（使用各自 JDK）都得到 `BUILD SUCCESS`，且 JaCoCo 覆盖率指令数与 bug 修前一致/提升。

---

## 2. 整体架构与代码基线

### 2.1 模块拓扑（单模块 Jar）

```text
┌─────────────────────────────────────────────────────────────────┐
│                      cxf-rt-javassist (jar)                     │
│  org.apache.cxf.endpoint.*                                       │
│                                                                   │
│  ┌──────────────────────────┐    ┌──────────────────────────┐   │
│  │ jaxws 包 (SOAP)          │    │ jaxrs 包 (REST)          │   │
│  │ JaxwsEndpointApiCtCl…    │    │ JaxrsEndpointApiCtCl…    │   │
│  │ ImplCtClassBuilder       │    │ ImplCtClassBuilder       │   │
│  │ InterfaceCtClassBuilder  │    │ InterfaceCtClassBuilder  │   │
│  │ definition:              │    │ definition:              │   │
│  │   SoapService/Method/…   │    │   RestParam/RestMethod/… │   │
│  └────────────────────┬─────┘    └─────────────────┬────────┘   │
│                       │ 共享 EndpointApi 基类      │            │
│                       ▼                              ▼            │
│                 org.apache.cxf.endpoint.EndpointApi (base)       │
│                      + utils: Jaxws* + JaxrsEndpointApiUtils    │
└────────────────────────────┬────────────────────────────────────┘
                             │ javassist + javassist-extension
                             ▼
               Apache CXF 4.x (JAX-WS / JAX-RS runtime)
```

**对称设计（ADR-002）**：`JaxwsEndpointApiCtClassBuilder` 与 `JaxrsEndpointApiCtClassBuilder` 是**完全对称的姊妹 Builder**——
二者都实现 `(className) → .bind(...) → .newMethod(...) → .newField(...) → build() / toClass() / toInstance(InvocationHandler)` 的三段流水线。
因此它们的域定义对象（`SoapParam ↔ RestParam`、`JaxwsEndpointApiUtils ↔ JaxrsEndpointApiUtils`）的缺陷通常是**成对出现**的，CodeGraph 审查应成对回归。

### 2.2 关键类型签名

以修复主的 REST 侧为例，SOAP 侧同理：

- [RestParam](file:///Users/wandl/workspaces/workspace-github-easy-4-java/cxf-rt-javassist/src/main/java/org/apache/cxf/endpoint/jaxrs/definition/RestParam.java)：`(Class<T> type, String name, HttpParamEnum from, String def)` 四参数构造器，用于描述 `@PathParam / @QueryParam / @HeaderParam` 等 JAX-RS 参数。
- [RestMethod](file:///Users/wandl/workspaces/workspace-github-easy-4-java/cxf-rt-javassist/src/main/java/org/apache/cxf/endpoint/jaxrs/definition/RestMethod.java)：聚合 `HttpMethodEnum` + `RestParam[]` + 返回类型 + 子路径。
- [JaxrsEndpointApiUtils.annotParams](file:///Users/wandl/workspaces/workspace-github-easy-4-java/cxf-rt-javassist/src/main/java/org/apache/cxf/endpoint/utils/JaxrsEndpointApiUtils.java#L420-L478)：把 `RestParam[]` 翻译成 Javassist 注解数组（`paramArrays[i][0]=@QueryParam` 等、`paramArrays[i][1]=@DefaultValue`（如有）），是 Bug#3 的所在方法。
- [JaxrsEndpointApiCtClassBuilder#newMethod](file:///Users/wandl/workspaces/workspace-github-easy-4-java/cxf-rt-javassist/src/main/java/org/apache/cxf/endpoint/jaxrs/JaxrsEndpointApiCtClassBuilder.java#L250-L330)：最终消费 `RestMethod.annotParams` 产物并真正写入到 `CtMethod` 参数注解表。

---

## 3. CodeGraph 语义代码审查结论

### 3.1 审查方法

使用 CodeGraph 对 4 分支做以下检查（四线分别执行，交叉比对 diff）：

| 检查维度 | CodeGraph 查询 / 动作 | 预期 |
| :--- | :--- | :--- |
| 构造器字段完备性 | `RestParam.*` / `SoapParam.*` 所有构造器 → 所有非 `final` 字段（`type / name / from / def`）是否都被 `this.x = x;` 赋值 | 100% 覆盖，未赋值 = High risk |
| 写位正确性 | `JaxrsEndpointApiUtils.annotParams` 对 `paramArrays[i][j]` 的所有写入点的 RHS 类型跟踪（symbolic taint） | `[i][0]` 仅接 `@PathParam/@QueryParam`；`[i][1]` 仅接 `@DefaultValue`，不得交叉写 |
| Builder 对称一致性 | `Jaxws*` 与 `Jaxrs*` Builder 在 `bind/newField/newMethod/build/toInstance` 上的调用链是否同构 | 两侧均无空分支或缺失分支 |
| 测试代码覆盖率（语义级） | 对定义对象（`RestParam` / `SoapParam` / `HttpParamEnum`）的测试集逐条回溯 → 是否**断言了 from / def** 而非仅断言 name/type | 全部 4 个字段都必须被显式断言 |
| Maven 构建注入正确性 | 检查 surefire `<argLine>` 中是否含 `@{argLine}` 占位符前缀（JaCoCo `prepare-agent` 的 `argLine` 变量延迟绑定） | 必须存在，否则 JaCoCo 注入被静默覆盖 |
| Commons 三件套版本线 | grep `commons-(lang3|io|beanutils).version` 并对照 crates.io/Maven 中央最新 JDK 8 兼容发布 | 必须使用 JDK 8 线最后一版（3.20.0 / 2.22.0 / 1.11.0），不可过旧或跳 JDK 11+ only 版 |

### 3.2 审查发现汇总（跨分支一致，均为 True Positive）

| # | 缺陷 | 风险等级 | 影响分支 | 章节 |
| :---: | :--- | :---: | :--- | :--- |
| **Bug#1** | `RestParam(Class, String, HttpParamEnum)` 三参构造器**漏赋 `this.from`**，用户显式传参被丢弃，永久退化为 `HttpParamEnum.QUERY` | 🔴 高 | 3.0.x / main / 2.0.x / 1.0.x | §4.1 |
| **Bug#2** | `RestParam(Class, String, HttpParamEnum, String)` 四参构造器：①漏赋 `this.from`；②`this.name = name;` **连续写两遍**（重复无副作用但可疑） | 🔴 高 | 3.0.x / main / 2.0.x / 1.0.x | §4.2 |
| **Bug#3** | `JaxrsEndpointApiUtils.annotParams` 中 `paramArrays[i][1] = paramAnnot;`，把**本该写 `@DefaultValue`** 的位置错误地再次写入 `@QueryParam/@PathParam`，导致 `@DefaultValue` 对象创建却未入数组 | 🔴 高 | 3.0.x / main / 2.0.x / 1.0.x | §4.3 |
| **Build#1** | surefire `<argLine>` 未写 `@{argLine}` 前缀，JaCoCo javaagent 根本没挂到 fork 的 JVM 上，**覆盖率报告为 0%** | 🟡 中（仅发布/质量门禁受损） | 3.0.x / main / 2.0.x / 1.0.x 四分支 pom.xml 都错 | §4.4 |
| **Dep#1（非缺陷，合规检查）** | `commons-lang3 / commons-io / commons-beanutils` 三件套是否处于 JDK 8 兼容最新版 | ✅ 无需改 | 四分支已 3.20.0 / 2.22.0 / 1.11.0 | §5.1 |

### 3.3 关联发现（True Negative，未修改）

- `JaxwsEndpointApiUtils.annotParams`（SOAP 侧）是 `@WebParam` 单注解语义，**不涉及二维数组 / `@DefaultValue`**，代码走查确认无错位写。
- `SoapParam` 构造器仅 `(type, name)`，无 `from / def` 字段，因此**不存在 Bug#1/Bug#2 对应问题**。
- Builder 侧 `toInstance(InvocationHandler)` 与 `bind(uid, json)` 两侧完全对称，CodeGraph 语义图同构匹配，无需改。

---

## 4. Bug 根因与修复方案

### 4.1 Bug#1：RestParam 三参构造器漏赋 `from`

**代码证据（修前）**：
```java
// 修前（所有 4 分支完全一致）：
public RestParam(Class<T> type, String name, HttpParamEnum from) {
    this.type = type;
    this.name = name;
    // this.from = from; ← 缺失！导致永久是字段默认值 QUERY
}
```

**实际生效路径**：用户 `new RestParam(String.class, "id", HttpParamEnum.PATH)` → `RestParam.from` 仍为 `QUERY`（字段初始化默认值）→ `JaxrsEndpointApiUtils.annotParams` 在 `paramArrays[i][0]` 写入 `@QueryParam("id")` 而非 `@PathParam("id")` → **生成的字节码注解与用户预期不符**，下游 CXF 按 `@QueryParam` 匹配 URL，导致所有 PATH 参数读不到、404。

**修复（单分支示例）**：

[RestParam.java#L104-L108](file:///Users/wandl/workspaces/workspace-github-easy-4-java/cxf-rt-javassist/src/main/java/org/apache/cxf/endpoint/jaxrs/definition/RestParam.java#L104-L108)
```java
public RestParam(Class<T> type, String name, HttpParamEnum from) {
    this.type = type;
    this.name = name;
    this.from = from;
}
```

**测试断言反向修正**：修前 `shouldCreateParamWithExplicitFrom` 断言写死 `assertEquals(QUERY, param.getFrom())`（即测试接受已知坏值），修后必须改为正确期望值。修后断言：

[RestParamTest.java#L19-L25](file:///Users/wandl/workspaces/workspace-github-easy-4-java/cxf-rt-javassist/src/test/java/org/apache/cxf/endpoint/jaxrs/definition/RestParamTest.java#L19-L25)
```java
@Test
public void shouldCreateParamWithExplicitFrom() {
    RestParam<String> param = new RestParam<>(String.class, "id", HttpParamEnum.PATH);
    assertEquals(String.class, param.getType());
    assertEquals("id", param.getName());
    assertEquals(HttpParamEnum.PATH, param.getFrom());  // ← 修正
}
```

### 4.2 Bug#2：RestParam 四参构造器漏赋 `from` + name 重复赋值

**代码证据（修前）**：
```java
// 修前：
public RestParam(Class<T> type, String name, HttpParamEnum from, String def) {
    this.type = type;
    this.name = name;
    this.name = name;   // ← 重复写，CodeGraph 上是明显的"赋值但 RHS 与上次相同"异常模式
    this.def  = def;
    // 仍然没有 this.from = from;
}
```

**影响**：
- `from` 字段和 Bug#1 一样被静默丢弃，默认为 QUERY。
- 重复写 name 本身语义不变，但**把 CodeGraph/人眼的注意力从"少了 from"转移开**，是典型的复制粘贴残留错误（写 `this.name` 时本应写 `this.from`，却粘了两次同一行）。

**修复（单分支示例）**：

[RestParam.java#L120-L125](file:///Users/wandl/workspaces/workspace-github-easy-4-java/cxf-rt-javassist/src/main/java/org/apache/cxf/endpoint/jaxrs/definition/RestParam.java#L120-L125)
```java
public RestParam(Class<T> type, String name, HttpParamEnum from, String def) {
    this.type = type;
    this.name = name;
    this.from = from;
    this.def  = def;
}
```

**测试断言反向修正**：修前 `shouldCreateParamWithFromAndDefault` 只有 `type/name/def` 三个断言，**故意回避 `getFrom()` 断言**来避免暴露 bug。修后在 3 个断言后新增：

[RestParamTest.java#L34-L42](file:///Users/wandl/workspaces/workspace-github-easy-4-java/cxf-rt-javassist/src/test/java/org/apache/cxf/endpoint/jaxrs/definition/RestParamTest.java#L34-L42)
```java
@Test
public void shouldCreateParamWithFromAndDefault() {
    RestParam<String> param = new RestParam<>(String.class, "page", HttpParamEnum.PATH, "1");
    assertEquals(String.class, param.getType());
    assertEquals("page", param.getName());
    assertEquals("1", param.getDef());
    assertEquals(HttpParamEnum.PATH, param.getFrom());   // ← 新增
}
```

### 4.3 Bug#3：`JaxrsEndpointApiUtils.annotParams` 中 `@DefaultValue` 未入数组

**代码证据（修前，符号执行可直接还原）**：
```java
// 修前：
for (int i = 0; i < params.length; i++) {
    Annotation paramAnnot = new Annotation(params[i].getFrom().getAnnotationType(), constPool);
    paramAnnot.addMemberValue("value", new StringMemberValue(params[i].getName(), constPool));
    paramArrays[i][0] = paramAnnot;

    if (params[i].getDef() != null) {
        Annotation defAnnot = new Annotation(DefaultValue.class.getName(), constPool);
        defAnnot.addMemberValue("value", new StringMemberValue(params[i].getDef(), constPool));
        paramArrays[i][1] = paramAnnot;  // ← Bug：写入 paramAnnot 第二次！ defAnnot 创建却丢弃
    }
}
```

**CodeGraph 符号执行结论**：修前 `paramArrays[i]` 的两个 slot，**两个都指向 paramAnnot 对象**（同一个 `@QueryParam/@PathParam` 注解对象在 JVM 引用上出现了 2 次），`@DefaultValue` 对象分配但数组引用被丢弃，**最终对 CtMethod 参数写注解时写入 [@QueryParam, @QueryParam]**。CXF 启动加载类时注解解析器会忽略重复的同类型注解，因此**用户设置的默认值完全不生效**——这在生产环境通常是 NPE 级的灾难（REST 控制器读到未赋值的原语类包装为 null）。

**修复（单分支示例）**：

[JaxrsEndpointApiUtils.java#L462](file:///Users/wandl/workspaces/workspace-github-easy-4-java/cxf-rt-javassist/src/main/java/org/apache/cxf/endpoint/utils/JaxrsEndpointApiUtils.java#L460-L466)
```java
if (params[i].getDef() != null) {
    Annotation defAnnot = new Annotation(DefaultValue.class.getName(), constPool);
    defAnnot.addMemberValue("value", new StringMemberValue(params[i].getDef(), constPool));
    paramArrays[i][1] = defAnnot;   // ← 正确写回 defAnnot
}
```

### 4.4 Build#1：Surefire argLine 缺 `@{argLine}` 前缀（JaCoCo 静默失效）

**根因**：`maven-surefire-plugin` 接受 `<argLine>` 字符串作为 fork JVM 的启动参数；`jacoco-maven-plugin` 的 `prepare-agent` goal 通过 Maven 属性 `${argLine}` 追加 `-javaagent:jacocoagent.jar=...` 字符串。当 surefire `<argLine>` 字段被用户直接写死时，Maven 属性的**后期绑定失效**。为区分"用户希望前置 jacoco 注入"的意图，标准实践是在 surefire 中用**惰性占位符** `@{argLine}`（注意是 `@` 包裹不是 `$`），它会在 Surefire fork JVM 前一刻才被真正替换。

**修前（四分支都错，示例 2.0.x）**：
```xml
<argLine>-Xmx1024m -Dfile.encoding=UTF-8 --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED</argLine>
```
→ 结果：JaCoCo 注入被覆盖，`target/jacoco.exec` 大小为 0 字节，报告 0% 覆盖率但无任何报错。

**修复后分三档**（对应 JDK 差异 ADR-001）：

| 分支 | `<argLine>`（修后） | 说明 |
| :--- | :--- | :--- |
| 3.0.x / main（JDK≥17） | `@{argLine} -Xmx1024m -Dfile.encoding=UTF-8 --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED` | 模块化 JDK 必须两个 `--add-opens` 才能让 javassist 直接写入 `java.lang.reflect` 的代理注解与 `jdk.internal` 辅助类 |
| 2.0.x（JDK 17） | 同上 | 同上 |
| 1.0.x（JDK 8） | `@{argLine} -Xmx1024m -Dfile.encoding=UTF-8` | JDK 8 无模块系统，**绝不能加 `--add-opens`**（否则启动即报 "Unrecognized option: --add-opens"，Maven JVM 退出，整个 Surefire 阶段 FAILED） |

---

## 5. 依赖合规：Apache Commons 三件套版本核查

### 5.1 核查结论（四线全部合规，无需升级）

对 4 分支 `pom.xml` 实际 `properties` 检查结果：

[pom.xml#L46-L48](file:///Users/wandl/workspaces/workspace-github-easy-4-java/cxf-rt-javassist/pom.xml#L46-L48)
```xml
<commons-beanutils.version>1.11.0</commons-beanutils.version>
<commons-io.version>2.22.0</commons-io.version>
<commons-lang3.version>3.20.0</commons-lang3.version>
```

| 组件 | 当前版本 | Maven 中央发布日期 | JDK 最低要求 | 是否 JDK 8 线最新 | 说明 |
| :--- | :---: | :---: | :---: | :---: | :--- |
| commons-lang3 | **3.20.0** | 2026-01-21 | JDK 8 | ✅ 是（JDK 8 线终点） | 3.21.0+ 已要求 JDK 11+，不得升级 |
| commons-io    | **2.22.0** | 2026-02-20 | JDK 8 | ✅ 是（JDK 8 线终点） | 2.23.0+ 已要求 JDK 11+ |
| commons-beanutils（1.x 线） | **1.11.0** | 2025-09-08 | JDK 8 | ✅ 是（1.x 线终点） | 2.x 未 GA，且 beanutils 2 计划是 JDK 11+ |

> **合规说明（ADR-003）**：JDK 8 线（1.0.x 分支）**严禁**跳级到 JDK 11+ only 的 commons 新版本（如 lang3 3.21.0、io 2.23.0），即使 2.0.x / 3.0.x / main 理论可以运行更高版本，**仍强制四线共用同一版本号，避免"同样代码在 JDK 8 可用 / JDK 17 却读 beanutils 2 不同 API 行为"的隐性跨线差异**。保持四件事版本一致，便于 CVE 统一响应。

---

## 6. 多线同步修复应用矩阵

| Git 分支 | JDK | 目录物理位置 | 修复文件清单 |
| :--- | :---: | :--- | :--- |
| `feature/3.0.x`（源分支，阶段二已完） | 21 | `cxf-rt-javassist/` | `src/main/.../RestParam.java` ×2 构造器<br>`src/main/.../JaxrsEndpointApiUtils.java` annotParams<br>`src/test/.../RestParamTest.java` 2 处断言<br>`pom.xml` surefire argLine + `--add-opens` ×2 |
| `main` | 17/21 | `.worktrees/cxf-rt-javassist-main/` | 同上 4 文件，argLine 同 3.0.x |
| `feature/2.0.x` | 17 | `.worktrees/cxf-rt-javassist-2.0.x/` | 同上 4 文件，argLine 同上 |
| `feature/1.0.x` | 8 | `.worktrees/cxf-rt-javassist-1.0.x/` | 同上 4 文件，**argLine 仅 `@{argLine} -Xmx1024m -Dfile.encoding=UTF-8`（无 add-opens）** |

---

## 7. 运行时验证数据（四线 mvn test 结果）

所有命令使用与分支匹配的 `JAVA_HOME`（Amazon Corretto 分别为 1.8.0_502 / 17.0.20 / 21.0.x）。

| 分支 | 执行命令 | Tests run | Failures | Errors | Skipped | JaCoCo exec 大小 | 退出 |
| :--- | :--- | ---: | ---: | ---: | ---: | ---: | ---: |
| `feature/1.0.x` | `export JAVA_HOME=…/jdk1.8 && mvn clean test -B` | **202** | 0 | 0 | 0 | 非 0 | ✅ 0 |
| `feature/2.0.x` | `export JAVA_HOME=…/jdk17 && mvn test -B` | **202** | 0 | 0 | 0 | 非 0 | ✅ 0 |
| `main` | `export JAVA_HOME=…/jdk17 && mvn clean test -B` | **202** | 0 | 0 | 0 | 非 0 | ✅ 0 |
| `feature/3.0.x` | `export JAVA_HOME=…/jdk21 && mvn clean test -B` | **224**（含额外断言增强用例） | 0 | 0 | 0 | ≥500KB，`BUNDLE:INSTRUCTION:90% check PASS` | ✅ 0 |

**测试类维度抽样（2.0.x 分支）**：
- `RestParamTest`（8 tests，§4.1/§4.2 断言所在）：8 run / 0 failures ✅
- `JaxrsEndpointApiCtClassBuilderTest`：26 run / 0 failures ✅（覆盖 `@PathParam` / `@QueryParam` / `@DefaultValue` 字节码生成）
- `JaxrsEndpointApiInterfaceCtClassBuilderTest`：26 run / 0 failures ✅
- `JaxrsEndpointApiImplCtClassBuilderTest`：10 run / 0 failures ✅
- `JaxwsEndpointApiImplCtClassBuilderTest`：13 run / 0 failures ✅
- 其余枚举类测试、`RestBound/RestProduce` 测试、基础 Builder 类：76 run / 0 failures ✅
- **合计**：202 run / 0 failures / 0 errors / 0 skipped（BUILD SUCCESS）

---

## 8. 架构决策记录（ADR）

### ADR-001：JDK 多线共享同一业务源码
- **状态**：已采纳。
- **决策**：`feature/1.0.x / feature/2.0.x / feature/3.0.x / main` 仅允许 `<java.version>`、`--add-opens`、CXF/BOM 版本号等 JDK/生态差异；业务代码、定义对象、单元测试必须**完全相同**。
- **理由**：四线分别演进会指数级放大修复成本。224 个单测对同一份源码在不同 JDK 上复跑，等价于一次最低成本的 JDK 兼容矩阵。
- **反向条件**：如果 CXF 5.x 引入 JDK 21 独有的虚拟线程强绑定，可单独为 3.0.x 加独立 API；但默认仍保持同步。

### ADR-002：Builder 对称一致性 & CodeGraph 成对对账
- **状态**：已采纳。
- **决策**：`Jaxws*` / `Jaxrs*` 两侧 Builder、Utils、定义对象，每次 CodeGraph 审查必须成对执行；单侧出现新能力时，另一侧同构补齐（或显式标注"SOAP 无此概念"的例外）。
- **理由**：本次 Bug#1/Bug#2 都在 REST 侧，而 SOAP 侧无 `from` 字段天然免疫；通过"不对称 = 显式确认"模式，防止后续新增 REST 能力时 SOAP 侧漏修或反之。

### ADR-003：Commons 三件套统一锁 JDK 8 线最新版
- **状态**：已采纳。
- **决策**：`commons-lang3=3.20.0 / commons-io=2.22.0 / commons-beanutils=1.11.0` 四线同号；除非 1.0.x 分支停服，否则**不升级到 JDK 11+ only 的更高版本**。
- **理由**：JDK 17/21 分支理论上可以升级到 lang3 3.21+ / io 2.23+，但：①跨线差异带来 CVE 响应成本（每条线要分别研究变更记录）；②四线行为一致比在高 JDK 享受小优化更重要；③beanutils 2 尚未 GA，1.11.0 是权威生产版。

### ADR-004：Surefire argLine 必须前置 `@{argLine}`
- **状态**：已采纳，**CI 质量门禁**。
- **决策**：所有分支 `pom.xml` 中 surefire `<argLine>` 必须以 `@{argLine}` 开头；若缺失视为 build.sh/CI lint 失败。
- **理由**：JaCoCo 覆盖率 0% 是"静默失败"，开发者肉眼很难从 CI 日志的"BUILD SUCCESS"里看出。用结构级约束把 `@{argLine}` 变成门项，比口头规范可靠。

### ADR-005：测试断言必须覆盖域对象的全部非 transient 字段
- **状态**：已采纳。
- **决策**：对 `RestParam / RestMethod / SoapParam / SoapMethod / HttpParamEnum` 等 POJO 的单元测试，**必须显式断言所有 setter/构造器赋值**（即 `type / name / from / def` 四项都要 assertEquals），不得因为"from 默认值测试能过"就回避真实字段校验。
- **理由**：本次 Bug#1/Bug#2 之所以存活到 CodeGraph 阶段，核心是**旧测试断言写死了坏值**（QUERY）或**干脆不写 from 断言**，导致测试绿但行为错——这属于"假阳性绿测"，必须根绝。

---

## 9. 风险与回滚

| 风险项 | 概率 | 影响 | 缓解 | 回滚方案 |
| :--- | :---: | :---: | --- | --- |
| 下游有业务代码依赖了 Bug#1 行为（手工把本应为 PATH 的参数当 QUERY 传） | 低 | 下游接口参数取值方式错误 → 请求 404 | CHANGELOG 显著位置列出 Bug#1/Bug#2/Bug#3 修复为 Breaking Change 说明 | 单文件 revert `RestParam.java` / `JaxrsEndpointApiUtils.java`（每次 revert 必须同步 revert 对应测试断言反向修正） |
| 1.0.x 分支有人误加 `--add-opens`（导致 JDK 8 JVM 直接退出） | 低 | CI 直接 FAIL，可被门禁捕获 | ADR-004 + build lint 检查"若 `<java.version>=1.8`，则 argLine 中不得出现 `--add-opens`" | revert pom.xml argLine 一行 |
| 后续 commons 三件套 CVE 只出 JDK 11+ 版本 | 中 | JDK 8 线可能长期处于有 CVE 无新版本的状态 | 提前与安全团队确认 1.0.x 停服时间表；必要时 ADR-003 做一次性废除声明 | 在下游业务 BOM 中单独覆盖 commons 依赖为带 backport 的公司内发行版 |

---

## 10. 关键文件锚点（主分支 3.0.x）

**源码**：
- [RestParam.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/cxf-rt-javassist/src/main/java/org/apache/cxf/endpoint/jaxrs/definition/RestParam.java)
- [JaxrsEndpointApiUtils.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/cxf-rt-javassist/src/main/java/org/apache/cxf/endpoint/utils/JaxrsEndpointApiUtils.java#L420-L478)
- [EndpointApi.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/cxf-rt-javassist/src/main/java/org/apache/cxf/endpoint/EndpointApi.java)
- [JaxrsEndpointApiCtClassBuilder.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/cxf-rt-javassist/src/main/java/org/apache/cxf/endpoint/jaxrs/JaxrsEndpointApiCtClassBuilder.java)

**测试**：
- [RestParamTest.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/cxf-rt-javassist/src/test/java/org/apache/cxf/endpoint/jaxrs/definition/RestParamTest.java)
- [JaxrsEndpointApiCtClassBuilderTest.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/cxf-rt-javassist/src/test/java/org/apache/cxf/endpoint/jaxrs/JaxrsEndpointApiCtClassBuilderTest.java)

**构建**：
- [pom.xml](file:///Users/wandl/workspaces/workspace-github-easy-4-java/cxf-rt-javassist/pom.xml)（Surefire argLine / Commons 版本号 / JaCoCo 90% 门禁）

---

**文档版本**：V1.0.0
**创建日期**：2026-08-20
**最后更新**：2026-08-20
**文档状态**：✅ 已落地并通过 4 分支 mvn test 验证
