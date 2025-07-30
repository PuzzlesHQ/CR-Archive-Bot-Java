package dev.puzzleshq.CRArchiveBot.utils;

import dev.puzzleshq.CRArchiveBot.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ButlerUtils {
    // BUTLER
    private static final String BUTLER_URL = "https://broth.itch.ovh/butler/windows-amd64/LATEST/archive/default";

    @Nullable
    private static URL getInstallURL() {
        String os = "";
        String arch = System.getProperty("os.arch");

        if (System.getProperty("os.name").toLowerCase().contains("windows")) os = "windows";
        else if (System.getProperty("os.name").toLowerCase().contains("linux")) os = "linux";

        URI uri = URI.create("https://broth.itch.ovh/butler/"+ os +"-"+ arch +"/LATEST/archive/default");
        URL url = null;
        try {
             url = uri.toURL();
        } catch (Exception e){
            e.printStackTrace();
        }
        return url;
    }

    // TODO make work the link is a Redirect
    private static Path downloadButler() throws IOException {
//        Path filePath = Constants.downloadPath.resolve("butler/butlerZip.zip");
        Path extractPath = Constants.downloadPath.toAbsolutePath();
        Files.createDirectories(extractPath);

        Path tempZip = Files.createTempFile("butlerZip-", ".zip");

        try (
                ReadableByteChannel rbc = Channels.newChannel(getInstallURL().openStream());
                FileOutputStream fos = new FileOutputStream(tempZip.toFile())
        ) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
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
        try {
            return downloadButler();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
        System.out.println(installButler());
    }

}
