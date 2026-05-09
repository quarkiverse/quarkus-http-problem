package io.quarkiverse.httpproblem.jaxrs;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

class NotFoundExceptionMapperTest {

    NotFoundExceptionMapper mapper = new NotFoundExceptionMapper();

    @Test
    void shouldProduceHttp404() {
        NotFoundException exception = new NotFoundException();

        Response response = mapper.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(404);
    }

}
