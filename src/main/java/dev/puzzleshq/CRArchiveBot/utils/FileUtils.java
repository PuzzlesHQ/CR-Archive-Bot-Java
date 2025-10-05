package dev.puzzleshq.CRArchiveBot.utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

    public static InputStream getInternalFile(String fileName) {
        ClassLoader classLoader = FileUtils.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }
    }

    public static String getInternalFileAsString_US_ASCII(String fileName) throws IOException {
        return IOUtils.toString(FileUtils.getInternalFile(fileName), StandardCharsets.US_ASCII); //US_ASCII
    }

    public static Path makeDir(Path path) {
        try {
            return Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path makeDir(String path) {
        try {
            return Files.createDirectories(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
