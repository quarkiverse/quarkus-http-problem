package io.quarkiverse.httpproblem.jackson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkiverse.httpproblem.HttpProblemMother;

class JacksonProblemDeserializerTest {

    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mapper.registerModule(
                new SimpleModule("RFC7807 problem")
                        .addSerializer(HttpProblem.class, new JacksonProblemSerializer())
                        .addDeserializer(HttpProblem.class, new JacksonProblemDeserializer(mapper)));

    }

    @Test
    void shouldDeserializeStandardFields() throws IOException {
        HttpProblem deserialized = deserialise(HttpProblemMother.SERIALIZED_BAD_REQUEST_PROBLEM);

        assertThat(deserialized)
                .usingRecursiveComparison()
                .isEqualTo(HttpProblemMother.badRequestProblem());
    }

    @Test
    void shouldDeserializeAllKnownTypesOfCustomFields() throws IOException {
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
                .containsEntry("custom_numeric_field", 456)
                .containsEntry("custom_text_field", "too short")
                .containsEntry("custom_array", List.of("1", "b", "c"))
                .containsEntry("custom_object", Map.of("elo", "too short"));
    }

    /**
     * Null fields are allowed by RFC, and some api servers configured with JsonInclude.Include.ALWAYS can return
     * explicit null fields.
     */
    @Test
    void shouldGracefullyHandleNullFields() throws IOException {
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
                .isInstanceOf(JsonMappingException.class)
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
                .isInstanceOf(JsonMappingException.class)
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
                .isInstanceOf(JsonMappingException.class)
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
                .isInstanceOf(JsonParseException.class)
                .hasMessageStartingWith(
                        "Unexpected character ('}' (code 125)): was expecting double-quote to start field name");
    }

    private HttpProblem deserialise(String json) throws IOException {
        return mapper.readValue(json, HttpProblem.class);
    }

}
