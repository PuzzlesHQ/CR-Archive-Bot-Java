package dev.puzzleshq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.puzzleshq.utils.GithubUtils;
import dev.puzzleshq.utils.ItchUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.util.*;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.stream.Stream;
import java.util.zip.*;

import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import java.io.IOException;

import static dev.puzzleshq.Main.*;
import static dev.puzzleshq.utils.FileUtils.calculateFileHash;
import static dev.puzzleshq.utils.FileUtils.getFileSize;
import static dev.puzzleshq.utils.StringUtils.title;

public class Bot {
    private static final Logger botLogger = LoggerFactory.getLogger("Archive Keeper");
    private static final Integer maxRetries = 5;

    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final ObjectMapper json = new ObjectMapper();

    private static final String RSS_URL = "https://finalforeach.itch.io/cosmic-reach/devlog.rss";
    private static final String USER_AGENT = "Mozilla/5.0";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final MediaType JAR = MediaType.get("application/java-archive");


    public static void runArchiveBot() throws Exception {
        // Lando's charger is a fucking bastard, and he really fucking hates it
        // In progress
        botLogger.info("Itch {} | Github {}", ItchUtils.fetchLatestItchVersion(), GithubUtils.fetchLatestRelease());
        if (!Objects.equals(ItchUtils.fetchLatestItchVersion(true), GithubUtils.fetchLatestRelease())) {
            botLogger.info("Version mismatch");

            // Create files to upload
            Map<String,String> filesToUpload = new HashMap<>();

            // Download client jar
            String clientJarName = "cosmic-reach-client-" + ItchUtils.fetchLatestItchVersion(true);
            String clientJarPath = downloadJar(clientJarName, false);
            if (clientJarPath != null) {
                filesToUpload.put("client", clientJarPath);
            }

            // Download server jar
            String serverJarName = "cosmic-reach-server-" + ItchUtils.fetchLatestItchVersion(true);
            String serverJarPath = downloadJar(serverJarName, true);
            if (serverJarPath != null) {
                filesToUpload.put("server", serverJarPath);
            }

            // Create the release on GitHub
            if (!filesToUpload.isEmpty()) {
                createRelease(ItchUtils.fetchLatestItchVersion(), filesToUpload);
            }

        } else {botLogger.info("Versions match");}
    }

    public static void createRelease(String version, Map<String, String> filesDict) throws Exception {
        String changelog = fetchChangelog(version);

        // Step 1: Get branch SHA
        JsonNode refRes = makeRequestWithRetries("GET",
                "https://api.github.com/repos/" + GITHUB_REPO + "/git/refs/heads/" + GITHUB_BRANCH,
                null, 3);
        String baseSha = refRes.get("object").get("sha").asText();

        JsonNode baseTreeRes = makeRequestWithRetries("GET",
                "https://api.github.com/repos/" + GITHUB_REPO + "/git/trees/" + baseSha,
                null, 3);
        String baseTreeSha = baseTreeRes.get("sha").asText();

        // Step 2: Create blobs for each file
        List<Map<String, String>> treeItems = new ArrayList<>();
        Map<String, String> headers = Map.of(
                "Authorization", "token " + TOKEN,
                "Accept", "application/vnd.github+json"
        );
        Map<String, String> filesToUpdate = updateVersionFile(version, changelog, filesDict, headers);

        for (Map.Entry<String, String> entry : filesToUpdate.entrySet()) {
            ObjectNode blobData = json.createObjectNode();
            blobData.put("content", entry.getValue());
            blobData.put("encoding", "utf-8");

            JsonNode blobRes = makeRequestWithRetries("POST",
                    "https://api.github.com/repos/" + GITHUB_REPO + "/git/blobs",
                    blobData, 3);

            Map<String, String> treeEntry = new HashMap<>();
            treeEntry.put("path", entry.getKey());
            treeEntry.put("mode", "100644");
            treeEntry.put("type", "blob");
            treeEntry.put("sha", blobRes.get("sha").asText());
            treeItems.add(treeEntry);
        }

        ObjectNode treePayload = json.createObjectNode();
        treePayload.put("base_tree", baseTreeSha);
        treePayload.set("tree", json.valueToTree(treeItems));

        JsonNode newTree = makeRequestWithRetries("POST",
                "https://api.github.com/repos/" + GITHUB_REPO + "/git/trees",
                treePayload, 3);
        String newTreeSha = newTree.get("sha").asText();

        // Step 3: Commit it
        ObjectNode commitData = json.createObjectNode();
        commitData.put("message", "Added " + version);
        commitData.put("tree", newTreeSha);
        commitData.set("parents", json.valueToTree(List.of(baseSha)));

        JsonNode commitRes = makeRequestWithRetries("POST",
                "https://api.github.com/repos/" + GITHUB_REPO + "/git/commits",
                commitData, 3);
        String newCommitSha = commitRes.get("sha").asText();

        ObjectNode patchRef = json.createObjectNode();
        patchRef.put("sha", newCommitSha);

        makeRequestWithRetries("PATCH",
                "https://api.github.com/repos/" + GITHUB_REPO + "/git/refs/heads/" + GITHUB_BRANCH,
                patchRef, 3);

        // Step 4: Create or find release
        String uploadUrlBase;
        try {
            JsonNode release = makeRequestWithRetries("GET",
                    "https://api.github.com/repos/" + GITHUB_REPO + "/releases/tags/" + version,
                    null, 1);
            uploadUrlBase = release.get("upload_url").asText().split("\\{")[0];
        } catch (Exception e) {
            ObjectNode releasePayload = json.createObjectNode();
            releasePayload.put("tag_name", version);
            releasePayload.put("name", title(version));
            releasePayload.put("body", changelog);
            releasePayload.put("draft", false);
            releasePayload.put("prerelease", false);

            JsonNode release = makeRequestWithRetries("POST",
                    "https://api.github.com/repos/" + GITHUB_REPO + "/releases",
                    releasePayload, 3);
            uploadUrlBase = release.get("upload_url").asText().split("\\{")[0];
        }

        // Step 5: Upload assets
        for (Map.Entry<String, String> fileEntry : filesDict.entrySet()) {
            Path filePath = Paths.get(fileEntry.getValue());
            File file = filePath.toFile();
            String filename = file.getName();

            HttpUrl uploadUrl = Objects.requireNonNull(HttpUrl.parse(uploadUrlBase)).newBuilder()
                    .addQueryParameter("name", filename)
                    .build();

            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .addHeader("Authorization", "token " + TOKEN)
                    .addHeader("Accept", "application/vnd.github+json")
                    .post(RequestBody.create(file, JAR))  // <--- FIXED: Use File, not byte[]
                    .build();

            botLogger.info("Uploading file {} to {}", filename, uploadUrl);
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    assert response.body() != null;
                    throw new IOException("Upload failed: " + response + "\n" + response.body().string());
                }
                botLogger.info("Uploaded {}", filename);
            }
        }

        botLogger.info("Finished uploading {}", title(version));
    }

    @NotNull @SuppressWarnings("unchecked")
    public static Map<String, String> updateVersionFile(
            String version,
            String changelog,
            Map<String, String> filesDict,
            Map<String, String> headers
    ) throws Exception {

        botLogger.info("Updating version files");
        String baseUrl = "https://github.com/" + GITHUB_REPO + "/releases/download/" + version;
        long currentTime = Instant.now().getEpochSecond();

        ObjectMapper mapper = new ObjectMapper();


        // === versions.json ===
        HttpResponse<String> versionsResponse = httpGet(
                "https://api.github.com/repos/" + GITHUB_REPO + "/contents/versions.json",
                headers
        );

        Map<String, Object> versionsData;
        try {
            String encoded = mapper.readTree(versionsResponse.body()).get("content").asText();
            byte[] decoded = Base64.getDecoder().decode(encoded);
            versionsData = mapper.readValue(decoded, Map.class);
        } catch (Exception e) {
            versionsData = new HashMap<>();
            versionsData.put("latest", Map.of("alpha", version, "pre_alpha", "0.3.27"));
            versionsData.put("versions", new ArrayList<>());
        }

        Map<String, Object> newVersion = new LinkedHashMap<>();
        newVersion.put("id", version);
        newVersion.put("type", "release");
        newVersion.put("phase", "alpha");
        newVersion.put("releaseTime", currentTime);

        // Attach client info if available
        if (filesDict.containsKey("client")) {
            Path client = Path.of(filesDict.get("client"));
            newVersion.put("client", Map.of(
                    "url", baseUrl + "/cosmic-reach-client-" + version + ".jar",
                    "sha256", calculateFileHash(client),
                    "size", getFileSize(client)
            ));
        }

        // Attach server info if available
        if (filesDict.containsKey("server")) {
            Path server = Path.of(filesDict.get("server"));
            newVersion.put("server", Map.of(
                    "url", baseUrl + "/cosmic-reach-server-" + version + ".jar",
                    "sha256", calculateFileHash(server),
                    "size", getFileSize(server)
            ));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> latest = new HashMap<>((Map<String, Object>) versionsData.get("latest"));
        versionsData.put("latest", latest); // optional if not already in map
        latest.put("alpha", version);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> versionsList = (List<Map<String, Object>>) versionsData.get("versions");
        versionsList.addFirst(newVersion);

        Map<String, String> filesToUpdate = new LinkedHashMap<>();
        filesToUpdate.put("versions.json", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(versionsData));

        return filesToUpdate;
    }

    private static HttpResponse<String> httpGet(String url, @NotNull Map<String, String> headers) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .headers(headers.entrySet().stream()
                        .flatMap(e -> Stream.of(e.getKey(), e.getValue()))
                        .toArray(String[]::new))
                .GET()
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static JsonNode makeRequestWithRetries(String method, String url, JsonNode body, int retries) throws Exception {
        for (int attempt = 0; attempt < retries; attempt++) {
            try {
                Request.Builder builder = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "token " + TOKEN)
                        .addHeader("Accept", "application/vnd.github+json");

                if (body != null) {
                    builder.method(method, RequestBody.create(json.writeValueAsString(body), JSON));
                } else {
                    builder.method(method, null);
                }

                try (Response response = httpClient.newCall(builder.build()).execute()) {
                    if (!response.isSuccessful()) {
                        assert response.body() != null;
                        throw new IOException("HTTP " + response.code() + ": " + response.body().string());
                    }
                    assert response.body() != null;
                    return json.readTree(response.body().string());
                }
            } catch (Exception e) {
                if (attempt == retries - 1) throw e;
                botLogger.error("Attempting {} failed:", attempt+ 1, e);
                Thread.sleep((long) Math.pow(2, attempt) * 1000);
            }
        }
        throw new IllegalStateException("Unreachable");
    }

    @NotNull
    public static String fetchChangelog(String version) {
        botLogger.info("Fetching changelog for {}", version);
        try {
            Request rssRequest = new Request.Builder()
                    .url(RSS_URL)
                    .header("User-Agent", USER_AGENT)
                    .build();

            try (Response response = httpClient.newCall(rssRequest).execute()) {
                if (!response.isSuccessful())
                    throw new IOException("Failed to fetch RSS feed: " + response.code());

                assert response.body() != null;
                String rssXml = response.body().string();
                Document rssDoc = Jsoup.parse(rssXml, "", org.jsoup.parser.Parser.xmlParser());
                Elements items = rssDoc.select("item");

                for (Element item : items) {
                    Element title = item.selectFirst("title");
                    if (title != null && title.text().contains(version.split("-")[0].toLowerCase())) {
                        botLogger.info("Found element");
                        Element link = item.selectFirst("link");
                        if (link == null) continue;

                        String devlogUrl = link.text().trim();
                        //noinspection HttpUrlsUsage
                        if (!devlogUrl.startsWith("http://") && !devlogUrl.startsWith("https://")) {
                            devlogUrl = "https://finalforeach.itch.io" + devlogUrl;
                        }

                        Request devlogRequest = new Request.Builder()
                                .url(devlogUrl)
                                .header("User-Agent", USER_AGENT)
                                .build();

                        try (Response devlogResponse = httpClient.newCall(devlogRequest).execute()) {
                            if (!devlogResponse.isSuccessful())
                                throw new IOException("Failed to fetch devlog page: " + devlogResponse.code());

                            assert devlogResponse.body() != null;
                            String html = devlogResponse.body().string();
                            Document devlogDoc = Jsoup.parse(html);
                            Element changelogSection = devlogDoc.selectFirst("section.post_body");

                            if (changelogSection != null) {
                                StringBuilder sb = new StringBuilder();

                                Element image = changelogSection.selectFirst("img");
                                if (image != null && image.hasAttr("src")) {
                                    sb.append("![Changelog Image](").append(image.attr("src")).append(")\n\n");
                                }

                                Element ul = changelogSection.selectFirst("ul");
                                if (ul != null) {
                                    for (Element li : ul.select("li")) {
                                        String text = li.text().trim().replaceAll(",$", "");
                                        sb.append("* ").append(text).append("\n");
                                    }
                                    return sb.toString();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                botLogger.error("Failed to fetch changelog for", e);
            }
        } catch (Exception e) {
            botLogger.error(String.valueOf(e));
        }

        return "Release for version " + version;
    }

    private static String downloadJar(String jarName, boolean isServer) {
        Path filePath = Paths.get("downloads/", jarName);
        if (Files.exists(filePath)) {
            botLogger.info("Jar {} already exists", jarName);
            return "downloads" + '/' + jarName + '/' + jarName + ".jar";
        }

        String environment = isServer ? "server" : "client";

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                botLogger.info("Fetching {} download URL", environment);

                String url = isServer
                        ? "https://finalforeach.itch.io/cosmic-reach/file/11884793"
                        : "https://finalforeach.itch.io/cosmic-reach/file/9891067";

                HttpRequest postRequest = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(30))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .header("Accept", "application/json")
                        .build();

                HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

                if (postResponse.statusCode() != 200)
                    throw new IOException("Failed to fetch download URL. Status: " + postResponse.statusCode());

                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(postResponse.body());
                String downloadUrl = json.path("url").isTextual() ? json.get("url").asText() : null;
                if (downloadUrl == null || downloadUrl.isEmpty())
                    throw new RuntimeException("Failed to get direct download URL from itch.io response.");

                botLogger.info("Downloading {} file", environment);

                HttpRequest getRequest = HttpRequest.newBuilder()
                        .uri(URI.create(downloadUrl))
                        .timeout(Duration.ofMinutes(1))
                        .build();

                HttpResponse<byte[]> fileResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofByteArray());

                if (fileResponse.statusCode() != 200)
                    throw new IOException("Failed to download file. Status: " + fileResponse.statusCode());

                File tempZip = File.createTempFile("download", ".zip");
                Files.write(tempZip.toPath(), fileResponse.body());

                try (ZipFile zipFile = new ZipFile(tempZip)) {
                    List<? extends ZipEntry> jarEntries = zipFile.stream()
                            .filter(e -> e.getName().endsWith(".jar"))
                            .toList();

                    if (jarEntries.isEmpty())
                        throw new FileNotFoundException("No .jar file found in the ZIP archive.");

                    if (jarEntries.size() > 1)
                        botLogger.warn("Multiple .jar files found in the ZIP archive.");

                    ZipEntry jarEntry = jarEntries.getFirst();
                    Path outputDir = Paths.get("downloads", jarName);
                    Files.createDirectories(outputDir);
                    Path outputFile = outputDir.resolve(jarName+".jar");

                    try (InputStream is = zipFile.getInputStream(jarEntry);
                         OutputStream os = Files.newOutputStream(outputFile)) {
                        is.transferTo(os);
                    }

                    botLogger.info("Downloaded and extracted {} to {}", jarName, outputFile);
                    return outputFile.toString();
                } finally {

                    //noinspection ResultOfMethodCallIgnored
                    tempZip.delete(); // Optional
                }

            } catch (IOException | InterruptedException e) {
                if (attempt == maxRetries - 1) {
                    botLogger.error("Failed to fetch download URL after {} attempts", attempt);
                    return null;
                }
                botLogger.warn("Attempt {} failed", attempt, e);
                try {
                    Thread.sleep(5000L * (attempt + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            } catch (Exception e) {
                botLogger.error("Unexpected error", e);
                return null;
            }
        }

        return null;
    }
}
