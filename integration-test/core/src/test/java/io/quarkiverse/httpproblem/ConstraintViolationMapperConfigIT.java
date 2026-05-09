package io.quarkiverse.httpproblem;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
@TestProfile(ConstraintViolationMapperConfigIT.CustomHttpStatus.class)
class ConstraintViolationMapperConfigIT {

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    void constraintViolationShouldProvideErrorDetails() {
        given()
                .body("{\"phraseName\": 10 }")
                .contentType(APPLICATION_JSON)
                .post("/throw/validation/constraint-violation-exception")
                .then()
                .statusCode(422)
                .body("title", equalTo("Constraint violation"))
                .body("status", equalTo(422))
                .body("violations", hasSize(1));
    }

    public static class CustomHttpStatus implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "quarkus.http-problem.constraint-violation.status", "422",
                    "quarkus.http-problem.constraint-violation.title", "Constraint violation"
            );
        }
    }
}
