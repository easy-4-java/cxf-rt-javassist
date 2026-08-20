# cxf-rt-javassist

<a id="readme-top"></a>

<div align="center">

**在运行时用 Javassist 生成 Apache CXF JAX-WS / JAX-RS 实现类**

[![Java](https://img.shields.io/badge/Java-8%20%7C%2017%20%7C%2021-orange)](#3-运行要求与兼容性)
[![License](https://img.shields.io/badge/license-Apache%202.0-green)](./LICENSE)
[![Maven Central placeholder](https://img.shields.io/badge/Maven%20Central-pending-lightgray)](#5-引入依赖)

[English](./README.md) · [简体中文](./README.zh-CN.md) · [技术方案与修复报告](./TECHNICAL-DESIGN.md)

[定位](#1-项目定位) · [特性状态](#2-核心能力与状态) · [兼容性](#3-运行要求与兼容性) · [架构](#4-架构与模块) ·
[引入依赖](#5-引入依赖) · [快速开始](#6-快速开始) · [API 参考](#7-核心用法-api) · [测试构建](#8-构建与测试) ·
[分支策略](#9-版本线与兼容策略) · [FAQ](#10-faq) · [贡献](#11-贡献与许可证)

</div>

---

> **当前版本**：`3.0.x.x-SNAPSHOT`（按分支各自演进，见 §9）<br>
> **JDK 基线**：8 / 17 / 21（四分支并发维护）<br>
> **构建工具**：Maven 3.0+（Maven Wrapper `./mvnw` 已包含）<br>
> **最后核验**：2026-08-20（四线 `mvn test` 全绿，共 202 / 202 / 202 / 224 用例）

## 1. 项目定位

**`cxf-rt-javassist` 是一个面向 Java 框架开发者 / 动态网关开发者的纯 Java 运行时代码生成库，基于 [Javassist](https://www.javassist.org) 与 `io.github.easy4j:javassist-extension`，把声明式的 SOAP / REST 端点定义直接翻译为带 `@WebService` 或 JAX-RS 注解的可加载 `Class<T>`。**

| 维度 | 定位 |
| :--- | :--- |
| 本质 | 运行时字节码生成器；单模块 jar |
| 消费方 | Spring Boot Starter、API Gateway、通用代理框架、动态导出端点 |
| 核心能力 | ① JAX-WS 端点类生成 ② JAX-RS 资源类生成 ③ 字节码 / `Class<?>` / 代理实例三种产出 ④ SOAP/REST 双 Builder 对称设计 |
| JDK（多线） | `feature/1.0.x` → JDK 8；`feature/2.0.x` → JDK 17；`feature/3.0.x` → JDK 21；`main` → JDK 17/21 主发行 |
| Maven 坐标 | `io.github.easy4j:cxf-rt-javassist` |
| 配置前缀 | 无（完全由 Builder 链式 API 驱动，不读属性文件） |

### 1.1 不是什么

- 不是 Apache CXF 运行时本身。要真正对外暴露生成的 SOAP / REST 端点，运行时需要额外引入 `cxf-rt-frontend-jaxws` / `cxf-rt-frontend-jaxrs`。
- 不是 Spring Boot Starter。不做自动装配；也不引入 Spring 依赖。
- 不承诺 JDK 版本矩阵外的组合（例如 JDK 11），本项目只维护 8 / 17 / 21 三条长期线。

### 1.2 典型使用场景

| 场景 | 使用方式 | 结果 |
| :--- | :--- | :--- |
| 通用代理网关 | `JaxrsEndpointApiCtClassBuilder → toInstance(InvocationHandler)` | 统一把所有方法调用转发到你的 InvocationHandler |
| 动态导入第三方 OpenAPI 定义 | 解析 OpenAPI → 实例化 `RestMethod[]` → `Jaxrs*Builder.newMethod(...)` | 生成一批 `@Path / @GET / @PathParam` 注解齐全的资源类 |
| 遗留 SOAP 服务自动代理 | `JaxwsEndpointApiCtClassBuilder.webService(...) + toInstance(handler)` | 生成 `@WebService` 类，方法调用全部打向你的 Handler |
| 离线分析 / 类导出 | `build() → CtClass → toBytecode() / writeFile(dumpDir)` | 把生成的字节码落盘，用 javap / IDA 分析注解 |

## 2. 核心能力与状态

| 能力 | 状态 | 说明 | 验证证据 |
| :--- | :---: | :--- | :--- |
| JAX-WS 端点类 Builder | ✅ 稳定 | `webService / webServiceProvider / addressing / serviceMode / bind / makeField / makeMethod / newMethod / build / toClass / toInstance` | `JaxwsEndpointApiImplCtClassBuilderTest`（13 tests） |
| JAX-RS 资源类 Builder | ✅ 稳定 | `path / produces / bind / makeField / newField / removeField / newMethod(rtClass, HttpMethodEnum, name, path, RestParam...) / removeMethod / build / toClass / toInstance` | `JaxrsEndpointApiCtClassBuilderTest`（26 tests） |
| SOAP 定义对象 | ✅ 稳定 | `SoapService / SoapMethod / SoapParam / SoapResult / SoapBound` | 对应 POJO Test 覆盖 |
| REST 定义对象 | ✅ 稳定 | `RestBound / RestMethod / RestParam / RestProduce / HttpMethodEnum / HttpParamEnum` | `RestParamTest`（8 tests）+ 各 Builder 回归 |
| Utils 注解注入 | ✅ 稳定（2026-08-20 已修复 **Bug#3**） | `JaxwsEndpointApiUtils / JaxrsEndpointApiUtils.annotParams` 负责把 POJO → Javassist `Annotation[][]` | `Jaxrs*InterfaceCtClassBuilderTest`（26 tests） |
| 字节码导出 + `toInstance(InvocationHandler)` | ✅ 稳定 | Javassist `CtClass.toClass()`、`toBytecode()`、`writeFile()`、以及代理 `Proxy.newProxyInstance` | `EndpointApiSample` / `EndpointApiInvocationHandler` / `Customer` |

> 2026-08-20 代码修复详情见 [TECHNICAL-DESIGN.md §3 / §4](./TECHNICAL-DESIGN.md#3-codegraph-语义代码审查结论)。

## 3. 运行要求与兼容性

### 3.1 基础要求

| 依赖 | 最低版本 | 推荐版本 | 说明 |
| :--- | ---: | ---: | :--- |
| JDK（按分支） | 见 §9 | 8u3xx / 17u20 / 21u最新 | 本项目按分支分 JDK 线长期并行 |
| Maven | 3.0+ | 3.9.16 | pom.xml 中已通过 Maven Enforcer 强制要求 |
| Apache CXF | 4.x | 4.0.x（1.0.x）/ 4.1.x（2.0.x）/ 4.2.x（3.0.x / main） | 由 pom.xml dependencyManagement 锁版 |
| Javassist | 3.30.2-GA | 3.30.2-GA | 同上 |
| javassist-extension | 同本项目版本号 | 同左 | `io.github.easy4j:javassist-extension`，同行维护 |
| commons-lang3 | 3.20.0 | 3.20.0 | JDK 8 线最新最终版（四线共用，见 §10.2） |
| commons-io | 2.22.0 | 2.22.0 | JDK 8 线最新最终版（四线共用） |
| commons-beanutils | 1.11.0 | 1.11.0 | beanutils 1.x 线最新版（四线共用） |

### 3.2 版本兼容矩阵

| 项目版本线 | JDK | CXF 基线 | 状态 | 维护策略 |
| :--- | :---: | :---: | :---: | :--- |
| `feature/3.0.x` | 21 | CXF 4.2.x | ✅ 活跃开发 | 新功能 + 缺陷修复 |
| `main` | 17 / 21 | CXF 4.2.x | ✅ 主发行 | 同步 3.0.x 缺陷修复；对外发行 GA |
| `feature/2.0.x` | 17 | CXF 4.1.x | 🛠️ 维护 | 只接收严重缺陷、CVE、依赖升级 |
| `feature/1.0.x` | 8 | CXF 4.0.x | 🛠️ 维护 | 只接收阻塞缺陷与 CVE；严禁使用 JDK 11+ only 的依赖版本 |

### 3.3 依赖边界

- 本项目是**单模块 jar**，依赖项按 "字节码生成最小集合" 约束：javassist + javassist-extension + Apache CXF 前端（provided 语义）+ commons 三件套（JSON 绑定 / 反射 Bean 拷贝需要）。
- 不引入 Spring、Jakarta Servlet、Jakarta Validation 等容器级依赖；下游使用方按需引入。
- `cxf-rt-frontend-jaxws / jaxrs` 不是 provided scope 而是 compile scope（生成的字节码需要注解类型在 ClassPool 中可解析）。

## 4. 架构与模块

### 4.1 一眼看懂

```text
┌─────────────── 业务应用 ───────────────┐
│  Gateway / Proxy / Dynamic Exporter    │
│  引入依赖: io.github.easy4j:cxf-rt-javassist   │
└──────────────┬────────────────────────┘
               │ 声明: SoapBound / RestBound
               ▼
┌──────────────────────────────────────────────────────┐
│                 cxf-rt-javassist (单模块 jar)          │
│  ┌──────────────────────┐   ┌──────────────────────┐  │
│  │  Jaxws Builder 侧     │   │   Jaxrs Builder 侧    │  │
│  │  JaxwsEndpointApiCt… │   │ JaxrsEndpointApiCt…   │  │
│  │  definition: Soap*   │   │ definition: Rest*     │  │
│  └──────────┬───────────┘   └──────────┬────────────┘  │
│             ▼ 共享 EndpointApi 基类 ▼                   │
│  EndpointApi (implements IEndpointApi)                 │
│  Utils: JaxwsEndpointApiUtils / JaxrsEndpointApiUtils │
└───────────────────────┬──────────────────────────────┘
                        │ CtClass / Class / Instance
                        ▼
              Apache CXF 4.x 前端运行时
            ┌─ JAX-WS Endpoint.publish ─┐
            └─ JAX-RS Server / Feature ─┘
```

### 4.2 Builder 对称设计

`JaxwsEndpointApiCtClassBuilder` ↔ `JaxrsEndpointApiCtClassBuilder` 是**姊妹 Builder**，API 结构严格同构：

| 阶段 | SOAP 侧（Jaxws） | REST 侧（Jaxrs） |
| :--- | :--- | :--- |
| 初始化 | `new JaxwsEndpointApiCtClassBuilder(fullyQualifiedClassName)` | `new JaxrsEndpointApiCtClassBuilder(fullyQualifiedClassName)` |
| 注解头 | `webService(name, tns, serviceName)` / `webServiceProvider(...)` / `addressing(...)` / `serviceMode(Mode.PAYLOAD)` | `path(root)` + `produces(MediaType...)` |
| 绑定 | `bind(String uid, String json)` / `bind(SoapBound)` | `bind(String uid, String json)` / `bind(RestBound)` |
| 字段 | `makeField(src)` / `newField(type, name, value)` / `removeField(name)` | 左侧三法完全相同 |
| 方法 | `makeMethod(src)` / `newMethod(name, SoapParam...)` / `removeMethod(...)` | `newMethod(rtClass, HttpMethodEnum, name, subPath, RestParam...)` / `removeMethod(...)` |
| 产出 | `build() → CtClass` / `toClass() → Class<?>` / `toInstance(InvocationHandler) → Object` | 左侧三法完全相同 |

> 代码审查约束（ADR-002）：新增能力 / 修复缺陷时，两侧必须成对走查；**除非 SOAP 与 REST 在语义上天然不同**（例如 REST 有 `@PathParam` / `@DefaultValue`，SOAP 没有对应概念），否则禁止只修一侧不修另一侧。

### 4.3 关键包一览（与 §1.2 场景对应）

| 包 | 内容 | 典型场景 |
| :--- | :--- | :--- |
| `org.apache.cxf.endpoint` | `EndpointApi` 公共基类、`IEndpointApi` 接口 | 通过基类复用 `bind` / `toInstance(InvocationHandler)` |
| `org.apache.cxf.endpoint.jaxws` | `JaxwsEndpointApiCtClassBuilder`、`…ImplCtClassBuilder`、`…InterfaceCtClassBuilder` | 生成 SOAP 实现类、接口、带实现的 endpoint |
| `org.apache.cxf.endpoint.jaxws.definition` | `SoapService / SoapMethod / SoapParam / SoapResult / SoapBound` | 声明式描述 SOAP 服务 |
| `org.apache.cxf.endpoint.jaxrs` | `JaxrsEndpointApiCtClassBuilder`、`…ImplCtClassBuilder`、`…InterfaceCtClassBuilder` | 生成 JAX-RS 实现类、接口、带实现的 resource |
| `org.apache.cxf.endpoint.jaxrs.definition` | `RestBound / RestMethod / RestParam / RestProduce / HttpMethodEnum / HttpParamEnum` | 声明式描述 REST 资源 |
| `org.apache.cxf.endpoint.utils` | `JaxwsEndpointApiUtils / JaxrsEndpointApiUtils` | 负责把定义对象翻译为 Javassist `Annotation[][]` 后注入 CtMethod |

## 5. 引入依赖

### 5.1 Maven

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>cxf-rt-javassist</artifactId>
    <!-- 版本号按分支选用：1.0.x.* (JDK 8) / 2.0.x.* (JDK 17) / 3.0.x.* (JDK 21) -->
    <version>3.0.x.x.20260630-SNAPSHOT</version>
</dependency>
```

### 5.2 Gradle

```kotlin
dependencies {
    implementation("io.github.easy4j:cxf-rt-javassist:3.0.x.x.20260630-SNAPSHOT")
}
```

### 5.3 仓库说明

项目目前**尚未发布 Maven Central**。发布前快照 / Release 通过阿里云私服仓库与 GitHub Releases 分发。如需私服地址，请联系仓库维护者。

## 6. 快速开始

### 6.1 最小 JAX-WS 示例（生成 `@WebService` 类并 `toClass`）

```java
import javassist.CtClass;
import org.apache.cxf.endpoint.jaxws.JaxwsEndpointApiCtClassBuilder;
import org.apache.cxf.endpoint.jaxws.definition.SoapParam;

CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.apache.cxf.spring.boot.FirstCaseV1")
        .webService("get", "http://ws.cxf.com", "getxx")
        .newField(String.class, "uid", java.util.UUID.randomUUID().toString())
        .newMethod("sayHello", new SoapParam(String.class, "text"))
        .build();

Class<?> clazz = ctClass.toClass();
Object endpoint = clazz.getConstructor().newInstance();
// endpoint 现在是一个完整的 @WebService 实例，可交给 org.apache.cxf.jaxws.EndpointImpl.publish(addr) 暴露
```

**预期结果**：`sayHello(String text)` 方法存在于生成的类；`@WebService(name="get", targetNamespace="http://ws.cxf.com", serviceName="getxx")` 注解可通过反射读取；`uid` 字段在默认构造器里被初始化为随机 UUID。

### 6.2 最小 JAX-RS 示例（生成 `@Path("/getxx")` 资源类）

```java
import javassist.CtClass;
import org.apache.cxf.endpoint.jaxrs.JaxrsEndpointApiCtClassBuilder;
import org.apache.cxf.endpoint.jaxrs.definition.HttpMethodEnum;
import org.apache.cxf.endpoint.jaxrs.definition.HttpParamEnum;
import org.apache.cxf.endpoint.jaxrs.definition.RestParam;

CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.apache.cxf.spring.boot.FirstCase1")
        .path("getxx")
        .newMethod(String.class, HttpMethodEnum.GET, "sayHello", "{id}/info",
                new RestParam(String.class, "id", HttpParamEnum.PATH))
        .build();

// 验证：反射读注解
Class<?> clazz = ctClass.toClass();
Path classPath = clazz.getAnnotation(Path.class);
assertEquals("getxx", classPath.value());   // 通过
```

> ⚠️ 2026-08-20 前的旧代码存在 Bug#1：上面 `HttpParamEnum.PATH` 会被静默退化成 QUERY（详见技术方案 §4.1）。当前主分支、main、2.0.x、1.0.x 均已修复。

### 6.3 调度到自定义 InvocationHandler（动态代理网关场景）

```java
import java.lang.reflect.InvocationHandler;
import org.apache.cxf.endpoint.jaxrs.JaxrsEndpointApiCtClassBuilder;
import org.apache.cxf.endpoint.jaxrs.definition.HttpMethodEnum;
import org.apache.cxf.endpoint.jaxrs.definition.HttpParamEnum;
import org.apache.cxf.endpoint.jaxrs.definition.RestParam;

// 你的业务调度逻辑：例如转发到 Feign / Dubbo / 本地服务
InvocationHandler handler = new InvocationHandler() {
    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("sayHello".equals(method.getName())) return "Hello, " + args[0];
        throw new UnsupportedOperationException(method.getName());
    }
};

Object resource = new JaxrsEndpointApiCtClassBuilder("com.example.DynamicHelloResource")
        .path("hello")
        .newMethod(String.class, HttpMethodEnum.GET, "sayHello", "{name}",
                new RestParam(String.class, "name", HttpParamEnum.PATH, "world"))   // Bug#2/Bug#3 都已修复：from=PATH 生效 + def=world 生效
        .toInstance(handler);

// resource.getClass().getMethod("sayHello", String.class).invoke(resource, "Alice")  → "Hello, Alice"
```

> 💡 `toInstance(handler)` 是唯一能同时得到"带注解的类 + 方法调用可被拦截"的入口，推荐给通用网关场景。`toClass()` 得到的类是普通 Javassist 字节码，方法体是空返回默认值或 0 / null（由 Builder 默认模板决定），需要业务逻辑请用 `ImplCtClassBuilder` 或 `toInstance`。

## 7. 核心用法 / API

### 7.1 定义对象速查

**HttpMethodEnum**（REST）：
```java
public enum HttpMethodEnum { GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS }
// 对应 jakarta.ws.rs.@GET / @POST / @PUT / ...
```

**HttpParamEnum**（REST）：
```java
public enum HttpParamEnum {
    PATH,     // @PathParam
    QUERY,    // @QueryParam（字段默认值；Bug#1/Bug#2 修前就是这个）
    HEADER,   // @HeaderParam
    COOKIE,   // @CookieParam
    FORM,     // @FormParam
    MATRIX,   // @MatrixParam
    BEAN      // @BeanParam
}
// getAnnotationType() → 对应 jakarta.ws.rs 的注解 FQCN；JaxrsEndpointApiUtils.annotParams 读它
```

**RestParam 构造器（4 个，2026-08-20 已修复字段赋值完整性）**：

| 构造器签名 | 字段行为 |
| :--- | :--- |
| `RestParam(Class<T> type, String name)` | `type/name` 赋值；`from=QUERY`（默认）；`def=null` |
| `RestParam(Class<T> type, String name, HttpParamEnum from)` | 三项全部赋值 ✅（Bug#1 修完后） |
| `RestParam(Class<T> type, String name, String def)` | 三项；`from=QUERY`（默认） |
| `RestParam(Class<T> type, String name, HttpParamEnum from, String def)` | 四项全部赋值 ✅（Bug#2 修完后） |

### 7.2 Builder 常用方法

`JaxrsEndpointApiCtClassBuilder`：

| 方法 | 返回值 | 说明 |
| :--- | :--- | :--- |
| `path(String rootPath)` | this | 设置类级 `@Path(rootPath)` |
| `produces(String... mediaTypes)` | this | 设置 `@Produces({...})` |
| `newField(Class<?> type, String name, Object value)` | this | 新建字段并初始化 |
| `removeField(String name)` | this | 移除已存在字段（用于默认字段） |
| `newMethod(Class<?> rtClass, HttpMethodEnum method, String name, String subPath, RestParam... params)` | this | 新建方法：返回类型 / HTTP 方法 / 方法名 / `@Path(subPath)` / 参数 |
| `removeMethod(String name, Class<?>... params)` | this | 删除已存在方法 |
| `build()` | `CtClass` | 构建完成（未 toClass，可继续 defrost 修改） |
| `toClass()` | `Class<?>` | 调用 `CtClass.toClass()`，加载到当前线程 ClassLoader |
| `toInstance(InvocationHandler h)` | `Object` | 先 `toClass()`，再用 `Proxy.newProxyInstance(IEndpointApi, h)` 包装，最后把代理 set 到生成类的 `invocationHandler` 字段（EndpointApi 基类约定） |

`JaxwsEndpointApiCtClassBuilder`：对称等价 API（§4.2）。

### 7.3 `@DefaultValue` 修复后示例（Bug#3）

```java
// 现在 @DefaultValue("1") 会真的写进字节码：
CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("com.example.PageResource")
        .path("items")
        .newMethod(List.class, HttpMethodEnum.GET, "list",
                   "{category}/page",
                   new RestParam(String.class, "category", HttpParamEnum.PATH),
                   new RestParam(Integer.class, "page", HttpParamEnum.QUERY, "1"))
        .build();

// 反射：Method.getParameterAnnotations()[1] 应得到 [@QueryParam("page"), @DefaultValue("1")]
```

## 8. 构建与测试

### 8.1 常用命令

```bash
./mvnw clean verify                  # 编译 + 单元测试 + JaCoCo check（90% 指令覆盖率）
./mvnw test -B -q                    # 安静跑单测
./mvnw clean compile -DskipTests     # 只编译
./mvnw -Prelease -Dgpg.skip=true package   # 带源码 / javadoc 包
```

### 8.2 测试矩阵

| 类型 | 工具 / 命令 | 说明 |
| :--- | :--- | :--- |
| 单元测试 | JUnit 4 + Maven Surefire | 202 / 202 / 202 / 224（四线） |
| 字节码断言 | JUnit + `ctClass.toClass().getAnnotation(...)` 反射读 | 覆盖 `@Path/@QueryParam/@DefaultValue/@WebService/@WebParam` |
| 覆盖率报告 | JaCoCo `prepare-agent` + `report` + `check` | **必须** `@{argLine}` 前缀（ADR-004）；否则 JaCoCo 挂不上 |
| 多 JDK 验证 | 四线各自 worktree 单独跑 `mvn test` | JDK 8 / 17 / 21 分别执行 |

### 8.3 发布门禁

- Maven Enforcer（版本基线 + banned-deps）
- JaCoCo 覆盖率 90% 指令（`BUNDLE` 级别，`haltOnFailure=false` 但 CI 会读报告 fail）
- `dependency:tree` 无 commons 三件套跨版本混用
- 对 `RestParamTest` 所有 4 个字段的断言都显式写了（ADR-005）

## 9. 版本线与兼容策略

| 分支（git worktree 位置） | Git 分支 | JDK | `<java.version>` | 状态 |
| :--- | :--- | :---: | :---: | :---: |
| `cxf-rt-javassist/`（主工作目录） | `feature/3.0.x` | 21 | 21 | 活跃开发 |
| `.worktrees/cxf-rt-javassist-main/` | `main` | 17 / 21 | 21 | 主发行 |
| `.worktrees/cxf-rt-javassist-2.0.x/` | `feature/2.0.x` | 17 | 17 | 维护 |
| `.worktrees/cxf-rt-javassist-1.0.x/` | `feature/1.0.x` | 8 | 1.8 | 维护 |

- **只允许在 JDK/框架版本上的必要差异**：共享业务逻辑、域对象、测试、文档保持一致。
- 任何通用 bug（如本次 3 个）**必须一次修复、四线同步**。

## 10. FAQ

### 10.1 为什么 1.0.x 的 Surefire 不加 `--add-opens`？

JDK 8 **没有** Java Platform Module System（JPMS），所以**无法识别 `--add-opens` 选项**——加了会直接导致 `Unrecognized option: --add-opens`，Maven Surefire 整个阶段 FAILED。对 JDK ≥ 17 的三条线则必须加两个 `--add-opens`：`java.base/java.lang=ALL-UNNAMED` 与 `java.base/java.lang.reflect=ALL-UNNAMED`，否则 Javassist 写 `jdk.internal.reflect.ConstructorAccessor` 等类时会被 JPMS 强封装拦死，运行时抛出 `IllegalAccessError`。详见技术方案 §4.4 与 ADR-004。

### 10.2 为什么不升级 commons-lang3 到 3.21.0 / commons-io 到 2.23.0？

`commons-lang3 3.21.0` / `commons-io 2.23.0` 已经要求最低 **JDK 11**，与 `feature/1.0.x` 的 JDK 8 要求不兼容。ADR-003 规定：**在 1.0.x 分支（JDK 8）宣布 EOL 前，四条线共用同一版本号**，因此统一停留在 JDK 8 线的最终版：`3.20.0 / 2.22.0 / 1.11.0`。详见技术方案 §5.1。

### 10.3 `toClass()` 之后还能不能改？

不行，Javassist 调用 `toClass() / toBytecode() / writeFile()` 之后会把 `CtClass` **冻结（frozen）**，后续 set* / addMethod 会抛 `RuntimeException: cannot modify frozen class`。解决方式：
1. **方案 A（推荐）**：先完成所有 Builder 调用再 build → toClass。
2. **方案 B**：`ctClass.defrost();` 解冻后再改，改完再 build → toClass（测试中广泛使用，见 `JaxrsEndpointApiCtClassBuilderTest` 的 `removeField` 用例）。

### 10.4 Bug 修复了什么？怎么影响到我？

2026-08-20 四线同步的 3 个修复：

| Bug | 修前行为 | 修后行为 | 对下游影响 |
| :--- | :--- | :--- | :--- |
| Bug#1（三参构造器） | `from` 显式传参被丢，永久是 QUERY | 用户传的 `HttpParamEnum` 正确生效 | 如果你的代码**手工把参数当 QUERY 读**（绕开 Bug），修完后可能需要同时修正 URL 写法。CHANGELOG 明确列出为**行为修复（Breaking Change 类的兼容性说明）** |
| Bug#2（四参构造器） | `from` 丢 + 重复写 name | 四字段都正确 | 同 Bug#1，且 `@DefaultValue` 参数的 from 也正确生效 |
| Bug#3（annotParams） | `@DefaultValue` 对象被创建却**不写进数组**，重复写两次 paramAnnot | `[paramAnnot, defAnnot]` 正确写回 | 如果你依赖 "缺省值全 null / 0" 的旧行为需要做兼容性处理 |

详细根因、CodeGraph 符号执行证据、修复代码见 [TECHNICAL-DESIGN.md](./TECHNICAL-DESIGN.md)。

## 11. 贡献与许可证

- 贡献前请在对应 worktree 执行 `mvn clean verify`，确认四线测试都绿；PR 请附 `mvn test` 输出与 JaCoCo 报告截图。
- 跨分支通用修复请按 ADR-001 同步到四线后再提 PR。
- 安全问题通过 GitHub Security Advisory 私密报告，不要在公共 Issue 泄露利用细节。

本项目采用 [Apache License 2.0](./LICENSE) 许可证。

---

<div align="center">

[返回顶部](#readme-top) · [技术方案（中文）](./TECHNICAL-DESIGN.md) · [问题反馈](https://github.com/easy-4-java/cxf-rt-javassist/issues)

</div>
