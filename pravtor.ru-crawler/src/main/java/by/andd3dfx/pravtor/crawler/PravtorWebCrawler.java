package by.andd3dfx.pravtor.crawler;

import by.andd3dfx.crawler.engine.WebCrawler;
import by.andd3dfx.pravtor.dto.TorrentData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

/**
 * Util to perform search on <a href="http://pravtor.ru">pravtor.ru</a> torrent tracker
 */
@Slf4j
public class PravtorWebCrawler extends WebCrawler<TorrentData> {

    private static final String BASE_URL = "https://pravtor.ru/";

    @Override
    protected Elements extractElements(Document document) {
        return document.select("tr[id^=tr-]");
    }

    @Override
    protected String extractNextUrl(Document document) {
        List<Element> pageItems = document
                .select("td[class=tRight vBottom nowrap small]")
                .select("a").stream()
                .filter(s -> s.text().contains("След."))
                .toList();

        if (pageItems.isEmpty()) {
            return null;
        }
        return BASE_URL + pageItems.get(0).attr("href");
    }

    @Override
    protected TorrentData mapElementToData(Element element) {
        return TorrentData.builder()
                .label(element.select("div[class=torTopic]").select("a").text())
                .linkUrl(extractLink(element.select("a[class=torTopic]").attr("href")))
                .seedsCount(convertToInteger(element.select("span[title=Seeders]").text()))
                .peersCount(convertToInteger(element.select("span[title=Leechers]").text()))
                .size(element.select("div[title=Скачать .torrent]").select("div[class=small]").text())
                .downloadedCount(convertToInteger(element.select("p[title=Скачан]").text()))
                .build();
    }

    private String extractLink(String href) {
        if (StringUtils.isEmpty(href)) {
            return href;
        }
        return BASE_URL + href.substring(2);
    }

    private Integer convertToInteger(String value) {
        if (!StringUtils.isNumeric(value)) {
            return null;
        }
        return Integer.parseInt(value);
    }
}