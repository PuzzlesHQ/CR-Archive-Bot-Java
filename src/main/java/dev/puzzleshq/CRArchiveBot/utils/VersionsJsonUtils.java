package dev.puzzleshq.CRArchiveBot.utils;

import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import org.kohsuke.github.GHContent;

import java.io.IOException;

public class VersionsJsonUtils {

    public static JsonObject getVersionsJson(){
        GHContent ghContent = GithubFileUtils.getFile("versions.json");
        return GithubFileUtils.getFileAsJson(ghContent).asObject();
    }





    public static void fixVersionsJson(){
        GHContent ghContent = GithubFileUtils.getFile("versions.json");
        JsonObject json = GithubFileUtils.getFileAsJson(ghContent).asObject();
        JsonArray versions  = json.get("versions").asArray();

        versions.forEach(JsonVersion -> {
            JsonObject version = JsonVersion.asObject();
            JsonValue client = version.get("client");
            if (client != null) {
                JsonObject clientObject = client.asObject();
                String url = clientObject.get("url").asString();
                String newUrl = url.replaceAll("[+]", "%2B");
                clientObject.set("url", newUrl);
            }
            JsonValue server = version.get("server");
            if (server != null) {
                JsonObject serverObject = server.asObject();
                String url = serverObject.get("url").asString();
                String newUrl = url.replaceAll("[+]", "%2B");
                serverObject.set("url", newUrl);
            }
        });


        try {
            ghContent.update(json.toString(Stringify.FORMATTED), "fix url having + instead of %2B");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
