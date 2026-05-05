package io.quarkiverse.httpproblem;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * Default exception mapper processing all exceptions not matching any more specific mapper.
 */
@Priority(Priorities.USER)
@APIResponse(responseCode = "500", description = "Internal Server Error: the server encountered an unexpected condition that prevented it from fulfilling the request")
public final class DefaultExceptionMapper extends ExceptionMapperBase<Exception>
        implements ExceptionMapper<Exception> {

    @Override
    protected HttpProblem toProblem(Exception exception) {
        return HttpProblem.valueOf(INTERNAL_SERVER_ERROR);
    }
}
