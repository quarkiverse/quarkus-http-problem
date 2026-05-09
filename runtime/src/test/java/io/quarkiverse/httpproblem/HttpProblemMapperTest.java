package io.quarkiverse.httpproblem;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

class HttpProblemMapperTest {

    HttpProblemMapper mapper = new HttpProblemMapper();

    @Test
    void responseShouldIncludeHeaders() {
        HttpProblem problem = HttpProblem.builder()
                .withHeader("X-Numeric-Header", 123)
                .withHeader("X-String-Header", "ABC")
                .build();

        Response response = mapper.toResponse(problem);

        assertThat(response.getHeaderString("X-Numeric-Header")).isEqualTo("123");
        assertThat(response.getHeaderString("X-String-Header")).isEqualTo("ABC");
    }

}
