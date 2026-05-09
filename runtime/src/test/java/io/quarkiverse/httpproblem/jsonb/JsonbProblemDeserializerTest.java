package io.quarkiverse.httpproblem.jsonb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.JsonbException;

import org.junit.jupiter.api.Test;

import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkiverse.httpproblem.HttpProblemMother;

class JsonbProblemDeserializerTest {

    Jsonb mapper = JsonbBuilder.newBuilder()
            .withConfig(new JsonbConfig()
                    .withDeserializers(new JsonbProblemDeserializer()))
            .build();

    @Test
    void shouldDeserializeStandardFields() {
        HttpProblem deserialized = deserialise(HttpProblemMother.SERIALIZED_BAD_REQUEST_PROBLEM);

        assertThat(deserialized)
                .usingRecursiveComparison()
                .isEqualTo(HttpProblemMother.badRequestProblem());
    }

    @Test
    void shouldDeserializeAllKnownTypesOfCustomFields() {
        String problem = """
                    {
                        "type": "http://tietoevry.com/problem",
                        "status": 401,
                        "title": "Something wrong in the dirt",
                        "detail": "Deep down wrongness, zażółć gęślą jaźń for Håkensth",
                        "instance": "/api/v1/endpoint",
                        "custom_numeric_field": 456,
                        "custom_text_field": "too short",
                        "custom_array": ["1", "b", "c"],
                        "custom_object": {
                            "elo" : "too short"
                        }
                    }
                """;

        HttpProblem deserialized = deserialise(problem);

        assertThat(deserialized.getType()).isEqualTo(URI.create("http://tietoevry.com/problem"));
        assertThat(deserialized.getStatusCode()).isEqualTo(401);
        assertThat(deserialized.getTitle()).isEqualTo("Something wrong in the dirt");
        assertThat(deserialized.getDetail()).isEqualTo("Deep down wrongness, zażółć gęślą jaźń for Håkensth");
        assertThat(deserialized.getInstance()).isEqualTo(URI.create("/api/v1/endpoint"));
        assertThat(deserialized.getParameters())
                .containsEntry("custom_numeric_field", new BigDecimal(456))
                .containsEntry("custom_text_field", "too short")
                .containsEntry("custom_array", List.of("1", "b", "c"))
                .containsEntry("custom_object", Map.of("elo", "too short"));
    }

    @Test
    void shouldGracefullyHandleNullFields() {
        String problemWithNullFields = """
                {
                    "detail": null,
                    "title": null,
                    "type": null,
                    "instance": null
                }
                """;

        HttpProblem deserialized = deserialise(problemWithNullFields);

        assertThat(deserialized.getType()).isNull();
        assertThat(deserialized.getTitle()).isNull();
        assertThat(deserialized.getDetail()).isNull();
        assertThat(deserialized.getInstance()).isNull();
    }

    @Test
    void shouldThrowWhenTypeIsNotValidUri() {
        String problem = """
                    {
                        "type": "this is not a valid uri",
                        "status": 401,
                        "title": "Something wrong in the dirt"
                    }
                """;

        assertThatThrownBy(() -> deserialise(problem))
                .isInstanceOf(JsonbException.class)
                .hasMessageStartingWith("'type' field must be a valid URI");
    }

    @Test
    void shouldThrowWhenInstanceIsNotValidUri() {
        String problem = """
                    {
                        "instance": "this is not a valid uri",
                        "status": 401,
                        "title": "Something wrong in the dirt"
                    }
                """;

        assertThatThrownBy(() -> deserialise(problem))
                .isInstanceOf(JsonbException.class)
                .hasMessageStartingWith("'instance' field must be a valid URI");
    }

    @Test
    void shouldThrowWhenStatusIsNotInteger() {
        String problem = """
                    {
                        "status": "_401"
                    }
                """;

        assertThatThrownBy(() -> deserialise(problem))
                .isInstanceOf(JsonbException.class)
                .hasMessageStartingWith("'status' field must be a valid http status code");
    }

    @Test
    void shouldThrowWhenJsonIsMalformed() {
        String problem = """
                    {
                        "status": 400,
                    }
                """;

        assertThatThrownBy(() -> deserialise(problem))
                .isInstanceOf(JsonbException.class)
                .hasMessageStartingWith(
                        "Internal error: Invalid token=CURLYCLOSE at (line no=3, column no=5, offset=33). Expected tokens are: [STRING]");
    }

    private HttpProblem deserialise(String json) {
        return mapper.fromJson(json, HttpProblem.class);
    }

}
