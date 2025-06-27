package dev.puzzleshq.CRArchiveBot.utils;

import org.jetbrains.annotations.Nullable;
import org.kohsuke.github.*;
import org.kohsuke.github.extras.authorization.JWTTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static dev.puzzleshq.CRArchiveBot.Constants.*;

public class GithubUtils {
    private static final Logger logger = LoggerFactory.getLogger("GithubUtils");

    // repo name, GHRepository
    private static Map<String, GHRepository> ghRepositories = new HashMap<>();
    private static GHAuthenticatedAppInstallation ghAppInstallation;

    public static GHRelease makeRelease(String tag, String releaseName, String releaseBody){
        try {
            return GithubUtils.getArchive().createRelease(tag)
                    .name(releaseName)
                    .body(releaseBody)
                    .create();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static GHRelease copyRelease(GHRelease release){
        return copyRelease(release, Objects.requireNonNull(getArchive()));
    }

    public static GHRelease copyRelease(GHRelease release, GHRepository to){
        try {
            GHRelease newRelease = to.createRelease(release.getTagName())
                    .name(release.getName())
                    .body(release.getBody())
                    .create();

            release.getAssets().forEach(asset -> {
                try {
                    Path assetPath = GithubAssetUtils.downloadGHAsset(asset);
                    newRelease.uploadAsset(assetPath.toFile(), asset.getContentType());
                    assetPath.toFile().delete();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            return newRelease;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static GHRelease updateRelease(GHRelease release, String tag, String releaseName){
        try {
            String oldTag = release.getTagName();
            GHRelease newRelease = release.update()
                    .tag(tag)
                    .name(releaseName)
                    .update();

            if (!Objects.equals(oldTag, tag)){
                Objects.requireNonNull(getArchive()).getRef("tags/" + oldTag).delete();
            }
            return newRelease;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteRelease(GHRelease release){
        try {
            String oldTag = release.getTagName();
            release.delete();
            Objects.requireNonNull(getArchive()).getRef("tags/" + oldTag).delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void init(){
        logger.info("initialising");
        GHApp app = getTmpGitHubApp();
        try {
            GHAppInstallation appInstallation = app.getInstallationByOrganization(archiveOrg);
            GHAppInstallationToken ghAppInstallationToken = appInstallation.createToken().create();

            ghAppInstallation = new GitHubBuilder()
                    .withAppInstallationToken(ghAppInstallationToken.getToken())
                    .build()
                    .getInstallation();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger.info("initialized");
    }

    @Nullable
    public static GHRepository getArchive(){
        if (isTest) return getTestArchive();
        else return getMainArchive();
    }

    @Nullable
    public static GHRepository getMainArchive(){
        return getRepo(archiveOrg, archiveRepo);
    }

    @Nullable
    public static GHRepository getTestArchive(){
        return getRepo(testOrg, testArchiveRepo);
    }

    @Nullable
    public static GHRepository getRepo(String orgName, String repoName){
        if (ghAppInstallation == null){
            throw new RuntimeException("call init you fucking idiot");
        }
        String fullName = orgName + "/" + repoName;
        if (ghRepositories.containsKey(fullName)){
            return ghRepositories.get(fullName);
        }

        List<GHRepository> list;
        try {
            list = ghAppInstallation.listRepositories().toList();
            for (GHRepository repo : list){
                String name = repo.getFullName();
                if (name.equals(fullName)){
                    ghRepositories.put(fullName, repo);
                    return repo;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static GHApp getTmpGitHubApp(){
        try {
            return new GitHubBuilder()
                    .withAuthorizationProvider(getJWTTokenProvider())
                    .build()
                    .getApp();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static JWTTokenProvider getJWTTokenProvider(){
        try {
            return new JWTTokenProvider(APP_ID, dev.puzzleshq.CRArchiveBot.utils.FileUtils.getInternalFileAsString_US_ASCII("key.pem"));
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}
