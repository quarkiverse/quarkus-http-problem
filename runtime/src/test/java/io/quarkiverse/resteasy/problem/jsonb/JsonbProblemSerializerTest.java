package io.quarkiverse.resteasy.problem.jsonb;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import jakarta.json.Json;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;

import org.eclipse.parsson.JsonProviderImpl;
import org.eclipse.yasson.internal.JsonbContext;
import org.eclipse.yasson.internal.SerializationContextImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.quarkiverse.resteasy.problem.HttpProblem;
import io.quarkiverse.resteasy.problem.HttpProblemMother;

class JsonbProblemSerializerTest {

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    JsonGenerator jsonGenerator = Json.createGenerator(outputStream);
    SerializationContext context = new SerializationContextImpl(new JsonbContext(new JsonbConfig(), new JsonProviderImpl()));

    JsonbProblemSerializer serializer = new JsonbProblemSerializer();

    @Test
    @DisplayName("Should serialize all provided fields")
    void shouldSerializeAllFields() {
        HttpProblem problem = HttpProblemMother.complexProblem().build();

        serializer.serialize(problem, jsonGenerator, context);

        assertThat(serializedProblem())
                .isEqualTo(HttpProblemMother.SERIALIZED_COMPLEX_PROBLEM);
    }

    @Test
    @DisplayName("Should serialize only not null fields")
    void shouldSerializeOnlyNotNullFields() {
        HttpProblem problem = HttpProblemMother.badRequestProblem();

        serializer.serialize(problem, jsonGenerator, context);

        assertThat(serializedProblem())
                .isEqualTo(HttpProblemMother.SERIALIZED_BAD_REQUEST_PROBLEM);
    }

    @Test
    @DisplayName("Should serialize instance field preserving URI encoding")
    void shouldSerializeInstanceFieldPreservingEncoding() {
        HttpProblem problem = HttpProblem.builder()
                .withStatus(NOT_FOUND)
                .withInstance(URI.create("%2Fnon%7Cexisting%7Bpath+%2Fwith%7Bunwise%5Ccharacters%3E%23"))
                .build();

        serializer.serialize(problem, jsonGenerator, null);

        assertThat(serializedProblem()).contains("""
                "instance":"%2Fnon%7Cexisting%7Bpath+%2Fwith%7Bunwise%5Ccharacters%3E%23"}""");
    }

    private String serializedProblem() {
        jsonGenerator.close();
        return outputStream.toString(StandardCharsets.UTF_8);
    }

}
