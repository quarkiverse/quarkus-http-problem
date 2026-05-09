package io.quarkiverse.httpproblem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.problem.Status.BAD_REQUEST;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;

class ZalandoProblemMapperTest {

    ZalandoProblemMapper mapper = new ZalandoProblemMapper();

    @Test
    void responseShouldUseProblemStatus() {
        ThrowableProblem problem = Problem.builder()
                .withTitle("There's something wrong with your request")
                .withStatus(BAD_REQUEST)
                .build();

        Response response = mapper.toResponse(problem);

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
    }

    @Test
    void problemWithoutStatusShouldDefaultTo500() {
        ThrowableProblem exception = Problem.builder().build();

        Response response = mapper.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(500);
    }

}
