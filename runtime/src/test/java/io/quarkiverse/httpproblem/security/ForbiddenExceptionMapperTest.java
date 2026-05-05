package io.quarkiverse.httpproblem.security;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import io.quarkus.security.ForbiddenException;

class ForbiddenExceptionMapperTest {

    ForbiddenExceptionMapper mapper = new ForbiddenExceptionMapper();

    @Test
    void shouldProduceHttp403() {
        ForbiddenException exception = new ForbiddenException();

        Response response = mapper.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(403);
    }

}
