# Migration from `quarkus-resteasy-problem`

The extension was renamed to reflect support for both RESTEasy Classic and Quarkus REST (formerly RESTEasy Reactive). Apply the following changes when moving from `io.quarkiverse.resteasy-problem:quarkus-resteasy-problem` (3.32.x or earlier) to `io.quarkiverse.httpproblem:quarkus-http-problem` (3.33.0 onward; on Central this may be **3.33.0.CR1** until a GA build is published).

If you only consumed the extension transitively and have no custom imports or `quarkus.resteasy.problem.*` configuration, replacing the dependency coordinates may be enough.

See [README.md — Versioning](README.md#versioning) for which extension release matches your Quarkus version.

## Dependency coordinates

### Maven

Replace the dependency coordinates:

```xml
<dependency>
    <groupId>io.quarkiverse.httpproblem</groupId>
    <artifactId>quarkus-http-problem</artifactId>
    <version>3.33.0.CR1</version>
</dependency>
```

Use a version compatible with your Quarkus release (see the [versioning table](README.md#versioning)).

Remove `io.quarkiverse.resteasy-problem:quarkus-resteasy-problem`.

### Gradle (Kotlin DSL)

```kotlin
implementation("io.quarkiverse.httpproblem:quarkus-http-problem:3.33.0.CR1")
```

## Configuration

### All settings are build-time

Every `quarkus.http-problem.*` configuration key is now fixed at build time. After changing values in `application.properties` or `application.yaml`, rebuild packaged applications; in `quarkus:dev`, hot reload applies the change automatically.

### Update property prefix

Rename the configuration root only: `quarkus.resteasy.problem` → `quarkus.http-problem`. All keys under the old prefix move in parallel; for example:

```properties
quarkus.resteasy.problem.include-mdc-properties=uuid,traceId
```

becomes:

```properties
quarkus.http-problem.include-mdc-properties=uuid,traceId
```

Unrelated to the `quarkus.http-problem.*` keys above: problem summaries still use Quarkus’s usual logging mechanism, under the SLF4J category `http-problem`. If you set something like `quarkus.log.category.http-problem.level`, keep it as-is when migrating—that category name did not change in recent `quarkus-resteasy-problem` releases either.

## Update Java imports

If you import any class from this extension, change the package from `io.quarkiverse.resteasy.problem` to `io.quarkiverse.httpproblem`. For example:

```java
import io.quarkiverse.resteasy.problem.HttpProblem;
// becomes
import io.quarkiverse.httpproblem.HttpProblem;
```
