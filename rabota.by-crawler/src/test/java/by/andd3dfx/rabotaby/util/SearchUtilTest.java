package by.andd3dfx.rabotaby.util;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

public class SearchUtilTest {

    private static final int RECORDS_PER_PAGE = 20;
    private SearchUtil searchUtil;
    private StatisticsUtil statisticsUtil;

    @Before
    public void setup() {
        searchUtil = new SearchUtil();
        statisticsUtil = new StatisticsUtil();
    }

    @Test
    public void singleSearch() {
        var pageUrl = searchUtil.buildStartingSearchUrl("java");
        var result = searchUtil.singleSearch(pageUrl);

        assertThat("Next url should be present", result.nextPageUrl(), is(
                "http://rabota.by/search/vacancy?area=1002&text=java&page=1&hhtmFrom=vacancy_search_list"));
        assertThat("At least 20 items expected", result.dataItems().size(), greaterThanOrEqualTo(RECORDS_PER_PAGE));
    }

    @Test
    public void batchSearch() {
        var pageUrl = searchUtil.buildStartingSearchUrl("java");
        var searchResult = searchUtil.batchSearch(pageUrl, 2);

        assertThat(searchResult.size(), is(2 * RECORDS_PER_PAGE));
    }
}
