package io.quarkiverse.httpproblem.jaxrs;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

import jakarta.annotation.Priority;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import io.quarkiverse.httpproblem.ExceptionMapperBase;
import io.quarkiverse.httpproblem.HttpProblem;

@Priority(Priorities.USER)
@APIResponse(responseCode = "404", description = "Not Found: server cannot find the requested resource")
public final class NotFoundExceptionMapper extends ExceptionMapperBase<NotFoundException>
        implements ExceptionMapper<NotFoundException> {

    @Override
    protected HttpProblem toProblem(NotFoundException exception) {
        return HttpProblem.valueOf(NOT_FOUND, exception.getMessage());
    }
}
