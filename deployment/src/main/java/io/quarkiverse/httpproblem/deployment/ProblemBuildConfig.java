package io.quarkiverse.httpproblem.deployment;

import java.util.Set;

import io.quarkiverse.httpproblem.ProblemRuntimeConfig;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "quarkus.resteasy.problem")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface ProblemBuildConfig {

    /**
     * MDC properties that should be included in problem responses.
     */
    @WithDefault("uuid")
    Set<String> includeMdcProperties();

    /**
     * OpenApi related configuration
     */
    @WithName("openapi")
    OpenApiConfig openapi();

    interface OpenApiConfig {

        /**
         * Which schema should be used by default for problem responses
         */
        @WithName("default-schema")
        @WithDefault("HttpProblem")
        String defaultSchema();

        /**
         * Which schema should be used by default for validation problem responses
         */
        @WithName("validation-problem-schema")
        @WithDefault("HttpValidationProblem")
        String validationProblemSchema();
    }

    /**
     * Config for OpenApi schema of HttpValidationProblem
     *
     * @implNote This duplicates ProblemRuntimeConfig as runtime config cannot be used in build-time, and constraint
     *           violation configuration is needed to enhance OpenApi documentation. The best would be to move everything to
     *           build-time configuration, but that would be a breaking change. To be removed in 4.0.0.
     */
    @WithName("constraint-violation")
    ProblemRuntimeConfig.ConstraintViolationMapperConfig constraintViolation();
}
