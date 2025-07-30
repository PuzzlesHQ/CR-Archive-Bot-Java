package dev.puzzleshq.CRArchiveBot;

import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class CRArchiveUpdater {

    public static void updateCRArchive(String changelog, String version, List<Message.Attachment> imageList){
        if (checkForUpdates()){
            // get info
            getChangelog();

            // make release
            GithubReleaseUtils.makeNewRelease();
        }
    }

    public static boolean checkForUpdates() {
        return false;
    }

    public static boolean getChangelog() {
        return false;
    }

//    headers = {
//              "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0 Safari/537.36",
//              "Accept": "application/json"
//           }

    //client https://finalforeach.itch.io/cosmic-reach/file/9891067
    //server https://finalforeach.itch.io/cosmic-reach/file/11884793

    // String token = System.getenv("DISCORD_TOKEN");
    //        if (token == null || token.isEmpty()) {
    //            throw new IllegalStateException("DISCORD_TOKEN not set");
    //        }

    public static boolean getVersionJar(){
        return true;
    }
}
