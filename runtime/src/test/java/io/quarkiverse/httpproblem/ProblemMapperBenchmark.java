package io.quarkiverse.httpproblem;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.google.common.collect.Sets;

import io.quarkiverse.httpproblem.postprocessing.ProblemRecorder;

/**
 * JMH benchmark for selected exception Mapper with all post-processors enabled + junit runner test for convenience.
 * JMH pro tip: run `mvn clean testCompile` to apply all JMH-related changes (this class)<br />
 * <br />
 *
 * Ref: http://tutorials.jenkov.com/java-performance/jmh.html#your-first-jmh-benchmark
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(value = 1, warmups = 0)
@Warmup(iterations = 15, time = 200, timeUnit = MILLISECONDS)
@Measurement(iterations = 15, time = 200, timeUnit = MILLISECONDS)
public class ProblemMapperBenchmark {

    @State(Scope.Thread)
    public static class BenchmarkState {

        public final HttpProblemMapper mapper = new HttpProblemMapper();

        public final HttpProblem problem = HttpProblemMother.complexProblem().build();

        @Setup(Level.Trial)
        public void initMapper() {
            ProblemRecorder recorder = new ProblemRecorder();
            recorder.configureMdc(Sets.newHashSet("uuid"));
        }
    }

    @Benchmark
    public Response mapperToResponse(BenchmarkState state) {
        return state.mapper.toResponse(state.problem);
    }

    static final double MAX_AVG_EXECUTION_TIME_MICROSECONDS = 60;

    @Test
    @Tag("performance-test")
    @EnabledIfSystemProperty(named = "performance-test", matches = "true")
    public void mapperShouldBeFasterThanTarget() throws RunnerException {
        Options jmhOptions = new OptionsBuilder()
                .include(this.getClass().getSimpleName())
                .build();

        Collection<RunResult> runResults = new Runner(jmhOptions).run();

        assertThat(runResults).isNotEmpty();
        for (RunResult runResult : runResults) {
            double score = runResult.getPrimaryResult().getScore();
            assertThat(score).isLessThan(MAX_AVG_EXECUTION_TIME_MICROSECONDS);
        }
    }

}
