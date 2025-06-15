package dev.puzzleshq.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {
    public static String calculateFileHash(Path filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(filePath.toFile()), digest)) {
            byte[] buffer = new byte[4096];
            while (dis.read(buffer) != -1) { /* digest is updated automatically */ }
        }
        byte[] hashBytes = digest.digest();
        return bytesToHex(hashBytes);
    }

    // Convert bytes to hex string (lowercase)
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Get file size in bytes
    public static long getFileSize(Path filePath) throws IOException {
        return Files.size(filePath);
    }
}
