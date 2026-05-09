package io.quarkiverse.httpproblem.jackson;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import io.quarkiverse.httpproblem.HttpProblem;

/**
 * Low level Jackson deserializer for HttpProblem type.
 */
public final class JacksonProblemDeserializer extends StdDeserializer<HttpProblem> {

    private final ObjectMapper mapper;

    public JacksonProblemDeserializer(ObjectMapper mapper) {
        super((Class<HttpProblem>) null);
        this.mapper = mapper;
    }

    @Override
    public HttpProblem deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        Map<String, Object> rawDeserializedProblem = mapper.readValue(jsonParser, new TypeReference<>() {
        });

        HttpProblem.Builder builder = HttpProblem.builder();
        for (String fieldName : rawDeserializedProblem.keySet()) {
            Object child = rawDeserializedProblem.get(fieldName);
            switch (fieldName) {
                case "type" -> builder.withType(uriOrThrow(child, fieldName, jsonParser));
                case "status" -> builder.withStatus(intOrThrow(child, fieldName, jsonParser));
                case "title" -> builder.withTitle((String) child);
                case "detail" -> builder.withDetail((String) child);
                case "instance" -> builder.withInstance(uriOrThrow(child, fieldName, jsonParser));
                default -> builder.with(fieldName, child);
            }
        }
        return builder.build();
    }

    private URI uriOrThrow(Object child, String fieldName, JsonParser jsonParser) throws JsonMappingException {
        if (child == null) {
            return null;
        }

        try {
            return URI.create((String) child);
        } catch (IllegalArgumentException e) {
            throw JsonMappingException.from(jsonParser, "'%s' field must be a valid URI".formatted(fieldName));
        }
    }

    private int intOrThrow(Object child, String fieldName, JsonParser jsonParser) throws JsonMappingException {
        try {
            return (int) child;
        } catch (ClassCastException e) {
            throw JsonMappingException.from(jsonParser, "'%s' field must be a valid http status code".formatted(fieldName));
        }
    }

}
