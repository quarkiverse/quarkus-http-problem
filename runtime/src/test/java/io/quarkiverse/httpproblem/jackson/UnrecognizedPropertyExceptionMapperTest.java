package io.quarkiverse.httpproblem.jackson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import io.quarkiverse.httpproblem.HttpProblem;

class UnrecognizedPropertyExceptionMapperTest {

    UnrecognizedPropertyExceptionMapper mapper = new UnrecognizedPropertyExceptionMapper();

    @Test
    void shouldProduceHttp400() {
        UnrecognizedPropertyException exception = UnrecognizedPropertyException.from(mock(JsonParser.class),
                this.getClass(), "unknown_field", new ArrayList<>());

        Response response = mapper.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getMediaType()).isEqualTo(HttpProblem.MEDIA_TYPE);
        assertThat(response.getEntity())
                .isInstanceOf(HttpProblem.class)
                .hasFieldOrPropertyWithValue("detail",
                        "Unrecognized field \"unknown_field\", not marked as ignorable");
    }

}
