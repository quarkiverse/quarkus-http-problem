package io.quarkiverse.httpproblem.postprocessing;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;

import io.quarkiverse.httpproblem.HttpProblem;

/**
 * Logs problems with ERROR (for HTTP 5XX) or INFO (other exceptions) log level. In case of ERROR (HTTP 5XX) stack trace is
 * printed as well.
 */
final class ProblemLogger implements ProblemPostProcessor {

    private final Logger logger;

    ProblemLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public HttpProblem apply(HttpProblem problem, ProblemContext context) {
        if (!logger.isErrorEnabled()) {
            return problem;
        }

        if (problem.getStatusCode() >= 500) {
            if (logger.isErrorEnabled()) {
                logger.error(serialize(problem), context.cause);
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info(serialize(problem));
            }
        }
        return problem;
    }

    private String serialize(HttpProblem problem) {
        Stream<String> basicFields = Stream.of(
                ("status=" + problem.getStatusCode()),
                (problem.getTitle() == null) ? null : ("title=\"" + problem.getTitle() + "\""),
                (problem.getDetail() == null) ? null : ("detail=\"" + problem.getDetail() + "\""),
                (problem.getInstance() == null) ? null : ("instance=\"" + problem.getInstance() + "\""),
                (problem.getType() == null) ? null : "type=" + problem.getType().toString());

        Stream<String> parameters = problem.getParameters().entrySet().stream().map(this::serializeParameter);

        return Stream.concat(basicFields, parameters)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

    private String serializeParameter(Map.Entry<String, Object> param) {
        String serializedValue = Optional.ofNullable(param.getValue())
                .map(value -> {
                    if (value instanceof String) {
                        return "\"" + value + "\"";
                    } else {
                        return value.toString();
                    }
                })
                .orElse("null");

        return param.getKey() + "=" + serializedValue;
    }

    @Override
    public int priority() {
        return 101;
    }
}
