package by.andd3dfx.pravtor.util;

import by.andd3dfx.pravtor.model.SingleSearchResult;
import by.andd3dfx.pravtor.model.TorrentData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

/**
 * Util to perform search on <a href="http://pravtor.ru">pravtor.ru</a> torrent tracker
 */
@Slf4j
public class SearchUtil {

    private static final String USER_AGENT = "Mozilla";
    private static final String PREFIX = "https://pravtor.ru/";

    public List<TorrentData> batchSearch(String startingPageUrl, int maxPagesCap, long throttlingDelay)
            throws InterruptedException, IOException {

        log.info("Starting URL: {}, maxPagesCap={}, delay={}ms", startingPageUrl, maxPagesCap, throttlingDelay);

        String nextPageUrl = startingPageUrl;
        int pagesCounter = 0;
        List<TorrentData> result = new ArrayList<>();

        while (nextPageUrl != null && (maxPagesCap == -1 || pagesCounter < maxPagesCap)) {

            SingleSearchResult singleSearchResult = singleSearch(nextPageUrl);
            log.info("Hit {}, {} retrieved", pagesCounter, singleSearchResult.getDataItems().size());
            pagesCounter++;
            nextPageUrl = singleSearchResult.getNextPageUrl();
            result.addAll(singleSearchResult.getDataItems());

            sleep(throttlingDelay);
        }
        log.info("Records retrieved: {}", result.size());

        return result;
    }

    SingleSearchResult singleSearch(String startingPageUrl) throws IOException {
        Document document = Jsoup
                .connect(startingPageUrl)
                .userAgent(USER_AGENT).get();

        Elements elements = document.select("tr[id^=tr-]");

        List<TorrentData> dataItems = elements.stream()
                .map(element -> TorrentData.builder()
                        .label(element.select("div[class=torTopic]").select("a").text())
                        .linkUrl(extractLink(element.select("a[class=torTopic]").attr("href")))
                        .seedsCount(convertToInteger(element.select("span[title=Seeders]").text()))
                        .peersCount(convertToInteger(element.select("span[title=Leechers]").text()))
                        .size(element.select("div[title=Скачать .torrent]").select("div[class=small]").text())
                        .downloadedCount(convertToInteger(element.select("p[title=Скачан]").text()))
                        .build()
                ).toList();

        String prevUrl = extractPrevOrNext(document, "Пред.");
        String nextUrl = extractPrevOrNext(document, "След.");
        return new SingleSearchResult(dataItems, prevUrl, nextUrl);
    }

    private String extractLink(String href) {
        return StringUtils.isEmpty(href) ? href : PREFIX + href.substring(2);
    }

    private Integer convertToInteger(String value) {
        if (!StringUtils.isNumeric(value)) {
            return null;
        }
        return Integer.parseInt(value);
    }

    private String extractPrevOrNext(Document document, String value) {
        List<Element> pageItems = document.select("td[class=tRight vBottom nowrap small]")
                .select("a").stream()
                .filter(s -> s.text().contains(value))
                .toList();

        if (pageItems.isEmpty()) {
            return null;
        }
        return PREFIX + pageItems.get(0).attr("href");
    }
}
