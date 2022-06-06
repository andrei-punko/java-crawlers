package by.andd3dfx.pravtor.util;

import by.andd3dfx.pravtor.model.SingleSearchResult;
import by.andd3dfx.pravtor.model.TorrentData;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static by.andd3dfx.pravtor.util.FileUtil.HEADER_LABELS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SearchUtilTest {

    private String STARTING_URL = "https://pravtor.ru/viewforum.php?f=28";  // Святоотеческие тексты и жития святых
    private String PARAMS_FILE = "src/test/resources/test-params.txt";
    private String RESULT_XLS_FILE = "target/tmp-result.xls";

    private SearchUtil searchUtil;

    @Before
    public void setup() {
        searchUtil = new SearchUtil();
    }

    @Test
    public void batchSearch() throws IOException, InterruptedException {
        List<TorrentData> result = searchUtil.batchSearch(STARTING_URL, 2, 20);

        assertThat("Wrong amount of result records", result.size(), is(100));
    }

    @Test
    public void singleSearch() throws IOException {
        SingleSearchResult result = searchUtil.singleSearch(STARTING_URL);

        assertThat("Wrong amount of result records", result.getDataItems().size(), is(50));
    }

    @Test
    public void testMain() throws IOException, InterruptedException {
        searchUtil.main(new String[]{PARAMS_FILE, RESULT_XLS_FILE});

        // Check output XLS existence
        assertTrue(Files.exists(Path.of(RESULT_XLS_FILE)));

        // Check output XLS content
        Workbook book = new HSSFWorkbook(new POIFSFileSystem(new File(RESULT_XLS_FILE)));
        assertThat("Two sheets expected", book.getNumberOfSheets(), is(2));
        assertThat("Unexpected name of first sheet", book.getSheetName(0), is("txt-molitvy"));
        assertThat("Unexpected name of second sheet", book.getSheetName(1), is("txt-kanony"));

        for (int i = 0; i < HEADER_LABELS.length; i++) {
            assertThat("Expected column name for sheet 0",
                    book.getSheetAt(0).getRow(0).getCell(i).getStringCellValue(), is(HEADER_LABELS[i]));
            assertThat("Expected column name for sheet 1",
                    book.getSheetAt(1).getRow(0).getCell(i).getStringCellValue(), is(HEADER_LABELS[i]));
        }

        assertThat(book.getSheetAt(0).getLastRowNum(),  greaterThanOrEqualTo(2));
        assertThat(book.getSheetAt(1).getLastRowNum(), greaterThanOrEqualTo(2));
    }

    @Test
    public void testMainWithNoParamsProvided() throws Exception {
        runMainNCheck(new String[]{});
    }

    @Test
    public void testMainWithOneParamProvided() throws Exception {
        runMainNCheck(new String[]{PARAMS_FILE});
    }

    @Test
    public void testMainWithThreeParamsProvided() throws Exception {
        runMainNCheck(new String[]{PARAMS_FILE, RESULT_XLS_FILE, "one more"});
    }

    private void runMainNCheck(String[] args) throws Exception {
        try {
            searchUtil.main(args);
            fail("Exception should be thrown");
        } catch (IllegalArgumentException iae) {
            assertThat(iae.getMessage(), is("Should be 2 parameters!"));
        }
    }
}
