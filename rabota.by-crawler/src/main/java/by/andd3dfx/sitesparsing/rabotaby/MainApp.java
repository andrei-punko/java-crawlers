package by.andd3dfx.sitesparsing.rabotaby;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainApp {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Path to output file should be populated!");
        }

        var searchUtil = new SearchUtil();
        var pageUrl = searchUtil.buildSearchUrl("java");
        var searchResult = searchUtil.batchSearch(pageUrl);

        var statistics = new Statistics();
        var statisticsSortedMap = statistics.collectStatistics(searchResult);

        Path path = Paths.get(args[0]);
        byte[] strToBytes = statisticsSortedMap.toString().getBytes();
        Files.write(path, strToBytes);
    }
}
