package io.quarkiverse.httpproblem.jackson;

import java.util.List;
import java.util.stream.Collectors;

import tools.jackson.databind.DatabindException;

final class JacksonFieldPathSerializer {

    private JacksonFieldPathSerializer() {
    }

    static String serializePath(List<DatabindException.Reference> path) {
        String pathString = path.stream()
                .map(JacksonFieldPathSerializer::refToString)
                .collect(Collectors.joining());
        return removeFirstDot(pathString);
    }

    private static String refToString(DatabindException.Reference ref) {
        if (ref.getPropertyName() != null) {
            return "." + ref.getPropertyName();
        }
        if (ref.getIndex() >= 0) {
            return "[" + ref.getIndex() + "]";
        }
        return ".?";
    }

    private static String removeFirstDot(String field) {
        if (field.isEmpty()) {
            return "?";
        }
        if (field.charAt(0) == '.') {
            return field.substring(1);
        }
        return field;
    }
}
