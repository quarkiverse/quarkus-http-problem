package io.quarkiverse.httpproblem.jackson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkiverse.httpproblem.postprocessing.PostProcessorsRegistry;
import io.quarkiverse.httpproblem.postprocessing.ProblemDefaultsProvider;
import io.quarkiverse.httpproblem.postprocessing.ProblemLogger;
import io.quarkiverse.httpproblem.postprocessing.ProblemLoggingConfig;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

class UnrecognizedPropertyExceptionMapperTest {

    PostProcessorsRegistry registry = new PostProcessorsRegistry(
            List.of(new ProblemLogger(ProblemLoggingConfig.defaults()), new ProblemDefaultsProvider()));
    UnrecognizedPropertyExceptionMapper mapper = new UnrecognizedPropertyExceptionMapper(registry);

    @Test
    void shouldProduceHttp400() {
        UnrecognizedPropertyException exception = UnrecognizedPropertyException.from(mock(JsonParser.class),
                this.getClass(), "unknown_field", new ArrayList<>());

        Response response = mapper.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getMediaType()).isEqualTo(HttpProblem.MEDIA_TYPE);
        assertThat(response.getEntity())
                .isInstanceOf(HttpProblem.class)
                .hasFieldOrPropertyWithValue("detail",
                        "Unrecognized field \"unknown_field\", not marked as ignorable");
    }

}
