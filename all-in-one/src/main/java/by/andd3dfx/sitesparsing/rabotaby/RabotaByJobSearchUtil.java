package by.andd3dfx.sitesparsing.rabotaby;

import by.andd3dfx.sitesparsing.rabotaby.dto.SingleSearchResult;
import by.andd3dfx.sitesparsing.rabotaby.dto.VacancyData;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Slf4j
public class RabotaByJobSearchUtil {

    private final String URL_PREFIX = "http://rabota.by";
    private final String USER_AGENT = "Mozilla";
    private final String searchUrlFormat = URL_PREFIX + "/search/vacancy?area=1002&text=%s&page=%d";

    public List<VacancyData> batchSearch(String searchString) {
        List<VacancyData> result = new ArrayList<>();

        String nextPageUrl = buildSearchUrl(searchString);
        while (nextPageUrl != null) {
            SingleSearchResult singleSearchResult = singleSearch(nextPageUrl);
            result.addAll(singleSearchResult.getDataItems());
            nextPageUrl = singleSearchResult.getNextPageUrl();
        }

        return result;
    }

    SingleSearchResult singleSearch(String searchUrl) {
        try {
            Document document = Jsoup
                .connect(searchUrl)
                .userAgent(USER_AGENT).get();

            Elements elements = document
                .select("a[data-qa=vacancy-serp__vacancy-title]");

            List<VacancyData> vacancyDataList = new ArrayList<>();
            elements.parallelStream().forEach(element -> {
                String vacancyDetailsUrl = element.select("a").attr("href");
                vacancyDataList.add(retrieveVacancyDetails(vacancyDetailsUrl));
            });

            final Elements nextPageItem = document.select("a[data-qa=pager-next]");
            String nextPageUrl = nextPageItem.isEmpty() ? null : URL_PREFIX + nextPageItem.attr("href");
            return new SingleSearchResult(vacancyDataList, nextPageUrl);
        } catch (IOException e) {
            throw new RuntimeException("Single search failed", e);
        }
    }

    private VacancyData retrieveVacancyDetails(String searchUrl) {
        log.info("Retrieve vacancy details for " + searchUrl);
        Document document;
        try {
            document = Jsoup
                .connect(searchUrl)
                .userAgent(USER_AGENT).get();
        } catch (IOException e) {
            throw new RuntimeException("Retrieve details failed", e);
        }

        return VacancyData.builder()
            .url(document.baseUri())
            .companyName(document.select("a[class=vacancy-company-name]").text())
            .textContent(document.select("div[data-qa=vacancy-description]").text())
            .keywords(document.select("span[data-qa=bloko-tag__text]")
                    .stream()
                    .map(Element::text)
                    .flatMap(keyword -> Arrays.asList(keyword.split(", ")).stream())
                    .flatMap(keyword -> Arrays.asList(keyword.split(" & ")).stream())
                    .collect(Collectors.toSet())
            )
            .addressString(document.select("div[class^=vacancy-address-text]").text())
            .build();
    }

    String buildSearchUrl(String searchString) {
        return String.format(searchUrlFormat, searchString, 0);
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Path to output file should be populated!");
        }
        final RabotaByJobSearchUtil searchUtil = new RabotaByJobSearchUtil();

        LinkedHashMap<String, Integer> statisticsSortedMap = searchUtil.collectStatistics("java");
        Path path = Paths.get(args[0]);
        byte[] strToBytes = statisticsSortedMap.toString().getBytes();
        Files.write(path, strToBytes);
    }

    public LinkedHashMap<String, Integer> collectStatistics(List<VacancyData> result) {
        final Statistics statistics = new Statistics();
        result.stream().forEach(vacancyData -> {
            vacancyData.getKeywords().stream().forEach(statistics::putKeyword);
        });
        return statistics.buildSortedMap();
    }

    public LinkedHashMap<String, Integer> collectStatistics(String searchString) {
        return collectStatistics(batchSearch(searchString));
    }
}
