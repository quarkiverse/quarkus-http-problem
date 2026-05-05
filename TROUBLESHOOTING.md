# Troubleshooting #
List of common problems you may encounter when using this extension.

## Some `WebApplicationException`s seems not to be handled properly
Long story short: any `WebApplicationException`, which wraps Response with non-null entity (body) will **bypass all JaxRS Exception Mappers**, both built-in and custom ones. HTTP response will be purely based on the `Response` object.

Here are some code examples. This exception:
```java
throw new WebApplicationException(Response.status(400).entity("{\"message\": \"This request is bad\"}").build());
```
will bypass all matching Exception Mappers, and http response will look like this (with HTTP status `400`):
```json
{
  "message": "This request is bad"
}
```

On the other hand, if your `WebApplicationException` does not wrap `Response` object, or `Response` does not have an entity (body):
```java
throw new WebApplicationException(400);
// or
throw new WebApplicationException(Response.status(400).build());
```
matching Exception Mapper will be triggered as expected, and the response will look like this:
```json
{
  "status": 400,
  "title": "Bad Request",
  "detail": "HTTP 400 Bad Request"
}
```

### RestClient case
This behaviour can be observed when RestClient is used without `ExceptionMapper` assigned (via `@RegisterProvider`): all `WebApplicationExceptions` (wrapping `Response` object containing entity) thrown by RestClient will be propagated 'as-is' directly to your REST API clients without any modification.  
**Fix**: create even the simplest `ExceptionMapper` via `@RegisterProvider` for your RestClient, where you can wrap `WebApplicationException` with custom `Exception` (even `RuntimeException` will do the trick).

### Explanation
This is all by-design: [JaxRS specification](https://raw.githubusercontent.com/javaee/jax-rs-spec/master/spec.pdf) defines how `WebApplicationException` should be translated into http response, which all implementations must comply.

```
Section 3.3.4
(...)
Instances of WebApplicationException and its subclasses MUST be mapped to a response as follows. If the response property of the exception does not contain an entity and an exception mapping
provider (see Section 4.4) is available for WebApplicationException or the corresponding subclass, an implementation MUST use the provider to create a new Response instance, otherwise the
response property is used directly. 
(...)
```

### References  
[JaxRS specification](https://raw.githubusercontent.com/javaee/jax-rs-spec/master/spec.pdf)  
[Quarkus issue related to this behaviour](https://github.com/quarkusio/quarkus/issues/4031)

## There are strange fields (stack traces etc.) in API error responses
Your code most likely overrides `Jackson`/`JsonB` configuration by providing bean of type `com.fasterxml.jackson.databind.ObjectMapper`/`jakarta.json.bind.JsonB`, thus deregistering serializers provided by this extension - this is considered a bad practice, as described in [Quarkus docs](https://quarkus.io/guides/rest-json#configuring-json-support). 
The easiest fix is to customize, not override existing configurations.

Jackson:
```java
@ApplicationScoped
public class RegisterCustomModuleCustomizer implements ObjectMapperCustomizer {
    public void customize(ObjectMapper mapper) {
        mapper.registerModule(new CustomModule());
    }
}
```

JsonB:
```java
@ApplicationScoped
public class FooSerializerRegistrationCustomizer implements JsonbConfigCustomizer {
    public void customize(JsonbConfig config) {
        config.withSerializers(new FooSerializer());
    }
}
```
This approach will **not** wipe out configuration provided by extensions (including this one) and is recommended by Quarkus.

In case you really have to provide your own configuration bean, and still want to use this extension you'll have to apply all customizers yourself as described below:

```
Jackson: it is very important to manually inject and apply all io.quarkus.jackson.ObjectMapperCustomizer beans in the CDI producer that produces ObjectMapper. Failure to do so will prevent Jackson specific customizations provided by various extensions from being applied.
JsonB: it is very important to manually inject and apply all io.quarkus.jsonb.JsonbConfigCustomizer beans in the CDI producer that produces jakarta.json.bind.Jsonb. Failure to do so will prevent JSON-B specific customizations provided by various extensions from being applied.
```
