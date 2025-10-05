package dev.puzzleshq.CRArchiveBot;

import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class CRArchiveUpdater {

    public static void updateCRArchive(String changelog, String version, List<Message.Attachment> imageList) {
        if (checkForUpdates()) {
            imageList.getFirst().getUrl();

            // make release
//            GithubReleaseUtils.makeNewRelease();
        }
    }

    public static boolean checkForUpdates() {
        return false;
    }

    public static boolean getChangelog() {
        return false;
        /*
         * ![](https://img.itch.zone/aW1nLzIyODc1MzM4LnBuZw==/original/h9TOi%2B.png)
         * <p>
         * - Added brick variants for Sandstone, Basalt, Gabbro, and Limestone
         * - Added the Remote Control, which is an item that can rename drones
         *   - In future updates, it will be able to change drone's behaviours
         * - Potential fix for the crash when you pick up items
         * - Fixed water being invisible in inventory
         * - Fixed multiplayer crashing when breaking a missing block
         * - Fixed multiplayer crashing when hitting the planteater
         */
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

    public static boolean getVersionJar() {
        return true;
    }
}
