package by.andd3dfx.rabotaby.crawler;

import by.andd3dfx.crawler.engine.WebCrawler;
import by.andd3dfx.rabotaby.dto.VacancyData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Crawler to extract data from <a href="https://rabota.by">rabota.by</a> vacancies aggregator
 */
@Slf4j
public class RabotaByWebCrawler extends WebCrawler<VacancyData> {

    private final String BASE_URL = "http://rabota.by";

    public String buildStartingSearchUrl(String searchString) {
        // /search/vacancy?text=java&area=1002&hhtmFrom=main&hhtmFromLabel=vacancy_search_line
        final var format = BASE_URL + "/search/vacancy?area=1002&text=%s&page=%d&hhtmFromLabel=vacancy_search_line";
        return String.format(format, searchString, 0);
    }

    @Override
    protected Elements extractElements(Document document) {
        return document.select("a[data-qa=serp-item__title]");
    }

    @Override
    protected String extractNextUrl(Document document) {
        Elements nextPageItem = document.select("a[data-qa=pager-next]");
        if (nextPageItem.isEmpty()) {
            return null;
        }
        return BASE_URL + nextPageItem.attr("href");
    }

    @SneakyThrows
    @Override
    protected VacancyData mapElementToData(Element element) {
        String searchUrl = element.select("a").attr("href");
        log.info("Retrieve vacancy details for {}", searchUrl);
        Document document = Jsoup
                    .connect(searchUrl)
                    .userAgent(USER_AGENT).get();

        return VacancyData.builder()
                .url(document.baseUri())
                .companyName(extractCompanyName(document))
                .textContent(document.select("div[data-qa=vacancy-description]").text())
                .keywords(extractKeywords(document))
                .addressString(extractAddressString(document))
                .build();
    }

    private static String extractCompanyName(Document document) {
        var elements = document.select("a[data-qa=vacancy-company-name]");
        if (elements.isEmpty()) {
            return null;
        }
        return elements.getFirst().text();
    }

    private static Set<String> extractKeywords(Document document) {
        return document.select("li[data-qa=skills-element]")
                .stream()
                .map(Element::text)
                .flatMap(keyword -> Arrays.asList(keyword.split(", ")).stream())
                .flatMap(keyword -> Arrays.asList(keyword.split(" & ")).stream())
                .collect(Collectors.toSet());
    }

    private static String extractAddressString(Document document) {
        var elements = document.select("span[data-qa=vacancy-view-raw-address]");
        if (elements.isEmpty()) {
            return null;
        }
        return elements.getFirst().text();
    }
}