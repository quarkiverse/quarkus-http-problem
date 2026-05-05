package io.quarkiverse.httpproblem;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * @deprecated v4.0.0 all configuration will be evaluated in build time, Use {@link ProblemBuildConfig} instead.
 */
@ConfigMapping(prefix = "quarkus.resteasy.problem")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@Deprecated(since = "3.20.0", forRemoval = true)
public interface ProblemRuntimeConfig {

    /**
     * Config for ConstraintViolationException mapper
     */
    @WithName("constraint-violation")
    ConstraintViolationMapperConfig constraintViolation();

    interface ConstraintViolationMapperConfig {
        static ConstraintViolationMapperConfig defaults() {
            return new ConstraintViolationMapperConfig() {
                @Override
                public int status() {
                    return 400;
                }

                @Override
                public String title() {
                    return "Bad Request";
                }

                @Override
                public String description() {
                    return "Bad request: server would not process the request due to something the server considered to be a client error";
                }
            };
        }

        /**
         * Response status code when ConstraintViolationException is thrown.
         */
        @WithDefault("400")
        int status();

        /**
         * Response title when ConstraintViolationException is thrown.
         */
        @WithDefault("Bad Request")
        String title();

        /**
         * OpenApi description for ConstraintViolationExceptions.
         */
        @WithDefault("Bad request: server would not process the request due to something the server considered to be a client error")
        String description();
    }
}
