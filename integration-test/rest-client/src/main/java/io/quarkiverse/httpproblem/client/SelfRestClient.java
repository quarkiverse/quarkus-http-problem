package io.quarkiverse.httpproblem.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "self")
public interface SelfRestClient {
    @GET
    @Path("/throw")
    void doThrow();
}