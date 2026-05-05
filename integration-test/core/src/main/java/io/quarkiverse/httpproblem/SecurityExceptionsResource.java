package io.quarkiverse.httpproblem;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/throw/security/")
@Produces(MediaType.APPLICATION_JSON)
public class SecurityExceptionsResource {

    @GET
    @Path("/unauthorized-exception")
    public void throwUnauthorizedException(@QueryParam("message") String message) {
        throw new UnauthorizedException(message);
    }

    @GET
    @Path("/authentication-failed-exception")
    public void throwAuthenticationFailedException(@QueryParam("message") String message) {
        throw new AuthenticationFailedException(message);
    }

    @GET
    @Path("/forbidden-exception")
    public void throwForbiddenException(@QueryParam("message") String message) {
        throw new ForbiddenException(message);
    }

    @RolesAllowed("rfc7807")
    @GET
    @Path("/secured-resource")
    public void securedResource() {
    }

}
