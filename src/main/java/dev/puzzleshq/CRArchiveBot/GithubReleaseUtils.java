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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static dev.puzzleshq.CRArchiveBot.utils.GithubUtils.getArchive;

public class GithubReleaseUtils {
    private static final Logger logger = LoggerFactory.getLogger("GithubReleaseUtils");

    public static boolean makeNewRelease(String tag, String releaseName, String releaseBody, List<Path> assets) {
        GHRelease ghRelease = makeRelease(tag, releaseName, releaseBody);

        GithubAssetsUtils.uploadAssets(ghRelease, assets);


        return true;
    }

    public static GHRelease makeRelease(String tag, String releaseName, String releaseBody) {
        try {
            return GithubUtils.getArchive().createRelease(tag)
                    .name(releaseName)
                    .body(releaseBody)
                    .create();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //TODO make this
    public static void renameRelease() {

    }

    public static GHRelease updateRelease(GHRelease release, String tag, String releaseName) {
        try {
            String oldTag = release.getTagName();
            GHRelease newRelease = release.update()
                    .tag(tag)
                    .name(releaseName)
                    .update();

            if (!Objects.equals(oldTag, tag)) {
                Objects.requireNonNull(getArchive()).getRef("tags/" + oldTag).delete();
            }
            return newRelease;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static GHRelease copyRelease(GHRelease release) {
        return copyRelease(release, Objects.requireNonNull(getArchive()));
    }

    public static GHRelease copyRelease(GHRelease release, GHRepository to) {
        try {
            GHRelease newRelease = to.createRelease(release.getTagName())
                    .name(release.getName())
                    .body(release.getBody())
                    .create();

            release.getAssets().forEach(asset -> {
                try {
                    Path assetPath = GithubAssetUtils.downloadGHAsset(asset);
                    newRelease.uploadAsset(assetPath.toFile(), asset.getContentType());
                    assetPath.toFile().delete();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            return newRelease;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyAllReleases(GHRepository from, GHRepository to) {
        logger.info("Starting copying of releases");
        from.listReleases().forEach(ghRelease -> {
            logger.info("copying release: {} to: {}", ghRelease.getName(), to.getFullName());
            copyRelease(ghRelease, to);
        });
        logger.info("Finished copying of releases");
    }

    public static void deleteAllReleases(GHRepository from) {
        try {
            logger.info("Start deleting all releases");
            from.listReleases().toList().forEach(GithubReleaseUtils::deleteReleases);
            logger.info("Finished deleting all releases");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteReleases(GHRelease ghRelease) {
        try {
            logger.info("Deleting release {}", ghRelease.getName());
            ghRelease.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteReleaseAndTag(GHRelease release) {
        try {
            String oldTag = release.getTagName();
            release.delete();
            Objects.requireNonNull(getArchive()).getRef("tags/" + oldTag).delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<GHRelease> listRelease() {
        try {
            return getArchive().listReleases().toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //TODO make a renameRelease
    public static void renameAllReleases() {
        logger.info("Starting renaming");
        //old version.  new version, new jar name server/client
        Map<String, Pair<String, Pair<String, String>>> renamer = new HashMap<>();

        logger.info("Start renaming of releases");
        getArchive().listReleases().forEach(ghRelease -> {
            logger.info("Renaming release: {}", ghRelease.getName());

            String version = FormatConverterUtils.convertFormat(ghRelease.getTagName());
            Path versionPath = Paths.get("downloads/", version);
            FileUtils.makeDir(versionPath);

            Map<Path, String> assets = new HashMap<>();
            Pair<String, String> fileNames = new Pair<>();
            ghRelease.listAssets().forEach(ghAsset -> {
                logger.info("Renaming asset: {}", ghAsset.getName());
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

            GHRelease release = updateRelease(ghRelease, version, releaseName);

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
        logger.info("Finished renaming of releases");

        logger.info("Starting updating of versions.json");

        GHContent ghContent = GithubFileUtils.getFile("versions.json");
        JsonObject json = GithubFileUtils.getFileAsJson(ghContent).asObject();
        JsonArray versions = json.get("versions").asArray();

        versions.forEach(version -> {
            logger.info("Updating version: {}", version.asObject().get("id"));
            JsonObject jsonObject = version.asObject();
            Pair<String, Pair<String, String>> stuff = renamer.get(jsonObject.get("id").asString());
            if (stuff != null) {
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
        logger.info("Finished updating of versions.json");

        System.out.println(json.toString(Stringify.FORMATTED));

        logger.info("Finished renaming");
    }

    private static void updateUrlJson(String version, String fileName, JsonValue value) {
        if (value != null) {
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

}
