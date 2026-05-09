package io.quarkiverse.httpproblem;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class InstanceUtils {

    public static URI pathToInstance(String path) {
        if (path == null) {
            return null;
        }
        try {
            return new URI(encodeUnwiseCharacters(path));
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * @see <a href="https://www.ietf.org/rfc/rfc2396.txt">About unwise characters in RFC-2396</a>
     */
    private static String encodeUnwiseCharacters(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }

    public static String instanceToPath(URI instance) {
        return URLDecoder.decode(instance.toString(), StandardCharsets.UTF_8);
    }

}
