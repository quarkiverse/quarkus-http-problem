package io.quarkiverse.httpproblem;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * RestAssured's JsonPath does not like slashes in jsonpath segments (e.g. application/problem+json) with dot notation,
 * but brackets work fine
 */
@QuarkusTest
class OpenApiIT {

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    void shouldIncludeHttpProblemSchema() {
        getOpenApi()
                .body("components.schemas.HttpProblem.description", equalTo("HTTP Problem Response according to RFC9457 and RFC7807"))
                .body("components.schemas.HttpProblem.properties.title.description", equalTo("A optional, short, human-readable summary of the problem type"))
                .body("components.schemas.HttpProblem.additionalProperties", equalTo(true))

                .body("components.schemas.HttpValidationProblem.description", equalTo("HTTP Validation Problem Response according to RFC9457 and RFC7807"))
                .body("components.schemas.HttpValidationProblem.properties.violations.description", equalTo("List of validation constraint violations that occurred"))
                .body("components.schemas.HttpValidationProblem.additionalProperties", equalTo(true));
    }

    @Test
    void shouldAddHttpProblemApiResponseFromAnnotationIfContentIsNotDefined() {
        getOpenApi()
                .body("paths['/openapi/documented']['post']['responses']['409']['description']", equalTo("Request received but there has been a conflict"))
                .body("paths['/openapi/documented']['post']['responses']['409']['content']['application/problem+json'].schema.$ref", equalTo("#/components/schemas/HttpProblem"));
    }

    @Test
    void shouldDeriveApiResponseFromEndpointMethodThrowsDeclaration() {
        getOpenApi()
                .body("paths['/openapi/throwing']['post']['responses']['400'].description", equalTo("Bad request: server would not process the request due to something the server considered to be a client error"))
                .body("paths['/openapi/throwing']['post']['responses']['400']['content']['application/problem+json'].schema.$ref", equalTo("#/components/schemas/HttpValidationProblem"))
                .body("paths['/openapi/throwing']['post']['responses']['403'].description", equalTo("Forbidden: server understood the request but refused to process it"))
                .body("paths['/openapi/throwing']['post']['responses']['403']['content']['application/problem+json'].schema.$ref", equalTo("#/components/schemas/HttpProblem"))
                .body("paths['/openapi/throwing']['post']['responses']['404'].description", equalTo("Not Found: server cannot find the requested resource"));
    }

    @Test
    void shouldAddApiResponsesBothFromAnnotationAndThrows() {
        getOpenApi()
                // derived from @ApiResponse annotation of the endpoint method
                .body("paths['/openapi/throwing-and-documented']['post']['responses']['401']['description']", equalTo("You are unauthorized"))
                .body("paths['/openapi/throwing-and-documented']['post']['responses']['401']['content']['application/problem+json'].schema.$ref", equalTo("#/components/schemas/HttpProblem"))
                // derived from `throws` :
                .body("paths['/openapi/throwing-and-documented']['post']['responses']['500']['description']", equalTo("Internal Server Error: the server encountered an unexpected condition that prevented it from fulfilling the request"));
    }

    private static ValidatableResponse getOpenApi() {
        return given()
                .accept(ContentType.JSON)
                .get("/q/openapi")
                .then()
                .statusCode(200);
    }

}
