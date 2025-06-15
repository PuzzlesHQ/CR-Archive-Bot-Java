package dev.puzzleshq;

public class Main {
    public static final String GITHUB_REPO = "PuzzlesHQ/CRArchive";

    public static void main(String[] args) throws Exception {
        Bot.runArchiveBot();
        ImageServer.runImageServer();
    }
}