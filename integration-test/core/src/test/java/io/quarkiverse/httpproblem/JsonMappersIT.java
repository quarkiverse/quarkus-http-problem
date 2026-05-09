package io.quarkiverse.httpproblem;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.oneOf;

import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@QuarkusTest
class JsonMappersIT {

    static final String JACKSON_MALFORMED_PAYLOAD_DETAIL = "Unexpected end-of-input within/between Object entries";
    static final String JACKSON_FIELD_SERIALIZATION_ERROR_DETAIL = "Cannot deserialize value of type `java.util.UUID` from String \"ABC-DEF-GHI\": UUID has to be represented by standard 36-char representation";

    static final String JSONB_CLASSIC_MALFORMED_PAYLOAD_DETAIL = "Internal error: Invalid token=EOF at (line no=1, column no=14, offset=13). Expected tokens are: [CURLYOPEN, SQUAREOPEN, STRING, NUMBER, TRUE, FALSE, NULL]";
    static final String JSONB_REACTIVE_MALFORMED_PAYLOAD_DETAIL = "Invalid token=EOF at (line no=1, column no=14, offset=13). Expected tokens are: [CURLYOPEN, SQUAREOPEN, STRING, NUMBER, TRUE, FALSE, NULL]";
    static final String JSONB_CLASSIC_FIELD_SERIALIZATION_ERROR_DETAIL = "Internal error: Invalid UUID string: ABC-DEF-GHI";
    static final String JSONB_REACTIVE_FIELD_SERIALIZATION_ERROR_DETAIL = "Invalid UUID string: ABC-DEF-GHI";
    static final String QUARKUS_2_15_JACKSON_REACTIVE_ERROR_DETAIL = "HTTP 400 Bad Request";

    private static final Logger logger = LoggerFactory.getLogger(JsonMappersIT.class);

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @DisplayName("Should return Bad Request(400) when request payload is malformed #1")
    void shouldThrowBadRequestOnMalformedBody() {
        given()
                .body("{\"key\":\"")
                .contentType(APPLICATION_JSON)
                .post("/throw/json")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    @DisplayName("Should return Bad Request(400) when request payload is malformed #2")
    @Disabled("TEMPORARY DISABLED")
    void shouldThrowBadRequestOnDifferentlyMalformedBody() {
        given()
                .body("{\"key\":")
                .contentType(APPLICATION_JSON)
                .post("/throw/json")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .body("detail", oneOf(JACKSON_MALFORMED_PAYLOAD_DETAIL, JSONB_CLASSIC_MALFORMED_PAYLOAD_DETAIL, JSONB_REACTIVE_MALFORMED_PAYLOAD_DETAIL, QUARKUS_2_15_JACKSON_REACTIVE_ERROR_DETAIL));
    }

    @Test
    @DisplayName("Should return Bad Request(400) when field in payload cannot be deserialized")
    void shouldThrowBadRequestForInvalidFieldFormat() throws IOException {
        ValidatableResponse response = given()
                .body("{\"uuid_field_1\":\"ABC-DEF-GHI\"}")
                .contentType(APPLICATION_JSON)
                .post("/throw/json")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        /**
         *  @see io.quarkus.resteasy.reactive.jackson.runtime.serialisers.JacksonMessageBodyReader, line 55
         */
        if(response.extract().body().asInputStream().available() == 0) {
            logger.info("Reactive impl returns empty body, skipping further validation");
            return;
        }

        response.body("detail", oneOf(JACKSON_FIELD_SERIALIZATION_ERROR_DETAIL, JSONB_CLASSIC_FIELD_SERIALIZATION_ERROR_DETAIL, JSONB_REACTIVE_FIELD_SERIALIZATION_ERROR_DETAIL))
                .body("field", anyOf(is("uuid_field_1"), nullValue())); // field not available in jsonb impl
    }

    @Test
    @DisplayName("Should return Bad Request(400) when nested field in payload cannot be deserialized")
    void shouldThrowBadRequestForInvalidFieldFormatInNestedObject() throws IOException {
        ValidatableResponse response = given()
                .body("{\"nested\": {\"uuid_field_2\":\"ABC-DEF-GHI\"}}")
                .contentType(APPLICATION_JSON)
                .post("/throw/json")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        /**
         *  @see io.quarkus.resteasy.reactive.jackson.runtime.serialisers.JacksonMessageBodyReader, line 55
         */
        if(response.extract().body().asInputStream().available() == 0) {
            logger.info("Reactive impl returns empty body, skipping further validation");
            return;
        }

        response.body("detail", oneOf(JACKSON_FIELD_SERIALIZATION_ERROR_DETAIL, JSONB_CLASSIC_FIELD_SERIALIZATION_ERROR_DETAIL, JSONB_REACTIVE_FIELD_SERIALIZATION_ERROR_DETAIL))
                .body("field", anyOf(is("nested.uuid_field_2"), nullValue())); // field not available in jsonb impl
    }


    @Test
    @DisplayName("Should return Bad Request(400) when field in payload collection item cannot be deserialized #3")
    void shouldThrowBadRequestForInvalidFieldFormatInCollectionItem() throws IOException {
        ValidatableResponse response = given()
                .body("{\"collection\": [{\"uuid_field_2\":\"ABC-DEF-GHI\"}]}")
                .contentType(APPLICATION_JSON)
                .post("/throw/json")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        /**
         *  @see io.quarkus.resteasy.reactive.jackson.runtime.serialisers.JacksonMessageBodyReader, line 55
         */
        if(response.extract().body().asInputStream().available() == 0) {
            logger.info("Reactive impl returns empty body, skipping further validation");
            return;
        }

        response.body("detail", oneOf(JACKSON_FIELD_SERIALIZATION_ERROR_DETAIL, JSONB_CLASSIC_FIELD_SERIALIZATION_ERROR_DETAIL, JSONB_REACTIVE_FIELD_SERIALIZATION_ERROR_DETAIL))
                .body("field", anyOf(is("collection[0].uuid_field_2"), nullValue()));
    }
}
