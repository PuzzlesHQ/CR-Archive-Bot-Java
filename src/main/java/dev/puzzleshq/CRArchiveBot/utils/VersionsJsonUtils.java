package dev.puzzleshq.CRArchiveBot.utils;

import com.github.villadora.semver.SemVer;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import org.kohsuke.github.GHContent;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionsJsonUtils {

    public static void fixVersionsJson() {
        GHContent ghContent = GithubFileUtils.getFile("versions.json");
        JsonObject json = GithubFileUtils.getFileAsJson(ghContent).asObject();
        JsonArray versions = json.get("versions").asArray();

        versions.forEach(JsonVersion -> {
            JsonObject version = JsonVersion.asObject();
            JsonValue client = version.get("client");
            if (client != null) {
                JsonObject clientObject = client.asObject();
                String url = clientObject.get("url").asString();
                String newUrl = url.replaceAll("[+]", "%2B");
                clientObject.set("url", newUrl);
            }
            JsonValue server = version.get("server");
            if (server != null) {
                JsonObject serverObject = server.asObject();
                String url = serverObject.get("url").asString();
                String newUrl = url.replaceAll("[+]", "%2B");
                serverObject.set("url", newUrl);
            }
        });


        try {
            ghContent.update(json.toString(Stringify.FORMATTED), "fix url having + instead of %2B");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static GHContent versionsJson;

    public static void updateVersionsJson(JsonObject versionsJson) {

//        GithubUtils.getArchive().getFileContent();

    }

    public static JsonObject getVersionsJson() {
        return getVersionsJson(false);
    }

    public static JsonObject getVersionsJson(boolean refresh) {
        if (refresh || versionsJson == null) {
            versionsJson = GithubFileUtils.getFile("versions.json");
            try {
                versionsJson.refresh();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return GithubFileUtils.getFileAsJson(versionsJson).asObject();
    }

    static boolean firstTime = true;
    static Map<String, JsonObject> versionsMap = new HashMap<>();

    private static void makeVersionsMap() {
        if (firstTime) {
            JsonObject versionsJson = getVersionsJson(true);

            JsonArray versionsJsonArray = versionsJson.get("versions").asArray();
            for (JsonValue jsonValue : versionsJsonArray) {
                JsonObject jsonObject = jsonValue.asObject();
                String id = jsonObject.get("id").asString();

                versionsMap.put(id, jsonObject);
            }
            firstTime = false;
        }
    }

    /*
    {
            "id": "0.4.17-alpha",
            "type": "release",
            "phase": "alpha",
            "releaseTime": 1755442878, ✓
            "client": {
                "url": "https://github.com/PuzzlesHQ/CRArchive/releases/download/0.4.17-alpha/cosmic-reach-client-0.4.17-alpha.jar",
                "sha256": "f8d8cca9450ebbc958b344986c604409db04c6296fc39bde72cbd178d24d6729",
                "size": 54995810
            },
            "server": {
                "url": "https://github.com/PuzzlesHQ/CRArchive/releases/download/0.4.17-alpha/cosmic-reach-server-0.4.17-alpha.jar",
                "sha256": "276fc676da65b74ee191d6c568b253e0514964fce9233da555810e6ac86ba2af",
                "size": 14552870
            }
        }
       */

    public static boolean containsId(String id) {
        makeVersionsMap();
        return versionsMap.containsKey(id);
    }

    public static long getTimeStamp(Path path) {
        JarFile jarFile;
        Manifest manifest;
        try {
            jarFile = new JarFile(path.toFile());
            manifest = jarFile.getManifest();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        long second = 0;
        if (manifest != null) {
            String mainClassName = manifest.getMainAttributes().getValue("Main-Class");

            String classPath = mainClassName.replace('.', '/') + ".class";

            JarEntry entry = jarFile.getJarEntry(classPath);
            long time = entry.getTime();
            Date date = new Date(time);
            second = date.toInstant().getEpochSecond();

        }
        return second;
    }

    public static long getFileSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getSha256(Path path){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        try (InputStream is = Files.newInputStream(path);
             DigestInputStream dis = new DigestInputStream(is, digest)) {
            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) {
                // reading file updates the digest
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    public static String getRawVersion(Path path){
        File jarFile = path.toFile();
        String versionTXT = "build_assets/version.txt";

        try (JarFile jar = new JarFile(jarFile)) {
            JarEntry entry = jar.getJarEntry(versionTXT);

            try (InputStream is = jar.getInputStream(entry);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.readLine();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPhase(Path path){
        String rawVersion = getRawVersion(path);
        Pair<String, String> versionPair = getVersionPair(rawVersion);

        return getVersionPhase(versionPair.getLeft()).replaceAll("-", "");
    }

    public static Pair<String, String> getVersionPair(String rawVersion){
        Pattern pattern = Pattern.compile("^([0-9.]+)([a-zA-Z]*)$");
        Matcher matcher = pattern.matcher(rawVersion);

        String checkVersion;
        String devVersion = null;

        if (!matcher.matches()) {
            checkVersion = matcher.group(1);
            devVersion = matcher.group(2);
        } else {
            checkVersion = rawVersion;
        }

        return new Pair<>(checkVersion, devVersion);
    }

    public static String getVersionPhase(String checkVersion){
        if (SemVer.gt("0.3.27", checkVersion)){
            return  "-pre_alpha";
        } else {
            return"-alpha";
        }
    }

    public static String getVersion(Path path){
        String rawVersion = getRawVersion(path);

        Pair<String, String> versionPair = getVersionPair(rawVersion);

        String phase = getVersionPhase(versionPair.getLeft());

        return versionPair.getLeft() + phase + (versionPair.getRight() != null ? "+" + versionPair.getRight() : "");
    }

    public static JsonObject getSideEntry(String version, String side, Path jarPath){
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("url", "https://github.com/PuzzlesHQ/CRArchive/releases/download/" + version + "/cosmic-reach-" + side + "-" + version + ".jar");
        jsonObject.add("sha256", getSha256(jarPath));
        jsonObject.add("size", getFileSize(jarPath));
        return jsonObject;
    }

    public static JsonObject getVersionJsonEntry(Path clientPath, Path serverPath){
        String version = getVersion(clientPath);
        String phase = getPhase(clientPath);
        long releaseTime = getTimeStamp(clientPath);

        JsonObject client = getSideEntry(version, "client", clientPath);
        JsonObject server = getSideEntry(version, "server", serverPath);

        JsonObject versionEntry = new JsonObject();
        versionEntry.add("id", version);
        versionEntry.add("type", "release");
        versionEntry.add("phase", phase);
        versionEntry.add("releaseTime", releaseTime);
        versionEntry.add("client", client);
        versionEntry.add("server", server);


        return versionEntry;
    }

    // adds any versions it fines in the release that are not in the versions.json
    public static void main(String[] args) {
//        GithubUtils.init();

//        List<GHRelease> releaseList =  GithubReleaseUtils.listRelease();

//        makeVersionsMap();
//        System.out.println(versionsMap);


        String home = System.getProperty("user.home");
        Path clientPath = Path.of(home, "/Projects/cosmic_reach/puzzle/CR-Archive-Bot-Java/downloads/cosmic-reach-client-0.5.5-alpha.jar");
        Path serverPath = Path.of(home, "/Projects/cosmic_reach/puzzle/CR-Archive-Bot-Java/downloads/cosmic-reach-server-0.5.5-alpha.jar");

        JsonObject versionsJson = getVersionJsonEntry(clientPath, serverPath);

        System.out.println(versionsJson.toString(Stringify.FORMATTED));


//        boolean a = true;
//        for (GHRelease release : releaseList) {
//            String tagName = release.getTagName();
//            if (containsId(tagName)){
//                if (a) {
//                    Path path = GithubAssetUtils.downloadGHAsset(release.getAssets().get(0), true);
//
//                    System.out.println(getTimeStamp(path));
//
//                    a = false;
//                }
//            }
//        }
    }

}
