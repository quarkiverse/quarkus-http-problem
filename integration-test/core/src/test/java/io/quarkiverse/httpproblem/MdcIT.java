package io.quarkiverse.httpproblem;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

@QuarkusTest
class MdcIT {

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    void mdcPropertiesShouldBeReturned() {
        given()
                .get("/throw/mdc")
                .then()
                .body("uuid", equalTo("30a48c9e"))
                .body("field-from-configuration", equalTo("123"));
    }
}
