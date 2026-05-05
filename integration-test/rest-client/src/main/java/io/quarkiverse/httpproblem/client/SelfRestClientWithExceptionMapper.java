package io.quarkiverse.httpproblem.client;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "self-with-mapper")
@RegisterProvider(value = ThrowingHttpProblemClientExceptionMapper.class)
public interface SelfRestClientWithExceptionMapper extends SelfRestClient {
}