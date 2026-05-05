package io.quarkiverse.httpproblem;

import static io.restassured.RestAssured.given;

import com.google.common.collect.Sets;
import io.restassured.specification.RequestSpecification;
import io.smallrye.jwt.build.Jwt;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * Utilities for testing application with quarkus-smallrye-jwt based application using RestAssured.
 */
public final class AuthTestUtils {

    private static final PrivateKey pk = readPrivateKey("/jwt/privateKey.pem");

    private AuthTestUtils() {
        // no-op: utility class
    }

    /**
     * Creates RestAssured request specification with Authorization Bearer for given username and roles.
     *
     * @param username Username of test principal
     * @param roles Roles/groups that will be included in JWT. All endpoints secured with @RolesAllowed(role)
     *        will be accessible for this user
     * @return RestAssured request specification with JWT bearer in Authorization header.
     */
    public static RequestSpecification givenUser(String username, String... roles) {
        String accessToken = generateToken(username, roles);
        return given()
                .auth()
                .oauth2(accessToken);
    }

    public static RequestSpecification givenAnonymous() {
        return given();
    }

    private static String generateToken(String username, String... groups) {
        return Jwt.claims()
                .issuer(ConfigProvider.getConfig().getValue("mp.jwt.verify.issuer", String.class))
                .subject(username + "-jwt-rbac")
                .claim("name", username)
                .claim("preferred_username", username)
                .claim("given_name", username)
                .claim("family_name", username)
                .claim("email", username + "@evry.com")
                .groups(Sets.newHashSet(groups))
                .expiresAt(System.currentTimeMillis() + 3600)
                .sign(pk);
    }

    private static PrivateKey readPrivateKey(final String pemResName) {
        try (InputStream contentIS = AuthTestUtils.class.getResourceAsStream(pemResName)) {
            byte[] tmp = new byte[4096];
            int length = contentIS.read(tmp);
            return decodePrivateKey(new String(tmp, 0, length, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static PrivateKey decodePrivateKey(final String pemEncoded) throws Exception {
        byte[] encodedBytes = toEncodedBytes(pemEncoded);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    private static byte[] toEncodedBytes(final String pemEncoded) {
        final String normalizedPem = removeBeginEnd(pemEncoded);
        return Base64.getDecoder().decode(normalizedPem);
    }

    private static String removeBeginEnd(String pem) {
        pem = pem.replaceAll("-----BEGIN (.*)-----", "");
        pem = pem.replaceAll("-----END (.*)----", "");
        pem = pem.replaceAll("\r\n", "");
        pem = pem.replaceAll("\n", "");
        return pem.trim();
    }

}
