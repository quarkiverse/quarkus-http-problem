package io.quarkiverse.httpproblem.postprocessing;

import static io.quarkiverse.httpproblem.postprocessing.ProblemContextMother.simpleContext;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkiverse.httpproblem.validation.Violation;

class ProblemLoggerTest {

    Logger logger = mock(Logger.class);
    ProblemLogger processor = new ProblemLogger(logger);

    @BeforeEach
    void init() {
        when(logger.isErrorEnabled()).thenReturn(true);
        when(logger.isInfoEnabled()).thenReturn(true);
    }

    @Test
    void shouldPrintOnlyNotNullFields() {
        HttpProblem problem = HttpProblem.builder()
                .withTitle("your fault")
                .withStatus(BAD_REQUEST)
                .build();

        processor.apply(problem, simpleContext());

        verify(logger).info("status=400, title=\"your fault\"");
    }

    @Test
    void shouldPrintCustomParameters() {
        HttpProblem problem = HttpProblem.builder()
                .withTitle("your fault")
                .withStatus(BAD_REQUEST)
                .with("custom-field", "123")
                .with("violations", Collections.singletonList(Violation.In.body.field("key").message("too small")))
                .with("nullable_field", null)
                .build();

        processor.apply(problem, simpleContext());

        verify(logger).info(
                "status=400, title=\"your fault\", custom-field=\"123\", violations=[Violation{field='key', in='body', message='too small'}], nullable_field=null");
    }

    @Test
    void shouldPrintStackTraceFor500s() {
        HttpProblem problem = HttpProblem.builder()
                .withTitle("my fault")
                .withStatus(INTERNAL_SERVER_ERROR)
                .build();
        RuntimeException cause = new RuntimeException("hey");

        processor.apply(problem, ProblemContextMother.withCause(cause));

        verify(logger).error("status=500, title=\"my fault\"", cause);
    }

}
