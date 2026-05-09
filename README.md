# Problem Details for HTTP APIs (RFC-7807) implementation for Quarkus REST / RESTEasy.

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/quarkiverse/quarkus-http-problem/blob/main/LICENSE.txt)
[![Documentation](https://img.shields.io/badge/docs-quarkus.io-0A6EBD)](https://quarkus.io/extensions/io.quarkiverse.httpproblem/quarkus-http-problem/)

[![Build status](https://github.com/quarkiverse/quarkus-http-problem/actions/workflows/unit-tests.yaml/badge.svg)](https://github.com/quarkiverse/quarkus-http-problem/actions/workflows/unit-tests.yaml)
[![Build status](https://github.com/quarkiverse/quarkus-http-problem/actions/workflows/integration-tests.yaml/badge.svg)](https://github.com/quarkiverse/quarkus-http-problem/actions/workflows/integration-tests.yaml)
[![Build status](https://github.com/quarkiverse/quarkus-http-problem/actions/workflows/native-mode-tests.yaml/badge.svg)](https://github.com/quarkiverse/quarkus-http-problem/actions/workflows/native-mode-tests.yaml)

[RFC7807 Problem](https://tools.ietf.org/html/rfc7807) extension for Quarkus Rest applications. It maps Exceptions to `application/problem+json` HTTP responses. Inspired by [Zalando Problem library](https://github.com/zalando/problem), originally open sourced by [Tietoevry](https://github.com/evry), now part of Quarkiverse.

This extension supports:
- `quarkus-rest-jackson` and `quarkus-rest-jsonb` (reactive and blocking)
- `quarkus-resteasy-jackson` and `quarkus-resteasy-jsonb` (classic blocking)
- OpenAPI integration (via `quarkus-smallrye-openapi`)
- JVM and native mode

## Why you should use this extension?
- __consistency__ - it unifies your REST API error messages, and gives it much needed consistency, no matter which JSON provider (Jackson vs JsonB) or paradigm (classic/blocking vs reactive) you're using.   

- __predictability__ - no matter what kind of exception is thrown: expected (thrown by you on purpose), or unexpected (not thrown 'by design') - your API consumer gets similar, repeatable experience.  

- __safety__ - it helps prevent leakage of some implementation details like stack-traces, DTO/resource class names etc.

- __time-saving__ - in most cases you will not have to implement your own JaxRS `ExceptionMapper`s anymore, which makes your app smaller, and less error-prone. 

See [Built-in Exception Mappers Wiki](https://github.com/quarkiverse/quarkus-http-problem/wiki#built-in-exception-mappers) for more details.

From [RFC7807](https://tools.ietf.org/html/rfc7807):
```
HTTP [RFC7230] status codes are sometimes not sufficient to convey
enough information about an error to be helpful.  While humans behind
Web browsers can be informed about the nature of the problem with an
HTML [W3C.REC-html5-20141028] response body, non-human consumers of
so-called "HTTP APIs" are usually not.
```

## Usage
### Quarkus 3.14+
Add this to your pom.xml:
```xml
<dependency>
    <groupId>io.quarkiverse.httpproblem</groupId>
    <artifactId>quarkus-http-problem</artifactId>
</dependency>
```

Once you run Quarkus: `./mvnw compile quarkus:dev`, and you will find `http-problem` in the logs:
<pre>
Installed features: [cdi, <b><u>http-problem</u></b>, rest-jackson, ...]
</pre>

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
        throw new HelloProblem("rfc7807-by-example");
    }

    static class HelloProblem extends HttpProblem {
        HelloProblem(String message) {
            super(builder()
                    .withTitle("Bad hello request")
                    .withStatus(Response.Status.BAD_REQUEST)
                    .withDetail(message)
                    .withHeader("X-RFC7807-Message", message)
                    .with("hello", "world"));
        }
    }
}
```

Open [http://localhost:8080/hello](http://localhost:8080/hello) in your browser, and you should see this response:

```json
HTTP/1.1 400 Bad Request
X-RFC7807-Message: rfc7807-by-example
Content-Type: application/problem+json
        
{
    "status": 400,
    "title": "Bad hello request",
    "detail": "rfc7807-by-example",
    "instance": "/hello",
    "hello": "world"
}
```

This extension will also produce the following log message:
```
10:53:48 INFO [http-problem] (executor-thread-1) status=400, title="Bad hello request", detail="rfc7807-by-example"
```
Exceptions transformed into http 500s (aka server errors) will be logged as `ERROR`, including full stacktrace.

More on throwing problems: [zalando/problem usage](https://github.com/zalando/problem#usage)

## RestClients (available since <a href="https://github.com/quarkiverse/quarkus-http-problem/releases/tag/3.20.0">v3.20.0</a>)
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

## OpenAPI integration (available since <a href="https://github.com/quarkiverse/quarkus-http-problem/releases/tag/3.20.0">v3.20.0</a>)
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

- (Runtime) Changes default `400 Bad request` response status when `ConstraintViolationException` is thrown (e.g. by Hibernate Validator)
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

## Troubles?

If you have questions, concerns, bug reports, etc, please file an issue in this repository's Issue Tracker. You may also want to have a look at [troubleshooting FAQ](./TROUBLESHOOTING.md).

## Contributing

To contribute, simply make a pull request and add a brief description (1-2 sentences) of your addition or change.
For more details check the [contribution guidelines](./CONTRIBUTING.md).
