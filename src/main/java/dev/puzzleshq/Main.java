package dev.puzzleshq;

import dev.puzzleshq.utils.StringUtils;

import static dev.puzzleshq.Bot.fetchChangelog;

public class Main {
    public static final String GITHUB_REPO = "PuzzlesHQ/CRArchive";
    public static final String GITHUB_BRANCH = "test";
    public static final String TOKEN;

    static {
        try {
            TOKEN = TokenFetcher.getToken();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
//        Bot.runArchiveBot();
        ImageServer.runImageServer();

    }
}