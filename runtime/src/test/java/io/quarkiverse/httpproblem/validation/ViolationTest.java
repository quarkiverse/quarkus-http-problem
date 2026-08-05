package io.quarkiverse.httpproblem.validation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ViolationTest {

    @Test
    void shouldRejectNullField() {
        assertThatThrownBy(() -> Violation.In.body.field(null).message("required"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("field");
    }

    @Test
    void shouldRejectNullMessage() {
        assertThatThrownBy(() -> Violation.In.body.field("email").message(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("message");
    }

}
