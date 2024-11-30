package by.andd3dfx.pravtor.dto;

import java.util.List;

/**
 * DTO to store results of batch search
 *
 * @param topic     name of topic
 * @param dataItems data items
 */
public record BatchSearchResult(String topic, List<TorrentData> dataItems) {

}
