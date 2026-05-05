package io.quarkiverse.httpproblem;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ZalandoProblemMapperIT {

    static final String SAMPLE_TITLE = "I'm a teapot";
    static final String SAMPLE_DETAIL = "A small one";

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    void problemShouldReturnGivenStatus() {
        final int status = 400;
        given()
                .queryParam("status", status)
                .queryParam("title", SAMPLE_TITLE)
                .queryParam("detail", SAMPLE_DETAIL)
                .get("/throw/zalando-problem")
                .then()
                .statusCode(status)
                .body("type", equalTo("/business-problem"))
                .body("instance", equalTo("/problem/special-case"))
                .body("title", equalTo(SAMPLE_TITLE))
                .body("status", equalTo(status))
                .body("detail", equalTo(SAMPLE_DETAIL))
                .body("stacktrace", nullValue());
    }

}
