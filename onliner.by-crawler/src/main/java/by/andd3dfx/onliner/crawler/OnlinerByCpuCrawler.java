package by.andd3dfx.onliner.crawler;

import by.andd3dfx.crawler.dto.SingleSearchResult;
import by.andd3dfx.crawler.engine.WebCrawler;
import by.andd3dfx.onliner.dto.ProcessorData;
import by.andd3dfx.onliner.util.ProcessorDescriptionParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Парсер каталога процессоров <a href="https://catalog.onliner.by/cpu">catalog.onliner.by</a>.
 * Фильтры задаются в URL: {@code socket_cpu[0]=am4}, {@code price[from]=1}.
 * Список позиций — JSON-LD {@code ItemList} в {@code script type=application/ld+json}.
 */
@Slf4j
public class OnlinerByCpuCrawler extends WebCrawler<ProcessorData> {

    public static final String AM4_CPU_LIST_URL =
            "https://catalog.onliner.by/cpu?socket_cpu%5B0%5D=am4&price%5Bfrom%5D=1";

    private static final int PRODUCTS_PER_PAGE = 30;
    private static final Pattern PAGE_PARAM = Pattern.compile("[?&]page=(\\d+)");

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String buildAm4StartingUrl() {
        return AM4_CPU_LIST_URL;
    }

    @Override
    @SneakyThrows
    public SingleSearchResult<ProcessorData> singleSearch(String pageUrl, long throttlingDelayMs) {
        Document document = retrieveDocument(pageUrl, throttlingDelayMs);
        String jsonLd = findItemListJson(document);
        if (jsonLd == null) {
            log.warn("Не найден JSON-LD ItemList на странице {}", pageUrl);
            return new SingleSearchResult<>(List.of(), null);
        }

        JsonNode root = objectMapper.readTree(jsonLd);
        JsonNode itemListElement = root.path("itemListElement");
        if (!itemListElement.isArray()) {
            return new SingleSearchResult<>(List.of(), null);
        }

        int rawCount = itemListElement.size();
        List<ProcessorData> items = new ArrayList<>();
        for (JsonNode listItem : itemListElement) {
            JsonNode product = listItem.path("item");
            if (product.isMissingNode() || !product.isObject()) {
                continue;
            }
            String description = textOrNull(product, "description");
            String name = textOrNull(product, "name");
            JsonNode offers = product.path("offers");
            items.add(ProcessorData.builder()
                    .name(name)
                    .brand(extractBrand(product))
                    .url(textOrNull(offers, "url"))
                    .price(offers.hasNonNull("price") ? offers.get("price").asText() : null)
                    .currency(textOrNull(offers, "priceCurrency"))
                    .description(description)
                    .socket("AM4")
                    .coreCount(ProcessorDescriptionParser.parseCoreCount(description, name))
                    .threadCount(ProcessorDescriptionParser.parseThreadCount(description, name))
                    .maxFrequencyGHz(ProcessorDescriptionParser.parseMaxFrequencyGHz(description))
                    .build());
        }

        String nextUrl = null;
        if (rawCount >= PRODUCTS_PER_PAGE) {
            int nextPage = parsePage(pageUrl) + 1;
            nextUrl = buildListingUrl(nextPage);
        }
        log.debug("Страница {}: позиций в JSON-LD {}", pageUrl, rawCount);

        return new SingleSearchResult<>(items, nextUrl);
    }

    private static String buildListingUrl(int page) {
        if (page <= 1) {
            return AM4_CPU_LIST_URL;
        }
        return AM4_CPU_LIST_URL + "&page=" + page;
    }

    private static int parsePage(String pageUrl) {
        var m = PAGE_PARAM.matcher(pageUrl);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 1;
    }

    private static String findItemListJson(Document document) {
        for (Element script : document.select("script[type=application/ld+json]")) {
            String data = script.data();
            if (data.contains("\"ItemList\"") && data.contains("itemListElement")) {
                return data;
            }
        }
        return null;
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode n = node.path(field);
        if (n.isMissingNode() || n.isNull()) {
            return null;
        }
        String t = n.asText();
        return t.isBlank() ? null : t;
    }

    private static String extractBrand(JsonNode product) {
        JsonNode brand = product.path("brand");
        if (brand.isObject()) {
            return textOrNull(brand, "name");
        }
        if (brand.isTextual()) {
            return brand.asText();
        }
        return null;
    }

    @Override
    protected Elements extractElements(Document document) {
        return new Elements();
    }

    @Override
    protected String extractNextUrl(Document document) {
        return null;
    }

    @Override
    protected ProcessorData mapElementToData(Element element, long throttlingDelayMs) {
        throw new UnsupportedOperationException(
                "Каталог Onliner отдаёт список в JSON-LD; используйте singleSearch/batchSearch.");
    }
}
