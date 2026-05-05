package io.quarkiverse.httpproblem.postprocessing;

import java.util.Set;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;

import io.quarkiverse.httpproblem.ExceptionMapperBase;
import io.quarkiverse.httpproblem.ProblemRuntimeConfig;
import io.quarkiverse.httpproblem.validation.ConstraintViolationExceptionMapper;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

/**
 * Quarkus Recorder that applies configuration in the runtime.
 */
@Recorder
public class ProblemRecorder {

    private final RuntimeValue<ProblemRuntimeConfig> runtimeConfig;

    @Inject
    public ProblemRecorder(RuntimeValue<ProblemRuntimeConfig> runtimeConfig) {
        this.runtimeConfig = runtimeConfig;
    }

    public void reset() {
        ExceptionMapperBase.postProcessorsRegistry.reset();
    }

    public void configureMdc(Set<String> includeMdcProperties) {
        if (!includeMdcProperties.isEmpty()) {
            ExceptionMapperBase.postProcessorsRegistry.register(new MdcPropertiesInjector(includeMdcProperties));
        }
    }

    public void registerCustomPostProcessors() {
        CDI.current().select(ProblemPostProcessor.class)
                .forEach(ExceptionMapperBase.postProcessorsRegistry::register);
    }

    public void applyRuntimeConfig() {
        ConstraintViolationExceptionMapper.configure(runtimeConfig.getValue().constraintViolation());
    }
}
