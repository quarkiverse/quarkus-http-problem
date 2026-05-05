package io.quarkiverse.httpproblem.deployment;

import static io.quarkiverse.httpproblem.deployment.ExceptionMapperDefinition.mapper;
import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;
import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.Priorities;

import org.eclipse.microprofile.openapi.OASFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkiverse.httpproblem.postprocessing.ProblemPostProcessor;
import io.quarkiverse.httpproblem.postprocessing.ProblemRecorder;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.LiveReloadBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.jsonb.spi.JsonbDeserializerBuildItem;
import io.quarkus.jsonb.spi.JsonbSerializerBuildItem;
import io.quarkus.resteasy.reactive.spi.CustomExceptionMapperBuildItem;
import io.quarkus.resteasy.reactive.spi.ExceptionMapperBuildItem;
import io.quarkus.smallrye.openapi.deployment.spi.AddToOpenAPIDefinitionBuildItem;

public class ProblemProcessor {

    private static final String FEATURE_NAME = "http-problem";
    private static final String EXTENSION_MAIN_PACKAGE = "io.quarkiverse.httpproblem.";

    /**
     * Don't change this to constants from Capability for the sake of older Quarkus versions
     */
    private static final List<String> REST_JSON_CAPABILITIES = Arrays.asList(
            "io.quarkus.jsonb",
            "io.quarkus.jackson");

    private static List<ExceptionMapperDefinition> neededExceptionMappers() {
        Stream<ExceptionMapperDefinition> allMappers = Stream.of(
                mapper(EXTENSION_MAIN_PACKAGE + "HttpProblemMapper")
                        .thatHandles(EXTENSION_MAIN_PACKAGE + "HttpProblem"),

                mapper(EXTENSION_MAIN_PACKAGE + "jaxrs.WebApplicationExceptionMapper")
                        .thatHandles("jakarta.ws.rs.WebApplicationException"),
                mapper(EXTENSION_MAIN_PACKAGE + "jaxrs.JaxRsForbiddenExceptionMapper")
                        .thatHandles("jakarta.ws.rs.ForbiddenException"),
                mapper(EXTENSION_MAIN_PACKAGE + "jaxrs.NotFoundExceptionMapper")
                        .thatHandles("jakarta.ws.rs.NotFoundException"),

                mapper(EXTENSION_MAIN_PACKAGE + "security.AuthenticationFailedExceptionMapper")
                        .thatHandles("io.quarkus.security.AuthenticationFailedException"),
                mapper(EXTENSION_MAIN_PACKAGE + "security.AuthenticationRedirectExceptionMapper")
                        .thatHandles("io.quarkus.security.AuthenticationRedirectException"),
                mapper(EXTENSION_MAIN_PACKAGE + "security.AuthenticationCompletionExceptionMapper")
                        .thatHandles("io.quarkus.security.AuthenticationCompletionException"),
                mapper(EXTENSION_MAIN_PACKAGE + "security.ForbiddenExceptionMapper")
                        .thatHandles("io.quarkus.security.ForbiddenException"),

                mapper(EXTENSION_MAIN_PACKAGE + "validation.ValidationExceptionMapper")
                        .thatHandles("jakarta.validation.ValidationException"),

                mapper(EXTENSION_MAIN_PACKAGE + "validation.ConstraintViolationExceptionMapper")
                        .thatHandles("jakarta.validation.ConstraintViolationException"),

                mapper(EXTENSION_MAIN_PACKAGE + "jackson.JsonProcessingExceptionMapper")
                        .thatHandles("com.fasterxml.jackson.core.JsonProcessingException")
                        .onlyIf(new JacksonDetector()),
                mapper(EXTENSION_MAIN_PACKAGE + "jackson.UnrecognizedPropertyExceptionMapper")
                        .thatHandles("com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException")
                        .onlyIf(new JacksonDetector()),
                mapper(EXTENSION_MAIN_PACKAGE + "jackson.InvalidFormatExceptionMapper")
                        .thatHandles("com.fasterxml.jackson.databind.exc.InvalidFormatException")
                        .onlyIf(new JacksonDetector()),

                mapper(EXTENSION_MAIN_PACKAGE + "jsonb.RestEasyClassicJsonbExceptionMapper")
                        .thatHandles("jakarta.ws.rs.ProcessingException")
                        .onlyIf(new JsonBDetector()),

                mapper(EXTENSION_MAIN_PACKAGE + "jsonb.JsonbExceptionMapper")
                        .thatHandles("jakarta.json.bind.JsonbException")
                        .onlyIf(new JsonBDetector()),

                mapper(EXTENSION_MAIN_PACKAGE + "ZalandoProblemMapper")
                        .thatHandles("org.zalando.problem.ThrowableProblem"),

                mapper(EXTENSION_MAIN_PACKAGE + "DefaultExceptionMapper")
                        .thatHandles("java.lang.Exception"));

        return allMappers
                .filter(ExceptionMapperDefinition::isNeeded)
                .collect(Collectors.toList());
    }

    @BuildStep
    FeatureBuildItem createFeature(Capabilities capabilities) {
        if (REST_JSON_CAPABILITIES.stream().noneMatch(capabilities::isPresent)) {
            logger().error("`quarkus-http-problem` extension is useless without json provider. Please add "
                    + "`quarkus-rest-jackson` or `quarkus-rest-jsonb` extension to your project.");
        }
        return new FeatureBuildItem(FEATURE_NAME);
    }

    @BuildStep
    void registerMappers(BuildProducer<ExceptionMapperBuildItem> providers) {
        neededExceptionMappers().forEach(mapper -> providers.produce(
                new ExceptionMapperBuildItem(mapper.mapperClassName,
                        mapper.exceptionClassName, Priorities.AUTHENTICATION - 1, true)));
    }

    // Unauthorized/AuthenticationFailed handling in Quarkus REST is reactive: it needs Vert.x RoutingContext and
    // asynchronous challenge/response resolution (Uni<Response>). These classes therefore use
    // @ServerExceptionMapper methods (not JAX-RS ExceptionMapper<T> providers) and must be registered as custom mappers.
    @BuildStep
    void registerCustomExceptionMappers(BuildProducer<CustomExceptionMapperBuildItem> customExceptionMapper) {
        customExceptionMapper.produce(
                new CustomExceptionMapperBuildItem(EXTENSION_MAIN_PACKAGE + "security.UnauthorizedExceptionReactiveMapper"));
        customExceptionMapper.produce(new CustomExceptionMapperBuildItem(
                EXTENSION_MAIN_PACKAGE + "security.AuthenticationFailedExceptionReactiveMapper"));
    }

    @BuildStep(onlyIf = JacksonDetector.class)
    void registerJacksonItems(BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        additionalBeans.produce(new AdditionalBeanBuildItem(
                EXTENSION_MAIN_PACKAGE + "jackson.JacksonProblemModuleRegistrar"));
    }

    @BuildStep(onlyIf = JsonBDetector.class)
    void registerJsonbItems(BuildProducer<JsonbSerializerBuildItem> serializers,
            BuildProducer<JsonbDeserializerBuildItem> deserializers) {
        serializers.produce(
                new JsonbSerializerBuildItem(EXTENSION_MAIN_PACKAGE + "jsonb.JsonbProblemSerializer"));
        deserializers.produce(
                new JsonbDeserializerBuildItem(EXTENSION_MAIN_PACKAGE + "jsonb.JsonbProblemDeserializer"));
    }

    /**
     * Force jandex indexing for runtime module classes so that @Schema annotated classes can be picked up by OpenApi.
     * It's an equivalent to adding beans.xml to runtime's module resources, but this has advantage of being enabled
     * conditionally: only if openapi is in the classpath.
     */
    @BuildStep(onlyIf = OpenApiDetector.class)
    void indexOpenApiClasses(BuildProducer<IndexDependencyBuildItem> indexDependency) {
        indexDependency.produce(new IndexDependencyBuildItem("io.quarkiverse.http-problem", "quarkus-http-problem"));
    }

    @BuildStep(onlyIf = OpenApiDetector.class)
    void registerOpenApiFilter(BuildProducer<AddToOpenAPIDefinitionBuildItem> openAPIProducer, ProblemBuildConfig config) {
        OASFilter filter = new OpenApiProblemFilter(config);
        openAPIProducer.produce(new AddToOpenAPIDefinitionBuildItem(filter));
    }

    @BuildStep
    ReflectiveClassBuildItem registerPojosForReflection() {
        return ReflectiveClassBuildItem
                .builder(EXTENSION_MAIN_PACKAGE + "validation.Violation")
                .methods()
                .fields()
                .build();
    }

    @Record(STATIC_INIT)
    @BuildStep
    void resetRecorder(ProblemRecorder recorder, LiveReloadBuildItem liveReload) {
        if (liveReload.isLiveReload()) {
            recorder.reset();
        }
    }

    @Record(RUNTIME_INIT)
    @BuildStep
    void setupMdc(ProblemRecorder recorder, ProblemBuildConfig config) {
        recorder.configureMdc(config.includeMdcProperties());
    }

    @Record(RUNTIME_INIT)
    @BuildStep
    void registerCustomPostProcessors(ProblemRecorder recorder) {
        recorder.registerCustomPostProcessors();
    }

    @Record(RUNTIME_INIT)
    @BuildStep
    void configureConstraintViolationMapper(ProblemRecorder recorder, ProblemBuildConfig config) {
        if (config.constraintViolation() != null) {
            recorder.configureConstraintViolationMapper(config.constraintViolation().status(),
                    config.constraintViolation().title());
        }
    }

    @BuildStep
    UnremovableBeanBuildItem markPostProcessorsUnremovable() {
        return UnremovableBeanBuildItem.beanTypes(ProblemPostProcessor.class);
    }

    protected Logger logger() {
        return LoggerFactory.getLogger(FEATURE_NAME);
    }
}
