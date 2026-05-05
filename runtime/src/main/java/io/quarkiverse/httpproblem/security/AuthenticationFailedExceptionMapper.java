package io.quarkiverse.httpproblem.security;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.Priorities;

import io.quarkiverse.httpproblem.ExceptionMapperBase;
import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;

/**
 * Mapper overriding default Quarkus exception mapper to make all error responses compliant with RFC7807.
 *
 * @see io.quarkus.resteasy.runtime.AuthenticationFailedExceptionMapper
 */
@Priority(Priorities.USER - 1)
public final class AuthenticationFailedExceptionMapper extends ExceptionMapperBase<AuthenticationFailedException> {

    @Override
    protected HttpProblem toProblem(AuthenticationFailedException exception) {
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
