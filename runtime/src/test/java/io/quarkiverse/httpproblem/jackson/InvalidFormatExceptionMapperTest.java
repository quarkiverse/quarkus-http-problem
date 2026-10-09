package io.quarkiverse.httpproblem.jackson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import io.quarkiverse.httpproblem.DetailSanitizer;
import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkiverse.httpproblem.postprocessing.PostProcessorsRegistry;
import io.quarkiverse.httpproblem.postprocessing.ProblemDefaultsProvider;
import io.quarkiverse.httpproblem.postprocessing.ProblemLogger;
import io.quarkiverse.httpproblem.postprocessing.ProblemLoggingConfig;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.exc.InvalidFormatException;

class InvalidFormatExceptionMapperTest {

    PostProcessorsRegistry registry = new PostProcessorsRegistry(
            List.of(new ProblemLogger(ProblemLoggingConfig.defaults()), new ProblemDefaultsProvider()));
    InvalidFormatExceptionMapper mapper = new InvalidFormatExceptionMapper(registry, new DetailSanitizer(true));

    @Test
    void shouldProduceHttp400WithFieldInfo() {
        InvalidFormatException exception = buildExceptionWithPath(
                new JacksonException.Reference(this, "customFieldName"));

        Response response = mapper.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getMediaType()).isEqualTo(HttpProblem.MEDIA_TYPE);
        assertThat(response.getEntity())
                .isInstanceOf(HttpProblem.class)
                .hasFieldOrPropertyWithValue("detail", "Invalid format of the field")
                .hasFieldOrPropertyWithValue("parameters.field", "customFieldName");
    }

    @Test
    void invalidFormatInsideCollectionShouldShowValidPath() {
        InvalidFormatException exception = buildExceptionWithPath(
                new JacksonException.Reference(this, "collection"),
                new JacksonException.Reference(this, 2),
                new JacksonException.Reference(this, "customFieldName"));

        Response response = mapper.toResponse(exception);

        assertThat(response.getEntity())
                .isInstanceOf(HttpProblem.class)
                .hasFieldOrPropertyWithValue("parameters.field", "collection[2].customFieldName");
    }

    @Test
    void emptyPathShouldNotCrash() {
        InvalidFormatException exception = buildExceptionWithPath();

        Response response = mapper.toResponse(exception);

        assertThat(response.getEntity())
                .isInstanceOf(HttpProblem.class)
                .hasFieldOrPropertyWithValue("parameters.field", "?");
    }

    @Test
    void shouldSanitizeDetailByDefault() {
        InvalidFormatExceptionMapper sanitizedMapper = new InvalidFormatExceptionMapper(registry, new DetailSanitizer(false));
        InvalidFormatException exception = buildExceptionWithPath(
                new JacksonException.Reference(this, "customFieldName"));

        Response response = sanitizedMapper.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity())
                .isInstanceOf(HttpProblem.class)
                .hasFieldOrPropertyWithValue("detail", DetailSanitizer.SANITIZED_DETAIL)
                .hasFieldOrPropertyWithValue("parameters.field", "customFieldName");
    }

    @Test
    void shouldPreserveDetailWhenIncludeDetails() {
        InvalidFormatException exception = buildExceptionWithPath(
                new JacksonException.Reference(this, "customFieldName"));

        Response response = mapper.toResponse(exception);

        assertThat(response.getEntity())
                .isInstanceOf(HttpProblem.class)
                .hasFieldOrPropertyWithValue("detail", "Invalid format of the field");
    }

    private InvalidFormatException buildExceptionWithPath(JacksonException.Reference... pathSegments) {
        InvalidFormatException exception = new InvalidFormatException(mock(JsonParser.class),
                "Invalid format of the field", this, this.getClass());

        for (int i = pathSegments.length - 1; i >= 0; --i) {
            exception.prependPath(pathSegments[i]);
        }
        return exception;
    }

}
