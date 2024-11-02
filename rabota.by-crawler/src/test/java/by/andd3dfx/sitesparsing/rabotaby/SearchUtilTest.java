package by.andd3dfx.sitesparsing.rabotaby;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

public class SearchUtilTest {

    private static final int RECORDS_PER_PAGE = 20;
    private SearchUtil searchUtil;
    private Statistics statistics;

    @Before
    public void setup() {
        searchUtil = new SearchUtil();
        statistics = new Statistics();
    }

    @Test
    public void singleSearch() {
        var pageUrl = searchUtil.buildSearchUrl("java");
        var result = searchUtil.singleSearch(pageUrl);

        assertThat("Next url should be present", result.getNextPageUrl(), is(
                "http://rabota.by/search/vacancy?area=1002&text=java&page=1&hhtmFrom=vacancy_search_list"));
        assertThat("At least 20 items expected", result.getDataItems().size(), greaterThanOrEqualTo(RECORDS_PER_PAGE));
    }

    @Test
    public void batchSearch() {
        var pageUrl = searchUtil.buildSearchUrl("java");
        var searchResult = searchUtil.batchSearch(pageUrl, 2);

        assertThat(searchResult.size(), is(2 * RECORDS_PER_PAGE));
    }
}
