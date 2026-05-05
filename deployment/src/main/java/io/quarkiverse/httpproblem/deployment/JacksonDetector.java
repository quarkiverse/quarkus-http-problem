package io.quarkiverse.httpproblem.deployment;

final class JacksonDetector extends ClasspathDetector {

    JacksonDetector() {
        super("io.quarkus.jackson.ObjectMapperCustomizer");
    }

}
