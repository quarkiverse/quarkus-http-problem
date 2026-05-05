package io.quarkiverse.httpproblem;

import java.util.Optional;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.assertj.core.api.AbstractAssert;

public final class ExceptionMapperAssert extends AbstractAssert<ExceptionMapperAssert, Class<? extends ExceptionMapper<?>>> {

    public static ExceptionMapperAssert assertThat(Class<? extends ExceptionMapper<?>> mapperClass) {
        return new ExceptionMapperAssert(mapperClass);
    }

    private ExceptionMapperAssert(Class<? extends ExceptionMapper<?>> actual) {
        super(actual, ExceptionMapperAssert.class);
    }

    public ExceptionMapperAssert hasPrecedenceOver(Class<? extends ExceptionMapper<?>> anotherMapperClass) {
        int actualPriority = priorityOf(this.actual);
        int anotherMapperClassPriority = priorityOf(anotherMapperClass);
        if (actualPriority >= anotherMapperClassPriority) {
            failWithMessage("Expected priority of <%s> to be lower than <%s> but was <%s>",
                    this.actual.getName(), anotherMapperClassPriority, actualPriority);
        }
        return this;
    }

    private int priorityOf(Class<? extends ExceptionMapper<?>> mapperClass) {
        return Optional.ofNullable(mapperClass.getAnnotation(Priority.class))
                .map(Priority::value)
                .orElse(Priorities.USER);
    }

}
