package io.quarkiverse.httpproblem.jackson;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Response;

import io.quarkiverse.httpproblem.DetailSanitizer;
import io.quarkiverse.httpproblem.ExceptionMapperBase;
import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkiverse.httpproblem.postprocessing.PostProcessorsRegistry;
import tools.jackson.databind.exc.InvalidDefinitionException;

/**
 * Mapper for Jackson InvalidDefinitionException, which is more specialized version of JacksonException,
 * This exception has a specific mapper in Quarkus REST.
 */
@Priority(Priorities.USER - 1)
public final class InvalidDefinitionExceptionMapper extends ExceptionMapperBase<InvalidDefinitionException> {

    private final DetailSanitizer detailSanitizer;

    public InvalidDefinitionExceptionMapper() {
        this.detailSanitizer = new DetailSanitizer();
    }

    @Inject
    public InvalidDefinitionExceptionMapper(PostProcessorsRegistry postProcessorsRegistry,
            DetailSanitizer detailSanitizer) {
        super(postProcessorsRegistry);
        this.detailSanitizer = detailSanitizer;
    }

    @Override
    protected HttpProblem toProblem(InvalidDefinitionException exception) {
        return HttpProblem.builder()
                .withStatus(Response.Status.BAD_REQUEST)
                .withTitle(Response.Status.BAD_REQUEST.getReasonPhrase())
                .withDetail(detailSanitizer.sanitize(exception.getOriginalMessage()))
                .with("field", JacksonFieldPathSerializer.serializePath(exception.getPath()))
                .build();
    }
}
