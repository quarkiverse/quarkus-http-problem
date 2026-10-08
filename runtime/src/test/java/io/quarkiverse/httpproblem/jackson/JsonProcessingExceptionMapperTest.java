package io.quarkiverse.httpproblem.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import io.quarkiverse.httpproblem.DetailSanitizer;
import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkiverse.httpproblem.postprocessing.PostProcessorsRegistry;
import io.quarkiverse.httpproblem.postprocessing.ProblemDefaultsProvider;
import io.quarkiverse.httpproblem.postprocessing.ProblemLogger;
import io.quarkiverse.httpproblem.postprocessing.ProblemLoggingConfig;
import tools.jackson.core.exc.StreamReadException;

class JsonProcessingExceptionMapperTest {

    PostProcessorsRegistry registry = new PostProcessorsRegistry(
            List.of(new ProblemLogger(ProblemLoggingConfig.defaults()), new ProblemDefaultsProvider()));

    @Test
    void shouldProduceHttp400WithOriginalMessageWhenIncludeDetails() {
        JsonProcessingExceptionMapper mapper = new JsonProcessingExceptionMapper(registry, new DetailSanitizer(true));
        StreamReadException exception = new StreamReadException("Unexpected end-of-input");

        Response response = mapper.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getMediaType()).isEqualTo(HttpProblem.MEDIA_TYPE);
        assertThat(response.getEntity())
                .isInstanceOf(HttpProblem.class)
                .hasFieldOrPropertyWithValue("detail", "Unexpected end-of-input");
    }

    @Test
    void shouldSanitizeDetailByDefault() {
        JsonProcessingExceptionMapper mapper = new JsonProcessingExceptionMapper(registry, new DetailSanitizer(false));
        StreamReadException exception = new StreamReadException("Unexpected end-of-input");

        Response response = mapper.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity())
                .isInstanceOf(HttpProblem.class)
                .hasFieldOrPropertyWithValue("detail", DetailSanitizer.SANITIZED_DETAIL);
    }
}
