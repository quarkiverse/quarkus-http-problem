package io.quarkiverse.httpproblem.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.security.UnauthorizedException;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;

class UnauthorizedExceptionMapperTest {

    UnauthorizedExceptionMapper mapper = new UnauthorizedExceptionMapper();

    @BeforeEach
    void setup() {
        mapper.currentVertxRequest = mock(CurrentVertxRequest.class);
    }

    @Test
    void shouldProduceHttp401() {
        UnauthorizedException exception = new UnauthorizedException();

        Response response = mapper.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(401);
    }

}
