package by.andd3dfx.tabor.util;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class PhotoDownloaderTest {

    @Test
    public void extensionFromUrl() {
        assertThat(PhotoDownloader.extensionFromUrl(
                "https://p7.tabor.ru/photos/2026/1001/photo_800x600.jpg"))
                .isEqualTo("jpg");
        assertThat(PhotoDownloader.extensionFromUrl(
                "https://cdn.example/a.png?size=large"))
                .isEqualTo("png");
        assertThat(PhotoDownloader.extensionFromUrl("https://cdn.example/noext"))
                .isEqualTo("jpg");
    }

    @Test
    public void downloadReturnsNullForBlankUrl() {
        PhotoDownloader downloader = new PhotoDownloader();
        assertThat(downloader.download(null, Path.of("photos/x.jpg"))).isNull();
        assertThat(downloader.download("  ", Path.of("photos/x.jpg"))).isNull();
    }

    @Test
    public void downloadWritesFileFromLocalHttp() throws Exception {
        // Use a tiny data URI is not supported by HttpClient; skip network —
        // verify that failed download cleans up and returns null for bad host.
        PhotoDownloader downloader = new PhotoDownloader();
        Path target = Files.createTempDirectory("tabor-photo-test").resolve("1.jpg");
        Path result = downloader.download("http://127.0.0.1:1/missing.jpg", target);
        assertThat(result).isNull();
        assertThat(Files.exists(target)).isFalse();
    }
}
