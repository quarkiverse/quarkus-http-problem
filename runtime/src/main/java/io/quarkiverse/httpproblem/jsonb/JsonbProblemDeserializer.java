package io.quarkiverse.httpproblem.jsonb;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;

import jakarta.json.bind.JsonbException;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.stream.JsonParser;

import io.quarkiverse.httpproblem.HttpProblem;

/**
 * Low level JsonB serializer for Problem type.
 */
public final class JsonbProblemDeserializer implements JsonbDeserializer<HttpProblem> {

    @Override
    public HttpProblem deserialize(JsonParser jsonParser, DeserializationContext deserializationContext, Type type) {
        Map<String, Object> rawDeserializedProblem = (Map<String, Object>) deserializationContext.deserialize(Map.class,
                jsonParser);

        HttpProblem.Builder builder = HttpProblem.builder();
        for (String fieldName : rawDeserializedProblem.keySet()) {
            Object child = rawDeserializedProblem.get(fieldName);
            switch (fieldName) {
                case "type" -> builder.withType(uriOrThrow(child, fieldName));
                case "status" -> builder.withStatus(intOrThrow(child, fieldName));
                case "title" -> builder.withTitle((String) child);
                case "detail" -> builder.withDetail((String) child);
                case "instance" -> builder.withInstance(uriOrThrow(child, fieldName));
                default -> builder.with(fieldName, child);
            }
        }
        return builder.build();
    }

    private URI uriOrThrow(Object child, String fieldName) {
        if (child == null) {
            return null;
        }

        try {
            return URI.create((String) child);
        } catch (IllegalArgumentException e) {
            throw new JsonbException("'%s' field must be a valid URI".formatted(fieldName));
        }
    }

    private int intOrThrow(Object child, String fieldName) {
        try {
            return ((BigDecimal) child).intValue();
        } catch (ClassCastException e) {
            throw new JsonbException("'%s' field must be a valid http status code".formatted(fieldName));
        }
    }
}
