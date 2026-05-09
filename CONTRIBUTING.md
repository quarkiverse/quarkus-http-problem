# Contributing guide

You're more than welcome to work on this extension. Check [issues](../../issues/) first and pick something from there, 
or add your own suggestions and start discussion. Then create fork or branch and Pull Request once it's ready.

## Setup
- JDK 11 & 17
- GraalVM for native test run, check [Quarkus Contributing guide](https://github.com/quarkusio/quarkus/blob/main/CONTRIBUTING.md#setup) for more details.

### IDE Config and Code Style

Quarkus has a strictly enforced code style, so as this extension. Code formatting is done by the Eclipse code formatter, using the config files
found in the `ide-config` directory. By default when you run `./mvnw install` the code will be formatted automatically.

If you want to run the formatting without doing a full build, you can run `./mvnw process-sources -P it` or `./mvnw formatter:format -P it`.

More details on how to setup your ide can be found in official [Quarkus Contributing guide](https://github.com/quarkusio/quarkus/blob/main/CONTRIBUTING.md#ide-config-and-code-style)

### Modules
`runtime` - this happens in the runtime: this code ends up in your app's classpath\
`deployment` - this is Quarkus compile-time stuff\
`integration-test` - test scenarios + test endpoints + runners for Jackson and JsonB apps

### Architecture
Everything in this extension revolves around `ExceptionMapperBase` abstract class from `runtime` module. All exceptions go through
exception mappers extending this class.

Exception lifecycle:
1. JaxRS/JakartaRS implementation (RESTeasy) catches exception, and immediately looks for best matching `ExceptionMapper`. Hopefully it will be one of our mappers :)
2. `ExceptionMapperBase::toResponse` method is called, where original exception is turned into `HttpProblem` object by the specific subclass mapper (e.g `WebApplicationExceptionMapper`).
3. `HttpProblem` goes into post-processing phase to apply logging, MCD properties injection etc.
4. Enhanced `HttpProblem` is turned into JaxRS/JakartaRS `Response` object, with `HttpProblem` object placed as entity (response body). This is where `ExceptionMapper` work is finished.
5. RESTeasy serializes `Response` object into raw HTTP response, with little help from our JSON serializer (either `JacksonProblemSerializer` or `JsonBProblemSerializer`)
6. End user gets nice `application/problem+json` HTTP response.

### Running tests
Jackson profile is active by default when you use IntelliJ test runner.

Command line:\
`./mvnw verify` - jackson profile with RESTeasy classic (blocking) enabled by default\ 
`./mvnw verify -pl integration-test -Pjackson`\
`./mvnw verify -pl integration-test -Pjsonb`\
`./mvnw clean verify -Pnative,jackson -pl integration-test` - running tests in native mode