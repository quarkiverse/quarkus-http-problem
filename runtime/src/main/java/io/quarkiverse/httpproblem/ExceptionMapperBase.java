package io.quarkiverse.httpproblem;

import java.util.List;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.slf4j.LoggerFactory;

import io.quarkiverse.httpproblem.postprocessing.PostProcessorsRegistry;
import io.quarkiverse.httpproblem.postprocessing.ProblemContext;
import io.quarkiverse.httpproblem.postprocessing.ProblemDefaultsProvider;
import io.quarkiverse.httpproblem.postprocessing.ProblemLogger;
import io.quarkiverse.httpproblem.postprocessing.ProblemPostProcessor;

/**
 * Base class for all ExceptionMappers in this extension, takes care of mapping Exceptions to Problems, triggering
 * post-processing stage, and creating final JaxRS Response.
 */
public abstract class ExceptionMapperBase<E extends Throwable> implements ExceptionMapper<E> {

    private static final List<ProblemPostProcessor> DEFAULT_POST_PROCESSORS = List.of(
            new ProblemLogger(LoggerFactory.getLogger("http-problem")),
            new ProblemDefaultsProvider());

    public static final PostProcessorsRegistry postProcessorsRegistry = new PostProcessorsRegistry(
            DEFAULT_POST_PROCESSORS::stream);

    private final PostProcessorsRegistry postProcessors;

    protected ExceptionMapperBase() {
        this(postProcessorsRegistry);
    }

    protected ExceptionMapperBase(PostProcessorsRegistry postProcessors) {
        this.postProcessors = postProcessors;
    }

    @Context
    UriInfo uriInfo;

    @Override
    public final Response toResponse(E exception) {
        HttpProblem problem = toProblem(exception);
        ProblemContext context = ProblemContext.of(exception, uriInfo);
        HttpProblem finalProblem = postProcessors.applyPostProcessing(problem, context);
        return finalProblem.toResponse();
    }

    protected abstract HttpProblem toProblem(E exception);

}
