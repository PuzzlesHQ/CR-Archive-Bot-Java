package dev.puzzleshq.CRArchiveBot.utils;

import dev.puzzleshq.CRArchiveBot.Constants;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.github.*;
import org.kohsuke.github.extras.authorization.JWTTokenProvider;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Objects;

import static dev.puzzleshq.CRArchiveBot.Constants.*;

public class GithubUtils {

    private static GHAuthenticatedAppInstallation ghAppInstallation;

    public static GHRelease makeRelease(String tag, String releaseName, String releaseBody){
        try {
            return GithubUtils.getCRArchive().createRelease(tag)
                    .name(releaseName)
                    .body(releaseBody)
                    .create();
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
                Objects.requireNonNull(getCRArchive()).getRef("tags/" + oldTag).delete();
            }
            return newRelease;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static GHRepository getCRArchive(){
        if (ghAppInstallation == null){
            ghAppInstallation = getGitHubAppInstallation();
        }
        PagedIterator<GHRepository> iterable = ghAppInstallation.listRepositories()._iterator(0);
        while (iterable.hasNext()){
            GHRepository repo = iterable.next();
            String repoName = repo.getFullName();
            if (repoName.equals(Constants.org +"/"+ Constants.repo)) return repo;
        }
        return null;
    }

    private static GHAuthenticatedAppInstallation getGitHubAppInstallation(){
        try {
            return new GitHubBuilder()
                    .withAppInstallationToken(getTmpCRArchiveInstallationToken().getToken())
                    .build()
                    .getInstallation();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static GHAppInstallationToken getTmpCRArchiveInstallationToken(){
        try {
            return getTmpCRArchiveInstallation().createToken().repositories(List.of(repo)).create();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static GHAppInstallation getTmpCRArchiveInstallation(){
        try {
            return getTmpGitHubApp().getInstallationByRepository(org, repo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
