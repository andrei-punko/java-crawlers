package by.andd3dfx.rabotaby.util;

import by.andd3dfx.crawler.engine.WebCrawler;
import by.andd3dfx.rabotaby.dto.VacancyData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
public class SearchUtil extends WebCrawler<VacancyData> {

    private final String BASE_URL = "http://rabota.by";

    public String buildStartingSearchUrl(String searchString) {
        final var format = BASE_URL + "/search/vacancy?area=1002&text=%s&page=%d";
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
}
