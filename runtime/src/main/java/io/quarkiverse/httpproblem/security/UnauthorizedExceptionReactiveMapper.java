package io.quarkiverse.httpproblem.security;

import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import io.quarkiverse.httpproblem.ExceptionMapperBase;
import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkiverse.httpproblem.postprocessing.ProblemContext;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

/**
 * Mapper overriding default Quarkus exception mapper to make all error responses compliant with RFC7807.
 *
 * @see io.quarkus.resteasy.runtime.UnauthorizedExceptionMapper
 */
public final class UnauthorizedExceptionReactiveMapper {

    @ServerExceptionMapper(value = UnauthorizedException.class, priority = Priorities.USER - 1)
    @APIResponse(responseCode = "401", description = "Unauthorized: request was not successful because it lacks valid authentication credentials for the requested resource")
    public Uni<Response> handle(RoutingContext routingContext, UnauthorizedException exception) {
        return HttpUnauthorizedUtils.toProblem(routingContext, exception)
                .map(problem -> {
                    ProblemContext context = ProblemContext.of(exception, routingContext.normalizedPath());
                    HttpProblem finalProblem = ExceptionMapperBase.postProcessorsRegistry.applyPostProcessing(problem,
                            context);
                    return finalProblem.toResponse();
                });
    }

}
