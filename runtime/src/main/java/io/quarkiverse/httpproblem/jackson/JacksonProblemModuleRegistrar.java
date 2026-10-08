package io.quarkiverse.httpproblem.jackson;

import jakarta.inject.Singleton;

import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkus.jackson.JsonMapperBuilderCustomizer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

@Singleton
public final class JacksonProblemModuleRegistrar implements JsonMapperBuilderCustomizer {

    @Override
    public void customize(JsonMapper.Builder builder) {
        SimpleModule module = new SimpleModule("RFC7807 problem")
                .addSerializer(HttpProblem.class, new JacksonProblemSerializer())
                .addDeserializer(HttpProblem.class, new JacksonProblemDeserializer());
        builder.addModule(module);
    }

}
