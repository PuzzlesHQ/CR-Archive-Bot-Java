package dev.puzzleshq.CRArchiveBot.old;

public class Main {
    public static final String GITHUB_REPO = "PuzzlesHQ/CRArchive";
    public static final String GITHUB_BRANCH = "test";
    public static final String TOKEN;


    public static void main(String[] args) throws Exception {
        dev.puzzleshq.CRArchiveBot.Main.main(args);
//        Bot.runArchiveBot();
//        ImageServer.runImageServer();

    }

    static {
        try {
            TOKEN = TokenFetcher.getToken();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}