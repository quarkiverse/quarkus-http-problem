package io.quarkiverse.httpproblem.client;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusTest
class RestClientTest {

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    void shouldCallItselfViaRestApiWithoutClientExceptionMapper() {
        String resteasyClassicDetail = "HTTP 409 Conflict";
        String resteasyReactiveDetail = "Received: 'Conflict, status code 409' when invoking REST Client method: 'io.quarkiverse.httpproblem.client.SelfRestClient#doThrow'";

        given()
                .accept(ContentType.JSON)
                .get("/throw-via-rest-client")
                .then()
                .statusCode(409)
                .body("title", equalTo("Conflict"))
                .body("detail", either(equalTo(resteasyClassicDetail)).or(equalTo(resteasyReactiveDetail)))
                .body("instance", equalTo("/throw-via-rest-client"));
    }

    // TODO remove @DisabledIfSystemProperty once https://github.com/quarkusio/quarkus/pull/57782 is included in Quarkus 4.0.0.Beta2
    @DisabledIfSystemProperty(named = "resteasy-classic", matches = "true")
    @Test
    void shouldRethrowHttpProblemFromUpstreamServiceWithOverriddenInstance() {
        given()
                .accept(ContentType.JSON)
                .get("/throw-via-rest-client-with-mapper")
                .then()
                .statusCode(409)
                .body("title", equalTo("Conflict from upstream service"))
                .body("detail", equalTo("Nothing to add"))
                .body("instance", equalTo("/throw-via-rest-client-with-mapper"));
    }

    @Test
    void shouldNotCrashWhenUpstreamReturnsMalformedProblemBody() {
        given()
                .accept(ContentType.JSON)
                .get("/throw-via-rest-client-with-mapper-malformed")
                .then()
                .statusCode(409); 
    }

    // TODO remove @DisabledIfSystemProperty once https://github.com/quarkusio/quarkus/pull/57782 is included in Quarkus 4.0.0.Beta2
    @DisabledIfSystemProperty(named = "resteasy-classic", matches = "true")
    @Test
    void shouldRethrowHttpProblemWhenContentTypeHasCharsetParameter() {    
        given()
                .accept(ContentType.JSON)
                .get("/throw-via-rest-client-with-mapper-charset")
                .then()
                .statusCode(409)
                .body("title", equalTo("Conflict from upstream service"));
    }

}
