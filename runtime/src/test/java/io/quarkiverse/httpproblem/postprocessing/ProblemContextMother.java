package io.quarkiverse.httpproblem.postprocessing;

import org.jboss.resteasy.specimpl.ResteasyUriInfo;

final class ProblemContextMother {

    private ProblemContextMother() {
    }

    static ProblemContext simpleContext() {
        return withCause(new RuntimeException());
    }

    static ProblemContext withCause(Throwable cause) {
        return ProblemContext.of(cause, new ResteasyUriInfo("http://localhost/endpoint", "endpoint"));
    }
}
