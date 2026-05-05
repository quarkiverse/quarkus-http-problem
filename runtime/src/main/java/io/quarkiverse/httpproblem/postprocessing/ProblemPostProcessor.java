package io.quarkiverse.httpproblem.postprocessing;

import java.util.Comparator;

import io.quarkiverse.httpproblem.HttpProblem;

/**
 * Post-processors use, change or enhance HttpProblem created by ExceptionMappers via 'apply' method, before they get
 * passed on to serializers.
 */
public interface ProblemPostProcessor {

    Comparator<ProblemPostProcessor> DEFAULT_ORDERING = Comparator.comparingInt(ProblemPostProcessor::priority).reversed();

    /**
     * Interceptor method for HttpProblems. In case problem should be changed or enhanced, one can use
     * 'HttpProblem.builder(httpProblem)'.
     * <p>
     * Implementations should be thread-safe.
     *
     * @param problem Original HttpProblem, possibly processed by other processors with higher priority.
     * @param context Additional, internal metadata not included in HttpProblem
     * @return Can be original HttpProblem (for peek-type processors), changed copy or completely new HttpProblem (for map-type
     *         processors.
     */
    HttpProblem apply(HttpProblem problem, ProblemContext context);

    /**
     * Defines order in which processors are triggered. Bigger value means precedence before processors
     * with lower priority.
     * When two processors have the same priority then order of invocation is undefined.
     */
    default int priority() {
        return Integer.MIN_VALUE;
    }

}
