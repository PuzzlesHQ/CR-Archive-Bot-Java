package dev.puzzleshq.CRArchiveBot;

import dev.puzzleshq.CRArchiveBot.utils.GithubAssetUtils;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHAsset;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GithubAssetUtilsTest {
    // Fake Github
    Path githubAssetPath = Path.of("repo/");

    // Test asset
    String tempFileName = "cosmic-reach-client-0.3.2-pre_alpha";
    String tempFileExtension = ".jar";
    String tempFileContent = "Test content";

    // Test out asset
    String newFileName = "newFileName";
    String newFileExtension = ".jar";

    Path NewSubFolder = Paths.get("tesSubFolder");
    Path NewFolder = Paths.get("newFolder");


    public GHAsset makeFakeAsset() throws IOException {
        GHAsset asset = Mockito.mock(GHAsset.class);

        Path tempFile = new File(githubAssetPath.toFile(), tempFileName + tempFileExtension).toPath();
        System.out.println(tempFile.toFile());
        System.out.println(tempFile.toFile().exists());
        if (!tempFile.toFile().exists()) {
            tempFile.toFile().mkdirs();
            tempFile.toFile().createNewFile();
        }

//        Path tempFile = Files.createFile(githubAssetPath, tempFileName, tempFileExtension);
        tempFile.toFile().deleteOnExit();
        Files.writeString(tempFile, tempFileContent);
        String fileUri = tempFile.toUri().toString();

        Mockito.when(asset.getBrowserDownloadUrl()).thenReturn(fileUri);
        Mockito.when(asset.getName()).thenReturn(tempFile.toFile().getName());

        return asset;
    }

    @Test
    void downloadAssetTest() throws IOException {
        GHAsset asset = makeFakeAsset();

        Path resultPath = GithubAssetUtils.downloadGHAsset(asset);

        assertTrue(Files.exists(resultPath), "Downloaded file does not exist");

        String content = Files.readString(resultPath);
        assertEquals("Test content", content, "File content does not match");

        Files.deleteIfExists(resultPath);

        System.out.println(resultPath);

//        Files.deleteIfExists(resultPath.getParent());


    }

}
