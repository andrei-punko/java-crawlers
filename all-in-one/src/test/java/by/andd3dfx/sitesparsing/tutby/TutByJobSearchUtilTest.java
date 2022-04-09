package by.andd3dfx.sitesparsing.tutby;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import by.andd3dfx.sitesparsing.tutby.dto.SingleSearchResult;
import java.util.LinkedHashMap;
import org.junit.Before;
import org.junit.Test;

public class TutByJobSearchUtilTest {

    private TutByJobSearchUtil util;

    @Before
    public void setup() {
        util = new TutByJobSearchUtil();
    }

    @Test
    public void search() {
        final SingleSearchResult result = util.singleSearch(util.buildSearchUrl("java"));

        assertThat("Next url should be present", result.getNextPageUrl(), is(
            "http://rabota.by/search/vacancy?area=1002&text=java&page=1&hhtmFrom=vacancy_search_list"));
        assertThat("At least 20 items expected", result.getDataItems().size(), greaterThanOrEqualTo(20));

        LinkedHashMap<String, Integer> statisticsSortedMap = util.collectStatistics(result.getDataItems());
        System.out.println(statisticsSortedMap);
    }
}
