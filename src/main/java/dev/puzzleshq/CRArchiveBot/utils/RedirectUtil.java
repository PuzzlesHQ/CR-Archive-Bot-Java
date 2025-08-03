package dev.puzzleshq.CRArchiveBot.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

public class RedirectUtil {
    public static URL getRedirectLocation(URL url) throws IOException, URISyntaxException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("HEAD");
        conn.connect();

        int status = conn.getResponseCode();
        if (status >= 300 && status < 400) {
            String loc = conn.getHeaderField("Location");
            if (loc == null) return null;
            URL next = url.toURI().resolve(loc).toURL();
            return getRedirectLocation(next);
        }
        return url;
    }
}

