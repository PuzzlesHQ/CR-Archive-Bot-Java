package dev.puzzleshq.CRArchiveBot;

import dev.puzzleshq.CRArchiveBot.utils.FileUtils;
import dev.puzzleshq.CRArchiveBot.utils.FormatConverterUtils;
import dev.puzzleshq.CRArchiveBot.utils.GithubAssetUtils;
import dev.puzzleshq.CRArchiveBot.utils.GithubUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class ReleaseMaker {

    public static void renameAllReleases(){
        StringBuilder builder = new StringBuilder();

        GithubUtils.getCRArchive().listReleases().forEach(ghRelease -> {

            String version = ghRelease.getTagName().replaceAll("-alpha", "").replaceAll("-pre_alpha", "");
            Path versionPath = Paths.get("downloads/", version);
            FileUtils.makeDir(versionPath);

            ghRelease.listAssets().forEach(ghAsset -> {

                builder.append(FormatConverterUtils.convertFileNameFormat(ghAsset.getName()));
                builder.append("\n");

            });
        });

        try {
            Files.writeString(Path.of("version.txt"), builder.toString(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void renameRelease() {

    }

}
