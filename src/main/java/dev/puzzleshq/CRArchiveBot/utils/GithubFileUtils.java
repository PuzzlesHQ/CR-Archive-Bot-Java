package dev.puzzleshq.CRArchiveBot.utils;

import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.kohsuke.github.GHContent;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class GithubFileUtils {

    public static JsonValue getFileAsJson(GHContent fileContent) {
        try {
            InputStream in = fileContent.read();
            InputStreamReader isr = new InputStreamReader(in);
            JsonValue jsonValue = JsonObject.readJSON(isr);
            in.close();
            isr.close();
            return jsonValue;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static GHContent getFile(String path) {
        try {
            return GithubUtils.getArchive().getFileContent(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
