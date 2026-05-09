package io.quarkiverse.httpproblem;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

import java.net.URI;

public final class HttpProblemMother {

    public static final String SERIALIZED_BAD_REQUEST_PROBLEM = "{\"status\":400,\"title\":\"There's something wrong with your request\"}";
    public static final String SERIALIZED_COMPLEX_PROBLEM = "{\"type\":\"http://tietoevry.com/problem\",\"status\":400,\"title\":\"Something wrong in the dirt\",\"detail\":\"Deep down wrongness, zażółć gęślą jaźń for Håkensth\",\"instance\":\"/endpoint\",\"custom_field_1\":\"too long\",\"custom_field_2\":\"too short\"}";

    private HttpProblemMother() {
    }

    public static HttpProblem badRequestProblem() {
        return badRequestProblemBuilder().build();
    }

    public static HttpProblem.Builder badRequestProblemBuilder() {
        return HttpProblem.builder()
                .withTitle("There's something wrong with your request")
                .withStatus(BAD_REQUEST);
    }

    public static HttpProblem.Builder complexProblem() {
        return HttpProblem.builder()
                .withType(URI.create("http://tietoevry.com/problem"))
                .withInstance(URI.create("/endpoint"))
                .withStatus(BAD_REQUEST)
                .withTitle("Something wrong in the dirt")
                .withDetail("Deep down wrongness, zażółć gęślą jaźń for Håkensth")
                .with("custom_field_1", "too long")
                .with("custom_field_2", "too short")
                .withHeader("X-Numeric-Header", 123)
                .withHeader("X-String-Header", "ABC");
    }

}
