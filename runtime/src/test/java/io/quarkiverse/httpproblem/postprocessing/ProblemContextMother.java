package io.quarkiverse.httpproblem.postprocessing;

final class ProblemContextMother {

    private ProblemContextMother() {
    }

    static ProblemContext simpleContext() {
        return withCause(new RuntimeException());
    }

    static ProblemContext withCause(Throwable cause) {
        return ProblemContext.of(cause, "endpoint");
    }
}
