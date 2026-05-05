package io.quarkiverse.httpproblem.postprocessing;

import static io.quarkiverse.httpproblem.HttpProblemMother.badRequestProblem;
import static io.quarkiverse.httpproblem.postprocessing.ProblemContextMother.simpleContext;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.quarkiverse.httpproblem.HttpProblem;

class PostProcessorsRegistryTest {

    static final int HIGHEST = 11;
    static final int MEDIUM = 10;
    static final int LOW = 9;

    PostProcessorsRegistry registry = new PostProcessorsRegistry();
    List<Integer> invocations = new ArrayList<>();

    @Test
    void shouldIterateFromHighestToLowestPriority() {
        registry.register(processorWithPriority(MEDIUM));
        registry.register(processorWithPriority(LOW));
        registry.register(processorWithPriority(HIGHEST));

        registry.applyPostProcessing(badRequestProblem(), simpleContext());

        assertThat(invocations).containsExactly(HIGHEST, MEDIUM, LOW);
    }

    @Test
    void shouldTolerateDuplicates() {
        registry.register(processorWithPriority(MEDIUM));
        registry.register(processorWithPriority(HIGHEST));
        registry.register(processorWithPriority(MEDIUM));
        registry.register(processorWithPriority(MEDIUM));

        registry.applyPostProcessing(badRequestProblem(), simpleContext());

        assertThat(invocations).containsExactly(HIGHEST, MEDIUM, MEDIUM, MEDIUM);
    }

    ProblemPostProcessor processorWithPriority(int priority) {
        return new TestProcessor(priority);
    }

    class TestProcessor implements ProblemPostProcessor {

        final int priority;

        TestProcessor(int priority) {
            this.priority = priority;
        }

        @Override
        public HttpProblem apply(HttpProblem problem, ProblemContext context) {
            invocations.add(priority);
            return problem;
        }

        @Override
        public int priority() {
            return priority;
        }
    }

}
