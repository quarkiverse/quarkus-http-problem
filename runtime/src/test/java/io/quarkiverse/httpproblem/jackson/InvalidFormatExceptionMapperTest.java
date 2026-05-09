package io.quarkiverse.httpproblem.jackson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import io.quarkiverse.httpproblem.HttpProblem;

class InvalidFormatExceptionMapperTest {

    InvalidFormatExceptionMapper mapper = new InvalidFormatExceptionMapper();

    @Test
    void shouldProduceHttp400WithFieldInfo() {
        InvalidFormatException exception = buildExceptionWithPath(
                new JsonMappingException.Reference(this, "customFieldName"));

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
                new JsonMappingException.Reference(this, "collection"),
                new JsonMappingException.Reference(this, 2),
                new JsonMappingException.Reference(this, "customFieldName"));

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

    private InvalidFormatException buildExceptionWithPath(JsonMappingException.Reference... pathSegments) {
        InvalidFormatException exception = new InvalidFormatException(mock(JsonParser.class),
                "Invalid format of the field", this, this.getClass());

        for (int i = pathSegments.length - 1; i >= 0; --i) {
            exception.prependPath(pathSegments[i]);
        }
        return exception;
    }

}
