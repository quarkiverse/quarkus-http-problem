package io.quarkiverse.httpproblem.jackson;

import java.net.URI;
import java.util.Map;

import io.quarkiverse.httpproblem.HttpProblem;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Low level Jackson deserializer for HttpProblem type.
 */
public final class JacksonProblemDeserializer extends StdDeserializer<HttpProblem> {

    public static final TypeReference<Map<String, Object>> VALUE_TYPE_REF = new TypeReference<>() {
    };

    public JacksonProblemDeserializer() {
        super(HttpProblem.class);
    }

    @Override
    public HttpProblem deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws JacksonException {
        Map<String, Object> rawDeserializedProblem = deserializationContext.readValue(jsonParser, VALUE_TYPE_REF);

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

    private URI uriOrThrow(Object child, String fieldName, JsonParser jsonParser) throws DatabindException {
        if (child == null) {
            return null;
        }

        try {
            return URI.create((String) child);
        } catch (IllegalArgumentException e) {
            throw DatabindException.from(jsonParser, "'%s' field must be a valid URI".formatted(fieldName));
        }
    }

    private int intOrThrow(Object child, String fieldName, JsonParser jsonParser) throws DatabindException {
        try {
            return (int) child;
        } catch (ClassCastException e) {
            throw DatabindException.from(jsonParser, "'%s' field must be a valid http status code".formatted(fieldName));
        }
    }

}
