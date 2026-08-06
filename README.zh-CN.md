# cxf-rt-javassist

[English](./README.md) | [简体中文](./README.zh-CN.md)

[![License](https://img.shields.io/badge/license-Apache%202.0-green)

> 使用 [Javassist](https://www.javassist.org) 生成基于 Apache CXF 的 JAX-WS /
> JAX-RS 实现：流式 `CtClass` 构建器在运行时产出带 `@WebService` / JAX-RS 注解的
> 端点类。

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 功能与状态](#2-功能与状态)
- [3. 环境要求与兼容性](#3-环境要求与兼容性)
- [4. 架构与模块](#4-架构与模块)
- [5. 安装](#5-安装)
- [6. 快速开始](#6-快速开始)
- [7. 配置](#7-配置)
- [8. 核心用法 / API](#8-核心用法--api)
- [9. 测试与构建](#9-测试与构建)
- [10. 版本与分支](#10-版本与分支)
- [11. 贡献与许可](#11-贡献与许可)

## 1. 项目概述

`cxf-rt-javassist` 使用 [Javassist](https://www.javassist.org) 在运行时生成 Apache
CXF JAX-WS / JAX-RS 端点实现。两个流式构建器把声明式描述转换为真实、可加载的类：

- **`JaxwsEndpointApiCtClassBuilder`** — 生成 `@WebService` 端点类（名称、目标命名
  空间、服务名、WSDL provider 模式、addressing、service mode、绑定数据、字段与
  SOAP 方法）。
- **`JaxrsEndpointApiCtClassBuilder`** — 生成 JAX-RS 资源类（`@Path`、produces、
  带 `HttpMethodEnum` / `RestParam` 的 REST 方法、绑定数据、字段）。

生成的类继承共享的 `EndpointApi` 基类；通过 `toInstance(InvocationHandler)` 可得到
方法调用被分发给自定义处理器的实例——非常适合代理 / 可插拔端点逻辑。

构建器依赖姊妹库 `io.github.easy4j:javassist-plus`（`CtFieldBuilder`、
`ClassPoolFactory`、`JavassistUtils`）。

它不是：

- CXF 运行时本身——提供服务生成的端点仍需要 Apache CXF frontend 构件。
- Spring Boot starter——不提供自动装配。

典型场景：

| 场景 | 使用内容 |
| :--- | :--- |
| 运行时生成 `@WebService` 端点类 | `JaxwsEndpointApiCtClassBuilder` + `webService(...)` / `newMethod(...)` |
| 运行时生成 JAX-RS 资源类 | `JaxrsEndpointApiCtClassBuilder` + `path(...)` / `newMethod(...)` |
| 把生成的方法调用分发给处理器 | `toInstance(InvocationHandler)` |
| 导出生成的字节码离线检查 | `build()` → `CtClass` → `toBytecode()` / `writeFile()` |

## 2. 功能与状态

| 能力 | 状态 | 说明 |
| :--- | :--- | :--- |
| JAX-WS 类构建器 | 稳定 | `webService`、`webServiceProvider`、`addressing`、`serviceMode`、`bind`、`makeField`、`makeMethod`、`newMethod`、`build` / `toClass` / `toInstance` |
| JAX-RS 类构建器 | 稳定 | `path`、`produces`、`bind`、`makeField`、`newField`、`removeField`、`newMethod`（`HttpMethodEnum` / `RestMethod`）、`removeMethod`、`build` / `toClass` / `toInstance` |
| SOAP 定义模型 | 稳定 | `SoapService`、`SoapMethod`、`SoapParam`、`SoapResult`、`SoapBound` |
| REST 定义模型 | 稳定 | `RestBound`、`RestMethod`、`RestParam`、`RestProduce`、`HttpMethodEnum`、`HttpParamEnum` |
| 工具类 | 稳定 | `JaxwsEndpointApiUtils`、`JaxrsEndpointApiUtils` |
| 测试 | 稳定 | `JaxwsApiCtClassBuilder_Test`、`JaxrsApiCtClassBuilder_Test` + 样例（`EndpointApiSample`、`EndpointApiInvocationHandler`、`Customer`） |

## 3. 环境要求与兼容性

| 要求 | 版本 / 说明 |
| :--- | :--- |
| JDK | 8+ |
| Maven | 3.0+（enforcer 强制；项目内置 Maven Wrapper `./mvnw`） |
| Apache CXF | 4.x（`cxf-rt-frontend-jaxws` / `cxf-rt-frontend-jaxrs`，由本 pom 管理） |
| Javassist | 3.30.2-GA（由本 pom 管理） |
| 姊妹库 | `io.github.easy4j:javassist-plus`（同一版本线） |

版本线：

| 分支 | JDK | 版本 |
| :--- | :--- | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` |
| `feature/2.0.x` | 17 | `2.0.x.*` |
| `feature/3.0.x` | 21 | `3.0.x.*` |

## 4. 架构与模块

```text
+------------------+   +------------------------------------------+
| Developer        |   | cxf-rt-javassist                         |
|                  |-->|  JaxwsEndpointApiCtClassBuilder          |
| declarative      |   |    (webService / newMethod / bind)       |
| endpoint spec    |   |  JaxrsEndpointApiCtClassBuilder          |
|                  |   |    (path / produces / newMethod)         |
|                  |   |  definitions: Soap*, Rest*               |
|                  |   |  base: EndpointApi (shared)              |
+------------------+   +-------------------+----------------------+
                                           |
                                           v
                     +-------------------------------------------+
                     | CtClass -> Class / instance               |
                     | toClass / toInstance(handler)             |
                     | -> CXF JAX-WS / JAX-RS implementation     |
                     +-------------------------------------------+
```

单模块 Maven 工程（`packaging: jar`），无子模块。

| 构件 | 职责 |
| :--- | :--- |
| `io.github.easy4j:cxf-rt-javassist` | 运行时类构建器、SOAP / REST 定义模型、工具类 |

关键包：

| 包 | 内容 |
| :--- | :--- |
| `org.apache.cxf.endpoint.jaxws` | `JaxwsEndpointApiCtClassBuilder`、`JaxwsEndpointApiImplCtClassBuilder`、`JaxwsEndpointApiInterfaceCtClassBuilder` |
| `org.apache.cxf.endpoint.jaxws.definition` | `SoapService`、`SoapMethod`、`SoapParam`、`SoapResult`、`SoapBound` |
| `org.apache.cxf.endpoint.jaxrs` | `JaxrsEndpointApiCtClassBuilder`、`JaxrsEndpointApiImplCtClassBuilder`、`JaxrsEndpointApiInterfaceCtClassBuilder` |
| `org.apache.cxf.endpoint.jaxrs.definition` | `RestBound`、`RestMethod`、`RestParam`、`RestProduce`、`HttpMethodEnum`、`HttpParamEnum` |
| `org.apache.cxf.endpoint` | `EndpointApi`（基类） |
| `org.apache.cxf.endpoint.utils` | `JaxwsEndpointApiUtils`、`JaxrsEndpointApiUtils` |

## 5. 安装

项目**尚未发布到 Maven Central**。快照 / 发布版本通过阿里云 Maven 仓库与 GitHub
Releases 分发。

Maven：

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>cxf-rt-javassist</artifactId>
    <version>1.0.x.20260630-SNAPSHOT</version>
</dependency>
```

Gradle：

```groovy
implementation 'io.github.easy4j:cxf-rt-javassist:1.0.x.20260630-SNAPSHOT'
```

## 6. 快速开始

运行时构建 JAX-WS 端点类（改编自仓库内已提交的测试）：

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
```

预期结果：创建新类 `org.apache.cxf.spring.boot.FirstCaseV1`，带 `@WebService` 注解
与 `String sayHello(String text)` 方法——可直接作为 CXF JAX-WS 端点实现使用。

## 7. 配置

本库没有配置文件或属性前缀。一切由构建器链与定义对象驱动：

| 构建器 | 关键方法 | 说明 |
| :--- | :--- | :--- |
| `JaxwsEndpointApiCtClassBuilder` | `webService(name, targetNamespace[, serviceName])`、`webServiceProvider(wsdlLocation, serviceName, ...)`、`addressing(boolean, boolean, Responses)`、`serviceMode(Service.Mode)`、`bind(uid, json)` / `bind(SoapBound)`、`makeField(src)`、`makeMethod(src)`、`newMethod(name, SoapParam...)`、`build()` / `toClass()` / `toInstance(InvocationHandler)` | JAX-WS 端点生成 |
| `JaxrsEndpointApiCtClassBuilder` | `path(path)`、`produces(mediaTypes...)`、`bind(uid, json)` / `bind(RestBound)`、`makeField(src)`、`newField(type, name, value)`、`removeField(name)`、`newMethod(rtClass, HttpMethodEnum, name, path, RestParam...)`、`removeMethod(...)`、`build()` / `toClass()` / `toInstance(InvocationHandler)` | JAX-RS 资源生成 |

## 8. 核心用法 / API

### 8.1 JAX-RS 资源生成

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
```

### 8.2 分发到 `InvocationHandler`

```java
import java.lang.reflect.InvocationHandler;
import org.apache.cxf.endpoint.jaxws.JaxwsEndpointApiCtClassBuilder;

InvocationHandler handler = new EndpointApiInvocationHandler(); // 你的处理器
Object endpoint = new JaxwsEndpointApiCtClassBuilder("org.apache.cxf.spring.boot.FirstCaseV2")
        .webService("get", "http://ws.cxf.com", "getxx")
        .newMethod("sayHello", new SoapParam(String.class, "text"))
        .toInstance(handler);   // 方法调用被分发给该处理器
```

注意：`toClass()` / `toBytecode()` / `writeFile()` 之后 Javassist 会冻结 `CtClass`；
继续修改前需调用 `defrost()`（参见已提交的测试）。

## 9. 测试与构建

```bash
./mvnw clean verify
```

- 构建配置了 JaCoCo Maven 插件（报告 + 绑定在 `verify` 阶段的 `check` 目标，
  行覆盖率规则为 90%；`haltOnFailure=false`）。
- 已提交测试：`JaxwsApiCtClassBuilder_Test`、`JaxrsApiCtClassBuilder_Test`
  （类构建、实例分发、字节码导出）。
- 本 worktree 的 `.github/` 下无 CI 工作流文件。

## 10. 版本与分支

| 分支 | JDK | 版本 | 说明 |
| :--- | :--- | :--- | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` | 当前分支，JDK 8 基线，维护中 |
| `feature/2.0.x` | 17 | `2.0.x.*` | JDK 17 版本线 |
| `feature/3.0.x` | 21 | `3.0.x.*` | JDK 21 版本线 |

维护策略：`1.0.x` 版本线接收针对 JDK 8 基线的缺陷修复与兼容性更新；面向新 JDK 的
新特性在 `2.0.x` / `3.0.x` 版本线开发。发布物通过阿里云 Maven 仓库与 GitHub
Releases 分发；项目尚未发布到 Maven Central。

## 11. 贡献与许可

欢迎通过 GitHub Issue 或 Pull Request 参与贡献。

本项目基于 [Apache License, Version 2.0](LICENSE) 许可。
