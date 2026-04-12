package by.andd3dfx.onliner.crawler;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OnlinerByCpuCrawlerTest {

    private OnlinerByCpuCrawler crawler;

    @Before
    public void setup() {
        crawler = new OnlinerByCpuCrawler();
    }

    @Test
    public void startingUrl_containsSocketAndPriceFilters() {
        assertThat(OnlinerByCpuCrawler.AM4_CPU_LIST_URL)
                .contains("socket_cpu%5B0%5D=am4")
                .contains("price%5Bfrom%5D=1");
    }

    @Test
    public void singleSearch_firstPage_returnsItemsAndOptionalNext() {
        var result = crawler.singleSearch(crawler.buildAm4StartingUrl(), 300);

        assertThat(result.dataItems())
                .isNotEmpty()
                .allMatch(p -> "AM4".equals(p.getSocket()))
                .allMatch(p -> p.getName() != null && !p.getName().isBlank());
        if (result.nextPageUrl() != null) {
            assertThat(result.nextPageUrl()).contains("page=2");
        }
    }
}
