package io.quarkiverse.httpproblem.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.core.JacksonException;

class JacksonFieldPathSerializerTest {

    @Test
    void singleFieldName() {
        List<JacksonException.Reference> path = List.of(
                new JacksonException.Reference(this, "userName"));

        assertThat(JacksonFieldPathSerializer.serializePath(path)).isEqualTo("userName");
    }

    @Test
    void nestedFieldPath() {
        List<JacksonException.Reference> path = List.of(
                new JacksonException.Reference(this, "address"),
                new JacksonException.Reference(this, "city"));

        assertThat(JacksonFieldPathSerializer.serializePath(path)).isEqualTo("address.city");
    }

    @Test
    void arrayIndexInPath() {
        List<JacksonException.Reference> path = List.of(
                new JacksonException.Reference(this, "items"),
                new JacksonException.Reference(this, 2),
                new JacksonException.Reference(this, "name"));

        assertThat(JacksonFieldPathSerializer.serializePath(path)).isEqualTo("items[2].name");
    }

    @Test
    void emptyPathReturnsQuestionMark() {
        assertThat(JacksonFieldPathSerializer.serializePath(Collections.emptyList())).isEqualTo("?");
    }

    @Test
    void pathStartingWithArrayIndex() {
        List<JacksonException.Reference> path = List.of(
                new JacksonException.Reference(this, 0),
                new JacksonException.Reference(this, "name"));

        assertThat(JacksonFieldPathSerializer.serializePath(path)).isEqualTo("[0].name");
    }

    @Test
    void onlyArrayIndex() {
        List<JacksonException.Reference> path = List.of(
                new JacksonException.Reference(this, 5));

        assertThat(JacksonFieldPathSerializer.serializePath(path)).isEqualTo("[5]");
    }

    @Test
    void consecutiveArrayIndices() {
        List<JacksonException.Reference> path = List.of(
                new JacksonException.Reference(this, "matrix"),
                new JacksonException.Reference(this, 1),
                new JacksonException.Reference(this, 3));

        assertThat(JacksonFieldPathSerializer.serializePath(path)).isEqualTo("matrix[1][3]");
    }
}
