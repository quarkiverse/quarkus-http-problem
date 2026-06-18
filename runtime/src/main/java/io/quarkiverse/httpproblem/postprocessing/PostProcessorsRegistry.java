package io.quarkiverse.httpproblem.postprocessing;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.quarkiverse.httpproblem.HttpProblem;

/**
 * Container for prioritised list of Problem post-processors. This class is thread-safe.
 */
public final class PostProcessorsRegistry {

    private final Supplier<Stream<ProblemPostProcessor>> inheritedProcessors;
    private final List<ProblemPostProcessor> processors = new CopyOnWriteArrayList<>();

    public PostProcessorsRegistry() {
        this(Stream::empty);
    }

    public PostProcessorsRegistry(PostProcessorsRegistry parent) {
        this(parent::stream);
    }

    public PostProcessorsRegistry(Supplier<Stream<ProblemPostProcessor>> inheritedProcessors) {
        this.inheritedProcessors = inheritedProcessors;
    }

    /**
     * Removes all registered local post-processors. Used mainly for Quarkus dev mode (live-reload) tests where there's a
     * need to reset registered processors because of config change.
     */
    public synchronized void reset() {
        processors.clear();
    }

    public synchronized void register(ProblemPostProcessor processor) {
        processors.add(processor);
        processors.sort(ProblemPostProcessor.DEFAULT_ORDERING);
    }

    /**
     * Applies all registered post-processors on a given Problem, in prioritized order.
     *
     * @param problem Original Problem produced by Exception Mapper
     * @param context Additional info on cause (original exception caught by ExceptionMapper) and HTTP request
     * @return Enhanced version of original Problem
     */
    public HttpProblem applyPostProcessing(HttpProblem problem, ProblemContext context) {
        HttpProblem finalProblem = problem;
        Iterable<ProblemPostProcessor> processors = stream()::iterator;
        for (ProblemPostProcessor processor : processors) {
            finalProblem = processor.apply(finalProblem, context);
        }
        return finalProblem;
    }

    public Stream<ProblemPostProcessor> stream() {
        return Stream.concat(inheritedProcessors.get(), processors.stream());
    }

}
