package dev.puzzleshq;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import java.io.FileReader;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;

public class TokenFetcher {
    private static final String APP_ID = "1399684";
    private static final String PRIVATE_KEY_PATH = "private-key.pem";
    private static final String GITHUB_API = "https://api.github.com";

    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper json = new ObjectMapper();

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
            List<Map<String, Object>> installations = json.readValue(
                    body,
                    json.getTypeFactory().constructCollectionType(List.class, Map.class)
            );

            if (installations.isEmpty()) {
                throw new IllegalStateException("No installations found. Did you install the app?");
            }

            int installationId = (Integer) installations.getFirst().get("id");

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
                Map<String, Object> tokenResponse = json.readValue(accessTokenResponse.body().string(), Map.class);
                return (String) tokenResponse.get("token");
            }
        }
    }

    public static PrivateKey loadPrivateKey(String filePath) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        try (PEMParser pemParser = new PEMParser(new FileReader(filePath))) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            if (object instanceof PEMKeyPair keyPair) {
                return converter.getKeyPair(keyPair).getPrivate();

            } else if (object instanceof org.bouncycastle.asn1.pkcs.PrivateKeyInfo privateKeyInfo) {
                return converter.getPrivateKey(privateKeyInfo);

            } else if (object instanceof PKCS8EncryptedPrivateKeyInfo encInfo) {
                // Uncomment and add password handling if encrypted
                throw new UnsupportedOperationException("Encrypted keys not supported in this example");

            } else {
                throw new IllegalArgumentException("Unsupported key format: " + object.getClass());
            }
        }
    }
}
