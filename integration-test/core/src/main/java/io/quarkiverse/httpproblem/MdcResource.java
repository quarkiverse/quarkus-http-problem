package io.quarkiverse.httpproblem;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.MDC;

@Path("/throw/mdc/")
@Produces(MediaType.APPLICATION_JSON)
public class MdcResource {

    @GET
    public void setMdcAndThrow() {
        MDC.put("uuid", "30a48c9e");
        MDC.put("field-from-configuration", "123");
        throw new RuntimeException("mdc test");
    }

}
