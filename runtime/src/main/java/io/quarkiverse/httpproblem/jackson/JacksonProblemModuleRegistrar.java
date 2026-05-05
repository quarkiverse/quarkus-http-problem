package io.quarkiverse.httpproblem.jackson;

import jakarta.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkus.jackson.ObjectMapperCustomizer;

@Singleton
public final class JacksonProblemModuleRegistrar implements ObjectMapperCustomizer {

    @Override
    public void customize(ObjectMapper mapper) {
        SimpleModule module = new SimpleModule("RFC7807 problem")
                .addSerializer(HttpProblem.class, new JacksonProblemSerializer())
                .addDeserializer(HttpProblem.class, new JacksonProblemDeserializer(mapper));
        mapper.registerModule(module);
    }

}
