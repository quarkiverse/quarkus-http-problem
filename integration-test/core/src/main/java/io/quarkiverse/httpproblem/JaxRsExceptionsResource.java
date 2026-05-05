package io.quarkiverse.httpproblem;

import static jakarta.ws.rs.core.HttpHeaders.RETRY_AFTER;

import java.net.URI;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.RedirectionException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/throw/jax-rs/")
@Produces(MediaType.APPLICATION_JSON)
public class JaxRsExceptionsResource {

    static final MediaType MEDIA_TYPE_SHOULD_BE_IGNORED = MediaType.TEXT_PLAIN_TYPE;

    @GET
    @Path("/web-application-exception")
    public void throwWebApplicationException(@QueryParam("status") int status) {
        throw new WebApplicationException(new TestRuntimeException(), status);
    }

    @GET
    @Path("/web-application-exception-with-headers")
    public void throwWebApplicationExceptionWithHeaders(@QueryParam("status") int status) {
        throw new WebApplicationException(
                Response.status(status)
                        .header(RETRY_AFTER, 120)
                        .type(MEDIA_TYPE_SHOULD_BE_IGNORED)
                        .build());
    }

    @GET
    @Path("/redirection-exception")
    public void throwRedirection() {
        throw new RedirectionException(
                Response.Status.MOVED_PERMANENTLY,
                URI.create("/new-location"));
    }

    @GET
    @Path("/not-found-exception")
    public void throwNotFoundException(@QueryParam("message") String message) {
        throw new NotFoundException(message);
    }

    @GET
    @Path("/forbidden-exception")
    public void throwForbiddenException(@QueryParam("message") String message) {
        throw new ForbiddenException(message);
    }

    private static final class TestRuntimeException extends RuntimeException {
        TestRuntimeException() {
            super("First cause", new RuntimeException("Root cause"));
        }
    }

}
