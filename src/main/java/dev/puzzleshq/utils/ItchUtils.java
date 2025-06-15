package dev.puzzleshq.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ItchUtils {
    private static final String ITCH_URL = "https://finalforeach.itch.io/cosmic-reach";  // replace with actual URL
    private static final Pattern VERSION_PATTERN = Pattern.compile("\\d+(\\.\\d+)+");

    @Nullable
    public static String fetchLatestItchVersion() {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ITCH_URL))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.out.println("Failed to fetch: HTTP " + response.statusCode());
                return null;
            }
            Document doc = Jsoup.parse(response.body());
            Element versionSpan = doc.selectFirst("span.version_name");

            if (versionSpan == null) {
                return null;
            }

            Matcher matcher = VERSION_PATTERN.matcher(versionSpan.text());
            return matcher.find() ? matcher.group() : null;
        } catch (Exception _) {}
        return null;
    }
}
