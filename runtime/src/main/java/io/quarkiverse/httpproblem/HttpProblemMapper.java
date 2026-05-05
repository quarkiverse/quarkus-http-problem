package io.quarkiverse.httpproblem;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;

@Priority(Priorities.USER)
public final class HttpProblemMapper extends ExceptionMapperBase<HttpProblem> {

    @Override
    protected HttpProblem toProblem(HttpProblem exception) {
        return exception;
    }
}
