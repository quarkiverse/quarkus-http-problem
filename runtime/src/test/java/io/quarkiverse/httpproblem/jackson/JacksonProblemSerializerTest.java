package io.quarkiverse.httpproblem.jackson;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkiverse.httpproblem.HttpProblemMother;

class JacksonProblemSerializerTest {

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    JsonGenerator jsonGenerator;

    JacksonProblemSerializer serializer = new JacksonProblemSerializer();

    @BeforeEach
    void setUp() throws IOException {
        jsonGenerator = new JsonFactory()
                .createGenerator(outputStream, JsonEncoding.UTF8);
    }

    @Test
    @DisplayName("Should serialize all provided fields")
    void shouldSerializeAllFields() throws IOException {
        HttpProblem problem = HttpProblemMother.complexProblem().build();

        serializer.serialize(problem, jsonGenerator, null);

        assertThat(serializedProblem())
                .isEqualTo(HttpProblemMother.SERIALIZED_COMPLEX_PROBLEM);
    }

    @Test
    @DisplayName("Should serialize only not null fields")
    void shouldSerializeOnlyNotNullFields() throws IOException {
        HttpProblem problem = HttpProblemMother.badRequestProblem();

        serializer.serialize(problem, jsonGenerator, null);

        assertThat(serializedProblem())
                .isEqualTo(HttpProblemMother.SERIALIZED_BAD_REQUEST_PROBLEM);
    }

    @Test
    @DisplayName("Should decode uri for instance field")
    void shouldDecodeUriForInstanceField() throws IOException {
        HttpProblem problem = HttpProblem.builder()
                .withStatus(NOT_FOUND)
                .withInstance(URI.create("%2Fnon%7Cexisting%7Bpath+%2Fwith%7Bunwise%5Ccharacters%3E%23"))
                .build();

        serializer.serialize(problem, jsonGenerator, null);

        assertThat(serializedProblem()).contains("""
                "instance":"/non|existing{path /with{unwise\\\\characters>#"}""");
    }

    private String serializedProblem() throws IOException {
        jsonGenerator.close();
        return outputStream.toString(StandardCharsets.UTF_8);
    }

}
