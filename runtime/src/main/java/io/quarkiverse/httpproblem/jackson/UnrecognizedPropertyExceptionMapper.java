package io.quarkiverse.httpproblem.jackson;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Response;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import io.quarkiverse.httpproblem.ExceptionMapperBase;
import io.quarkiverse.httpproblem.HttpProblem;

/**
 * UnrecognizedPropertyException is thrown by Jackson, when request payload json does not fit DTO object with @Valid annotation
 * (e.g field has different name) and FAIL_ON_UNKNOWN_PROPERTIES is enabled (default changed in quarkus 1.11)
 */
@Priority(Priorities.USER - 1)
public final class UnrecognizedPropertyExceptionMapper extends ExceptionMapperBase<UnrecognizedPropertyException> {

    @Override
    protected HttpProblem toProblem(UnrecognizedPropertyException exception) {
        String msg = "Unrecognized field \"" + exception.getPropertyName() + "\", not marked as ignorable";

        return HttpProblem.builder()
                .withStatus(Response.Status.BAD_REQUEST)
                .withTitle(Response.Status.BAD_REQUEST.getReasonPhrase())
                .withDetail(msg)
                .with("field", exception.getPropertyName())
                .build();
    }
}
