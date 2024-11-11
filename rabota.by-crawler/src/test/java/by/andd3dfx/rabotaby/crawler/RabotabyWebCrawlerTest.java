package by.andd3dfx.rabotaby.crawler;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

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
        var result = crawler.singleSearch(pageUrl);

        assertThat("Next url should be present", result.nextPageUrl(), is(
                """
                        http://rabota.by/search/vacancy?area=1002&text=java&page=1\
                        &hhtmFromLabel=vacancy_search_line\
                        &hhtmFrom=vacancy_search_list\
                        """));
        assertThat("At least 20 items expected", result.dataItems().size(), greaterThanOrEqualTo(RECORDS_PER_PAGE));
    }

    @Test
    public void batchSearch() {
        var pageUrl = crawler.buildStartingSearchUrl("java");
        var searchResult = crawler.batchSearch(pageUrl, 2);

        assertThat(searchResult.size(), is(2 * RECORDS_PER_PAGE));
    }
}
