package by.andd3dfx.pravtor.util;

import by.andd3dfx.pravtor.model.SingleSearchResult;
import by.andd3dfx.pravtor.model.TorrentData;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SearchUtilTest {

    private String STARTING_URL = "https://pravtor.ru/viewforum.php?f=28";  // Святоотеческие тексты и жития святых
    private String PARAMS_FILE = "src/test/resources/test-params.txt";
    private String RESULT_XLS_FILE = "target/tmp-result.xls";

    private SearchUtil searchUtil;

    @Before
    public void setup() throws IOException {
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
        SearchUtil.main(new String[]{PARAMS_FILE, RESULT_XLS_FILE});

        assertTrue(Files.exists(Path.of(RESULT_XLS_FILE)));
        // TODO: add extended check of file content
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
            SearchUtil.main(args);
            fail("Exception should be thrown");
        } catch (IllegalArgumentException iae) {
            assertThat(iae.getMessage(), is("Should be 2 parameters!"));
        }
    }
}
