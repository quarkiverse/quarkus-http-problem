package io.quarkiverse.httpproblem;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.nullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class Rfc9457CompleteProblemTest {

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @DisplayName("RFC 9457 Section 3 - Content-Type should be application/problem+json for complete problem")
    void contentTypeShouldBeApplicationProblemJsonForCompleteProblem() {
        String contentType = when()
                .get("/throw/rfc9457/complete")
                .then()
                .statusCode(FORBIDDEN.getStatusCode())
                .extract().contentType();

        assertThat(contentType).contains("application/problem+json");
    }

    @Test
    @DisplayName("RFC 9457 Section 3 - Content-Type should be application/problem+json for minimal problem")
    void contentTypeShouldBeApplicationProblemJsonForMinimalProblem() {
        String contentType = when()
                .get("/throw/rfc9457/minimal")
                .then()
                .statusCode(INTERNAL_SERVER_ERROR.getStatusCode())
                .extract().contentType();

        assertThat(contentType).contains("application/problem+json");
    }

    @Test
    @DisplayName("RFC 9457 Section 3 - Content-Type should be application/problem+json for about:blank problem")
    void contentTypeShouldBeApplicationProblemJsonForAboutBlank() {
        String contentType = given()
                .queryParam("status", NOT_FOUND.getStatusCode())
                .get("/throw/rfc9457/about-blank")
                .then()
                .statusCode(NOT_FOUND.getStatusCode())
                .extract().contentType();

        assertThat(contentType).contains("application/problem+json");
    }

    @Test
    @DisplayName("RFC 9457 Section 3 - complete problem should contain all standard and extension members")
    void completeProblemShouldContainAllMembers() {
        when()
                .get("/throw/rfc9457/complete")
                .then()
                .statusCode(FORBIDDEN.getStatusCode())
                .body("type", equalTo("https://example.com/probs/out-of-credit"))
                .body("title", equalTo("You do not have enough credit."))
                .body("status", equalTo(FORBIDDEN.getStatusCode()))
                .body("detail", equalTo("Your current balance is 30, but that costs 50."))
                .body("instance", equalTo("https://example.net/account/12345/msgs/abc"))
                .body("balance", equalTo(30))
                .body("accounts", hasItems("/account/12345", "/account/67890"));
    }

    @Test
    @DisplayName("RFC 9457 - minimal problem should contain only status and auto-set instance")
    void minimalProblemShouldContainOnlyStatusAndInstance() {
        when()
                .get("/throw/rfc9457/minimal")
                .then()
                .statusCode(INTERNAL_SERVER_ERROR.getStatusCode())
                .body("status", equalTo(INTERNAL_SERVER_ERROR.getStatusCode()))
                .body("type", nullValue())
                .body("title", nullValue())
                .body("detail", nullValue())
                .body("instance", equalTo("/throw/rfc9457/minimal"));
    }

    @Test
    @DisplayName("RFC 9457 - null optional fields should not appear in JSON (not serialized as null)")
    void nullFieldsShouldNotBeSerialized() {
        String body = when()
                .get("/throw/rfc9457/minimal")
                .then()
                .extract().body().asString();

        assertThat(body).doesNotContain("\"type\"");
        assertThat(body).doesNotContain("\"title\"");
        assertThat(body).doesNotContain("\"detail\"");
        assertThat(body).contains("\"status\"");
        assertThat(body).contains("\"instance\"");
    }

    @Test
    @DisplayName("RFC 9457 - stacktrace should never be exposed in problem response")
    void stacktraceShouldNeverAppear() {
        when()
                .get("/throw/rfc9457/complete")
                .then()
                .body("stacktrace", nullValue());

        when()
                .get("/throw/rfc9457/minimal")
                .then()
                .body("stacktrace", nullValue());

        when()
                .get("/throw/rfc9457/with-extensions")
                .then()
                .body("stacktrace", nullValue());

        given()
                .queryParam("status", 500)
                .get("/throw/rfc9457/about-blank")
                .then()
                .body("stacktrace", nullValue());
    }
}
