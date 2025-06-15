package dev.puzzleshq;

import dev.puzzleshq.utils.GithubUtils;
import dev.puzzleshq.utils.ItchUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Bot {
    private static final Logger botLogger = LoggerFactory.getLogger("Archive Keeper");

    public static void runArchiveBot() throws Exception {
        // Lando's charger is a fucking bastard, and he really fucking hates it
        // To be continued
        if (!Objects.equals(ItchUtils.fetchLatestItchVersion(true), GithubUtils.fetchLatestRelease())) {
            botLogger.info("Version mismatch");
        } else {botLogger.info("Versions match");}
        botLogger.info("{}:{}", ItchUtils.fetchLatestItchVersion(), GithubUtils.fetchLatestRelease());
    }
}
