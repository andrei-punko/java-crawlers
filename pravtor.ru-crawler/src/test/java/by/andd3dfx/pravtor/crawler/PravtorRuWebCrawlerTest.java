package by.andd3dfx.pravtor.crawler;

import by.andd3dfx.pravtor.dto.TorrentData;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PravtorRuWebCrawlerTest {

    private final String STARTING_URL = "https://pravtor.ru/viewforum.php?f=28";  // Святоотеческие тексты и жития святых

    private PravtorRuWebCrawler crawler;

    @Before
    public void setup() {
        crawler = new PravtorRuWebCrawler();
    }

    @Test
    public void batchSearch() {
        List<TorrentData> result = crawler.batchSearch(STARTING_URL, 2, 20);

        assertThat(result).as("Wrong amount of result records").hasSize(100);
    }

    @Test
    public void singleSearch() {
        var result = crawler.singleSearch(STARTING_URL);

        assertThat(result.dataItems()).as("Wrong amount of result records").hasSize(50);
    }
}
