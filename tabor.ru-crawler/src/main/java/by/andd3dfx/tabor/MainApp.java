package by.andd3dfx.tabor;

import by.andd3dfx.tabor.crawler.TaborRuWebCrawler;
import by.andd3dfx.tabor.dto.ProfileData;
import by.andd3dfx.tabor.util.PhotoDownloader;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MainApp {

    private static final PhotoDownloader photoDownloader = new PhotoDownloader();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Search women profiles in Minsk (configurable age, with photo), download cover photos, store JSON.
     *
     * @param args outputJson [pagesCap] [throttlingDelayMs] [minAge] [maxAge]
     *             {@code pagesCap=-1} — all pages, hard-capped at {@link TaborRuWebCrawler#UNLIMITED_PAGES_HARD_CAP}.
     *             Age defaults: {@link TaborRuWebCrawler#DEFAULT_MIN_AGE}–{@link TaborRuWebCrawler#DEFAULT_MAX_AGE}.
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            throw new IllegalArgumentException(
                    "Usage: <outputJson> [pagesCap] [throttlingDelayMs] [minAge] [maxAge]");
        }
        Path jsonPath = Paths.get(args[0]);

        int pagesCap = -1;
        if (args.length >= 2) {
            pagesCap = Integer.parseInt(args[1]);
        }

        int timeoutMs = 500;
        if (args.length >= 3) {
            timeoutMs = Integer.parseInt(args[2]);
        }

        int minAge = TaborRuWebCrawler.DEFAULT_MIN_AGE;
        if (args.length >= 4) {
            minAge = Integer.parseInt(args[3]);
        }

        int maxAge = TaborRuWebCrawler.DEFAULT_MAX_AGE;
        if (args.length >= 5) {
            maxAge = Integer.parseInt(args[4]);
        }

        TaborRuWebCrawler crawler = new TaborRuWebCrawler(minAge, maxAge);
        log.info("Search criteria: women, city={}, age={}–{}", TaborRuWebCrawler.CITY, minAge, maxAge);

        var pageUrl = crawler.buildStartingUrl();
        long parseStartedAt = System.nanoTime();
        var profiles = crawler.batchSearch(pageUrl, pagesCap, timeoutMs);
        long parseElapsedMs = (System.nanoTime() - parseStartedAt) / 1_000_000L;

        Path photosDir = jsonPath.toAbsolutePath().getParent() != null
                ? jsonPath.toAbsolutePath().getParent().resolve("photos")
                : Paths.get("photos");

        Map<String, Integer> rejectionStats = new LinkedHashMap<>();
        List<ProfileData> filtered = new ArrayList<>();
        for (ProfileData profile : profiles) {
            String reason = crawler.rejectionReason(profile);
            if (reason == null) {
                filtered.add(profile);
            } else {
                rejectionStats.merge(reason, 1, Integer::sum);
            }
        }

        filtered.sort(Comparator
                .comparing(ProfileData::getAge, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ProfileData::getName, Comparator.nullsLast(String::compareTo))
                .thenComparing(ProfileData::getUrl, Comparator.nullsLast(String::compareTo)));

        log.info("Fetched: {}, kept: {}, rejected: {}",
                profiles.size(), filtered.size(), profiles.size() - filtered.size());
        if (rejectionStats.isEmpty()) {
            log.info("Rejection stats by reason: (none)");
        } else {
            log.info("Rejection stats by reason:");
            rejectionStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                            .thenComparing(Map.Entry.comparingByKey()))
                    .forEach(e -> log.info("  {}: {}", e.getKey(), e.getValue()));
        }

        log.info("Downloading {} cover photos into {}", filtered.size(), photosDir);
        for (ProfileData profile : filtered) {
            downloadCoverPhoto(profile, photosDir);
        }
        log.info("Photo download finished");

        var jsonString = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(filtered);
        Files.writeString(jsonPath, jsonString, StandardCharsets.UTF_8);
        log.info("Saved {} profiles to {}", filtered.size(), jsonPath.toAbsolutePath());
        log.info("Parsing took {}", formatDuration(parseElapsedMs));
    }

    static String formatDuration(long elapsedMs) {
        long totalSeconds = elapsedMs / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        long millis = elapsedMs % 1000L;
        if (hours > 0) {
            return "%dh %dm %ds".formatted(hours, minutes, seconds);
        }
        if (minutes > 0) {
            return "%dm %ds".formatted(minutes, seconds);
        }
        if (totalSeconds > 0) {
            return "%d.%03ds".formatted(seconds, millis);
        }
        return elapsedMs + "ms";
    }

    private static void downloadCoverPhoto(ProfileData profile, Path photosDir) {
        if (profile.getId() == null || profile.getPhotoUrl() == null) {
            return;
        }
        String ext = PhotoDownloader.extensionFromUrl(profile.getPhotoUrl());
        Path target = photosDir.resolve(profile.getId() + "." + ext);
        Path saved = photoDownloader.download(profile.getPhotoUrl(), target);
        if (saved != null) {
            profile.setPhotoPath(saved.toString());
        }
    }
}
