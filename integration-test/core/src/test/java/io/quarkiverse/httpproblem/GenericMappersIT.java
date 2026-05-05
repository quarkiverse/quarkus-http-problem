package io.quarkiverse.httpproblem;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

@QuarkusTest
class GenericMappersIT {

    static final String SAMPLE_DETAIL = "A small one";

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    void runtimeExceptionShouldReturn500() {
        given()
                .queryParam("message", SAMPLE_DETAIL)
                .get("/throw/generic/runtime-exception")
                .then()
                .statusCode(INTERNAL_SERVER_ERROR.getStatusCode())
                .body("instance", equalTo("/throw/generic/runtime-exception"))
                .body("title", equalTo(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                .body("status", equalTo(INTERNAL_SERVER_ERROR.getStatusCode()))
                .body("detail", nullValue())
                .body("stacktrace", nullValue());
    }

    @Test
    void httpProblemShouldReturnHeaders() {
        when()
                .get("/throw/generic/http-problem")
                .then()
                .statusCode(409)
                .header("X-RFC7807", equalTo("IsAlive"))
                .body("title", equalTo("Product is out of stock"))
                .body("status", equalTo(409))
                .body("product", equalTo("rfc7807"))
                .body("stacktrace", nullValue());
    }

    @Test
    void shouldRegisterProblemPostProcessorCustomImplementationsFromCDI() {
        given()
                .queryParam("message", SAMPLE_DETAIL)
                .get("/throw/generic/runtime-exception")
                .then()
                .body("injected_from_custom_post_processor", equalTo("you called /throw/generic/runtime-exception"));
    }

    @Test
    void instanceShouldHandleUnwiseCharactersProperly() {
        given()
                .get("/non|existing path /with unwisecharacters")
                .then()
                .body("instance", equalTo("/non|existing path /with unwisecharacters"));
    }
}
