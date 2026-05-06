package io.quarkiverse.httpproblem.security;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import io.quarkiverse.httpproblem.ExceptionMapperBase;
import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkus.security.UnauthorizedException;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;

/**
 * Mapper overriding default Quarkus exception mapper to make all error responses compliant with RFC7807.
 */
@Priority(Priorities.USER)
@APIResponse(responseCode = "401", description = "Unauthorized: request was not successful because it lacks valid authentication credentials for the requested resource")
public final class UnauthorizedExceptionMapper extends ExceptionMapperBase<UnauthorizedException>
        implements ExceptionMapper<UnauthorizedException> {

    @Override
    protected HttpProblem toProblem(UnauthorizedException exception) {
        return HttpUnauthorizedUtils.toProblem(currentVertxRequest().getCurrent(), exception)
                .await().indefinitely();
    }

    volatile CurrentVertxRequest currentVertxRequest;

    private CurrentVertxRequest currentVertxRequest() {
        if (currentVertxRequest == null) {
            currentVertxRequest = CDI.current().select(CurrentVertxRequest.class).get();
        }
        return currentVertxRequest;
    }

}
