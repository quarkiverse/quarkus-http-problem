package io.quarkiverse.httpproblem;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Representation of RFC9457/RFC7807 Problem schema.
 */
@Schema(name = "HttpProblem", description = "HTTP Problem Response according to RFC9457 and RFC7807", additionalProperties = Schema.True.class)
public class HttpProblem extends RuntimeException {

    public static final MediaType MEDIA_TYPE = new MediaType("application", "problem+json");

    @Schema(description = "A optional URI reference that identifies the problem type", examples = "https://example.com/errors/not-found")
    private final URI type;

    @Schema(description = "A optional, short, human-readable summary of the problem type", examples = "Not Found")
    private final String title;

    @Schema(name = "status", description = "The HTTP status code for this occurrence of the problem", examples = "404")
    private final int statusCode;

    @Schema(description = "A optional human-readable explanation specific to this occurrence of the problem", examples = "Record not found")
    private final String detail;

    @Schema(description = "A URI reference that identifies the specific occurrence of the problem", examples = "https://api.example.com/errors/123")
    private final URI instance;

    @Schema(hidden = true)
    private final Map<String, Object> parameters;

    @Schema(hidden = true)
    private final Map<String, Object> headers;

    protected HttpProblem(Builder builder) {
        super(createMessage(builder.title, builder.detail));

        this.type = builder.type;
        this.title = builder.title;
        this.statusCode = builder.statusCode;
        this.detail = builder.detail;
        this.instance = builder.instance;
        this.parameters = Collections.unmodifiableMap(Optional.ofNullable(builder.parameters).orElseGet(LinkedHashMap::new));
        this.headers = Collections.unmodifiableMap(Optional.ofNullable(builder.headers).orElseGet(LinkedHashMap::new));
    }

    private static String createMessage(String title, String detail) {
        return Stream.of(title, detail)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(": "));
    }

    public static HttpProblem valueOf(Response.Status status) {
        return builder()
                .withTitle(status.getReasonPhrase())
                .withStatus(status)
                .build();
    }

    public static HttpProblem valueOf(Response.Status status, String detail) {
        return builder()
                .withTitle(status.getReasonPhrase())
                .withStatus(status)
                .withDetail(detail)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates Builder instance and initializes it with fields from given HttpProblem
     *
     * @param original Problem 'prototype'
     * @return Builder object with values taken from origin HttpProblem
     */
    public static Builder builder(HttpProblem original) {
        Builder builder = builder()
                .withType(original.getType())
                .withInstance(original.getInstance())
                .withTitle(original.getTitle())
                .withStatus(original.getStatusCode())
                .withDetail(original.getDetail());
        original.parameters.forEach(builder::with);
        original.headers.forEach(builder::withHeader);
        return builder;
    }

    public URI getType() {
        return this.type;
    }

    public String getTitle() {
        return this.title;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Returns null if status code has no representation in Response.Status enum.
     *
     * @deprecated Replaced by {@link #getStatusCode()}, will be removed in 4.0.0 release as part of minimizing
     *             dependencies to JaxRS/JakartaRS classes.
     */
    @Deprecated(since = "3.1.0", forRemoval = true)
    @Schema(hidden = true)
    public Response.StatusType getStatus() {
        return Response.Status.fromStatusCode(statusCode);
    }

    public String getDetail() {
        return this.detail;
    }

    public URI getInstance() {
        return this.instance;
    }

    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    public Map<String, Object> getHeaders() {
        return this.headers;
    }

    public Response toResponse() {
        Response.ResponseBuilder builder = Response
                .status(getStatusCode())
                .type(HttpProblem.MEDIA_TYPE)
                .entity(this);

        getHeaders().forEach(builder::header);

        return builder.build();
    }

    public static class Builder {

        private static final Set<String> RESERVED_PROPERTIES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                "type", "title", "status", "detail", "instance")));

        private URI type;
        private String title;
        private int statusCode = 500;
        private String detail;
        private URI instance;
        private final Map<String, Object> headers = new LinkedHashMap<>();
        private final Map<String, Object> parameters = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder withType(@Nullable URI type) {
            this.type = type;
            return this;
        }

        public Builder withTitle(@Nullable String title) {
            this.title = title;
            return this;
        }

        public Builder withStatus(Response.StatusType status) {
            Objects.requireNonNull(status);
            this.statusCode = status.getStatusCode();
            return this;
        }

        public Builder withStatus(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder withDetail(@Nullable String detail) {
            this.detail = detail;
            return this;
        }

        public Builder withInstance(@Nullable URI instance) {
            this.instance = instance;
            return this;
        }

        public Builder withHeader(String headerName, Object value) {
            headers.put(headerName, value);
            return this;
        }

        /**
         * Adds custom property to the response.
         *
         * @throws IllegalArgumentException if key is any of type, title, status, detail or instance
         */
        public Builder with(String key, Object value) throws IllegalArgumentException {
            if (RESERVED_PROPERTIES.contains(key)) {
                throw new IllegalArgumentException("Property " + key + " is reserved");
            }
            parameters.put(key, value);
            return this;
        }

        public HttpProblem build() {
            return new HttpProblem(this);
        }

    }

}
