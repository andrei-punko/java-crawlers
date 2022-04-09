package by.andd3dfx.sitesparsing.firstcatalogby;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class PriceSearchUtilTest {

    private String FIRST_CATALOG_SEARCH_URL = "https://komp.1k.by/utility-graphicscards/msi/" +
        "MSI_GeForce_RTX_3060_GAMING_X_12GB-4439542.html";

    private PriceSearchUtil priceSearchUtil;

    @Before
    public void setup() {
        priceSearchUtil = new PriceSearchUtil();
    }

    @Test
    public void testGetLowestFirstCatalogPriceByUrl() throws Exception {
        Double price = priceSearchUtil.getLowestFirstCatalogPriceByUrl(FIRST_CATALOG_SEARCH_URL);
        assertThat("Price should be greater than 0", price, Matchers.greaterThan(0.0));
    }
}
