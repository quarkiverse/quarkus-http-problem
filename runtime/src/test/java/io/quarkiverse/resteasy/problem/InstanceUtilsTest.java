package io.quarkiverse.resteasy.problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InstanceUtilsTest {

    @Test
    void shouldConvertSimplePathToInstance() {
        assertThat(InstanceUtils.pathToInstance("/api/items/123"))
                .isEqualTo(URI.create("/api/items/123"));
    }

    @Test
    void shouldEncodeSpaceInPathSegment() {
        assertThat(InstanceUtils.pathToInstance("/api/items/X 1/details"))
                .isEqualTo(URI.create("/api/items/X%201/details"));
    }

    @Test
    void shouldReturnNullForNullPath() {
        assertThat(InstanceUtils.pathToInstance(null)).isNull();
    }

    @Test
    void shouldSerializeSimpleInstanceAsPath() {
        assertThat(InstanceUtils.instanceToPath(URI.create("/api/items/123")))
                .isEqualTo("/api/items/123");
    }

    @Test
    void shouldSerializeEncodedInstancePreservingEncoding() {
        URI instance = InstanceUtils.pathToInstance("/api/items/X 1/details");
        assertThat(InstanceUtils.instanceToPath(instance))
                .isEqualTo("/api/items/X%201/details");
    }

    @Test
    void shouldRoundTripEncodedInstance() {
        URI instance = InstanceUtils.pathToInstance("/api/items/X 1/details");
        String serialized = InstanceUtils.instanceToPath(instance);
        assertThat(URI.create(serialized))
                .isEqualTo(instance);
    }

    @Test
    void shouldSerializeFullUriInstance() {
        URI instance = URI.create("https://example.com/api/items/123");
        assertThat(InstanceUtils.instanceToPath(instance))
                .isEqualTo("https://example.com/api/items/123");
    }

    @ParameterizedTest(name = "''{0}'' encoded as ''{1}''")
    @CsvSource({
            "{, %7B",
            "}, %7D",
            "|, %7C",
            "\\, %5C",
            "^, %5E",
            "[, %5B",
            "], %5D",
            "`, %60",
    })
    void shouldEncodeRfc2396UnwiseCharactersInPathSegment(String unwiseChar, String expectedEncoding) {
        String path = "/api/items/" + unwiseChar + "/details";

        URI instance = InstanceUtils.pathToInstance(path);
        String serialized = InstanceUtils.instanceToPath(instance);

        assertThat(serialized).contains(expectedEncoding);
        assertThat(serialized).doesNotContain(unwiseChar);
        assertThatCode(() -> URI.create(serialized)).doesNotThrowAnyException();
    }

}
