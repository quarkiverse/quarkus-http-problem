package io.quarkiverse.httpproblem.deployment;

final class JsonBDetector extends ClasspathDetector {

    JsonBDetector() {
        super("io.quarkus.jsonb.JsonbProducer");
    }

}