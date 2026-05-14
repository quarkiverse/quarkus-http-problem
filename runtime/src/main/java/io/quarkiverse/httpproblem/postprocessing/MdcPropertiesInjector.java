package io.quarkiverse.httpproblem.postprocessing;

import java.util.Collections;
import java.util.Set;

import org.slf4j.MDC;

import io.quarkiverse.httpproblem.HttpProblem;

/**
 * Injects existing MDC properties listed in the configuration into final response. Missing MDC values and properties already
 * defined in Problem instance are skipped.
 */
public final class MdcPropertiesInjector implements ProblemPostProcessor {

    private final Set<String> properties;

    public MdcPropertiesInjector(Set<String> properties) {
        this.properties = Collections.unmodifiableSet(properties);
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public HttpProblem apply(HttpProblem problem, ProblemContext context) {
        if (properties.isEmpty()) {
            return problem;
        }

        HttpProblem.Builder builder = HttpProblem.builder(problem);

        properties.stream()
                .filter(propertyName -> !problem.getParameters().containsKey(propertyName))
                .filter(propertyName -> MDC.get(propertyName) != null)
                .forEach(propertyName -> builder.with(propertyName, MDC.get(propertyName)));

        return builder.build();
    }

}
