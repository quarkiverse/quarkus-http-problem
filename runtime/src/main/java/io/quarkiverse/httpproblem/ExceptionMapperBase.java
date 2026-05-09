package io.quarkiverse.httpproblem;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;

import io.quarkiverse.httpproblem.postprocessing.PostProcessorsRegistry;
import io.quarkiverse.httpproblem.postprocessing.ProblemContext;

/**
 * Base class for all ExceptionMappers in this extension, takes care of mapping Exceptions to Problems, triggering
 * post-processing stage, and creating final JaxRS Response.
 */
public abstract class ExceptionMapperBase<E extends Throwable> implements ExceptionMapper<E> {

    public static final PostProcessorsRegistry postProcessorsRegistry = new PostProcessorsRegistry();

    @Context
    UriInfo uriInfo;

    @Override
    public final Response toResponse(E exception) {
        HttpProblem problem = toProblem(exception);
        ProblemContext context = ProblemContext.of(exception, uriInfo);
        HttpProblem finalProblem = postProcessorsRegistry.applyPostProcessing(problem, context);
        return finalProblem.toResponse();
    }

    protected abstract HttpProblem toProblem(E exception);

}
