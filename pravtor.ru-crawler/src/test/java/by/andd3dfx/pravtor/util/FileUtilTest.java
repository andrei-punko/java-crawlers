package by.andd3dfx.pravtor.util;

import by.andd3dfx.pravtor.model.BatchSearchResult;
import by.andd3dfx.pravtor.model.SearchCriteria;
import by.andd3dfx.pravtor.model.TorrentData;
import org.dbunit.dataset.excel.XlsDataSet;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.dbunit.Assertion.assertEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FileUtilTest {

    private final String TEST_PARAMS_FILE = "src/test/resources/test-params.txt";
    private final String GENERATED_XLS_FILE = "target/generated-test-file.xls";
    private final String EXPECTED_XLS_FILE = "src/test/resources/expected-test-file.xls";

    private FileUtil fileUtil;

    @Before
    public void setup() {
        fileUtil = new FileUtil();
    }

    @Test
    public void loadSearchCriteria() throws IOException {
        List<SearchCriteria> criteriaItems = fileUtil.loadSearchCriteria(TEST_PARAMS_FILE);

        assertThat("Wrong count of criteria items", criteriaItems.size(), is(2));
        var item0 = criteriaItems.get(0);
        assertThat("Wrong url of first item", item0.getTopic(), is("txt-molitvy"));
        assertThat("Wrong label of first item", item0.getUrl(), is("https://pravtor.ru/viewforum.php?f=184"));
        var item1 = criteriaItems.get(1);
        assertThat("Wrong label of second item", item1.getTopic(), is("txt-kanony"));
        assertThat("Wrong url of second item", item1.getUrl(), is("https://pravtor.ru/viewforum.php?f=183"));
    }

    @Test
    public void writeIntoExcel() throws Exception {
        List<BatchSearchResult> searchItems = List.of(
                new BatchSearchResult("Sheet label", List.of(
                        buildTorrentData("label 1", 23, 12, 234, "23 Mb", "link1"),
                        buildTorrentData("label 2", 22, 2, 54, "13 Mb", "link2")
                )),
                new BatchSearchResult("Sheet label 2", List.of(
                        buildTorrentData("label 3", 32, 3, 678, "55 Mb", "link3"),
                        buildTorrentData("label 4", 45, 5, 434, "22 Mb", "link4")
                ))
        );

        fileUtil.writeIntoExcel(GENERATED_XLS_FILE, searchItems);

        XlsDataSet generated = new XlsDataSet(new FileInputStream(GENERATED_XLS_FILE));
        XlsDataSet expected = new XlsDataSet(new FileInputStream(EXPECTED_XLS_FILE));
        assertEquals(generated, expected);
    }

    private TorrentData buildTorrentData(String label, int seedsCount, int peersCount, int downloadedCount,
                                         String size, String linkUrl) {
        return TorrentData.builder()
                .label(label)
                .seedsCount(seedsCount)
                .peersCount(peersCount)
                .downloadedCount(downloadedCount)
                .size(size)
                .linkUrl(linkUrl)
                .build();
    }
}
