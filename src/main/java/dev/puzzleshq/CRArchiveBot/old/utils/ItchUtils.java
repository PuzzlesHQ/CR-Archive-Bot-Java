package dev.puzzleshq.CRArchiveBot.old.utils;

import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItchUtils {
    private static final String ITCH_URL = "https://finalforeach.itch.io/cosmic-reach";  // replace with actual URL
    private static final Pattern VERSION_PATTERN = Pattern.compile("\\d+(\\.\\d+)+");
    private static final Logger itchUtilsLogger = LoggerFactory.getLogger("ItchUtils");

    @Nullable
    public static String fetchLatestItchVersion(Boolean fakePhase) {
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
            String match = matcher.find() ? matcher.group() : null;
            VersionUtils version = new VersionUtils(match);
            String phase = ((version.compareTo(new VersionUtils("0.3.27")) > 0) ? "-alpha" : "-pre_alpha");
            return match + (fakePhase ? phase : "");
        } catch (Exception e) {itchUtilsLogger.info(String.valueOf(e));}
        return null;
    }

    public  static String fetchLatestItchVersion() {
        return fetchLatestItchVersion(true);
    }
}
