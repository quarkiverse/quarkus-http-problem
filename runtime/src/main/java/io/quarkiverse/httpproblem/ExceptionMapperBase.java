package io.quarkiverse.httpproblem;

import java.util.Objects;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.jboss.logging.Logger;

import io.quarkiverse.httpproblem.postprocessing.PostProcessorsRegistry;
import io.quarkiverse.httpproblem.postprocessing.ProblemContext;

public abstract class ExceptionMapperBase<E extends Throwable> implements ExceptionMapper<E> {

    private static final Logger LOG = Logger.getLogger(ExceptionMapperBase.class);

    private PostProcessorsRegistry postProcessorsRegistry;

    @Context
    UriInfo uriInfo;

    protected ExceptionMapperBase() {
    }

    protected ExceptionMapperBase(PostProcessorsRegistry postProcessorsRegistry) {
        this.postProcessorsRegistry = postProcessorsRegistry;
    }

    @Override
    public final Response toResponse(E exception) {
        Objects.requireNonNull(postProcessorsRegistry,
                "PostProcessorsRegistry not injected — mapper must be instantiated via CDI");

        HttpProblem problem;
        try {
            problem = toProblem(exception);
        } catch (Exception e) {
            LOG.errorf(e, "toProblem() failed for %s, falling back to generic 500",
                    exception.getClass().getName());
            problem = HttpProblem.valueOf(Response.Status.INTERNAL_SERVER_ERROR);
        }

        if (problem == null) {
            LOG.errorf("toProblem() returned null for %s, falling back to generic 500",
                    exception.getClass().getName());
            problem = HttpProblem.valueOf(Response.Status.INTERNAL_SERVER_ERROR);
        }

        ProblemContext context = ProblemContext.of(exception, uriInfo);
        HttpProblem finalProblem = postProcessorsRegistry.applyPostProcessing(problem, context);
        return finalProblem.toResponse();
    }

    protected abstract HttpProblem toProblem(E exception);

}
