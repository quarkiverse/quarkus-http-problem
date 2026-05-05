package io.quarkiverse.httpproblem.validation;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "Violation", description = "Validation constraint violation details")
public final class Violation {

    public enum In {
        query,
        path,
        header,
        form,
        body,
        unknown() {
            @Override
            protected String serialize() {
                return "?";
            }
        };

        public MessageSupplier field(String field) {
            return message -> new Violation(field, this.serialize(), message);
        }

        protected String serialize() {
            return name();
        }
    }

    public interface MessageSupplier {
        Violation message(String message);
    }

    @Schema(description = "The field for which the validation failed", examples = "#/profile/email")
    public final String field;

    @Schema(description = "Part of the http request where the validation error occurred such as query, path, header, form, body", examples = {
            "query", "path", "header", "form", "body" })
    public final String in;

    @Schema(description = "Description of the validation error", examples = "Invalid email format")
    public final String message;

    private Violation(String field, String in, String message) {
        this.field = field;
        this.in = in;
        this.message = message;
    }

    @Override
    public String toString() {
        return "Violation{" +
                "field='" + field + '\'' +
                ", in='" + in + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
