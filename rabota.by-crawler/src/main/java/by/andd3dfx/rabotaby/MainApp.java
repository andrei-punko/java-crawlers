package by.andd3dfx.rabotaby;

import by.andd3dfx.rabotaby.crawler.RabotabyWebCrawler;
import by.andd3dfx.rabotaby.util.StatisticsUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainApp {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Path to output file should be populated!");
        }

        var crawler = new RabotabyWebCrawler();
        var pageUrl = crawler.buildStartingSearchUrl("java");
        var searchResult = crawler.batchSearch(pageUrl);

        var statisticsSortedMap = new StatisticsUtil().collectStatistics(searchResult);

        Path path = Paths.get(args[0]);
        byte[] strToBytes = statisticsSortedMap.toString().getBytes();
        Files.write(path, strToBytes);
    }
}
