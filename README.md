# cxf-rt-javassist

<a id="readme-top"></a>

<div align="center">

**Runtime Apache CXF JAX-WS / JAX-RS implementation generation with Javassist.**

[![Java](https://img.shields.io/badge/Java-8%20%7C%2017%20%7C%2021-orange)](#3-requirements--compatibility)
[![License](https://img.shields.io/badge/license-Apache%202.0-green)](./LICENSE)
[![Maven Central placeholder](https://img.shields.io/badge/Maven%20Central-pending-lightgray)](#5-installation)

[English](./README.md) · [简体中文](./README.zh-CN.md) · [Technical Design & Bug Fix Report (zh-CN)](./TECHNICAL-DESIGN.md)

[Overview](#1-project-overview) · [Features](#2-features--status) · [Compatibility](#3-requirements--compatibility) · [Architecture](#4-architecture--modules) ·
[Installation](#5-installation) · [Quick Start](#6-quick-start) · [Core API](#7-core-usage--api) · [Build & Test](#8-testing--build) ·
[Versioning](#9-versioning--branches) · [FAQ](#10-faq) · [Contributing](#11-contributing--license)

</div>

---

> **Current version** (depends on branch, see §9): `3.0.x.x-SNAPSHOT`<br>
> **JDK baselines**: 8 / 17 / 21 (four long-term worktree branches maintained in parallel)<br>
> **Build tool**: Maven 3.0+ (Maven Wrapper `./mvnw` included)<br>
> **Last verified**: 2026-08-20 (`mvn test` green across all four branches: 202 / 202 / 202 / 224 tests)

## 1. Project Overview

**`cxf-rt-javassist` is a runtime bytecode-generation library for Java framework / dynamic-gateway developers. Built on top of [Javassist](https://www.javassist.org) and `io.github.easy4j:javassist-extension`, it translates declarative SOAP / REST endpoint definitions into loadable `Class<T>` objects carrying the correct `@WebService` or JAX-RS annotations.**

| Dimension | Positioning |
| :--- | :--- |
| What it is | A runtime bytecode generator; single-module jar |
| Consumers | Spring Boot Starters, API gateways, generic proxy frameworks, dynamically exported endpoints |
| Core capabilities | ① JAX-WS endpoint class generation ② JAX-RS resource class generation ③ three outputs (bytecode / `Class<?>` / proxied instance) ④ fully symmetric SOAP ↔ REST builder design |
| JDK (multi-line) | `feature/1.0.x` → JDK 8; `feature/2.0.x` → JDK 17; `feature/3.0.x` → JDK 21; `main` → primary JDK 17/21 release line |
| Coordinates | `io.github.easy4j:cxf-rt-javassist` |
| Property prefix | None (everything is driven by fluent builder APIs; no files are read) |

### 1.1 What it is NOT

- Not the Apache CXF runtime itself. To actually expose the generated SOAP / REST endpoints, you also need `cxf-rt-frontend-jaxws` / `cxf-rt-frontend-jaxrs` on the runtime classpath.
- Not a Spring Boot Starter. No auto-configuration; no Spring dependency is ever pulled by this artifact.
- No commitment to unlisted JDK combinations (e.g. JDK 11); only 8 / 17 / 21 are long-term lines.

### 1.2 Typical use cases

| Scenario | How to use | Outcome |
| :--- | :--- | :--- |
| Generic proxy gateway | `JaxrsEndpointApiCtClassBuilder → toInstance(InvocationHandler)` | Every generated method call dispatches to your handler |
| Import a 3rd-party OpenAPI on the fly | Parse OpenAPI → materialise `RestMethod[]` → call `Jaxrs*Builder.newMethod(...)` repeatedly | A bundle of resource classes with fully-populated `@Path / @GET / @PathParam` annotations |
| Automatically proxy legacy SOAP services | `JaxwsEndpointApiCtClassBuilder.webService(...) + toInstance(handler)` | A `@WebService` class whose every method enters your dispatch handler |
| Offline bytecode analysis | `build() → CtClass → toBytecode() / writeFile(dumpDir)` | Bytecode written to disk; readable via javap / IDA / annotation reflect |

## 2. Features & Status

| Capability | Status | Details | Evidence |
| :--- | :---: | :--- | :--- |
| JAX-WS endpoint Builder | ✅ Stable | `webService / webServiceProvider / addressing / serviceMode / bind / makeField / makeMethod / newMethod / build / toClass / toInstance` | `JaxwsEndpointApiImplCtClassBuilderTest` (13 tests) |
| JAX-RS resource Builder | ✅ Stable | `path / produces / bind / makeField / newField / removeField / newMethod(rtClass, HttpMethodEnum, name, path, RestParam...) / removeMethod / build / toClass / toInstance` | `JaxrsEndpointApiCtClassBuilderTest` (26 tests) |
| SOAP definition objects | ✅ Stable | `SoapService / SoapMethod / SoapParam / SoapResult / SoapBound` | Corresponding POJO tests cover all 4 non-default fields |
| REST definition objects | ✅ Stable | `RestBound / RestMethod / RestParam / RestProduce / HttpMethodEnum / HttpParamEnum` | `RestParamTest` (8 tests) + builder regressions (26+26+10) |
| Utils / annotation injection | ✅ Stable (**Bug#3 fixed 2026-08-20**) | `JaxwsEndpointApiUtils.annotParams` / `JaxrsEndpointApiUtils.annotParams` translate definition POJOs → Javassist `Annotation[][]` | `Jaxrs*InterfaceCtClassBuilderTest` (26 tests) |
| Bytecode export + `toInstance(InvocationHandler)` | ✅ Stable | `CtClass.toClass()`, `toBytecode()`, `writeFile()`, plus `Proxy.newProxyInstance` | `EndpointApiSample` / `EndpointApiInvocationHandler` / `Customer` |

> For the 2026-08-20 CodeGraph-based fix details see [TECHNICAL-DESIGN.md §3 / §4](./TECHNICAL-DESIGN.md#3-codegraph-语义代码审查结论) (Chinese; all code snippets and anchors are still readable to English readers).

## 3. Requirements & Compatibility

### 3.1 Baseline requirements

| Dependency | Minimum version | Recommended version | Notes |
| :--- | ---: | ---: | :--- |
| JDK (branch-dependent) | see §9 | 8u3xx / 17u20 / latest 21 | Long-term parallel maintenance on 3 baselines |
| Maven | 3.0+ | 3.9.16 | Maven Enforcer rule in pom.xml |
| Apache CXF | 4.x | 4.0.x (1.0.x) / 4.1.x (2.0.x) / 4.2.x (3.0.x / main) | Locked via dependencyManagement in each line |
| Javassist | 3.30.2-GA | 3.30.2-GA | Same |
| javassist-extension | Same as this artifact | Same | `io.github.easy4j:javassist-extension`, released together |
| commons-lang3 | 3.20.0 | 3.20.0 | Final JDK 8-compatible release; shared across 4 lines (see §10.2) |
| commons-io | 2.22.0 | 2.22.0 | Final JDK 8-compatible release; shared across 4 lines |
| commons-beanutils | 1.11.0 | 1.11.0 | Latest 1.x release (beanutils 2.x not GA); shared across 4 lines |

### 3.2 Compatibility matrix

| Line | JDK | CXF baseline | Status | Maintenance policy |
| :--- | :---: | :---: | :---: | :--- |
| `feature/3.0.x` | 21 | CXF 4.2.x | ✅ Active dev | New features + bug fixes |
| `main` | 17 / 21 | CXF 4.2.x | ✅ Primary release | Syncs fixes from 3.0.x; GA channel |
| `feature/2.0.x` | 17 | CXF 4.1.x | 🛠️ Maintenance | Severe bugs, CVE, dependency bumps only |
| `feature/1.0.x` | 8 | CXF 4.0.x | 🛠️ Maintenance | Blocking bugs + CVE only; JDK-11+ only dependencies are FORBIDDEN |

### 3.3 Dependency boundary

- This project is a **single-module jar**. Dependencies are strictly kept to the "bytecode-generation minimal set": Javassist, javassist-extension, CXF frontends (compile scope because generated bytecodes require annotation types resolvable in the ClassPool), plus the Apache Commons trio for JSON binding / bean-copy utility.
- No Spring, Jakarta Servlet, Jakarta Validation, or any container-level dependency is introduced. Downstream consumers bring their own container.

## 4. Architecture & Modules

### 4.1 High-level view

```text
┌─────────────── Business app ───────────────┐
│   Gateway / Proxy / Dynamic exporter       │
│   pulls: io.github.easy4j:cxf-rt-javassist │
└──────────────┬─────────────────────────────┘
               │ Declarative: SoapBound / RestBound
               ▼
┌──────────────────────────────────────────────────────┐
│               cxf-rt-javassist (single-module jar)    │
│  ┌──────────────────────┐   ┌──────────────────────┐  │
│  │  Jaxws builder side   │   │  Jaxrs builder side   │  │
│  │  JaxwsEndpointApiCt…  │   │ JaxrsEndpointApiCt…   │  │
│  │  definition: Soap*    │   │ definition: Rest*     │  │
│  └──────────┬────────────┘   └──────────┬────────────┘  │
│             ▼ shared EndpointApi base ▼                 │
│  EndpointApi (implements IEndpointApi)                  │
│  Utils: JaxwsEndpointApiUtils / JaxrsEndpointApiUtils   │
└───────────────────────┬────────────────────────────────┘
                        │ CtClass / Class / Proxied instance
                        ▼
              Apache CXF 4.x frontend runtime
            ┌─ JAX-WS Endpoint.publish ─┐
            └─ JAX-RS Server / Feature ─┘
```

### 4.2 Symmetric builder pattern

`JaxwsEndpointApiCtClassBuilder` ↔ `JaxrsEndpointApiCtClassBuilder` are **sibling builders** with strictly isomorphic API shapes:

| Stage | SOAP (Jaxws) | REST (Jaxrs) |
| :--- | :--- | :--- |
| Init | `new JaxwsEndpointApiCtClassBuilder(fullyQualifiedClassName)` | `new JaxrsEndpointApiCtClassBuilder(fullyQualifiedClassName)` |
| Header annotations | `webService(name, tns, serviceName)` / `webServiceProvider(...)` / `addressing(...)` / `serviceMode(Mode.PAYLOAD)` | `path(root)` + `produces(MediaType...)` |
| Bind | `bind(String uid, String json)` / `bind(SoapBound)` | `bind(String uid, String json)` / `bind(RestBound)` |
| Fields | `makeField(src)` / `newField(type, name, value)` / `removeField(name)` | identical API |
| Methods | `makeMethod(src)` / `newMethod(name, SoapParam...)` / `removeMethod(...)` | `newMethod(rtClass, HttpMethodEnum, name, subPath, RestParam...)` / `removeMethod(...)` |
| Outputs | `build() → CtClass` / `toClass() → Class<?>` / `toInstance(InvocationHandler) → Object` | identical API |

> Review rule (ADR-002): When adding capabilities or fixing bugs, always walk both sides; only when the SOAP / REST semantic gap is fundamental (e.g. REST has `@PathParam` / `@DefaultValue`, SOAP does not) may a capability land on a single side without corresponding touch.

### 4.3 Package map (maps directly to §1.2 use cases)

| Package | Content | Typical scenario |
| :--- | :--- | :--- |
| `org.apache.cxf.endpoint` | `EndpointApi` base class, `IEndpointApi` interface | Reuse `bind` + `toInstance(InvocationHandler)` contract |
| `org.apache.cxf.endpoint.jaxws` | `JaxwsEndpointApiCtClassBuilder`, `…ImplCtClassBuilder`, `…InterfaceCtClassBuilder` | Generate SOAP impl / interface / impl-with-bound-data classes |
| `org.apache.cxf.endpoint.jaxws.definition` | `SoapService / SoapMethod / SoapParam / SoapResult / SoapBound` | Declarative SOAP description |
| `org.apache.cxf.endpoint.jaxrs` | `JaxrsEndpointApiCtClassBuilder`, `…ImplCtClassBuilder`, `…InterfaceCtClassBuilder` | Generate JAX-RS impl / interface / impl-with-bound-data classes |
| `org.apache.cxf.endpoint.jaxrs.definition` | `RestBound / RestMethod / RestParam / RestProduce / HttpMethodEnum / HttpParamEnum` | Declarative REST description |
| `org.apache.cxf.endpoint.utils` | `JaxwsEndpointApiUtils / JaxrsEndpointApiUtils` | Translate POJOs → Javassist `Annotation[][]` then inject into `CtMethod` / `CtClass` |

## 5. Installation

### 5.1 Maven

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>cxf-rt-javassist</artifactId>
    <!-- Select by line: 1.0.x.* (JDK 8) / 2.0.x.* (JDK 17) / 3.0.x.* (JDK 21) -->
    <version>3.0.x.x.20260630-SNAPSHOT</version>
</dependency>
```

### 5.2 Gradle

```kotlin
dependencies {
    implementation("io.github.easy4j:cxf-rt-javassist:3.0.x.x.20260630-SNAPSHOT")
}
```

### 5.3 Repository note

The project is **not yet published to Maven Central**. Snapshots / releases are distributed through the Aliyun private Maven repository and GitHub Releases until Central onboarding is complete. Contact repository maintainers for the private repo URL.

## 6. Quick Start

### 6.1 Minimal JAX-WS example (build `@WebService` class then `toClass`)

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
// endpoint is now a fully-annotated @WebService instance; pass it to
// org.apache.cxf.jaxws.EndpointImpl.publish(addr) to expose.
```

**Expected**: A `sayHello(String text)` method exists on the generated class; reflection can read `@WebService(name="get", targetNamespace="http://ws.cxf.com", serviceName="getxx")`; the `uid` field is initialised in the zero-arg constructor.

### 6.2 Minimal JAX-RS example (build a `@Path("/getxx")` resource)

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

// Verify via reflection:
Class<?> clazz = ctClass.toClass();
Path classPath = clazz.getAnnotation(Path.class);
assertEquals("getxx", classPath.value());   // PASS
```

> ⚠️ **Historical bug pre-2026-08-20**: Bug#1 used to silently downgrade `HttpParamEnum.PATH` → `QUERY` (see Tech Design §4.1). All four lines (3.0.x, main, 2.0.x, 1.0.x) have been fixed.

### 6.3 Dispatch to a custom `InvocationHandler` (dynamic gateway pattern)

```java
import java.lang.reflect.InvocationHandler;
import org.apache.cxf.endpoint.jaxrs.JaxrsEndpointApiCtClassBuilder;
import org.apache.cxf.endpoint.jaxrs.definition.HttpMethodEnum;
import org.apache.cxf.endpoint.jaxrs.definition.HttpParamEnum;
import org.apache.cxf.endpoint.jaxrs.definition.RestParam;

// Your business dispatcher — e.g. forward to Feign / Dubbo / a local service
InvocationHandler handler = new InvocationHandler() {
    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("sayHello".equals(method.getName())) return "Hello, " + args[0];
        throw new UnsupportedOperationException(method.getName());
    }
};

Object resource = new JaxrsEndpointApiCtClassBuilder("com.example.DynamicHelloResource")
        .path("hello")
        .newMethod(String.class, HttpMethodEnum.GET, "sayHello", "{name}",
                new RestParam(String.class, "name", HttpParamEnum.PATH, "world"))
        // Bug#2 + Bug#3 are both fixed: from=PATH actually applies; def="world" actually applies
        .toInstance(handler);

// resource.getClass().getMethod("sayHello", String.class).invoke(resource, "Alice")  → "Hello, Alice"
```

> 💡 **Tip**: `toInstance(handler)` is the **only entry point that combines both** (a) correctly-annotated class and (b) interceptible methods. Prefer it for generic gateway scenarios. Classes produced by `toClass()` have the default method body template inside the generated bytecode (nulls / zeros / default returns); plug in your business logic either via `ImplCtClassBuilder` or via `toInstance(handler)`.

## 7. Core Usage / API

### 7.1 Definition objects cheat sheet

**`HttpMethodEnum` (REST)**:
```java
public enum HttpMethodEnum { GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS }
// Maps one-to-one to jakarta.ws.rs.@GET / @POST / ...
```

**`HttpParamEnum` (REST)**:
```java
public enum HttpParamEnum {
    PATH,     // @PathParam
    QUERY,    // @QueryParam (field default; was the accidental "drop everything to QUERY" target of Bug#1 / Bug#2)
    HEADER,   // @HeaderParam
    COOKIE,   // @CookieParam
    FORM,     // @FormParam
    MATRIX,   // @MatrixParam
    BEAN      // @BeanParam
}
// getAnnotationType() returns the FQCN of the corresponding jakarta.ws.rs annotation; consumed by JaxrsEndpointApiUtils.annotParams
```

**`RestParam` constructors (4 overloads; all 4 fields correctly assigned after the 2026-08-20 fixes)**:

| Signature | Field assignment |
| :--- | :--- |
| `RestParam(Class<T> type, String name)` | `type/name` set; `from=QUERY` (default); `def=null` |
| `RestParam(Class<T> type, String name, HttpParamEnum from)` | All 3 set ✅ (Bug#1 fixed) |
| `RestParam(Class<T> type, String name, String def)` | All 3 set; `from=QUERY` (default) |
| `RestParam(Class<T> type, String name, HttpParamEnum from, String def)` | All 4 set ✅ (Bug#2 fixed; duplicate `this.name=name` removed) |

### 7.2 Common builder methods

**`JaxrsEndpointApiCtClassBuilder`**:

| Method | Returns | Purpose |
| :--- | :--- | :--- |
| `path(String rootPath)` | this | Set class-level `@Path(rootPath)` |
| `produces(String... mediaTypes)` | this | Set `@Produces({...})` |
| `newField(Class<?> type, String name, Object value)` | this | Create + initialise field |
| `removeField(String name)` | this | Drop a field (used to mutate a default template) |
| `newMethod(Class<?> rtClass, HttpMethodEnum method, String name, String subPath, RestParam... params)` | this | Add a method: return type / HTTP verb / name / `@Path(subPath)` / params |
| `removeMethod(String name, Class<?>... params)` | this | Drop a method |
| `build()` | `CtClass` | Finish, but **do not** `toClass()` yet (can defrost & mutate further) |
| `toClass()` | `Class<?>` | Call `CtClass.toClass()` into the thread-context ClassLoader |
| `toInstance(InvocationHandler h)` | `Object` | `toClass()` first, then `Proxy.newProxyInstance(IEndpointApi, h)` and inject the proxy into the generated class's EndpointApi `invocationHandler` slot |

**`JaxwsEndpointApiCtClassBuilder`**: symmetric equivalent (§4.2).

### 7.3 `@DefaultValue` after Bug#3

```java
// @DefaultValue("1") now actually ends up in the bytecode:
CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("com.example.PageResource")
        .path("items")
        .newMethod(List.class, HttpMethodEnum.GET, "list", "{category}/page",
                new RestParam(String.class, "category", HttpParamEnum.PATH),
                new RestParam(Integer.class, "page", HttpParamEnum.QUERY, "1"))
        .build();

// Reflection: Method.getParameterAnnotations()[1] == [@QueryParam("page"), @DefaultValue("1")]
```

## 8. Testing & Build

### 8.1 Common commands

```bash
./mvnw clean verify                  # compile + unit tests + JaCoCo 90% rule check
./mvnw test -B -q                    # quiet unit tests
./mvnw clean compile -DskipTests     # compile only
./mvnw -Prelease -Dgpg.skip=true package  # source + javadoc jar
```

### 8.2 Test matrix

| Kind | Tool / command | Scale |
| :--- | :--- | :--- |
| Unit tests | JUnit 4 + Maven Surefire | 202 / 202 / 202 / 224 (four lines) |
| Bytecode assertion | JUnit + reflection against `ctClass.toClass().getAnnotation(...)` | `@Path/@QueryParam/@DefaultValue/@WebService/@WebParam` coverage |
| Coverage report | JaCoCo `prepare-agent` + `report` + `check` | **Mandatory** `@{argLine}` prefix (ADR-004); without it JaCoCo is never attached and reports 0% |
| Multi-JDK verification | Run `mvn test` inside each of the 4 worktrees with the matching `JAVA_HOME` | JDK 8 / 17 / 21 each execute independently |

### 8.3 Release gates

- Maven Enforcer (version baselines + banned-deps list)
- JaCoCo 90% instruction coverage (bundle level, `haltOnFailure=false` but CI fails by reading the report)
- No cross-version Commons trio mixing (verified via `dependency:tree`)
- All 4 fields of every definition-object POJO test have an explicit assertion (ADR-005)

## 9. Versioning & Branches

| Worktree location on disk | Git branch | JDK | `<java.version>` | Status |
| :--- | :--- | :---: | :---: | :---: |
| `cxf-rt-javassist/` (primary checkout) | `feature/3.0.x` | 21 | `21` | Active dev |
| `.worktrees/cxf-rt-javassist-main/` | `main` | 17 / 21 | `21` | Primary release |
| `.worktrees/cxf-rt-javassist-2.0.x/` | `feature/2.0.x` | 17 | `17` | Maintenance |
| `.worktrees/cxf-rt-javassist-1.0.x/` | `feature/1.0.x` | 8 | `1.8` | Maintenance |

- **Only JDK / framework-version deltas are allowed to diverge**; business logic, definitions, tests, and docs must stay byte-identical across lines.
- Any shared bug (like the three documented here) **must be patched across all 4 lines atomically**.

## 10. FAQ

### 10.1 Why no `--add-opens` on the 1.0.x surefire line?

JDK 8 **predates the Java Platform Module System (JPMS)**, so it literally **cannot parse** `--add-opens`. Adding the flag would kill the surefire forked JVM immediately with `Unrecognized option: --add-opens`. Conversely, on all JDK ≥ 17 lines, the two `--add-opens` clauses (`java.base/java.lang=ALL-UNNAMED` and `java.base/java.lang.reflect=ALL-UNNAMED`) are **mandatory** — without them, Javassist writing to internal `jdk.internal.reflect.ConstructorAccessor` helpers triggers an `IllegalAccessError` from the strong encapsulation of JPMS. See Tech Design §4.4 and ADR-004.

### 10.2 Why not upgrade commons-lang3 to 3.21.0 / commons-io to 2.23.0?

`commons-lang3 3.21.0` and `commons-io 2.23.0` already raise their minimum Java floor to **JDK 11**, which is incompatible with the `feature/1.0.x` JDK 8 line. Per ADR-003, **until the JDK-8 line is formally declared EOL, all four lines share the same version numbers**, so the project pins the last JDK-8-compatible releases across the board: `3.20.0 / 2.22.0 / 1.11.0`. See Tech Design §5.1.

### 10.3 Can I mutate a `CtClass` after `toClass()`?

No — after `toClass() / toBytecode() / writeFile()`, Javassist **freezes** the `CtClass`. Any later `set*` / `addMethod` throws `RuntimeException: cannot modify frozen class`. Workarounds:
- **Preferred (A)**: finish all builder mutations before any `build → toClass` call.
- **Plan B**: Call `ctClass.defrost();` first, then mutate, then `build → toClass` again. Used pervasively inside the `removeField` tests.

### 10.4 Which behaviours changed in the 2026-08-20 bug-fix batch?

Three shared-code fixes applied across all four lines:

| Bug | Pre-fix behaviour | Post-fix behaviour | Downstream impact |
| :--- | :--- | :--- | :--- |
| Bug#1 (3-arg RestParam ctor) | Explicit `from` discarded; always resolves to QUERY | User-provided `HttpParamEnum` honoured | Code that worked around Bug#1 by **reading params as query strings anyway** needs to stop doing so. Listed as behaviour-fix in CHANGELOG. |
| Bug#2 (4-arg RestParam ctor) | `from` discarded + `this.name` written twice | All 4 fields correct; duplicate write eliminated | Same impact scope as Bug#1; also affects any param that relies on a `@DefaultValue` together with an explicit PATH/HEADER/... binding. |
| Bug#3 (annotParams) | `@DefaultValue` object allocated but **never written to the array**; slot 1 is the duplicate paramAnnot | `[paramAnnot, defAnnot]` correctly written | If you relied on "default values always null / 0" you need explicit compatibility handling. |

For the full root-cause analysis, CodeGraph symbolic-execution evidence, and exact patch lines see [TECHNICAL-DESIGN.md](./TECHNICAL-DESIGN.md) (written in Chinese, all file anchors / code sections / test counts are language-agnostic).

## 11. Contributing & License

- Before opening a PR, run `mvn clean verify` inside the target worktree, then re-run inside the other three lines because ADR-001 requires shared logic to stay byte-identical across 4 JDKs. Include tail of the Surefire report + JaCoCo coverage screenshot in your PR description.
- Any cross-cutting bug fix must be applied to the 4 branches together before submitting.
- Report security issues privately via GitHub Security Advisories. Do **not** open a public issue with exploit details.

Licensed under the [Apache License 2.0](./LICENSE).

---

<div align="center">

[Back to top](#readme-top) · [Technical Design (zh-CN)](./TECHNICAL-DESIGN.md) · [Issues](https://github.com/easy-4-java/cxf-rt-javassist/issues)

</div>
