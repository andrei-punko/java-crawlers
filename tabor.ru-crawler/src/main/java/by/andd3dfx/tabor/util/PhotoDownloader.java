package by.andd3dfx.tabor.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Downloads a single cover photo by URL.
 */
@Slf4j
public class PhotoDownloader {

    private final HttpClient httpClient;

    public PhotoDownloader() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Download photo from {@code photoUrl} into {@code targetFile}.
     *
     * @return {@code targetFile} on success, or {@code null} on failure
     */
    public Path download(String photoUrl, Path targetFile) {
        if (photoUrl == null || photoUrl.isBlank()) {
            return null;
        }
        try {
            Files.createDirectories(targetFile.getParent());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(photoUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("User-Agent", "Mozilla")
                    .GET()
                    .build();
            HttpResponse<Path> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofFile(targetFile));
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return targetFile;
            }
            log.warn("Failed to download photo {}: HTTP {}", photoUrl, response.statusCode());
            Files.deleteIfExists(targetFile);
            return null;
        } catch (IOException | InterruptedException e) {
            log.warn("Failed to download photo {}: {}", photoUrl, e.getMessage());
            try {
                Files.deleteIfExists(targetFile);
            } catch (IOException ignored) {
                // ignore cleanup failure
            }
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return null;
        }
    }

    public static String extensionFromUrl(String photoUrl) {
        if (photoUrl == null) {
            return "jpg";
        }
        String path = photoUrl;
        int query = path.indexOf('?');
        if (query >= 0) {
            path = path.substring(0, query);
        }
        int slash = path.lastIndexOf('/');
        String name = slash >= 0 ? path.substring(slash + 1) : path;
        int dot = name.lastIndexOf('.');
        if (dot > 0 && dot < name.length() - 1) {
            return name.substring(dot + 1).toLowerCase();
        }
        return "jpg";
    }
}
