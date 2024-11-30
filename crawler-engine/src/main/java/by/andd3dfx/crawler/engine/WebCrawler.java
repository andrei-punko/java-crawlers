package by.andd3dfx.crawler.engine;

import by.andd3dfx.crawler.dto.CrawlerData;
import by.andd3dfx.crawler.dto.SingleSearchResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Web crawler for retrieving list of data with type T by consequent visiting of pages.
 * Process started using provided starting page URL; next link on each page retrieved from current page.
 *
 * @param <T> data items type
 */
@Slf4j
public abstract class WebCrawler<T extends CrawlerData> {

    protected static final String USER_AGENT = "Mozilla";
    private static final int DEFAULT_MAX_PAGES_CAP = 10;
    private static final long DEFAULT_THROTTLING_DELAY_MS = 20;

    /**
     * Search and extract data from page with provided URL
     *
     * @param pageUrl URL of page
     * @return search result
     */
    @SneakyThrows
    public SingleSearchResult<T> singleSearch(String pageUrl) {
        Document document = Jsoup
                .connect(pageUrl)
                .userAgent(USER_AGENT).get();

        Elements elements = extractElements(document);

        List<T> dataItems = elements.stream()
                .map(this::mapElementToData)
                .toList();
        log.debug("Single search: url={}, items={}", pageUrl, dataItems.size());

        String nextUrl = extractNextUrl(document);
        return new SingleSearchResult<>(dataItems, nextUrl);
    }

    /**
     * Extract elements from parsed Jsoup document
     *
     * @param document Jsoup document
     * @return extracted elements
     */
    protected abstract Elements extractElements(Document document);

    /**
     * Extract next URL from parsed Jsoup document
     *
     * @param document Jsoup document
     * @return next URL
     */
    protected abstract String extractNextUrl(Document document);

    /**
     * Map element to result DTO object
     *
     * @param element
     * @return DTO object of type T
     */
    protected abstract T mapElementToData(Element element);

    /**
     * Batch search using provided starting page URL, max pages cap 10 and throttling delay 20ms
     *
     * @param pageUrl starting page URL
     * @return list of retrieved items
     */
    public List<T> batchSearch(String pageUrl) {
        return batchSearch(pageUrl, DEFAULT_MAX_PAGES_CAP);
    }

    /**
     * Batch search using provided starting page URL and max pages cap. Used throttling delay is 20ms.
     * Use value -1 for max pages cap to visit all available pages.
     *
     * @param pageUrl     starting page URL
     * @param maxPagesCap max pages amount (search will be stopped when this amount of pages requested or no more pages available)
     * @return list of retrieved items
     */
    public List<T> batchSearch(String pageUrl, int maxPagesCap) {
        return batchSearch(pageUrl, maxPagesCap, DEFAULT_THROTTLING_DELAY_MS);
    }

    /**
     * Batch search using provided starting page URL, max pages cap and throttling delay.
     * Use value -1 for max pages cap to visit all available pages.
     *
     * @param pageUrl           starting page URL
     * @param maxPagesCap       max pages amount (search will be stopped when this amount of pages requested or no more pages available)
     * @param throttlingDelayMs delay between two consequent page requests, milliseconds
     * @return list of retrieved items
     */
    @SneakyThrows
    public List<T> batchSearch(String pageUrl, int maxPagesCap, long throttlingDelayMs) {
        assert (throttlingDelayMs > 0);
        log.info("Batch search. Starting URL={}, maxPagesCap={}, delay={}ms", pageUrl, maxPagesCap, throttlingDelayMs);

        int pagesCounter = 0;
        var nextPage = pageUrl;
        List<T> result = new ArrayList<>();

        while (nextPage != null && (maxPagesCap == -1 || pagesCounter < maxPagesCap)) {
            SingleSearchResult<T> searchResult = singleSearch(nextPage);
            List<T> dataItems = searchResult.dataItems();
            log.info("Hit №{}, {} items retrieved", pagesCounter, dataItems.size());
            pagesCounter++;
            result.addAll(dataItems);
            nextPage = searchResult.nextPageUrl();

            Thread.sleep(throttlingDelayMs);
        }
        log.info("Total records retrieved: {}", result.size());

        return result;
    }
}
