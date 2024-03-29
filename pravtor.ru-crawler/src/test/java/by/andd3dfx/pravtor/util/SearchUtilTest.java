package by.andd3dfx.pravtor.util;

import by.andd3dfx.pravtor.model.SingleSearchResult;
import by.andd3dfx.pravtor.model.TorrentData;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SearchUtilTest {

    private final String STARTING_URL = "https://pravtor.ru/viewforum.php?f=28";  // Святоотеческие тексты и жития святых

    private SearchUtil searchUtil;

    @Before
    public void setup() {
        searchUtil = new SearchUtil();
    }

    @Test
    public void batchSearch() throws IOException, InterruptedException {
        List<TorrentData> result = searchUtil.batchSearch(STARTING_URL, 2, 20);

        assertThat("Wrong amount of result records", result.size(), is(100));
    }

    @Test
    public void singleSearch() throws IOException {
        SingleSearchResult result = searchUtil.singleSearch(STARTING_URL);

        assertThat("Wrong amount of result records", result.getDataItems().size(), is(50));
    }
}
