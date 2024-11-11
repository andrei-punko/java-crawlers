package by.andd3dfx.pravtor;

import by.andd3dfx.pravtor.dto.BatchSearchResult;
import by.andd3dfx.pravtor.dto.TorrentData;
import by.andd3dfx.pravtor.util.FileUtil;
import by.andd3dfx.pravtor.crawler.PravtorRuWebCrawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class MainApp {

    private static final PravtorRuWebCrawler crawler = new PravtorRuWebCrawler();;
    private static final FileUtil fileUtil = new FileUtil();

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Two 2 params should be provided: paramsFileName & excelFileName!");
        }
        String paramsFileName = args[0];
        String excelFileName = args[1];

        var searchItems = new ArrayList<BatchSearchResult>();
        for (var searchCriteria : fileUtil.loadSearchCriteria(paramsFileName)) {
            String startingUrl = searchCriteria.url();
            String label = searchCriteria.topic();

            var result = crawler.batchSearch(startingUrl, -1, 20)
                    .stream()
                    .filter(torrentData -> torrentData.getDownloadedCount() != null)
                    .sorted(Comparator.comparingInt(TorrentData::getDownloadedCount).reversed())
                    .toList();
            searchItems.add(new BatchSearchResult(label, result));
        }

        fileUtil.writeIntoExcel(excelFileName, searchItems);
    }
}
