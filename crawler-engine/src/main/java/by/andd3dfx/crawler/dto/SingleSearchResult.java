package by.andd3dfx.crawler.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class SingleSearchResult<T extends CrawlerData> {

    private final List<T> dataItems;
    private final String prevPageUrl;
    private final String nextPageUrl;
}
