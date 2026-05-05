package io.quarkiverse.httpproblem.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.annotation.Priority;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import io.quarkiverse.httpproblem.ExceptionMapperBase;
import io.quarkiverse.httpproblem.ProblemRuntimeConfig.ConstraintViolationMapperConfig;

/**
 * Exception Mapper for ConstraintViolationException from Bean Validation API. Hibernate Validator, among others throw
 * these exceptions. Adds 'violations' field into `application/problem` responses.
 */
@Priority(Priorities.USER)
@APIResponse(responseCode = ConstraintViolationExceptionMapper.HTTP_VALIDATION_PROBLEM_STATUS_CODE)
public final class ConstraintViolationExceptionMapper extends ExceptionMapperBase<ConstraintViolationException>
        implements ExceptionMapper<ConstraintViolationException> {

    /**
     * APIResponse annotations' responseCode must be set according to the configuration, it can't be hardcoded as for
     * other mappers. OpenApiProblemFilter handles this accordingly.
     */
    public static final String HTTP_VALIDATION_PROBLEM_STATUS_CODE = "[HttpValidationProblem]";

    private static final List<ParamSpec<?>> PARAM_SPECS = Stream.of(
            ParamSpec.of("jakarta.ws.rs.QueryParam", Violation.In.query),
            ParamSpec.of("jakarta.ws.rs.PathParam", Violation.In.path),
            ParamSpec.of("jakarta.ws.rs.HeaderParam", Violation.In.header),
            ParamSpec.of("jakarta.ws.rs.FormParam", Violation.In.form),
            ParamSpec.of("org.jboss.resteasy.reactive.RestQuery", Violation.In.query),
            ParamSpec.of("org.jboss.resteasy.reactive.RestPath", Violation.In.path),
            ParamSpec.of("org.jboss.resteasy.reactive.RestHeader", Violation.In.header),
            ParamSpec.of("org.jboss.resteasy.reactive.RestForm", Violation.In.form))
            .flatMap(Optional::stream)
            .toList();

    private static ConstraintViolationMapperConfig config = ConstraintViolationMapperConfig.defaults();

    @Context
    ResourceInfo resourceInfo;

    public static void configure(ConstraintViolationMapperConfig config) {
        ConstraintViolationExceptionMapper.config = config;
    }

    @Override
    protected HttpValidationProblem toProblem(ConstraintViolationException exception) {
        return new HttpValidationProblem(config.status(), config.title(), toViolations(exception.getConstraintViolations()));
    }

    private List<Violation> toViolations(Set<ConstraintViolation<?>> constraintViolations) {
        if (constraintViolations == null) {
            return List.of();
        }
        return constraintViolations
                .stream()
                .map(this::toViolation)
                .collect(Collectors.toList());
    }

    private Violation toViolation(ConstraintViolation<?> constraintViolation) {
        return matchEndpointMethodParameter(constraintViolation)
                .map(param -> createViolation(constraintViolation, param))
                .orElseGet(() -> {
                    String field = isDeclarativeValidation(constraintViolation)
                            ? dropMethodName(constraintViolation.getPropertyPath())
                            : constraintViolation.getPropertyPath().toString();
                    return Violation.In.unknown
                            .field(field)
                            .message(constraintViolation.getMessage());
                });

    }

    private Optional<Parameter> matchEndpointMethodParameter(ConstraintViolation<?> violation) {
        if (resourceInfo == null) {
            return Optional.empty();
        }

        Iterator<Path.Node> propertyPathIterator = violation.getPropertyPath().iterator();
        if (!propertyPathIterator.hasNext()) {
            return Optional.empty();
        }
        propertyPathIterator.next();
        if (!propertyPathIterator.hasNext()) {
            return Optional.empty();
        }
        String paramName = propertyPathIterator.next().getName();

        return Optional.ofNullable(resourceInfo.getResourceMethod())
                .flatMap(method -> findParameterInHierarchy(method, paramName));
    }

    private Optional<Parameter> findParameterInHierarchy(Method method, String paramName) {
        return Stream.concat(Stream.of(method), interfaceMethods(method))
                .flatMap(m -> Stream.of(m.getParameters()))
                .filter(p -> p.getName().equals(paramName))
                .filter(this::hasKnownParamAnnotation)
                .findFirst()
                .or(() -> findParameterByName(method, paramName));
    }

    private Stream<Method> interfaceMethods(Method method) {
        return Stream.of(method.getDeclaringClass().getInterfaces())
                .map(iface -> findMethodInInterface(iface, method))
                .flatMap(Optional::stream);
    }

    private Optional<Method> findMethodInInterface(Class<?> iface, Method method) {
        try {
            return Optional.of(iface.getMethod(method.getName(), method.getParameterTypes()));
        } catch (NoSuchMethodException ignored) {
            return Optional.empty();
        }
    }

    private Optional<Parameter> findParameterByName(Method method, String paramName) {
        return Stream.of(method.getParameters())
                .filter(param -> param.getName().equals(paramName))
                .findFirst();
    }

    private boolean hasKnownParamAnnotation(Parameter param) {
        return PARAM_SPECS.stream().anyMatch(spec -> spec.isPresent(param));
    }

    private Violation createViolation(ConstraintViolation<?> constraintViolation, Parameter param) {
        final String message = constraintViolation.getMessage();
        final String fieldName = dropMethodNameAndArgumentPositionFromPath(constraintViolation.getPropertyPath());
        final AnnotatedElement element = isBeanParam(param)
                ? findField(param.getType(), fieldName).orElse(null)
                : param;
        return Optional.ofNullable(element)
                .flatMap(e -> PARAM_SPECS.stream()
                        .filter(spec -> spec.isPresent(e))
                        .findFirst()
                        .map(spec -> spec.toViolation(e, message)))
                .orElseGet(() -> Violation.In.body.field(fieldName).message(message));
    }

    private boolean isBeanParam(Parameter param) {
        return param.isAnnotationPresent(jakarta.ws.rs.BeanParam.class);
    }

    private Optional<Field> findField(Class<?> beanClass, String fieldName) {
        try {
            return Optional.of(beanClass.getDeclaredField(fieldName));
        } catch (NoSuchFieldException ignored) {
            return Optional.empty();
        }
    }

    private String dropMethodNameAndArgumentPositionFromPath(Path propertyPath) {
        return serializePath(propertyPath, 2);
    }

    private String dropMethodName(Path propertyPath) {
        return serializePath(propertyPath, 1);
    }

    private String serializePath(Path propertyPath, int skipFirstSegments) {
        Iterator<Path.Node> segmentIterator = propertyPath.iterator();

        List<String> pathSegments = new ArrayList<>();
        while (segmentIterator.hasNext()) {
            if (skipFirstSegments > 0) {
                skipFirstSegments -= 1;
                segmentIterator.next();
            } else {
                String segment = segmentIterator.next().toString();
                if (!segment.isBlank()) {
                    pathSegments.add(segment);
                }
            }
        }
        return String.join(".", pathSegments);
    }

    private boolean isDeclarativeValidation(ConstraintViolation<?> violation) {
        return hasJaxRsContext() &&
                (rootBeanResourceClassMatches(violation) || propertyPathStartsWithMethod(violation));
    }

    private boolean hasJaxRsContext() {
        return resourceInfo != null;
    }

    private boolean rootBeanResourceClassMatches(ConstraintViolation<?> violation) {
        Object rootBean = violation.getRootBean();
        return rootBean != null && resourceInfo.getResourceClass().isInstance(rootBean);
    }

    private boolean propertyPathStartsWithMethod(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        Method method = resourceInfo.getResourceMethod();
        return method != null && propertyPath.startsWith(method.getName() + ".");
    }

    private static final class ParamSpec<A extends Annotation> {

        private final Class<A> annotationType;
        private final Violation.In location;
        private final Function<A, String> fieldExtractor;

        private ParamSpec(Class<A> annotationType, Violation.In location, Function<A, String> fieldExtractor) {
            this.annotationType = annotationType;
            this.location = location;
            this.fieldExtractor = fieldExtractor;
        }

        @SuppressWarnings("unchecked")
        static <A extends Annotation> Optional<ParamSpec<?>> of(String className, Violation.In location) {
            try {
                Class<A> annotationType = (Class<A>) Class.forName(className);
                java.lang.reflect.Method valueMethod = annotationType.getMethod("value");
                Function<A, String> fieldExtractor = ann -> {
                    try {
                        return (String) valueMethod.invoke(ann);
                    } catch (Exception e) {
                        return "";
                    }
                };
                return Optional.of(new ParamSpec<>(annotationType, location, fieldExtractor));
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {
                return Optional.empty();
            }
        }

        boolean isPresent(AnnotatedElement element) {
            return element.getAnnotation(annotationType) != null;
        }

        Violation toViolation(AnnotatedElement element, String message) {
            String field = fieldExtractor.apply(element.getAnnotation(annotationType));
            if (field.isEmpty()) {
                field = element instanceof Parameter ? ((Parameter) element).getName()
                        : ((Field) element).getName();
            }
            return location.field(field).message(message);
        }
    }

}
