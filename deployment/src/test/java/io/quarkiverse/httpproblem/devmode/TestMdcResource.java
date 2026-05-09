package io.quarkiverse.httpproblem.devmode;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.MDC;

@Path("/throw-exception")
@Produces(MediaType.APPLICATION_JSON)
public class TestMdcResource {

    @GET
    public String setMdcAndThrow() {
        MDC.put(PropertiesLiveReloadTest.ORIGINAL_PROPERTY_NAME, PropertiesLiveReloadTest.PROPERTY_VALUE);
        MDC.put(PropertiesLiveReloadTest.NEW_PROPERTY_NAME, PropertiesLiveReloadTest.PROPERTY_VALUE);
        throw new RuntimeException("mdc test");
    }

}
