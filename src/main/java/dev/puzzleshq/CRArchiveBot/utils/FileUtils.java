package dev.puzzleshq.CRArchiveBot.utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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

    public static String getInternalFileAsString_US_ASCII (String fileName) throws IOException {
        return IOUtils.toString(FileUtils.getInternalFile(fileName), StandardCharsets.US_ASCII); //US_ASCII
    }
}
