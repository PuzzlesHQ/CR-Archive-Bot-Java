package dev.puzzleshq.CRArchiveBot.old.myShit;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import dev.puzzleshq.CRArchiveBot.utils.FileUtils;
import io.jsonwebtoken.Jwts;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.kohsuke.github.*;
import org.kohsuke.github.extras.authorization.JWTTokenProvider;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static dev.puzzleshq.CRArchiveBot.old.TokenFetcher.*;

public class PemUtils {


    static RSAPublicKey getPublicKey(String filename) throws Exception {
        byte[] keyBytes = FileUtils.getInternalFile(filename).readAllBytes();

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(spec);
    }

    static RSAPrivateKey getPrivateKey(String filename) throws Exception {
        byte[] keyBytes = FileUtils.getInternalFile(filename).readAllBytes();

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) kf.generatePrivate(spec);
    }

//    // Creates JWT signed with private key (RS256 inferred automatically)
//    static String createJWT(String githubAppId, long ttlMillis) throws Exception {
//        long nowMillis = System.currentTimeMillis() - 60_000;
//        Date now = new Date(nowMillis);
//
//        PrivateKey signingKey = get("github-api-app.private-key.der");
//
//        JwtBuilder builder = Jwts.builder()
//                .issuedAt(now)
//                .issuer(githubAppId)
//                .signWith(signingKey);  // jjwt infers RS256 automatically
//
//        if (ttlMillis > 0) {
//            Date exp = new Date(nowMillis + ttlMillis);
//            builder.expiration(exp);
//        }
//
//        return builder.compact();
//    }
//
//    public static void main(String[] args) throws Exception {
//        String jwtToken = createJWT("1399684", 10 * 60 * 1000); //sdk-github-api-app-test
//        GitHub gitHubApp = new GitHubBuilder().withJwtToken(jwtToken).build();
//        gitHubApp.checkApiUrlValidity();
//    }


    private static String createJwtToken(String keyFileResouceName, String appId) {
        try {
            String keyPEM = IOUtils.toString(FileUtils.getInternalFile(keyFileResouceName), StandardCharsets.US_ASCII)
                    .replaceAll("(?m)^--.*", "") // remove comments from PEM to allow decoding
                    .replaceAll("\\s", "");

            System.out.println(keyPEM);

            PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyPEM));
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpecPKCS8);

            return Jwts.builder().issuedAt(Date.from(Instant.now())).expiration(Date.from(Instant.now().plus(5, ChronoUnit.MINUTES))).issuer(appId)
                    .signWith(privateKey)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Error creating JWT token.", e);
        }
    }

    public static String getToken() throws Exception {
        RSAPrivateKey privateKey = (RSAPrivateKey) loadPrivateKey("cr-archive-keeper.2025-06-18.private-key.pem");

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

    public static class GitHubAppAuthUtil {

        private final String appId;
        private final PrivateKey privateKey;

        public GitHubAppAuthUtil(String appId, String privateKeyPath) throws Exception {
            this.appId = appId;
            this.privateKey = loadPrivateKey(privateKeyPath);
        }

        private PrivateKey loadPrivateKey(String pemPath) throws Exception {
            StringBuilder pem = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(pemPath))) {
                String line;
                boolean insideKey = false;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("BEGIN PRIVATE KEY")) {
                        insideKey = true;
                    } else if (line.contains("END PRIVATE KEY")) {
                        break;
                    } else if (insideKey) {
                        pem.append(line.trim());
                    }
                }
            }

            byte[] decoded = Base64.getDecoder().decode(pem.toString());
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        }

        private String createJWT() {
            long now = System.currentTimeMillis();
            return Jwts.builder()
                    .issuedAt(new Date(now - 60_000))               // allow 1 min skew
                    .expiration(new Date(now + 600_000))            // expires in 10 mins
                    .issuer(appId)
                    .signWith(privateKey)
                    .compact();
        }

        public GitHub getAppClient() throws IOException {
            String jwt = createJWT();
            return new GitHubBuilder()
                    .withJwtToken(jwt)
                    .build();
        }

        public GitHub getInstallationClient(long installationId) throws IOException {
            GitHub appClient = getAppClient();
            GHApp app = appClient.getApp();
            GHAppInstallation installation = app.getInstallationById(installationId);
            GHAppInstallationToken token = installation.createToken().create();
            return new GitHubBuilder()
                    .withOAuthToken(token.getToken())
                    .build();
        }

        public void listInstallations() throws IOException {
            GitHub appClient = getAppClient();
            for (GHAppInstallation inst : appClient.getApp().listInstallations()) {
                System.out.println("Installation ID: " + inst.getId());
            }
        }
    }


    public static void main(String[] args) throws Exception {
//        RSAPrivateKey privateKey = getPrivateKey("github-api-app.private-key.der");
//        RSAPublicKey publicKey = getPublicKey("github-api-app.private-key.der");
//        String jwtToken = createJWT("1399684", 10 * 60 * 1000, privateKey);

//        System.out.println(jwtToken);

//        long now = Instant.now().getEpochSecond();
//        Algorithm algorithm = Algorithm.RSA256(null, privateKey);
//        String jwtToken = JWT.create()
//                .withIssuer("1399684")
//                .withIssuedAt(java.util.Date.from(Instant.ofEpochSecond(now)))
//                .withExpiresAt(java.util.Date.from(Instant.ofEpochSecond(now + 600)))
//                .sign(algorithm);

//        RSAPrivateKey privateKey = (RSAPrivateKey) loadPrivateKey("cr-archive-keeper.2025-06-18.private-key.pem");
//
//        long now = Instant.now().getEpochSecond();
//        Algorithm algorithm = Algorithm.RSA256(null, privateKey);
//        String jwtToken = JWT.create()
//                .withIssuer(APP_ID)
//                .withIssuedAt(java.util.Date.from(Instant.ofEpochSecond(now)))
//                .withExpiresAt(java.util.Date.from(Instant.ofEpochSecond(now + 600)))
//                .sign(algorithm);

//        GitHub gitHubApp = new GitHubBuilder()
//                .withJwtToken(createJwtToken("cr-archive-keeper.2025-06-18.private-key.pem", "1399684"))
//                .build();

//        GHApp ghApp = new GHApp();


        JWTTokenProvider jwtTokenProvider = new JWTTokenProvider(APP_ID, Path.of("key.pem"));


        GitHub gitHubApp = new GitHubBuilder()
                .withAuthorizationProvider(jwtTokenProvider)
                .build();


        GHAppInstallation ghAppInstallation = gitHubApp.getApp().getInstallationByRepository("PuzzlesHQ", "CRArchive");



        System.out.println(ghAppInstallation.createToken().repositories(List.of("CRArchive")).create().getRepositories().get(0).getName());
//        gitHubApp.checkApiUrlValidity();  // this should pass without 401 now

//        GitHubAppAuthUtil authUtil = new GitHubAppAuthUtil(APP_ID, "key.pem");
//
//        // List installations (helpful if you don’t know the ID)
//        authUtil.listInstallations();
//
//        // Authenticate as an installation
//        long installationId = 71053700L;  // Replace with your real ID
//        GitHub github = authUtil.getInstallationClient(installationId);
//
//        // Access repository
//        GHRepository repo = github.getRepository("PuzzlesHQ/PuzzleLoader");
//        System.out.println("Repo full name: " + repo.getFullName());
    }

}
