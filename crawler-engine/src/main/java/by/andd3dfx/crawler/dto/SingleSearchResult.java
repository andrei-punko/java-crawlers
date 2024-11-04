package by.andd3dfx.crawler.dto;

import java.util.List;

public record SingleSearchResult<T extends CrawlerData>(List<T> dataItems, String nextPageUrl) {
}
