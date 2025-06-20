package dev.puzzleshq.CRArchiveBot.utils;

import dev.puzzleshq.CRArchiveBot.Constants;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.github.*;
import org.kohsuke.github.extras.authorization.JWTTokenProvider;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import static dev.puzzleshq.CRArchiveBot.Constants.*;

public class GithubUtils {

    private static GHAuthenticatedAppInstallation ghAppInstallation;

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
