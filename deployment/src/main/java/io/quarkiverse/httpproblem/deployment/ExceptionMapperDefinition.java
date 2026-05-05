package io.quarkiverse.httpproblem.deployment;

import java.util.Objects;
import java.util.function.BooleanSupplier;

final class ExceptionMapperDefinition {

    static ExceptionClassSupplier mapper(String mapper) {
        Objects.requireNonNull(mapper);
        return new ExceptionClassSupplier(mapper);
    }

    static class ExceptionClassSupplier {
        private final String mapper;

        private ExceptionClassSupplier(String mapper) {
            this.mapper = mapper;
        }

        ExceptionMapperDefinition thatHandles(String exception) {
            Objects.requireNonNull(exception);
            return new ExceptionMapperDefinition(exception, this.mapper, new ClasspathDetector(exception));
        }
    }

    final String exceptionClassName;
    final String mapperClassName;
    private final BooleanSupplier detector;

    private ExceptionMapperDefinition(String exceptionClassName, String mapperClassName, BooleanSupplier detector) {
        this.exceptionClassName = exceptionClassName;
        this.mapperClassName = mapperClassName;
        this.detector = detector;
    }

    ExceptionMapperDefinition onlyIf(BooleanSupplier newDetector) {
        return new ExceptionMapperDefinition(this.exceptionClassName, this.mapperClassName, newDetector);
    }

    boolean isNeeded() {
        return this.detector.getAsBoolean();
    }
}
