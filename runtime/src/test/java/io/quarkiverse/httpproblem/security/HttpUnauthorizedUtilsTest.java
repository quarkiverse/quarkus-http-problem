package io.quarkiverse.httpproblem.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticator;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

class HttpUnauthorizedUtilsTest {

    @Test
    void shouldBuildUnauthorizedProblemWithoutRoutingContext() {
        Exception exception = new IllegalArgumentException("Invalid credentials");

        HttpProblem problem = HttpUnauthorizedUtils.toProblem(null, exception)
                .await().atMost(Duration.ofSeconds(5));

        assertThat(problem.getStatusCode()).isEqualTo(401);
        assertThat(problem.getTitle()).isEqualTo("Unauthorized");
        assertThat(problem.getDetail()).isEqualTo("Invalid credentials");
    }

    @Test
    void shouldBuildUnauthorizedProblemWhenAuthenticatorReturns401Challenge() {
        RoutingContext routingContext = routingContextReturningChallenge(401);

        HttpProblem problem = HttpUnauthorizedUtils.toProblem(routingContext, new RuntimeException("ignored"))
                .await().atMost(Duration.ofSeconds(5));

        assertThat(problem.getStatusCode()).isEqualTo(401);
        assertThat(problem.getTitle()).isEqualTo("Unauthorized");
        assertThat(problem.getDetail()).isNull();
    }

    @Test
    void shouldBuildProblemWhenAuthenticatorReturnsNonStandard499ChallengeStatus() {
        RoutingContext routingContext = routingContextReturningChallenge(499);

        HttpProblem problem = HttpUnauthorizedUtils.toProblem(routingContext, new RuntimeException("ignored"))
                .await().atMost(Duration.ofSeconds(5));

        assertThat(problem.getStatusCode()).isEqualTo(499);
        assertThat(problem.getTitle()).isEqualTo("HTTP Status 499");
        assertThat(problem.getDetail()).isNull();
    }

    private static RoutingContext routingContextReturningChallenge(int challengeStatusCode) {
        RoutingContext routingContext = mock(RoutingContext.class);
        HttpAuthenticator authenticator = mock(HttpAuthenticator.class);
        when(routingContext.get(HttpAuthenticator.class.getName())).thenReturn(authenticator);
        when(authenticator.getChallenge(any(RoutingContext.class)))
                .thenReturn(Uni.createFrom().item(new ChallengeData(challengeStatusCode)));
        return routingContext;
    }
}
