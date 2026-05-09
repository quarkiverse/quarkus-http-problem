package io.quarkiverse.httpproblem.jaxrs;

import java.util.Optional;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import io.quarkiverse.httpproblem.ExceptionMapperBase;
import io.quarkiverse.httpproblem.HttpProblem;

/**
 * Generic exception mapper for JaxRS WebApplicationExceptions - it passes status and message to application/problem response.
 */
@Priority(Priorities.USER)
public final class WebApplicationExceptionMapper extends ExceptionMapperBase<WebApplicationException> {

    @Override
    protected HttpProblem toProblem(WebApplicationException exception) {
        Response.StatusType status = exception.getResponse().getStatusInfo();

        HttpProblem.Builder problem = HttpProblem.builder()
                .withTitle(status.getReasonPhrase())
                .withStatus(status)
                .withDetail(exception.getMessage());

        Optional.ofNullable(exception.getResponse().getHeaders())
                .ifPresent(headers -> {
                    headers.entrySet().stream()
                            .filter(entry -> !"content-length".equalsIgnoreCase(entry.getKey()))
                            .forEach(entry -> entry.getValue().forEach(value -> problem.withHeader(entry.getKey(), value)));
                });

        return problem.build();
    }
}
