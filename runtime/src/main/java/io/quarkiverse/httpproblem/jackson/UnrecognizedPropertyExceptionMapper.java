package io.quarkiverse.httpproblem.jackson;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Response;

import io.quarkiverse.httpproblem.ExceptionMapperBase;
import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkiverse.httpproblem.postprocessing.PostProcessorsRegistry;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * UnrecognizedPropertyException is thrown by Jackson, when request payload json does not fit DTO object with @Valid annotation
 * (e.g field has different name) and FAIL_ON_UNKNOWN_PROPERTIES is enabled (default changed in quarkus 1.11)
 */
@Priority(Priorities.USER - 1)
public final class UnrecognizedPropertyExceptionMapper extends ExceptionMapperBase<UnrecognizedPropertyException> {

    public UnrecognizedPropertyExceptionMapper() {
    }

    @Inject
    public UnrecognizedPropertyExceptionMapper(PostProcessorsRegistry postProcessorsRegistry) {
        super(postProcessorsRegistry);
    }

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
