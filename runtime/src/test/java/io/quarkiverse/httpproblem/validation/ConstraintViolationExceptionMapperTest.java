package io.quarkiverse.httpproblem.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ParameterNameProvider;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Response;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.spi.nodenameprovider.JavaBeanProperty;
import org.hibernate.validator.spi.nodenameprovider.Property;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestHeader;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import io.quarkiverse.httpproblem.HttpProblem;

class ConstraintViolationExceptionMapperTest {

    public static final String TOO_SHORT_CITY = "A";
    final String INVALID = "a";
    final String VALID = "aaaaaaa";
    final String TOO_SHORT_COMPANY_NAME = "CO";

    final ConstraintViolationExceptionMapper mapper = new ConstraintViolationExceptionMapper();
    final StubResourceInfo resourceInfo = StubResourceInfo.withDefaultValidator();

    @BeforeEach
    void setup() {
        mapper.resourceInfo = resourceInfo;
    }

    @Test
    void nullConstraintViolationsSetShouldNotCrash() {
        ConstraintViolationException exception = new ConstraintViolationException("omg!", null);

        List<Violation> violations = mapAndExtractViolations(exception);

        assertThat(violations).isEmpty();
    }

    @Test
    void invalidPathParamShouldBeReported() {
        ConstraintViolationException exception = resourceInfo.validateParameters(INVALID, VALID, VALID, VALID,
                RequestBody.valid());

        List<Violation> violations = mapAndExtractViolations(exception);

        assertThat(violations)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(Violation.In.path.field("param_name").message("length must be between 2 and 10"));
    }

    @Test
    void invalidQueryParamShouldBeReported() {
        ConstraintViolationException exception = resourceInfo.validateParameters(VALID, INVALID, VALID, VALID,
                RequestBody.valid());

        List<Violation> violations = mapAndExtractViolations(exception);

        assertThat(violations)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(Violation.In.query.field("param_name").message("length must be between 3 and 10"));
    }

    @Test
    void invalidHeaderParamShouldBeReported() {
        ConstraintViolationException exception = resourceInfo.validateParameters(VALID, VALID, INVALID, VALID,
                RequestBody.valid());

        List<Violation> violations = mapAndExtractViolations(exception);

        assertThat(violations)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(Violation.In.header.field("param_name").message("length must be between 4 and 10"));
    }

    @Test
    void invalidFormParamShouldBeReported() {
        ConstraintViolationException exception = resourceInfo.validateParameters(VALID, VALID, VALID, INVALID,
                RequestBody.valid());

        List<Violation> violations = mapAndExtractViolations(exception);

        assertThat(violations)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(Violation.In.form.field("param_name").message("length must be between 5 and 10"));
    }

    @Test
    void invalidBodyShouldBeReported() {
        ConstraintViolationException exception = resourceInfo.validateParameters(VALID, VALID, VALID, VALID,
                RequestBody.invalid());

        List<Violation> violations = mapAndExtractViolations(exception);

        assertThat(violations)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        Violation.In.body.field("param_name")
                                .message("length must be between 6 and 10"),
                        Violation.In.body.field("items[1].param_name")
                                .message("length must be between 7 and 10"),
                        Violation.In.body.field("items2")
                                .message("size must be between 1 and 10"),
                        Violation.In.body.field("inner.param_name")
                                .message("length must be between 8 and 10"));
    }

    @Test
    void shouldIncludeAllViolations() {
        ConstraintViolationException exception = resourceInfo.validateParameters(INVALID, INVALID, INVALID, INVALID,
                RequestBody.invalid());

        List<Violation> violations = mapAndExtractViolations(exception);

        assertThat(violations).hasSize(8);
    }

    @Test
    void shouldUseJsonPropertyNamingStrategy() {
        StubResourceInfo resourceInfo = StubResourceInfo.withJsonPropertyAwareValidator();
        mapper.resourceInfo = resourceInfo;
        ConstraintViolationException exception = resourceInfo.validateParameters(VALID, VALID, VALID, VALID,
                RequestBody.invalid());

        List<Violation> violations = mapAndExtractViolations(exception);
        assertThat(violations)
                .usingRecursiveFieldByFieldElementComparator()
                .contains(
                        Violation.In.body.field("param_name_from_annotation")
                                .message("length must be between 6 and 10"),
                        Violation.In.body.field("items[1].param_name_from_annotation")
                                .message("length must be between 7 and 10"),
                        Violation.In.body.field("inner.param_name_from_annotation")
                                .message("length must be between 8 and 10"),
                        Violation.In.body.field("items2")
                                .message("size must be between 1 and 10"));
    }

    @Test
    void customParameterNamingStrategyShouldBeTolerated() {
        StubResourceInfo resourceInfo = StubResourceInfo.withCustomParameterNameProvider();
        mapper.resourceInfo = resourceInfo;
        ConstraintViolationException exception = resourceInfo.validateParameters(INVALID, VALID, VALID, VALID,
                RequestBody.invalid());

        List<Violation> violations = mapAndExtractViolations(exception);
        assertThat(violations)
                .usingRecursiveFieldByFieldElementComparator()
                .contains(
                        Violation.In.unknown.field("firstParam")
                                .message("length must be between 2 and 10"),
                        Violation.In.unknown.field("fifthParam.param_name")
                                .message("length must be between 6 and 10"),
                        Violation.In.unknown.field("fifthParam.items[1].param_name")
                                .message("length must be between 7 and 10"),
                        Violation.In.unknown.field("fifthParam.inner.param_name")
                                .message("length must be between 8 and 10"));
    }

    @Test
    void programmaticConstraintViolationShouldNotStripMethodNames() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        ProgrammaticTestBean bean = new ProgrammaticTestBean();
        bean.name = null;
        bean.email = "invalid-email";

        Set<ConstraintViolation<ProgrammaticTestBean>> violations = validator.validate(bean);
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ConstraintViolationExceptionMapper resourceInfoLessMapper = new ConstraintViolationExceptionMapper();
        resourceInfoLessMapper.resourceInfo = null;

        List<Violation> mappedViolations = mapAndExtractViolations(exception, resourceInfoLessMapper);

        assertThat(mappedViolations)
                .hasSize(2)
                .extracting(v -> v.field)
                .containsExactlyInAnyOrder("name", "email");
    }

    @Test
    void programmaticNestedConstraintViolationShouldPreservePropertyPath() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        ProgrammaticNestedTestBean bean = new ProgrammaticNestedTestBean();
        bean.companyName = TOO_SHORT_COMPANY_NAME;
        bean.address = new ProgrammaticTestAddress();
        bean.address.street = "";
        bean.address.city = TOO_SHORT_CITY;

        Set<ConstraintViolation<ProgrammaticNestedTestBean>> violations = validator.validate(bean);
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ConstraintViolationExceptionMapper resourceInfoLessMapper = new ConstraintViolationExceptionMapper();
        resourceInfoLessMapper.resourceInfo = null;

        List<Violation> mappedViolations = mapAndExtractViolations(exception, resourceInfoLessMapper);

        assertThat(mappedViolations)
                .hasSize(3)
                .extracting(v -> v.field)
                .containsExactlyInAnyOrder("companyName", "address.street", "address.city");
    }

    @Test
    void declarativeConstraintViolationShouldStripMethodNames() {
        ConstraintViolationException exception = resourceInfo.validateParameters(VALID, VALID, VALID, VALID,
                RequestBody.invalid());

        List<Violation> violations = mapAndExtractViolations(exception);

        assertThat(violations)
                .extracting(v -> v.field)
                .allMatch(field -> !field.contains("validateParameters"));
    }

    private List<Violation> mapAndExtractViolations(ConstraintViolationException exception) {
        return mapAndExtractViolations(exception, mapper);
    }

    private List<Violation> mapAndExtractViolations(ConstraintViolationException exception,
            ConstraintViolationExceptionMapper mapper) {
        Response response = mapper.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getMediaType()).isEqualTo(HttpProblem.MEDIA_TYPE);
        HttpProblem problem = (HttpProblem) response.getEntity();
        return (List<Violation>) problem.getParameters().get("violations");
    }

    @Test
    void shouldReportCorrectFieldAndLocationWhenConstraintsDefinedOnInterface() throws NoSuchMethodException {
        Validator validator = Validation.byProvider(HibernateValidator.class)
                .configure()
                .buildValidatorFactory()
                .getValidator();

        SearchServiceImpl impl = new SearchServiceImpl();
        Method implMethod = SearchServiceImpl.class.getMethod("search", String.class);

        Set<ConstraintViolation<SearchServiceImpl>> constraintViolations = validator.forExecutables()
                .validateParameters(impl, implMethod, new Object[] { "short" });
        ConstraintViolationException exception = new ConstraintViolationException(constraintViolations);

        ConstraintViolationExceptionMapper testMapper = new ConstraintViolationExceptionMapper();
        testMapper.resourceInfo = new ResourceInfo() {
            @Override
            public Method getResourceMethod() {
                return implMethod;
            }

            @Override
            public Class<?> getResourceClass() {
                return SearchServiceImpl.class;
            }
        };

        List<Violation> violations = mapAndExtractViolations(exception, testMapper);

        assertThat(violations)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(Violation.In.query.field("query").message("size must be between 10 and 128"));
    }

    static class StubResourceInfo implements ResourceInfo {

        private final Validator validator;

        StubResourceInfo(Validator validator) {
            this.validator = validator;
        }

        public static StubResourceInfo withDefaultValidator() {
            return new StubResourceInfo(Validation.byProvider(HibernateValidator.class)
                    .configure()
                    .buildValidatorFactory()
                    .getValidator());
        }

        public static StubResourceInfo withJsonPropertyAwareValidator() {
            return new StubResourceInfo(Validation.byProvider(HibernateValidator.class)
                    .configure()
                    .propertyNodeNameProvider(new JacksonPropertyNodeNameProvider())
                    .buildValidatorFactory()
                    .getValidator());
        }

        public static StubResourceInfo withCustomParameterNameProvider() {
            return new StubResourceInfo(Validation.byProvider(HibernateValidator.class)
                    .configure()
                    .parameterNameProvider(new ParameterNameProvider() {
                        private final List<String> hardcodedNames = Arrays.asList("firstParam", "secondParam", "thirdParam",
                                "forthParam", "fifthParam");

                        @Override
                        public List<String> getParameterNames(Constructor<?> constructor) {
                            return hardcodedNames;
                        }

                        @Override
                        public List<String> getParameterNames(Method method) {
                            return hardcodedNames;
                        }
                    })
                    .buildValidatorFactory()
                    .getValidator());
        }

        @Path("/{param_name}")
        public ConstraintViolationException validateParameters(
                @Valid @PathParam("param_name") @Length(min = 2, max = 10) String pathParam,
                @Valid @QueryParam("param_name") @Length(min = 3, max = 10) String queryParam,
                @Valid @HeaderParam("param_name") @Length(min = 4, max = 10) String headerParam,
                @Valid @FormParam("param_name") @Length(min = 5, max = 10) String formParam,
                @Valid RequestBody requestBody) {

            Set<ConstraintViolation<StubResourceInfo>> violations = validator.forExecutables().validateParameters(
                    this,
                    this.getResourceMethod(),
                    new Object[] { pathParam, queryParam, headerParam, formParam, requestBody });
            return new ConstraintViolationException(violations);
        }

        @Override
        public Method getResourceMethod() {
            try {
                return this.getClass().getMethod("validateParameters", String.class, String.class, String.class,
                        String.class, RequestBody.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Class<?> getResourceClass() {
            return this.getClass();
        }

    }

    static class RequestBody {

        static RequestBody valid() {
            RequestBody validBody = new RequestBody();
            validBody.param_name = "1234567";
            validBody.items = Collections.singletonList(Item.valid());
            validBody.items2 = Collections.singletonList(Item.valid());
            validBody.inner.param_name = "123456789";
            return validBody;
        }

        static RequestBody invalid() {
            RequestBody invalidBody = new RequestBody();
            invalidBody.items = Arrays.asList(
                    Item.valid(),
                    Item.invalid());
            return invalidBody;
        }

        @Length(min = 6, max = 10)
        @JsonProperty("param_name_from_annotation")
        String param_name = "";

        @Valid
        List<Item> items;

        @Size(min = 1, max = 10)
        List<Item> items2 = Collections.emptyList();

        static class Item {

            static Item valid() {
                Item item = new Item();
                item.param_name = "12345678";
                return item;
            }

            static Item invalid() {
                return new Item();
            }

            @Length(min = 7, max = 10)
            @JsonProperty("param_name_from_annotation")
            String param_name = "";
        }

        @Valid
        InnerStubBody inner = new InnerStubBody();

        static class InnerStubBody {
            @Length(min = 8, max = 10)
            @JsonProperty("param_name_from_annotation")
            String param_name = "";
        }
    }

    /**
     * Validation takes @JsonProperty annotations into account.
     */
    static class JacksonPropertyNodeNameProvider implements PropertyNodeNameProvider {

        ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public String getName(Property property) {
            if (property instanceof JavaBeanProperty) {
                return getJavaBeanPropertyName((JavaBeanProperty) property);
            }

            return getDefaultName(property);
        }

        private String getJavaBeanPropertyName(JavaBeanProperty property) {
            JavaType type = objectMapper.constructType(property.getDeclaringClass());
            BeanDescription desc = objectMapper.getSerializationConfig().introspect(type);

            return desc.findProperties()
                    .stream()
                    .filter(prop -> prop.getInternalName().equals(property.getName()))
                    .map(BeanPropertyDefinition::getName)
                    .findFirst()
                    .orElse(property.getName());
        }

        private String getDefaultName(Property property) {
            return property.getName();
        }
    }

    static class ProgrammaticTestBean {
        @NotNull
        @Length(min = 2, max = 50)
        public String name;

        @NotNull
        @jakarta.validation.constraints.Email
        public String email;
    }

    static class ProgrammaticNestedTestBean {
        @NotNull
        @Length(min = 3, max = 100)
        public String companyName;

        @jakarta.validation.Valid
        @NotNull
        public ProgrammaticTestAddress address;
    }

    static class ProgrammaticTestAddress {
        @NotNull
        @Length(min = 5, max = 200)
        public String street;

        @NotNull
        @Length(min = 2, max = 100)
        public String city;
    }

    interface SearchResource {
        void search(@QueryParam("query") @Size(min = 10, max = 128) String query);
    }

    static class SearchServiceImpl implements SearchResource {
        @Override
        public void search(String query) {
        }
    }

    static Stream<Arguments> reactiveParamViolations() {
        return Stream.of(
                Arguments.of(new Object[] { "x", "valid", "valid", "valid" }, Violation.In.query, "name"),
                Arguments.of(new Object[] { "valid", "x", "valid", "valid" }, Violation.In.header, "header"),
                Arguments.of(new Object[] { "valid", "valid", "x", "valid" }, Violation.In.path, "id"),
                Arguments.of(new Object[] { "valid", "valid", "valid", "x" }, Violation.In.form, "formField"));
    }

    @ParameterizedTest
    @MethodSource("reactiveParamViolations")
    void shouldReportCorrectLocationForRestAnnotations(Object[] args, Violation.In expectedIn,
            String expectedField) throws NoSuchMethodException {
        Validator validator = Validation.byProvider(HibernateValidator.class).configure().buildValidatorFactory()
                .getValidator();
        ReactiveParamResource resource = new ReactiveParamResource();
        Method method = ReactiveParamResource.class.getMethod("list", String.class, String.class, String.class,
                String.class);
        Set<ConstraintViolation<ReactiveParamResource>> violations = validator.forExecutables()
                .validateParameters(resource, method, args);
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ConstraintViolationExceptionMapper testMapper = new ConstraintViolationExceptionMapper();
        testMapper.resourceInfo = new ResourceInfo() {
            @Override
            public Method getResourceMethod() {
                return method;
            }

            @Override
            public Class<?> getResourceClass() {
                return ReactiveParamResource.class;
            }
        };

        List<Violation> mappedViolations = mapAndExtractViolations(exception, testMapper);

        assertThat(mappedViolations)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(expectedIn.field(expectedField).message("size must be between 5 and 2147483647"));
    }

    static class ReactiveParamResource {
        public void list(
                @Size(min = 5) @RestQuery String name,
                @Size(min = 5) @RestHeader String header,
                @Size(min = 5) @RestPath String id,
                @Size(min = 5) @RestForm String formField) {
        }
    }

    static Stream<Arguments> beanParamViolations() {
        return Stream.of(
                Arguments.of(new MixedBeanParam("x", "valid", "valid", "valid"), Violation.In.query, "page_size"),
                Arguments.of(new MixedBeanParam("valid", "x", "valid", "valid"), Violation.In.path, "resource_id"),
                Arguments.of(new MixedBeanParam("valid", "valid", "x", "valid"), Violation.In.header, "x_token"),
                Arguments.of(new MixedBeanParam("valid", "valid", "valid", "x"), Violation.In.form, "form_data"));
    }

    @ParameterizedTest
    @MethodSource("beanParamViolations")
    void shouldReportCorrectLocationForParamInsideBeanParam(MixedBeanParam beanParam, Violation.In expectedIn,
            String expectedField) throws NoSuchMethodException {
        Validator validator = Validation.byProvider(HibernateValidator.class).configure().buildValidatorFactory()
                .getValidator();
        BeanParamResource resource = new BeanParamResource();
        Method method = BeanParamResource.class.getMethod("list", MixedBeanParam.class);
        Set<ConstraintViolation<BeanParamResource>> violations = validator.forExecutables()
                .validateParameters(resource, method, new Object[] { beanParam });
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ConstraintViolationExceptionMapper testMapper = new ConstraintViolationExceptionMapper();
        testMapper.resourceInfo = new ResourceInfo() {
            @Override
            public Method getResourceMethod() {
                return method;
            }

            @Override
            public Class<?> getResourceClass() {
                return BeanParamResource.class;
            }
        };

        List<Violation> mappedViolations = mapAndExtractViolations(exception, testMapper);

        assertThat(mappedViolations)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(expectedIn.field(expectedField).message("size must be between 5 and 2147483647"));
    }

    static class BeanParamResource {
        public void list(@Valid @BeanParam MixedBeanParam params) {
        }
    }

    static class MixedBeanParam {
        @Size(min = 5)
        @QueryParam("page_size")
        String size;

        @Size(min = 5)
        @PathParam("resource_id")
        String id;

        @Size(min = 5)
        @HeaderParam("x_token")
        String token;

        @Size(min = 5)
        @FormParam("form_data")
        String data;

        MixedBeanParam(String size, String id, String token, String data) {
            this.size = size;
            this.id = id;
            this.token = token;
            this.data = data;
        }
    }
}
