package by.andd3dfx.rabotaby;

import by.andd3dfx.rabotaby.crawler.RabotaByWebCrawler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainApp {

    private static RabotaByWebCrawler crawler = new RabotaByWebCrawler();
    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Start vacancies search process, store results into JSON file after that
     *
     * @param args command-line params: path+name of output file [and pages cap amount]
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            throw new IllegalArgumentException("Output file path+name should be populated!");
        }
        Path path = Paths.get(args[0]);

        int pagesCap = -1;
        if (args.length >= 2) {
            pagesCap = Integer.parseInt(args[1]);
        }

        int timeoutMs = 20;
        if (args.length >= 3) {
            timeoutMs = Integer.parseInt(args[2]);
        }

        var pageUrl = crawler.buildStartingSearchUrl("java");
        var searchResult = crawler.batchSearch(pageUrl, pagesCap, timeoutMs);

        var jsonString = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(searchResult);
        Files.write(path, jsonString.getBytes());
    }
}
