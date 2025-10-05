package dev.puzzleshq.CRArchiveBot.utils;

import org.kohsuke.github.GHAsset;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class GithubAssetUtils {

    /**
     * Downloads the {@link GHAsset} to the downloads folder as the assets file name.
     *
     * @param asset            The {@link GHAsset} to download.
     * @param REPLACE_EXISTING Replace an existing file if it exists.
     * @return The {@link Path} of the downloaded file.
     */
    public static Path downloadGHAsset(GHAsset asset, boolean REPLACE_EXISTING) {
        try {
            InputStream in = URI.create(asset.getBrowserDownloadUrl()).toURL().openStream();
            Path filePath = Paths.get("downloads/", asset.getName());
            if (!filePath.getParent().toFile().exists()) {
                filePath.toFile().mkdirs();
            }
            if (REPLACE_EXISTING) {
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(in, filePath);
            }
            in.close();
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Downloads the {@link GHAsset} to the {@link Path} set, saved as the assets file name.
     *
     * @param asset            The {@link GHAsset} to download.
     * @param path             The {@link Path} the Assets is to be downloaded to.
     * @param REPLACE_EXISTING Replace an existing file if it exists.
     * @return The {@link Path} of the downloaded file.
     */
    public static Path downloadGHAsset(GHAsset asset, Path path, boolean REPLACE_EXISTING) {
        try {
            InputStream in = URI.create(asset.getBrowserDownloadUrl()).toURL().openStream();
            Path filePath = Paths.get(path.toString(), asset.getName());
            if (!filePath.getParent().toFile().exists()) {
                filePath.toFile().mkdirs();
            }
            if (REPLACE_EXISTING) {
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(in, filePath);
            }
            in.close();
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Downloads the {@link GHAsset} to the {@link Path} and fileName you set but not the fileType.
     *
     * @param asset            The {@link GHAsset} to download.
     * @param path             The {@link Path} the Assets is to be downloaded to.
     * @param fileName         The fileName the Assets is to be saved as.
     * @param REPLACE_EXISTING Replace an existing file if it exists.
     * @return The {@link Path} of the downloaded file.
     */
    public static Path downloadGHAsset(GHAsset asset, Path path, String fileName, boolean REPLACE_EXISTING) {
        try {
            InputStream in = URI.create(asset.getBrowserDownloadUrl()).toURL().openStream();
            String newFileName = fileName + asset.getName().substring(asset.getName().lastIndexOf('.'));
            Path filePath = Paths.get(path.toString(), newFileName);
            if (!filePath.getParent().toFile().exists()) {
                filePath.toFile().mkdirs();
            }
            if (REPLACE_EXISTING) {
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(in, filePath);
            }
            in.close();
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Downloads the {@link GHAsset} to the given {@link Path}, saving it as the provided file name and with the file type.
     *
     * @param asset            The {@link GHAsset} to download.
     * @param path             The {@link Path} the Assets is to be downloaded to.
     * @param fileName         The fileName the Assets is to be saved as.
     * @param filetype         The file extension or type of the file.
     * @param REPLACE_EXISTING Replace an existing file if it exists.
     * @return The {@link Path} of the downloaded file.
     */
    public static Path downloadGHAsset(GHAsset asset, Path path, String fileName, String filetype, boolean REPLACE_EXISTING) {
        try {
            InputStream in = URI.create(asset.getBrowserDownloadUrl()).toURL().openStream();
            String newFileName = fileName + filetype;
            Path filePath = Paths.get(path.toString(), newFileName);
            if (!filePath.getParent().toFile().exists()) {
                filePath.toFile().mkdirs();
            }
            if (REPLACE_EXISTING) {
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(in, filePath);
            }
            in.close();
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Downloads the {@link GHAsset} to the downloads folder and fileName you set.
     *
     * @param asset            The {@link GHAsset} to download.
     * @param fileName         The fileName the Assets is to be saved as.
     * @param REPLACE_EXISTING Replace an existing file if it exists.
     * @return The {@link Path} of the downloaded file.
     */
    public static Path downloadGHAsset(GHAsset asset, String fileName, boolean REPLACE_EXISTING) {
        try {
            InputStream in = URI.create(asset.getBrowserDownloadUrl()).toURL().openStream();
            String newFileName = fileName + asset.getName().substring(asset.getName().lastIndexOf('.'));
            Path filePath = Paths.get("downloads/", newFileName);
            if (!filePath.getParent().toFile().exists()) {
                filePath.toFile().mkdirs();
            }
            if (REPLACE_EXISTING) {
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(in, filePath);
            }
            in.close();
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Downloads the {@link GHAsset} to the given the downloads folder, saving it as the provided file name and with the file type.
     *
     * @param asset            The {@link GHAsset} to download.
     * @param fileName         The fileName the Assets is to be saved as.
     * @param REPLACE_EXISTING Replace an existing file if it exists.
     * @return The {@link Path} of the downloaded file.
     */
    public static Path downloadGHAsset(GHAsset asset, String fileName, String filetype, boolean REPLACE_EXISTING) {
        try {
            InputStream in = URI.create(asset.getBrowserDownloadUrl()).toURL().openStream();
            String newFileName = fileName + filetype;
            Path filePath = Paths.get("downloads/", newFileName);
            if (!filePath.getParent().toFile().exists()) {
                filePath.toFile().mkdirs();
            }
            if (REPLACE_EXISTING) {
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(in, filePath);
            }
            in.close();
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Downloads the {@link GHAsset} to the {@link Path} in the downloads folder, saved as the assets file name.
     *
     * @param asset            The {@link GHAsset} to download.
     * @param path             The path in the downloads folder to download to.
     * @param REPLACE_EXISTING Replace an existing file if it exists.
     * @return The {@link Path} of the downloaded file.
     */
    public static Path downloadInDownloadsGHAsset(GHAsset asset, Path path, boolean REPLACE_EXISTING) {
        try {
            InputStream in = URI.create(asset.getBrowserDownloadUrl()).toURL().openStream();
            Path filePath = Paths.get("downloads/", path.toString(), asset.getName());
            if (!filePath.getParent().toFile().exists()) {
                filePath.toFile().mkdirs();
            }
            if (REPLACE_EXISTING) {
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(in, filePath);
            }
            in.close();
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Downloads the {@link GHAsset} to the {@link Path} in the download folder and fileName you set.
     *
     * @param asset            The {@link GHAsset} to download.
     * @param path             The {@link Path} the Assets is to be downloaded to.
     * @param fileName         The fileName the Assets is to be saved as.
     * @param REPLACE_EXISTING Replace an existing file if it exists.
     * @return The {@link Path} of the downloaded file.
     */
    public static Path downloadInDownloadsGHAsset(GHAsset asset, Path path, String fileName, boolean REPLACE_EXISTING) {
        try {
            InputStream in = URI.create(asset.getBrowserDownloadUrl()).toURL().openStream();
            String newFileName = fileName + asset.getName().substring(asset.getName().lastIndexOf('.'));
            Path filePath = Paths.get("downloads/", path.toString(), newFileName);
            if (!filePath.getParent().toFile().exists()) {
                filePath.toFile().mkdirs();
            }
            if (REPLACE_EXISTING) {
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(in, filePath);
            }
            in.close();
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Downloads the {@link GHAsset} to the given {@link Path} in the download folder, saving it as the provided file name and with the file type.
     *
     * @param asset            The {@link GHAsset} to download.
     * @param path             The {@link Path} the Assets is to be downloaded to.
     * @param fileName         The fileName the Assets is to be saved as.
     * @param filetype         The file extension or type of the file.
     * @param REPLACE_EXISTING Replace an existing file if it exists.
     * @return The {@link Path} of the downloaded file.
     */
    public static Path downloadInDownloadsGHAsset(GHAsset asset, Path path, String fileName, String filetype, boolean REPLACE_EXISTING) {
        try {
            InputStream in = URI.create(asset.getBrowserDownloadUrl()).toURL().openStream();
            String newFileName = fileName + filetype;
            Path filePath = Paths.get("downloads/", path.toString(), newFileName);
            if (!filePath.getParent().toFile().exists()) {
                filePath.toFile().mkdirs();
            }
            if (REPLACE_EXISTING) {
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(in, filePath);
            }
            in.close();
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Downloads the {@link GHAsset} to the downloads folder as the assets file name.
     *
     * @param asset The {@link GHAsset} to download.
     * @return The {@link Path} of the downloaded file.
     */
    public static Path downloadGHAsset(GHAsset asset) {
        return downloadGHAsset(asset, false);
    }

    /**
     * Downloads the {@link GHAsset} to the {@link Path} set, saved as the assets file name.
     *
     * @param asset The {@link GHAsset} to download.
     * @param path  The {@link Path} the Assets is to be downloaded to.
     * @return The {@link Path} of the downloaded file.
     */
    public static Path downloadGHAsset(GHAsset asset, Path path) {
        return downloadGHAsset(asset, path, false);
    }

    /**
     * Downloads the {@link GHAsset} to the {@link Path} and fileName you set but not the fileType.
     *
     * @param asset    The {@link GHAsset} to download.
     * @param path     The {@link Path} the Assets is to be downloaded to.
     * @param fileName The fileName the Assets is to be saved as.
     * @return The {@link Path} of the downloaded file.
     */
    public static Path downloadGHAsset(GHAsset asset, Path path, String fileName) {
        return downloadGHAsset(asset, path, fileName, false);
    }

    /**
     * Downloads the {@link GHAsset} to the given {@link Path}, saving it as the provided file name and with the file type.
     *
     * @param asset    The {@link GHAsset} to download.
     * @param path     The {@link Path} the Assets is to be downloaded to.
     * @param fileName The fileName the Assets is to be saved as.
     * @param filetype The file extension or type of the file.
     * @return The {@link Path} of the downloaded file.
     */
    public static Path downloadGHAsset(GHAsset asset, Path path, String fileName, String filetype) {
        return downloadGHAsset(asset, path, fileName, filetype, false);
    }

    /**
     * Downloads the {@link GHAsset} to the downloads folder and fileName you set.
     *
     * @param asset    The {@link GHAsset} to download.
     * @param fileName The fileName the Assets is to be saved as.
     * @return The {@link Path} of the downloaded file.
     */
    public static Path downloadGHAsset(GHAsset asset, String fileName) {
        return downloadGHAsset(asset, fileName, false);
    }

    /**
     * Downloads the {@link GHAsset} to the given the downloads folder, saving it as the provided file name and with the file type.
     *
     * @param asset    The {@link GHAsset} to download.
     * @param fileName The fileName the Assets is to be saved as.
     * @return The {@link Path} of the downloaded file.
     */
    public static Path downloadGHAsset(GHAsset asset, String fileName, String filetype) {
        return downloadGHAsset(asset, fileName, filetype, false);
    }

    /**
     * Downloads the {@link GHAsset} to the {@link Path} in the downloads folder, saved as the assets file name.
     *
     * @param asset The {@link GHAsset} to download.
     * @param path  The path in the downloads folder to download to.
     * @return The {@link Path} of the downloaded file.
     */
    public static Path downloadInDownloadsGHAsset(GHAsset asset, Path path) {
        return downloadInDownloadsGHAsset(asset, path, false);
    }

    /**
     * Downloads the {@link GHAsset} to the {@link Path} in the download folder and fileName you set.
     *
     * @param asset    The {@link GHAsset} to download.
     * @param path     The {@link Path} the Assets is to be downloaded to.
     * @param fileName The fileName the Assets is to be saved as.
     * @return The {@link Path} of the downloaded file.
     */
    public static Path downloadInDownloadsGHAsset(GHAsset asset, Path path, String fileName) {
        return downloadInDownloadsGHAsset(asset, path, fileName, false);
    }

    /**
     * Downloads the {@link GHAsset} to the given {@link Path} in the download folder, saving it as the provided file name and with the file type.
     *
     * @param asset    The {@link GHAsset} to download.
     * @param path     The {@link Path} the Assets is to be downloaded to.
     * @param fileName The fileName the Assets is to be saved as.
     * @param filetype The file extension or type of the file.
     * @return The {@link Path} of the downloaded file.
     */
    public static Path downloadInDownloadsGHAsset(GHAsset asset, Path path, String fileName, String filetype) {
        return downloadInDownloadsGHAsset(asset, path, fileName, filetype, false);
    }


}
