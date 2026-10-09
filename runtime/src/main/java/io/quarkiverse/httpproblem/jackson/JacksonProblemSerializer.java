package io.quarkiverse.httpproblem.jackson;

import java.util.Map;

import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkiverse.httpproblem.InstanceUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

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
    public void serialize(final HttpProblem problem, final JsonGenerator json, final SerializationContext serializers)
            throws JacksonException {
        json.writeStartObject();
        if (problem.getType() != null) {
            json.writeStringProperty("type", problem.getType().toASCIIString());
        }
        json.writeNumberProperty("status", problem.getStatusCode());

        if (problem.getTitle() != null) {
            json.writeStringProperty("title", problem.getTitle());
        }
        if (problem.getDetail() != null) {
            json.writeStringProperty("detail", problem.getDetail());
        }
        if (problem.getInstance() != null) {
            json.writeStringProperty("instance", InstanceUtils.instanceToPath(problem.getInstance()));
        }

        for (Map.Entry<String, Object> entry : problem.getParameters().entrySet()) {
            json.writePOJOProperty(entry.getKey(), entry.getValue());
        }

        json.writeEndObject();
    }
}
