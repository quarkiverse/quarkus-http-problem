package io.quarkiverse.httpproblem.deployment;

final class OpenApiDetector extends ClasspathDetector {

    OpenApiDetector() {
        super("io.quarkus.smallrye.openapi.OpenApiFilter");
    }

}
