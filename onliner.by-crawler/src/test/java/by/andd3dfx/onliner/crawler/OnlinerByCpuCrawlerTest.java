package by.andd3dfx.onliner.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
                .allMatch(p -> p.getName() != null && !p.getName().isBlank());
        if (result.nextPageUrl() != null) {
            assertThat(result.nextPageUrl()).contains("page=2");
        }
    }

    @Test
    public void singleSearch_usesDetailPageSpecs_notOnlyDescription() {
        var stubCrawler = new OnlinerByCpuCrawlerOfflineStub();
        var result = stubCrawler.singleSearch(stubCrawler.buildAm4StartingUrl(), 1);

        assertThat(result.dataItems()).hasSize(1);
        var cpu = result.dataItems().getFirst();
        assertThat(cpu.getCoreCount()).isEqualTo(8);
        assertThat(cpu.getThreadCount()).isEqualTo(16);
        assertThat(cpu.getMaxFrequencyGHz()).isEqualTo(4.7);
    }

    private static final class OnlinerByCpuCrawlerOfflineStub extends OnlinerByCpuCrawler {
        @Override
        protected Document retrieveDocument(String pageUrl, long throttlingDelayMs) {
            if (pageUrl.contains("catalog.onliner.by/cpu?")) {
                return buildListingPage(pageUrl);
            }
            return buildDetailPage(pageUrl);
        }

        private static Document buildListingPage(String url) {
            String html = """
                    <html><body>
                    <script type="application/ld+json">
                    {
                      "@context": "https://schema.org",
                      "@type": "ItemList",
                      "itemListElement": [
                        {
                          "@type": "ListItem",
                          "position": 1,
                          "item": {
                            "@type": "Product",
                            "name": "AMD Ryzen 7 5700X",
                            "description": "Короткое описание без ядер и потоков",
                            "offers": {
                              "@type": "Offer",
                              "url": "https://catalog.onliner.by/cpu/amd/ryzen75700x",
                              "price": "500"
                            }
                          }
                        }
                      ]
                    }
                    </script>
                    </body></html>
                    """;
            return Jsoup.parse(html, url);
        }

        private static Document buildDetailPage(String url) {
            String html = """
                    <html><body>
                    <table>
                      <tr><td>Количество ядер</td><td>8</td></tr>
                      <tr><td>Количество потоков</td><td>16</td></tr>
                      <tr><td>Максимальная частота</td><td>4700 МГц</td></tr>
                    </table>
                    </body></html>
                    """;
            return Jsoup.parse(html, url);
        }
    }
}
