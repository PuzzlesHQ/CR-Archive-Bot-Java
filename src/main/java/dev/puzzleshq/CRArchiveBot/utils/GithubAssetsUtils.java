package dev.puzzleshq.CRArchiveBot.utils;

import org.kohsuke.github.GHRelease;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GithubAssetsUtils {

    public static void uploadAssets(GHRelease ghRelease, List<Path> assets) {
        for (Path asset : assets) {
            try {
                ghRelease.uploadAsset(asset.toFile(), Files.probeContentType(asset));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Path asset = Path.of("downloads/cosmic-reach-client-0.4.17-alpha.jar");
        String contentype = Files.probeContentType(asset);
        System.out.println(contentype);
    }

}
