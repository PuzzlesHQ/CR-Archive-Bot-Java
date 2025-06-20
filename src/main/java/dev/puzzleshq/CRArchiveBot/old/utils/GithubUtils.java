package dev.puzzleshq.CRArchiveBot.old.utils;

import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static dev.puzzleshq.CRArchiveBot.old.Main.GITHUB_REPO;

public class GithubUtils {

    public static String fetchLatestRelease(Boolean asTitle) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.github.com/repos/" + GITHUB_REPO + "/releases/latest"))
                .header("Accept", "application/vnd.github+json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch release: " + response.statusCode());
        }

        JsonObject json = JsonValue.readJSON(response.body()).asObject();
        return ( asTitle ? json.get("name").asString() : json.get("tag_name").asString() ); // or "name" depending on format
    }

    public static String fetchLatestRelease() throws Exception {
        return fetchLatestRelease(false);
    }

}
