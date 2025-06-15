package dev.puzzleshq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static dev.puzzleshq.utils.GithubUtils.fetchLatestRelease;
import static dev.puzzleshq.utils.ItchUtils.fetchLatestItchVersion;

import io.javalin.Javalin;

public class ImageServer {
    private static final Logger imageServerLogger = LoggerFactory.getLogger("ImageServer");
    private static volatile String cachedVersion = "loading...";
    private static final Thread imageServerThread;
    private static volatile boolean stopImageServer = false;

    static {
        imageServerThread = new Thread(() -> {
            Javalin app = Javalin.create().start(8080);


            app.get("/status.svg", ctx -> {
                String version = cachedVersion;
                try {
                    String latest = fetchLatestRelease(true);
                    if (!latest.equals(version)) {
                        cachedVersion = latest;
                        version = latest;
                    }
                } catch (Exception e) {
                    imageServerLogger.error("Houston we have a problem", e);
                }

                String shieldsUrl = "https://img.shields.io/static/v1?label=Version&message=" + version.replaceAll(" ", "%20") + "&color=blue";
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(shieldsUrl).openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(3000);
                    conn.setReadTimeout(3000);

                    ctx.contentType("image/svg+xml");
                    InputStream inputStream = conn.getInputStream();
                    ctx.result(inputStream);
                } catch (Exception e) {
                    ctx.status(500).result("Failed to fetch shield SVG");
                    imageServerLogger.error("Could not fetch shield URL", e);
                }
            });

            // Keep the thread alive if needed
            while (!stopImageServer) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }

            app.stop();
        });
    }

    public static void runImageServer() {
        stopImageServer = false;
        imageServerThread.start();
    }

    public static void stopImageServer() {
        stopImageServer = true;
        imageServerThread.interrupt();
    }
}
