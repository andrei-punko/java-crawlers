package by.andd3dfx.pravtor.dto;

import by.andd3dfx.crawler.dto.CrawlerData;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TorrentData implements CrawlerData {

    private String label;
    private String linkUrl;
    private Integer seedsCount;
    private Integer peersCount;
    private Integer downloadedCount;
    private String size;
}
