package io.quarkiverse.httpproblem.security;

import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;

import jakarta.ws.rs.core.Response;

import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticator;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

final class HttpUnauthorizedUtils {

    static Uni<HttpProblem> toProblem(RoutingContext routingContext, Exception exception) {
        return extractChallenge(routingContext)
                .onItemOrFailure()
                .transform((challenge, e) -> {
                    if (challenge == null) {
                        return HttpProblem.builder()
                                .withTitle(UNAUTHORIZED.getReasonPhrase())
                                .withStatus(UNAUTHORIZED)
                                .withDetail(exception.getMessage())
                                .build();
                    }

                    Response.Status status = Response.Status.fromStatusCode(challenge.status);
                    HttpProblem.Builder builder = HttpProblem.builder()
                            .withStatus(status)
                            .withTitle(status.getReasonPhrase());
                    if (challenge.getHeaders() != null) {
                        for (CharSequence headerName : challenge.getHeaders().keySet()) {
                            builder = builder.withHeader(headerName.toString(), challenge.getHeaders().get(headerName));
                        }
                    }
                    return builder.build();
                });
    }

    private static Uni<ChallengeData> extractChallenge(RoutingContext routingContext) {
        if (routingContext == null) {
            return Uni.createFrom().nullItem();
        }

        HttpAuthenticator authenticator = routingContext.get(HttpAuthenticator.class.getName());
        if (authenticator == null) {
            return Uni.createFrom().nullItem();
        }
        return authenticator.getChallenge(routingContext)
                .onFailure()
                .recoverWithUni(Uni.createFrom().nullItem());
    }

}
