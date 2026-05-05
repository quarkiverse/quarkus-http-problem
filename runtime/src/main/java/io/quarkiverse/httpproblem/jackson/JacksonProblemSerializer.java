package io.quarkiverse.httpproblem.jackson;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkiverse.httpproblem.InstanceUtils;

/**
 * Low level Jackson serializer for HttpProblem type.
 */
public final class JacksonProblemSerializer extends StdSerializer<HttpProblem> {

    public JacksonProblemSerializer() {
        this(null);
    }

    public JacksonProblemSerializer(Class<HttpProblem> t) {
        super(t);
    }

    @Override
    public void serialize(final HttpProblem problem, final JsonGenerator json, final SerializerProvider serializers)
            throws IOException {
        json.writeStartObject();
        if (problem.getType() != null) {
            json.writeStringField("type", problem.getType().toASCIIString());
        }
        json.writeNumberField("status", problem.getStatusCode());

        if (problem.getTitle() != null) {
            json.writeStringField("title", problem.getTitle());
        }
        if (problem.getDetail() != null) {
            json.writeStringField("detail", problem.getDetail());
        }
        if (problem.getInstance() != null) {
            json.writeStringField("instance", InstanceUtils.instanceToPath(problem.getInstance()));
        }

        for (Map.Entry<String, Object> entry : problem.getParameters().entrySet()) {
            json.writeObjectField(entry.getKey(), entry.getValue());
        }

        json.writeEndObject();
    }
}
