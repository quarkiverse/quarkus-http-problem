package io.quarkiverse.httpproblem.jackson;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;

import io.quarkiverse.httpproblem.DetailSanitizer;
import io.quarkiverse.httpproblem.ExceptionMapperBase;
import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkiverse.httpproblem.postprocessing.PostProcessorsRegistry;
import tools.jackson.core.JacksonException;

/**
 * Mapper for Jackson payload processing exceptions.
 */
@Priority(Priorities.USER)
public final class JsonProcessingExceptionMapper extends ExceptionMapperBase<JacksonException> {

    private final DetailSanitizer detailSanitizer;

    public JsonProcessingExceptionMapper() {
        this.detailSanitizer = new DetailSanitizer();
    }

    @Inject
    public JsonProcessingExceptionMapper(PostProcessorsRegistry postProcessorsRegistry,
            DetailSanitizer detailSanitizer) {
        super(postProcessorsRegistry);
        this.detailSanitizer = detailSanitizer;
    }

    @Override
    protected HttpProblem toProblem(JacksonException exception) {
        return HttpProblem.valueOf(BAD_REQUEST, detailSanitizer.sanitize(exception.getOriginalMessage()));
    }
}
