package io.quarkiverse.httpproblem.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.DatabindException;

class JacksonFieldPathSerializerTest {

    @Test
    void singleFieldName() {
        List<DatabindException.Reference> path = List.of(
                new DatabindException.Reference(this, "userName"));

        assertThat(JacksonFieldPathSerializer.serializePath(path)).isEqualTo("userName");
    }

    @Test
    void nestedFieldPath() {
        List<DatabindException.Reference> path = List.of(
                new DatabindException.Reference(this, "address"),
                new DatabindException.Reference(this, "city"));

        assertThat(JacksonFieldPathSerializer.serializePath(path)).isEqualTo("address.city");
    }

    @Test
    void arrayIndexInPath() {
        List<DatabindException.Reference> path = List.of(
                new DatabindException.Reference(this, "items"),
                new DatabindException.Reference(this, 2),
                new DatabindException.Reference(this, "name"));

        assertThat(JacksonFieldPathSerializer.serializePath(path)).isEqualTo("items[2].name");
    }

    @Test
    void emptyPathReturnsQuestionMark() {
        assertThat(JacksonFieldPathSerializer.serializePath(Collections.emptyList())).isEqualTo("?");
    }

    @Test
    void pathStartingWithArrayIndex() {
        List<DatabindException.Reference> path = List.of(
                new DatabindException.Reference(this, 0),
                new DatabindException.Reference(this, "name"));

        assertThat(JacksonFieldPathSerializer.serializePath(path)).isEqualTo("[0].name");
    }

    @Test
    void onlyArrayIndex() {
        List<DatabindException.Reference> path = List.of(
                new DatabindException.Reference(this, 5));

        assertThat(JacksonFieldPathSerializer.serializePath(path)).isEqualTo("[5]");
    }

    @Test
    void consecutiveArrayIndices() {
        List<DatabindException.Reference> path = List.of(
                new DatabindException.Reference(this, "matrix"),
                new DatabindException.Reference(this, 1),
                new DatabindException.Reference(this, 3));

        assertThat(JacksonFieldPathSerializer.serializePath(path)).isEqualTo("matrix[1][3]");
    }
}
