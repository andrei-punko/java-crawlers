package by.andd3dfx.rabotaby.crawler;

import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RabotaByWebCrawlerTest {

    private static final int RECORDS_PER_PAGE = 20;

    @Test
    public void singleSearch() {
        var crawler = new RabotaByWebCrawler();
        var pageUrl = crawler.buildStartingSearchUrl("java");
        var result = crawler.singleSearch(pageUrl, 200);

        var nextUrl = result.nextPageUrl();
        assertThat(nextUrl).startsWith(
                "http://rabota.by/search/vacancy?area=1002&text=java&page=1&hhtmFromLabel=vacancy_search_line&");
        assertThat(nextUrl)
                .containsPattern("(search_session_id|searchSessionId)=");
        assertThat(nextUrl).endsWith("&hhtmFrom=vacancy_search_list");
        assertThat(result.dataItems().size())
                .as("At least 20 items expected")
                .isGreaterThanOrEqualTo(RECORDS_PER_PAGE);
    }

    @Test
    public void batchSearch() {
        var stubCrawler = new RabotaByWebCrawlerOfflineStub();
        var pageUrl = stubCrawler.buildStartingSearchUrl("java");
        var searchResult = stubCrawler.batchSearch(pageUrl, 2, 1);

        assertThat(searchResult.size()).isEqualTo(2 * RECORDS_PER_PAGE);
    }

    /**
     * Не ходит в сеть: {@code batchSearch} тянет десятки страниц подряд, из‑за чего rabota.by часто отвечает
     * {@link java.net.SocketException} (connection reset / антибот).
     */
    private static final class RabotaByWebCrawlerOfflineStub extends RabotaByWebCrawler {

        private int searchVacancyListRequestOrdinal;

        @Override
        protected Document retrieveDocument(String pageUrl, long throttlingDelayMs) {
            if (pageUrl.contains("/search/vacancy")) {
                int page = Math.min(searchVacancyListRequestOrdinal++, 1);
                return buildSearchListPage(page);
            }
            return buildVacancyDetailPage(pageUrl);
        }

        private static Document buildSearchListPage(int pageIndex) {
            StringBuilder html = new StringBuilder("<html><body>");
            int baseId = pageIndex * 1000;
            for (int i = 0; i < RECORDS_PER_PAGE; i++) {
                int id = baseId + i;
                html.append("<a data-qa=\"serp-item__title\" href=\"http://rabota.by/vacancy/")
                        .append(id)
                        .append("\">Title ")
                        .append(id)
                        .append("</a>");
            }
            html.append("<div>");
            if (pageIndex == 0) {
                html.append("<a data-qa=\"pager-page\" href=\"/search/vacancy?area=1002&text=java&page=0\" aria-current=\"true\">1</a>");
                html.append("<a data-qa=\"pager-page\" href=\"/search/vacancy?area=1002&text=java&page=1\">2</a>");
            } else {
                html.append("<a data-qa=\"pager-page\" href=\"/search/vacancy?area=1002&text=java&page=0\">1</a>");
                html.append("<a data-qa=\"pager-page\" href=\"/search/vacancy?area=1002&text=java&page=1\" aria-current=\"true\">2</a>");
            }
            html.append("</div></body></html>");
            return org.jsoup.Jsoup.parse(html.toString(), "http://rabota.by/");
        }

        private static Document buildVacancyDetailPage(String url) {
            String html = """
                    <html><body>
                    <a data-qa="vacancy-company-name">TestCo</a>
                    <span data-qa="vacancy-experience">1–3 года</span>
                    <div data-qa="vacancy-description">Описание вакансии</div>
                    <span data-qa="vacancy-salary-compensation-type-net">1000 BYN</span>
                    <li data-qa="skills-element">Java</li>
                    <span data-qa="vacancy-view-raw-address">Минск</span>
                    <p data-qa="work-formats-text">Формат работы: Удалённо</p>
                    </body></html>""";
            return org.jsoup.Jsoup.parse(html, url);
        }
    }
}
