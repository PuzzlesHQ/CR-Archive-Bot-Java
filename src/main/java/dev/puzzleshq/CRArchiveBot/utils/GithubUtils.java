package dev.puzzleshq.CRArchiveBot.utils;

import org.jetbrains.annotations.Nullable;
import org.kohsuke.github.*;
import org.kohsuke.github.extras.authorization.JWTTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.puzzleshq.CRArchiveBot.Constants.*;

public class GithubUtils {
    private static final Logger logger = LoggerFactory.getLogger("GithubUtils");

    // repo name, GHRepository
    private static Map<String, GHRepository> ghRepositories = new HashMap<>();
    private static GHAuthenticatedAppInstallation ghAppInstallation;

    public static void init() {
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
    public static GHRepository getArchive() {
        if (isTest) return getTestArchive();
        else return getMainArchive();
    }

    @Nullable
    public static GHRepository getMainArchive() {
        return getRepo(archiveOrg, archiveRepo);
    }

    @Nullable
    public static GHRepository getTestArchive() {
        return getRepo(testOrg, testArchiveRepo);
    }

    @Nullable
    public static GHRepository getRepo(String orgName, String repoName) {
        if (ghAppInstallation == null) {
            throw new RuntimeException("call init you fucking idiot");
        }
        String fullName = orgName + "/" + repoName;
        if (ghRepositories.containsKey(fullName)) {
            return ghRepositories.get(fullName);
        }

        List<GHRepository> list;
        try {
            list = ghAppInstallation.listRepositories().toList();
            for (GHRepository repo : list) {
                String name = repo.getFullName();
                if (name.equals(fullName)) {
                    ghRepositories.put(fullName, repo);
                    return repo;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static GHApp getTmpGitHubApp() {
        try {
            return new GitHubBuilder()
                    .withAuthorizationProvider(getJWTTokenProvider())
                    .build()
                    .getApp();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static JWTTokenProvider getJWTTokenProvider() {
        try {
            return new JWTTokenProvider(APP_ID, dev.puzzleshq.CRArchiveBot.utils.FileUtils.getInternalFileAsString_US_ASCII("key.pem"));
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}
