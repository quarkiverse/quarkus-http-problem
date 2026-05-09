package io.quarkiverse.httpproblem.postprocessing;

import java.util.Set;

import jakarta.enterprise.inject.spi.CDI;

import io.quarkiverse.httpproblem.ExceptionMapperBase;
import io.quarkiverse.httpproblem.validation.ConstraintViolationExceptionMapper;
import io.quarkus.runtime.annotations.Recorder;

/**
 * Quarkus Recorder that applies configuration in the runtime.
 */
@Recorder
public class ProblemRecorder {

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

    public void configureConstraintViolationMapper(int status, String title) {
        ConstraintViolationExceptionMapper.configure(status, title);
    }
}
