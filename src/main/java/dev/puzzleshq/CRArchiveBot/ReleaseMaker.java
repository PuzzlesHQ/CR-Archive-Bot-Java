package dev.puzzleshq.CRArchiveBot;

import dev.puzzleshq.CRArchiveBot.utils.GithubAssetUtils;
import dev.puzzleshq.CRArchiveBot.utils.GithubUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ReleaseMaker {

    public static void renameAllReleases(){
        GithubUtils.getCRArchive().listReleases().forEach(ghRelease -> {

            String version = ghRelease.getTagName().replaceAll("-alpha", "").replaceAll("-pre_alpha", "");
            Path versionPath = Paths.get("downloads/", version);
            try {
                Files.createDirectories(versionPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            
            ghRelease.listAssets().forEach(ghAsset -> {
//                try {
//                    InputStream in = URI.create(ghAsset.getBrowserDownloadUrl()).toURL().openStream();
//                    Files.copy(in, Paths.get(String.valueOf(versionPath), version, ".jar"), StandardCopyOption.REPLACE_EXISTING);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                System.out.println(ghAsset.getContentType());
//                System.out.println(ghAsset.getBrowserDownloadUrl());

                GithubAssetUtils.downloadGHAsset(ghAsset);

            });
        });
    }

    public static void renameRelease() {

    }

}
