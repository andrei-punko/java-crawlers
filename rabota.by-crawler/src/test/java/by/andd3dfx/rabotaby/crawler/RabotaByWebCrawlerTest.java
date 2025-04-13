package by.andd3dfx.rabotaby.crawler;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RabotaByWebCrawlerTest {

    private static final int RECORDS_PER_PAGE = 20;
    private RabotaByWebCrawler crawler;

    @Before
    public void setup() {
        crawler = new RabotaByWebCrawler();
    }

    @Test
    public void singleSearch() {
        var pageUrl = crawler.buildStartingSearchUrl("java");
        var result = crawler.singleSearch(pageUrl, 100);

        assertThat(result.nextPageUrl()).startsWith(
                """
                        http://rabota.by/search/vacancy?area=1002&text=java&page=1\
                        &hhtmFromLabel=vacancy_search_line\
                        &searchSessionId=\
                        """);
        assertThat(result.nextPageUrl()).endsWith("&hhtmFrom=vacancy_search_list");
        assertThat(result.dataItems().size())
                .as("At least 20 items expected")
                .isGreaterThanOrEqualTo(RECORDS_PER_PAGE);
    }

    @Test
    public void batchSearch() {
        var pageUrl = crawler.buildStartingSearchUrl("java");
        var searchResult = crawler.batchSearch(pageUrl, 2, 100);

        assertThat(searchResult.size()).isEqualTo(2 * RECORDS_PER_PAGE);
    }
}
