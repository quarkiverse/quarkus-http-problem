package io.quarkiverse.httpproblem.client;

import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

import io.quarkiverse.httpproblem.HttpProblem;

/**
 * Parses application/problem+json response types and rethrows them as `HttpProblem`.
 * <p>
 * Intended to be used as @Provider for RestClients via @RegisterRestClient.
 */
public class ThrowingHttpProblemClientExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

    @Override
    public RuntimeException toThrowable(Response response) {
        if (!HttpProblem.MEDIA_TYPE.isCompatible(response.getMediaType())) { // TODO add tests here where UTF+8 is appended to respons
            return null; // Let others handle non-problem formats
        }

        HttpProblem returnedProblem = response.readEntity(HttpProblem.class);

        // instance must be nullified, otherwise it will be propagated as-is
        return HttpProblem.builder(returnedProblem)
                .withInstance(null)
                .build();
    }

}
