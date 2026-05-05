package io.quarkiverse.httpproblem.security;

import static io.quarkiverse.httpproblem.ExceptionMapperAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import io.quarkus.security.AuthenticationCompletionException;

class AuthenticationCompletionExceptionMapperTest {

    AuthenticationCompletionExceptionMapper mapper = new AuthenticationCompletionExceptionMapper();

    @Test
    void shouldHaveHigherPriorityThanBuiltInMapper() {
        assertThat(mapper.getClass())
                .hasPrecedenceOver(io.quarkus.resteasy.runtime.AuthenticationCompletionExceptionMapper.class);
    }

    @Test
    void shouldProduceHttp401() {
        AuthenticationCompletionException exception = new AuthenticationCompletionException();

        Response response = mapper.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(401);
    }
}
