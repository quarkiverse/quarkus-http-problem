# Problem Details for HTTP APIs ([RFC 9457](https://datatracker.ietf.org/doc/html/rfc9457)) for Quarkus REST / RESTEasy

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/quarkiverse/quarkus-http-problem/blob/main/LICENSE.txt)
[![Documentation](https://img.shields.io/badge/docs-quarkus.io-0A6EBD)](https://quarkus.io/extensions/io.quarkiverse.httpproblem/quarkus-http-problem/)

[![Build status](https://github.com/quarkiverse/quarkus-http-problem/actions/workflows/unit-tests.yaml/badge.svg)](https://github.com/quarkiverse/quarkus-http-problem/actions/workflows/unit-tests.yaml)
[![Build status](https://github.com/quarkiverse/quarkus-http-problem/actions/workflows/integration-tests.yaml/badge.svg)](https://github.com/quarkiverse/quarkus-http-problem/actions/workflows/integration-tests.yaml)
[![Build status](https://github.com/quarkiverse/quarkus-http-problem/actions/workflows/native-mode-tests.yaml/badge.svg)](https://github.com/quarkiverse/quarkus-http-problem/actions/workflows/native-mode-tests.yaml)

Extension implementing **Problem Details for HTTP APIs** as defined in [RFC 9457](https://datatracker.ietf.org/doc/html/rfc9457) (which [obsoletes RFC 7807](https://datatracker.ietf.org/doc/html/rfc7807); the wire format and media type remain the same for typical JSON usage). It maps exceptions to `application/problem+json` HTTP responses. Inspired by the [Zalando Problem library](https://github.com/zalando/problem), originally open sourced by [Tieto](https://github.com/tieto), now part of Quarkiverse.

This extension supports:

- `quarkus-rest-jackson` and `quarkus-rest-jsonb` (reactive and blocking)
- `quarkus-resteasy-jackson` and `quarkus-resteasy-jsonb` (classic blocking)
- OpenAPI integration (via `quarkus-smallrye-openapi`)
- JVM and native mode

## Why use this extension?
From [RFC 9457](https://datatracker.ietf.org/doc/html/rfc9457):

> HTTP status codes (...) cannot always convey enough information about errors to be helpful. While humans using web browsers can often understand an HTML response content, non-human consumers of HTTP APIs have difficulty doing so.

`quarkus-http-problem` helps address these concerns by providing:
- **consistency** - it standardizes REST API error responses and ensures a consistent format, regardless of the JSON provider (Jackson vs JSON-B) or execution model (classic blocking vs reactive).   
- **predictability** - whether an exception is expected (thrown intentionally) or unexpected, your API consumers get a similar and repeatable error format.  
- **safety** - it helps prevent leaking implementation details such as stack traces, DTO names, and resource class names.
- **time savings** - in most cases, you no longer need to implement your own JAX-RS `ExceptionMapper`s, which keeps your app smaller and less error-prone.

See [Built-in Exception Mappers Wiki](https://github.com/quarkiverse/quarkus-http-problem/wiki#built-in-exception-mappers) for more details.

## Getting started

Use `[io.quarkiverse.httpproblem:quarkus-http-problem](https://central.sonatype.com/artifact/io.quarkiverse.httpproblem/quarkus-http-problem)` with **Quarkus 3.32 or newer** (same baseline as the extension version you choose). Older Quarkus releases should keep using `[quarkus-resteasy-problem](#quarkus-resteasy-problem-legacy-coordinates)` until you upgrade.

Add this to your pom.xml (this extension is not on the Quarkus Platform BOM yet, so the version must be explicit):

```xml
<dependency>
    <groupId>io.quarkiverse.httpproblem</groupId>
    <artifactId>quarkus-http-problem</artifactId>
    <version>3.33.1</version>
</dependency>
```

Pick a version that matches your Quarkus release; see [Versioning](#versioning) below.

Once you run Quarkus: `./mvnw compile quarkus:dev`, you should see `http-problem` in the logs:

```
Installed features: [cdi, http-problem, rest-jackson, ...]
```

Now you can throw `HttpProblem`s (using builder or a subclass), JaxRS exceptions (e.g `NotFoundException`) or `ThrowableProblem`s from Zalando library:

```java
package problem;

import io.quarkiverse.httpproblem.HttpProblem;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/hello")
public class HelloResource {

    @GET
    public String hello() {
        throw HttpProblem.builder()
            .withTitle("Bad hello request")
            .withStatus(Response.Status.BAD_REQUEST)
            .withDetail("rfc9457-by-example")
            .with("hello", "world")
            .withHeader("X-Problem-Message", message)
            .build();
    }
}
```

Open [http://localhost:8080/hello](http://localhost:8080/hello) in your browser, and you should see this response:

```json
HTTP/1.1 400 Bad Request
X-Problem-Message: rfc9457-by-example
Content-Type: application/problem+json
        
{
    "status": 400,
    "title": "Bad hello request",
    "detail": "rfc9457-by-example",
    "instance": "/hello",
    "hello": "world"
}
```

This extension will also produce the following log message:

```
10:53:48 INFO [http-problem] (executor-thread-1) status=400, title="Bad hello request", detail="rfc9457-by-example"
```

Exceptions transformed into http 500s (aka server errors) will be logged as `ERROR`, including full stacktrace.

More on throwing problems: [zalando/problem usage](https://github.com/zalando/problem#usage)

## Versioning

### `quarkus-http-problem`

Published as [io.quarkiverse.httpproblem:quarkus-http-problem](https://central.sonatype.com/artifact/io.quarkiverse.httpproblem/quarkus-http-problem). This line targets **Quarkus 3.32+**; add the dependency and version from **Getting started**. 

If you run an **older Quarkus**, stay on `**quarkus-resteasy-problem`** until you upgrade (table in the subsection below), or bump Quarkus first and then switch here.

If you are upgrading from `io.quarkiverse.resteasy-problem`, see [Migration from](./MIGRATION-FROM-RESTEASY-PROBLEM.md) `quarkus-resteasy-problem`.

### `quarkus-resteasy-problem` (legacy coordinates)

New development continues only as `quarkus-http-problem` (same Git repository, new groupId, artifactId, and Java packages). The tables below pick the Quarkus line first, then the **latest** matching extension release on Central—use the linked version pages on Maven Central for older patch numbers if you must stay on an older line.

`io.quarkiverse.resteasy-problem` (last release for this artifact: [3.32.1](https://central.sonatype.com/artifact/io.quarkiverse.resteasy-problem/quarkus-resteasy-problem/3.32.1)):


| Quarkus version | Extension version                                                                                               |
| --------------- | --------------------------------------------------------------------------------------------------------------- |
| 3.32+           | [3.32.1](https://central.sonatype.com/artifact/io.quarkiverse.resteasy-problem/quarkus-resteasy-problem/3.32.1) |
| 3.9+            | [3.21.1](https://central.sonatype.com/artifact/io.quarkiverse.resteasy-problem/quarkus-resteasy-problem/3.21.1) |


`com.tietoevry.quarkus` (original vendor coordinates before the Quarkiverse move; last release: [3.9.0](https://central.sonatype.com/artifact/com.tietoevry.quarkus/quarkus-resteasy-problem/3.9.0)):


| Quarkus version | Extension version                                                                                   |
| --------------- | --------------------------------------------------------------------------------------------------- |
| 3.9+            | [3.9.0](https://central.sonatype.com/artifact/com.tietoevry.quarkus/quarkus-resteasy-problem/3.9.0) |
| 3.7.x – 3.8.x   | [3.7.0](https://central.sonatype.com/artifact/com.tietoevry.quarkus/quarkus-resteasy-problem/3.7.0) |
| < 3.7           | [3.1.0](https://central.sonatype.com/artifact/com.tietoevry.quarkus/quarkus-resteasy-problem/3.1.0) |


Quarkus **2.x** / **1.x** applications that still depend on `com.tietoevry.quarkus:quarkus-resteasy-problem` are not covered by `quarkus-http-problem`; upgrade Quarkus first, then switch to `[io.quarkiverse.httpproblem:quarkus-http-problem](https://central.sonatype.com/artifact/io.quarkiverse.httpproblem/quarkus-http-problem)` when you are on a supported Quarkus 3.x line.

## RestClients (available since [v3.20.0](https://github.com/quarkiverse/quarkus-http-problem/releases/tag/3.20.0))

If you use RestClients and your upstream services return `application/problem+json` responses, you can register `ThrowingHttpProblemClientExceptionMapper` for your client to get automatic deserialization and rethrowing `HttpProblem` instead of `ClientWebApplicationException` 

```java
@RegisterRestClient(configKey = "my-rest-client")
@RegisterProvider(value = ThrowingHttpProblemClientExceptionMapper.class)
public interface MyRestClient {
    @GET
    @Path("/resource")
    void getResource();
}
```

## OpenAPI integration (available since [v3.20.0](https://github.com/quarkiverse/quarkus-http-problem/releases/tag/3.20.0))

When `quarkus-smallrye-openapi` is in the classpath, this extension provides a bunch of out-of-the-box features :

- complete OpenApi schema definitions for `HttpProblem` and `HttpValidationProblem` that can be used in annotations (e.g. `@Schema(implementation = HttpProblem.class)`)
- auto-generating documentation for endpoints declaring `throws` for few common exceptions, e.g. `NotFoundException`,`ForbiddenException` or even `Exception`

```java
@GET
@Path("/my-endpoint")
@APIResponse(responseCode = "409", description = "Request received but there has been a conflict")
public void endpoint() throws NotFoundException {}
```

this endpoint will automatically get both 409 (from `@APIResponse`) and 404 (derived from `throws`) responses documented in open api.

- attaching `HttpProblem` schema to endpoints declaring error api responses (4XX and 5XX) without `content` field specified:

```java
@APIResponse(
  responseCode = "409", 
  description = "Request received but there has been a conflict"
)
```

is an equivalent to this:

```java
@APIResponse(
  responseCode = "409", 
  description = "Request received but there has been a conflict",
  content = @Content(
    mediaType = "application/problem+json",
    schema = @Schema(implementation = HttpProblem.class)
  )
)
```

- if you project needs to define and document additional Problem Detail properties, you need to extend `HttpProblem`, annotate it with OpenApi annotations:

```java
@Schema(name = "MyHttpProblem", description = "HTTP Problem Response according to MyProject",
        additionalProperties = Schema.True.class)
public class MyHttpProblem extends HttpProblem {

    @Schema(description = "Additional parameters providing more details about the problem", examples = "{\"timestamp\":\"2024-03-20T10:00:00Z\",\"traceId\":\"550e8400-e29b-41d4-a716-446655440000\"}")
    private SortedMap<String, Object> contexts;

    @Schema(description = "Original cause of error, only set when forwarding an underlying problem")
    private MyHttpProblem cause;

}
```

and tell this extension which schema is default for Problem Details: 

```properties
quarkus.http-problem.openapi.default-schema=MyHttpProblem
```

## Configuration options

- (Build time) Include MDC properties in the API response. You have to provide those properties to MDC using `MDC.put`

```
quarkus.http-problem.include-mdc-properties=uuid,application,version
```

Result:

```json
{
  "status": 500,
  "title": "Internal Server Error",
  "uuid": "d79f8cfa-ef5b-4501-a2c4-8f537c08ec0c",
  "application": "awesome-microservice",
  "version": "1.0"
}
```

- (Build time) Changes default `400 Bad request` response status when `ConstraintViolationException` is thrown (e.g. by Hibernate Validator)

```
quarkus.http-problem.constraint-violation.status=422
quarkus.http-problem.constraint-violation.title=Constraint violation
```

Result:

```json
HTTP/1.1 422 Unprocessable Entity
Content-Type: application/problem+json

{
    "status": 422,
    "title": "Constraint violation",
    (...)
}
```

- (Runtime) Tuning logging

```
quarkus.log.category.http-problem.level=INFO # default: all problems are logged
quarkus.log.category.http-problem.level=ERROR # only HTTP 5XX problems are logged
quarkus.log.category.http-problem.level=OFF # disables all problems-related logging
```

## Custom ProblemPostProcessor

If you want to intercept, change or augment a mapped `HttpProblem` before it gets serialized into raw HTTP response 
body, you can create a bean extending `ProblemPostProcessor`, and override `apply` method.

Example:

```java
@ApplicationScoped
class CustomPostProcessor implements ProblemPostProcessor {
    
    @Inject // acts like normal bean, DI works fine etc
    Validator validator;
    
    @Override
    public HttpProblem apply(HttpProblem problem, ProblemContext context) {
        return HttpProblem.builder(problem)
                .with("injected_from_custom_post_processor", "hello world " + context.path)
                .build();
    }
    
}
```

Custom post-processors registered as CDI beans are applied globally. If a particular mapper needs its own post-processing
pipeline, pass a custom `PostProcessorsRegistry` to `ExceptionMapperBase`:

```java
class CustomExceptionMapper extends ExceptionMapperBase<CustomException> {

    private static final PostProcessorsRegistry POST_PROCESSORS = new PostProcessorsRegistry(
            () -> ExceptionMapperBase.postProcessorsRegistry.stream()
                    .filter(processor -> !(processor instanceof ProblemLogger)));

    CustomExceptionMapper() {
        super(POST_PROCESSORS);
    }

    @Override
    protected HttpProblem toProblem(CustomException exception) {
        return HttpProblem.valueOf(Response.Status.BAD_REQUEST);
    }
}
```

`new PostProcessorsRegistry()` creates an empty custom registry. `new PostProcessorsRegistry(parentRegistry)` creates a
custom registry that live-inherits from the parent registry and can register mapper-specific post-processors. The supplier
constructor and `stream()` method can be used for more advanced live views, for example to filter globally registered
post-processors. Custom-created registries do not include extension defaults unless they inherit them from another registry
or register them explicitly.

## Troubles?

If you have questions, concerns, bug reports, etc, please file an issue in this repository's Issue Tracker. You may also want to have a look at [troubleshooting FAQ](./TROUBLESHOOTING.md).

## Contributing

To contribute, simply make a pull request and add a brief description (1-2 sentences) of your addition or change.
For more details check the [contribution guidelines](./CONTRIBUTING.md).
