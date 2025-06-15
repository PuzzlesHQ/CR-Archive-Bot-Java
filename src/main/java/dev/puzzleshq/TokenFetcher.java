package dev.puzzleshq;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.io.FileReader;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;

public class TokenFetcher {
    private static final String APP_ID = "1399684";
    private static final String PRIVATE_KEY_PATH = "private-key.pem";
    private static final String GITHUB_API = "https://api.github.com";

    private static final OkHttpClient client = new OkHttpClient();

    public static String getToken() throws Exception {
        RSAPrivateKey privateKey = (RSAPrivateKey) loadPrivateKey(PRIVATE_KEY_PATH);

        long now = Instant.now().getEpochSecond();
        Algorithm algorithm = Algorithm.RSA256(null, privateKey);
        String jwtToken = JWT.create()
                .withIssuer(APP_ID)
                .withIssuedAt(java.util.Date.from(Instant.ofEpochSecond(now)))
                .withExpiresAt(java.util.Date.from(Instant.ofEpochSecond(now + 600)))
                .sign(algorithm);

        // === Create GitHub client ===
        Request request = new Request.Builder()
                .url(GITHUB_API + "/app/installations")
                .addHeader("Authorization", "Bearer " + jwtToken)
                .addHeader("Accept", "application/vnd.github+json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Installations request failed: " + response.code());
            }

            assert response.body() != null;
            String body = response.body().string();
            JsonArray installations = JsonValue.readHjson(body).asArray();

            if (installations.isEmpty()) {
                throw new IllegalStateException("No installations found. Did you install the app?");
            }

            JsonObject first = installations.get(0).asObject();
            int installationId = first.getInt("id", -1);
            if (installationId == -1) throw new IllegalStateException("No installation id found");

            // === Exchange JWT for installation token ===
            Request accessTokenRequest = new Request.Builder()
                    .url(GITHUB_API + "/app/installations/" + installationId + "/access_tokens")
                    .addHeader("Authorization", "Bearer " + jwtToken)
                    .addHeader("Accept", "application/vnd.github+json")
                    .post(RequestBody.create(new byte[0]))
                    .build();

            try (Response accessTokenResponse = client.newCall(accessTokenRequest).execute()) {
                if (!accessTokenResponse.isSuccessful()) {
                    throw new IOException("Access token request failed: " + accessTokenResponse.code());
                }

                assert accessTokenResponse.body() != null;
                String accessBody = accessTokenResponse.body().string();
                JsonObject tokenObj = JsonValue.readHjson(accessBody).asObject();
                return tokenObj.getString("token", null);
            }
        }
    }

    public static PrivateKey loadPrivateKey(String filePath) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        try (PEMParser pemParser = new PEMParser(new FileReader(filePath))) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            // Uncomment and add password handling if encrypted
            return switch (object) {
                case PEMKeyPair keyPair -> converter.getKeyPair(keyPair).getPrivate();
                case org.bouncycastle.asn1.pkcs.PrivateKeyInfo privateKeyInfo ->
                        converter.getPrivateKey(privateKeyInfo);
                case PKCS8EncryptedPrivateKeyInfo encInfo ->
                        throw new UnsupportedOperationException("Encrypted keys not supported in this example");
                default -> throw new IllegalArgumentException("Unsupported key format: " + object.getClass());
            };
        }
    }
}
