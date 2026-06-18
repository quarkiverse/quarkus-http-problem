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

    @Test
    void shouldCreateEmptyRegistry() {
        HttpProblem originalProblem = badRequestProblem();

        HttpProblem finalProblem = new PostProcessorsRegistry()
                .applyPostProcessing(originalProblem, simpleContext());

        assertThat(finalProblem).isSameAs(originalProblem);
    }

    @Test
    void shouldOnlyClearLocalProcessorsWhenResettingCustomRegistry() {
        List<ProblemPostProcessor> inheritedProcessors = new ArrayList<>();
        PostProcessorsRegistry customRegistry = new PostProcessorsRegistry(inheritedProcessors::stream);
        inheritedProcessors.add(processorWithPriority(LOW));
        customRegistry.register(processorWithPriority(MEDIUM));
        customRegistry.reset();

        customRegistry.applyPostProcessing(badRequestProblem(), simpleContext());

        assertThat(invocations).containsExactly(LOW);
    }

    @Test
    void shouldApplyLocalProcessorsAfterInheritedStream() {
        PostProcessorsRegistry childRegistry = new PostProcessorsRegistry(registry);
        registry.register(processorWithPriority(LOW));
        childRegistry.register(processorWithPriority(HIGHEST));

        childRegistry.applyPostProcessing(badRequestProblem(), simpleContext());

        assertThat(invocations).containsExactly(LOW, HIGHEST);
    }

    @Test
    void shouldUseSupplierStreamAtApplyTime() {
        List<ProblemPostProcessor> inheritedProcessors = new ArrayList<>();
        PostProcessorsRegistry registryWithLiveInheritedStream = new PostProcessorsRegistry(inheritedProcessors::stream);
        inheritedProcessors.add(processorWithPriority(MEDIUM));

        registryWithLiveInheritedStream.applyPostProcessing(badRequestProblem(), simpleContext());

        assertThat(invocations).containsExactly(MEDIUM);
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
