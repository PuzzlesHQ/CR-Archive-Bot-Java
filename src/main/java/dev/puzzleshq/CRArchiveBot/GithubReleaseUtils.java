package dev.puzzleshq.CRArchiveBot;

import dev.puzzleshq.CRArchiveBot.utils.*;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GithubReleaseUtils {
    private static final Logger logger = LoggerFactory.getLogger("GithubReleaseUtils");

    //TODO make a renameRelease
    public static void renameAllReleases(){
        //old version.  new version, new jar name server/client
        Map<String, Pair<String, Pair<String, String>>> renamer = new HashMap<>();

        GithubUtils.getArchive().listReleases().forEach(ghRelease -> {

            String version = FormatConverterUtils.convertFormat(ghRelease.getTagName());
            Path versionPath = Paths.get("downloads/", version);
            FileUtils.makeDir(versionPath);

            Map<Path, String> assets = new HashMap<>();
            Pair<String, String> fileNames = new Pair<>();
            ghRelease.listAssets().forEach(ghAsset -> {
                String fileName = FormatConverterUtils.convertFileNameFormat(ghAsset.getName());
                Path path = GithubAssetUtils.downloadGHAsset(ghAsset, versionPath, fileName, "", true);
                assets.put(path, ghAsset.getContentType());
                try {
                    ghAsset.delete();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (fileName.contains("server")) {
                    fileNames.setLeft(fileName);
                } else if (fileName.contains("client")) {
                    fileNames.setRight(fileName);
                } else {
                    logger.error(fileName);
                }

            });

            String releaseName = version
                    .replaceAll("-", " ")
                    .replaceAll("[+]", " ");

            renamer.put(ghRelease.getTagName(), new Pair<>(version, fileNames));

            GHRelease release = GithubUtils.updateRelease(ghRelease, version, releaseName);

            assets.forEach((path, contentType) -> {
                try {
                    release.uploadAsset(path.toFile(), contentType);
                    path.toFile().delete();
                    path.getParent().toFile().delete();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        });

        GHContent ghContent = GithubFileUtils.getFile("versions.json");
        JsonObject json = GithubFileUtils.getFileAsJson(ghContent).asObject();
        JsonArray versions  = json.get("versions").asArray();

        versions.forEach(version -> {
            JsonObject jsonObject = version.asObject();
            Pair<String, Pair<String, String>> stuff = renamer.get(jsonObject.get("id").asString());
            if (stuff != null){
                jsonObject.set("id", stuff.getLeft());

                JsonValue clientValue = jsonObject.get("client");
                updateUrlJson(stuff.getLeft(), stuff.getRight().getRight(), clientValue);
                JsonValue serverValue = jsonObject.get("server");
                updateUrlJson(stuff.getLeft(), stuff.getRight().getLeft(), serverValue);
            } else {
                logger.error(jsonObject.get("id").asString());
            }
        });

        try {
            ghContent.update(json.toString(Stringify.FORMATTED), "update versions.json to new format");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println(json.toString(Stringify.FORMATTED));

    }

    private static void updateUrlJson(String version, String fileName, JsonValue value) {
        if (value != null){
            JsonObject JsonObject = value.asObject();
            String url = JsonObject.get("url").asString();
            String[] splitUrl = url.split("/");
            splitUrl[7] = version;
            splitUrl[8] = fileName;
            StringBuilder builder = new StringBuilder();
            for (String s : splitUrl) {
                builder.append(s);
                if (!s.endsWith(".jar")) builder.append("/");
            }

            JsonObject.set("url", builder.toString());
        }
    }

    //TODO make this
    public static void renameRelease() {

    }

    public static void copyAllReleases(GHRepository from, GHRepository to) {
        from.listReleases().forEach(ghRelease -> {
            GithubUtils.copyRelease(ghRelease, to);
        });
    }

    public static void deleteAllReleases(GHRepository from) {
        try {
            from.listReleases().toList().forEach(GithubUtils::deleteRelease);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
