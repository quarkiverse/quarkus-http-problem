package io.quarkiverse.httpproblem;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import io.quarkiverse.httpproblem.postprocessing.PostProcessorsRegistry;

class ExceptionMapperBaseTest {

    PostProcessorsRegistry registry = new PostProcessorsRegistry(List.of());

    @Test
    void shouldFallBackToGeneric500WhenToProblemThrows() {
        ExceptionMapperBase<RuntimeException> mapper = new ExceptionMapperBase<>(registry) {
            @Override
            protected HttpProblem toProblem(RuntimeException exception) {
                throw new IllegalStateException("mapper bug");
            }
        };

        Response response = mapper.toResponse(new RuntimeException("original"));

        assertThat(response.getStatus()).isEqualTo(500);
    }

    @Test
    void shouldFallBackToGeneric500WhenToProblemReturnsNull() {
        ExceptionMapperBase<RuntimeException> mapper = new ExceptionMapperBase<>(registry) {
            @Override
            protected HttpProblem toProblem(RuntimeException exception) {
                return null;
            }
        };

        Response response = mapper.toResponse(new RuntimeException("original"));

        assertThat(response.getStatus()).isEqualTo(500);
    }

    @Test
    void shouldProcessNormallyWhenToProblemSucceeds() {
        ExceptionMapperBase<RuntimeException> mapper = new ExceptionMapperBase<>(registry) {
            @Override
            protected HttpProblem toProblem(RuntimeException exception) {
                return HttpProblem.valueOf(Response.Status.BAD_REQUEST);
            }
        };

        Response response = mapper.toResponse(new RuntimeException("bad input"));

        assertThat(response.getStatus()).isEqualTo(400);
    }

}
