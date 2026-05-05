package io.quarkiverse.httpproblem;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/throw/generic/")
@Produces(MediaType.APPLICATION_JSON)
public class GenericExceptionsResource {

    @GET
    @Path("/runtime-exception")
    public void throwRuntimeException(@QueryParam("message") String message) {
        throw new TestRuntimeException(message);
    }

    static final class TestRuntimeException extends RuntimeException {
        TestRuntimeException(String message) {
            super(message, new RuntimeException("Root cause"));
        }
    }

    @GET
    @Path("/http-problem")
    public void throwHttpProblem() {
        throw new OutOfStock("rfc7807");
    }

    static class OutOfStock extends HttpProblem {

        OutOfStock(String product) {
            super(builder()
                    .withTitle("Product is out of stock")
                    .withStatus(Response.Status.CONFLICT)
                    .withHeader("X-RFC7807", "IsAlive")
                    .with("product", product));
        }

    }

}
