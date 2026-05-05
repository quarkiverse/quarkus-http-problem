package io.quarkiverse.httpproblem.deployment;

import static io.quarkiverse.httpproblem.validation.ConstraintViolationExceptionMapper.HTTP_VALIDATION_PROBLEM_STATUS_CODE;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;

import io.quarkiverse.httpproblem.HttpProblem;

/**
 * OpenAPI build-time filter that automatically augments various OpenApi model parts:
 * - error responses without explicit @Content defined in @APIResponse
 * - updates status code and description of HttpValidationProblem according to configuration
 */
public class OpenApiProblemFilter implements OASFilter {

    private final ProblemBuildConfig config;
    private final Content problemContent;
    private final Content validationProblemContent;

    public OpenApiProblemFilter(ProblemBuildConfig config) {
        this.config = config;
        this.problemContent = createContent(config.openapi().defaultSchema());
        this.validationProblemContent = createContent(config.openapi().validationProblemSchema());
    }

    /**
     * HttpValidationProblem is configurable in regard to status code (e.g. 422 instead of default 400). But ApiResponse
     * annotation on ConstraintViolationExceptionMapper obviously cannot use even a build time config: annotations must
     * use pure constants. Because of this, a fake special responseCode for this specific responses is introduced:
     * <HttpValidationProblem>. Once it is detected, it is augmented with status code and description defined in the
     * config.
     */
    @Override
    public Operation filterOperation(Operation operation) {
        if (operation.getResponses().hasAPIResponse(HTTP_VALIDATION_PROBLEM_STATUS_CODE)) {
            APIResponse response = operation.getResponses().getAPIResponse(HTTP_VALIDATION_PROBLEM_STATUS_CODE)
                    .description(config.constraintViolation().description())
                    .content(validationProblemContent);

            operation.getResponses().addAPIResponse(String.valueOf(config.constraintViolation().status()), response);
            operation.getResponses().removeAPIResponse(HTTP_VALIDATION_PROBLEM_STATUS_CODE);
        }
        return operation;
    }

    /**
     * Augments HttpProblem schema for 4xx and 5xx error @ApiResponses that don't have explicit @Content defined
     */
    @Override
    public APIResponse filterAPIResponse(APIResponse apiResponse) {
        if (apiResponse == null || apiResponse.getRef() != null || apiResponse.getContent() != null) {
            return apiResponse;
        }

        if (!(apiResponse instanceof io.smallrye.openapi.internal.models.responses.APIResponse internalResponse)) {
            return apiResponse;
        }

        String responseCode = (String) internalResponse.getExtension("x-smallrye-private-response-code");
        if (responseCode == null || responseCode.isEmpty()) {
            return apiResponse;
        }

        try {
            int httpStatus = Integer.parseInt(responseCode);
            if (httpStatus >= 400) {
                apiResponse.setContent(problemContent);
            }
        } catch (NumberFormatException e) {
            return apiResponse;
        }

        return apiResponse;
    }

    private static Content createContent(String schemaName) {
        Schema schema = OASFactory.createSchema();
        schema.setRef("#/components/schemas/" + schemaName);

        MediaType mediaType = OASFactory.createMediaType();
        mediaType.setSchema(schema);

        Content content = OASFactory.createContent();
        content.addMediaType(HttpProblem.MEDIA_TYPE.toString(), mediaType);
        return content;
    }
}