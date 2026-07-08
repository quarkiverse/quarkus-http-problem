package io.quarkiverse.httpproblem;

import static io.quarkiverse.httpproblem.HttpProblemMother.badRequestProblem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;

import io.quarkiverse.httpproblem.postprocessing.PostProcessorsRegistry;
import io.quarkiverse.httpproblem.postprocessing.ProblemPostProcessor;

class ExceptionMapperBaseTest {

    @Test
    void shouldUseDefaultPostProcessorsRegistryWhenCustomRegistryIsNotSupplied() {
        TestMapper mapper = new TestMapper();
        mapper.uriInfo = uriInfoWithPath("endpoint");

        Response response = mapper.toResponse(new RuntimeException());

        HttpProblem problem = (HttpProblem) response.getEntity();
        assertThat(problem.getInstance()).hasToString("endpoint");
    }

    @Test
    void shouldUseCustomPostProcessorsRegistry() {
        PostProcessorsRegistry registry = new PostProcessorsRegistry();
        registry.register(customPostProcessor());
        TestMapper mapper = new TestMapper(registry);
        mapper.uriInfo = uriInfoWithPath("endpoint");

        Response response = mapper.toResponse(new RuntimeException());

        HttpProblem problem = (HttpProblem) response.getEntity();
        assertThat(problem.getParameters()).containsEntry("registry", "custom");
        assertThat(problem.getInstance()).isNull();
    }

    private ProblemPostProcessor customPostProcessor() {
        return (problem, context) -> HttpProblem.builder(problem)
                .with("registry", "custom")
                .build();
    }

    private UriInfo uriInfoWithPath(String path) {
        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getPath()).thenReturn(path);
        return uriInfo;
    }

    private static class TestMapper extends ExceptionMapperBase<RuntimeException> {

        TestMapper() {
        }

        TestMapper(PostProcessorsRegistry registry) {
            super(registry);
        }

        @Override
        protected HttpProblem toProblem(RuntimeException exception) {
            return badRequestProblem();
        }
    }
}
