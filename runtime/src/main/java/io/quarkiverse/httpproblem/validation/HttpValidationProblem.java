package io.quarkiverse.httpproblem.validation;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import io.quarkiverse.httpproblem.HttpProblem;

/**
 * Representation of RFC7807 Problem schema.
 */
@Schema(name = "HttpValidationProblem", description = "HTTP Validation Problem Response according to RFC9457 and RFC7807", additionalProperties = Schema.True.class)
public class HttpValidationProblem extends HttpProblem {

    @Schema(description = "List of validation constraint violations that occurred")
    List<Violation> violations;

    public HttpValidationProblem(int status, String title, List<Violation> violations) {
        super(
                HttpProblem.builder()
                        .withStatus(status)
                        .withTitle(title)
                        .with("violations", violations));
        this.violations = violations;
    }

}
