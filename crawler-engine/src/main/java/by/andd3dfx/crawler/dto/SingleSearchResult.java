package by.andd3dfx.crawler.dto;

import java.util.List;

/**
 * DTO to store results of single search
 *
 * @param dataItems   list of data items with type T
 * @param nextPageUrl URL of next page
 * @param <T>         data items type
 */
public record SingleSearchResult<T extends CrawlerData>(List<T> dataItems, String nextPageUrl) {
}
