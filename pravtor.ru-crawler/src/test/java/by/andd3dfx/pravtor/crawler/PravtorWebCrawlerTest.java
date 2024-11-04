package by.andd3dfx.pravtor.crawler;

import by.andd3dfx.pravtor.dto.TorrentData;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PravtorWebCrawlerTest {

    private final String STARTING_URL = "https://pravtor.ru/viewforum.php?f=28";  // Святоотеческие тексты и жития святых

    private PravtorWebCrawler crawler;

    @Before
    public void setup() {
        crawler = new PravtorWebCrawler();
    }

    @Test
    public void batchSearch() {
        List<TorrentData> result = crawler.batchSearch(STARTING_URL, 2, 20);

        assertThat("Wrong amount of result records", result.size(), is(100));
    }

    @Test
    public void singleSearch() {
        var result = crawler.singleSearch(STARTING_URL);

        assertThat("Wrong amount of result records", result.dataItems().size(), is(50));
    }
}
