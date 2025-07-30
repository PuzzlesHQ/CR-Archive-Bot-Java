package dev.puzzleshq.CRArchiveBot;

import java.nio.file.Path;

public class Constants {
    // APP STUFF
    public static final String APP_ID = "1399684";

    // MAIN ARCHIVE
    public static String archiveOrg = "PuzzlesHQ";
    public static String archiveRepo = "CRArchive";

    // TEST ARCHIVE
    public static String testOrg = "PuzzlesHQ";
    public static String testArchiveRepo = "TestCRArchive";

    // DISCORD BOT
    public static String serverID = "1269396617514188915";
    public static String channelID = "1399916671581290576";

    // SETTINGS
    public static boolean isTest = true;

    // Paths
    public static Path downloadPath = Path.of("downloads/");

}
