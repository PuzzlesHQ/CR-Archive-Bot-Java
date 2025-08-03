package dev.puzzleshq.CRArchiveBot.utils;

import dev.puzzleshq.CRArchiveBot.Constants;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static dev.puzzleshq.CRArchiveBot.utils.RedirectUtil.getRedirectLocation;

public class ButlerUtils {
    // BUTLER
    private static @NotNull URL getInstallURL() throws Exception {
        String os;
        String arch = System.getProperty("os.arch");

        if (arch.equals("x86_64")) arch = "amd64";
        else if (arch.equals("aarch64")) arch = "arm64";

        if (System.getProperty("os.name").toLowerCase().contains("windows")) os = "windows";
        else if (System.getProperty("os.name").toLowerCase().contains("linux")) os = "linux";
        else throw new IOException("Unsupported OS");

        URI uri = URI.create("https://broth.itch.ovh/butler/" + os + "-" + arch + "/LATEST/archive/default");

        return Objects.requireNonNull(getRedirectLocation(uri.toURL()));
    }

    private static Path downloadButler() throws IOException {
//        Path filePath = Constants.downloadPath.resolve("butler/butlerZip.zip");
        Path extractPath = Constants.downloadPath.resolve("butler/").toAbsolutePath();
        Files.createDirectories(extractPath);

        Path tempZip = Files.createTempFile("butlerZip-", ".zip");
        System.out.println(tempZip);

        try (
                ReadableByteChannel rbc = Channels.newChannel(Objects.requireNonNull(getInstallURL()).openStream());
                FileOutputStream fos = new FileOutputStream(tempZip.toFile())
        ) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try (
                FileInputStream fis = new FileInputStream(tempZip.toFile());
                ZipInputStream zis = new ZipInputStream(fis)
        ) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = extractPath.resolve(entry.getName()).normalize();

                if (!entryPath.startsWith(extractPath)) {
                    throw new IOException("Zip entry is outside the target dir: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }

                zis.closeEntry();
            }
        }

        return extractPath;
    }


    public static Path installButler() {
        Path path = Constants.downloadPath.resolve("butler/").toAbsolutePath();
        if (Files.exists(path.resolve("butler.exe"))) return path;
        try {
            return downloadButler();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Running Butler...");
        ProcessBuilder pb = new ProcessBuilder(installButler().resolve("butler.exe").toString(), "status", "finalforeach/cosmic-reach", "--json");
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        process.waitFor();
    }

}
