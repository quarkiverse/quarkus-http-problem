package io.quarkiverse.httpproblem.client;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusTest
class RestClientIT {

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    void shouldCallItselfViaRestApiWithoutClientExceptionMapper() {
        given()
                .accept(ContentType.JSON)
                .get("/throw-via-rest-client")
                .then()
                .statusCode(409)
                .body("title", equalTo("Conflict"))
                .body("detail", equalTo("Received: 'Conflict, status code 409' when invoking REST Client method: 'io.quarkiverse.httpproblem.client.SelfRestClient#doThrow'"))
                .body("instance", equalTo("/throw-via-rest-client"));
    }

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

}
