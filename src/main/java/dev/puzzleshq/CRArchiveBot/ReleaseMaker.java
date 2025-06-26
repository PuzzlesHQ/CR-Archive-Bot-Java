package dev.puzzleshq.CRArchiveBot;

import dev.puzzleshq.CRArchiveBot.utils.FileUtils;
import dev.puzzleshq.CRArchiveBot.utils.FormatConverterUtils;
import dev.puzzleshq.CRArchiveBot.utils.GithubAssetUtils;
import dev.puzzleshq.CRArchiveBot.utils.GithubUtils;
import org.kohsuke.github.GHRelease;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ReleaseMaker {

    public static void renameAllReleases(){

        GithubUtils.getCRArchive().listReleases().forEach(ghRelease -> {

            String version =  FormatConverterUtils.convertFormat(ghRelease.getTagName());
            Path versionPath = Paths.get("downloads/", version);
            FileUtils.makeDir(versionPath);

            Map<Path, String> assets = new HashMap<>();
            ghRelease.listAssets().forEach(ghAsset -> {
                String fileName = FormatConverterUtils.convertFileNameFormat(ghAsset.getName());
                Path path = GithubAssetUtils.downloadGHAsset(ghAsset, versionPath, fileName, "", true);
                assets.put(path, ghAsset.getContentType());
                try {
                    ghAsset.delete();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });

            String releaseName = version
                    .replaceAll("-", " ")
                    .replaceAll("[+]", " ");

            GHRelease release = GithubUtils.updateRelease(ghRelease, version+ 5, releaseName+ 5);

            assets.forEach((path, contentType) -> {
                try {
                    release.uploadAsset(path.toFile(), contentType);
                    path.toFile().delete();
                    path.getParent().toFile().delete();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            throw new RuntimeException();
        });
    }

    public static void renameRelease() {

    }

}
