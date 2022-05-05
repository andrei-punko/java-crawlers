package by.andd3dfx.pravtor.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TorrentData {

    private String label;
    private String linkUrl;
    private Integer seedsCount;
    private Integer peersCount;
    private Integer downloadedCount;
    private String size;
}
