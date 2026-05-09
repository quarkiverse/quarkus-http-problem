package io.quarkiverse.httpproblem.deployment;

final class RestEasyReactiveDetector extends ClasspathDetector {

    RestEasyReactiveDetector() {
        super("io.quarkus.resteasy.reactive.server.runtime.QuarkusContextProducers");
    }

}
