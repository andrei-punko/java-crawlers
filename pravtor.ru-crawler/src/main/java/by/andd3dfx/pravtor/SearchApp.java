package by.andd3dfx.pravtor;

import by.andd3dfx.pravtor.model.BatchSearchResult;
import by.andd3dfx.pravtor.model.TorrentData;
import by.andd3dfx.pravtor.util.FileUtil;
import by.andd3dfx.pravtor.util.SearchUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class SearchApp {

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Should be 2 parameters!");
        }
        String paramsFileName = args[0];
        String excelFileName = args[1];

        var searchUtil = new SearchUtil();
        var fileUtil = new FileUtil();

        var searchItems = new ArrayList<BatchSearchResult>();
        for (var searchCriteria : fileUtil.loadSearchCriteria(paramsFileName)) {
            String startingUrl = searchCriteria.getUrl();
            String label = searchCriteria.getTopic();

            var result = searchUtil.batchSearch(startingUrl, -1, 20)
                    .stream()
                    .filter(torrentData -> torrentData.getDownloadedCount() != null)
                    .sorted(Comparator.comparingInt(TorrentData::getDownloadedCount).reversed())
                    .toList();
            searchItems.add(new BatchSearchResult(label, result));
        }

        fileUtil.writeIntoExcel(excelFileName, searchItems);
    }
}
