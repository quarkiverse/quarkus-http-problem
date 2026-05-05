package io.quarkiverse.httpproblem;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;
import static jakarta.ws.rs.core.Response.Status.MOVED_PERMANENTLY;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.nullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
class JaxRsMappersIT {

    static final String SAMPLE_DETAIL = "A small one";

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @ParameterizedTest(name = "http status: {0}")
    @ValueSource(ints = { 400, 401, 403, 404, 500, 502, 511 })
    void webApplicationExceptionShouldReturnGivenStatus(int status) {
        given()
                .queryParam("status", status)
                .get("/throw/jax-rs/web-application-exception")
                .then()
                .statusCode(status)
                .body("title", equalTo(Response.Status.fromStatusCode(status).getReasonPhrase()))
                .body("status", equalTo(status))
                .body("stacktrace", nullValue());
    }

    @ParameterizedTest(name = "http status: {0}")
    @ValueSource(ints = { 318, 499, 533 })
    void webApplicationExceptionWithUnassignedHttpStatusCodeShouldNotFail(int status) {
        given()
            .queryParam("status", status)
            .get("/throw/jax-rs/web-application-exception")
            .then()
            .statusCode(status)
            .body("title", equalToIgnoringCase("Unknown Code"))
            .body("status", equalTo(status))
            .body("stacktrace", nullValue());
    }

    @Test
    void webApplicationExceptionShouldReturnHeaders() {
        int status = 429;
        given()
                .queryParam("status", status)
                .get("/throw/jax-rs/web-application-exception-with-headers")
                .then()
                .statusCode(status)
                .header(HttpHeaders.RETRY_AFTER, equalTo("120"))
                .body("title", equalTo(Response.Status.fromStatusCode(status).getReasonPhrase()))
                .body("status", equalTo(status))
                .body("stacktrace", nullValue());
    }

    @Test
    void redirectionExceptionShouldReturnLocationHeader() {
        given()
                .redirects().follow(false)
                .get("/throw/jax-rs/redirection-exception")
                .then()
                .statusCode(MOVED_PERMANENTLY.getStatusCode())
                .header("Location", endsWith("/new-location"))
                .body("title", equalTo(MOVED_PERMANENTLY.getReasonPhrase()))
                .body("status", equalTo(MOVED_PERMANENTLY.getStatusCode()))
                .body("stacktrace", nullValue());
    }

    @Test
    void jaxRsNotFoundShouldReturn404ProblemInsteadOfDefaultRestEasyDefaultResponse() {
        given()
                .queryParam("message", SAMPLE_DETAIL)
                .get("/throw/jax-rs/not-found-exception")
                .then()
                .statusCode(NOT_FOUND.getStatusCode())
                .body("title", equalTo(NOT_FOUND.getReasonPhrase()))
                .body("status", equalTo(NOT_FOUND.getStatusCode()))
                .body("detail", equalTo(SAMPLE_DETAIL))
                .body("stacktrace", nullValue());
    }

    @Test
    void jaxRsForbiddenShouldReturn403ProblemInsteadOfDefaultRestEasyDefaultResponse() {
        given()
                .queryParam("message", SAMPLE_DETAIL)
                .get("/throw/jax-rs/forbidden-exception")
                .then()
                .statusCode(FORBIDDEN.getStatusCode())
                .body("title", equalTo(FORBIDDEN.getReasonPhrase()))
                .body("status", equalTo(FORBIDDEN.getStatusCode()))
                .body("detail", equalTo(SAMPLE_DETAIL))
                .body("stacktrace", nullValue());
    }

}
