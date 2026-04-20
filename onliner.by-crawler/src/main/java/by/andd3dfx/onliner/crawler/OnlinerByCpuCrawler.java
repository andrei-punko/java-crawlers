package by.andd3dfx.onliner.crawler;

import by.andd3dfx.crawler.dto.SingleSearchResult;
import by.andd3dfx.crawler.engine.WebCrawler;
import by.andd3dfx.onliner.dto.ProcessorData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
            String productUrl = textOrNull(offers, "url");
            CpuSpecs specs = extractCpuSpecsFromDetailPage(productUrl, throttlingDelayMs);
            items.add(ProcessorData.builder()
                    .name(name)
                    .brand(extractBrand(product))
                    .url(productUrl)
                    .price(offers.hasNonNull("price") ? offers.get("price").asText() : null)
                    .currency(textOrNull(offers, "priceCurrency"))
                    .description(description)
                    .socket(specs.socket())
                    .coreCount(specs.coreCount())
                    .threadCount(specs.threadCount())
                    .maxFrequencyGHz(specs.maxFrequencyGHz())
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

    private CpuSpecs extractCpuSpecsFromDetailPage(String productUrl, long throttlingDelayMs) {
        if (productUrl == null || productUrl.isBlank()) {
            return CpuSpecs.EMPTY;
        }

        try {
            Document detailDoc = retrieveDocument(productUrl, throttlingDelayMs);
            String socket = normalizeSocket(findSpecValue(detailDoc, "сокет"));
            Integer coreCount = parseInteger(findSpecValue(detailDoc, "количество ядер"));
            Integer threadCount = parseInteger(findSpecValue(detailDoc, "количество потоков"));

            Double maxFrequencyGHz = parseFrequencyGHz(findSpecValue(detailDoc, "максимальная частота"));
            if (maxFrequencyGHz == null) {
                maxFrequencyGHz = parseFrequencyGHz(findSpecValue(detailDoc, "тактовая частота"));
            }

            return new CpuSpecs(socket, coreCount, threadCount, maxFrequencyGHz);
        } catch (Exception e) {
            log.warn("Не удалось получить детали CPU со страницы {}: {}", productUrl, e.getMessage());
            return CpuSpecs.EMPTY;
        }
    }

    private static String findSpecValue(Document detailDoc, String labelPartLowerCase) {
        for (Element row : detailDoc.select("tr")) {
            Elements allCells = row.select("th,td");
            if (allCells.size() < 2) {
                continue;
            }
            String key = allCells.getFirst().text();
            if (!labelMatches(key, labelPartLowerCase)) {
                continue;
            }
            return textOrNull(allCells.get(1).text());
        }

        for (Element dl : detailDoc.select("dl")) {
            Elements terms = dl.select("dt");
            for (Element term : terms) {
                if (!labelMatches(term.text(), labelPartLowerCase)) {
                    continue;
                }
                Element valueNode = term.nextElementSibling();
                if (valueNode != null && "dd".equalsIgnoreCase(valueNode.tagName())) {
                    return textOrNull(valueNode.text());
                }
            }
        }

        for (Element item : detailDoc.select("li, div")) {
            String text = textOrNull(item.text());
            if (text == null || !text.contains(":")) {
                continue;
            }
            String key = text.substring(0, text.indexOf(':'));
            if (!labelMatches(key, labelPartLowerCase)) {
                continue;
            }
            String value = text.substring(text.indexOf(':') + 1).trim();
            return textOrNull(value);
        }
        return null;
    }

    private static Integer parseInteger(String raw) {
        if (raw == null) {
            return null;
        }
        var m = Pattern.compile("\\b(\\d{1,2})\\b").matcher(raw);
        if (!m.find()) {
            return null;
        }
        int value = Integer.parseInt(m.group(1));
        return value > 0 && value <= 64 ? value : null;
    }

    private static Double parseFrequencyGHz(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.toLowerCase(Locale.ROOT)
                .replace('\u00A0', ' ')
                .replace("mhz", "мгц")
                .replace("ghz", "ггц");
        var m = Pattern.compile("([0-9]+(?:[.,][0-9]+)?)\\s*(ггц|мгц)", Pattern.CASE_INSENSITIVE)
                .matcher(normalized);
        if (!m.find()) {
            return null;
        }
        double value = Double.parseDouble(m.group(1).replace(',', '.'));
        String unit = m.group(2).toLowerCase(Locale.ROOT);
        if (unit.contains("мгц") || unit.contains("mhz")) {
            return value / 1000.0;
        }
        return value;
    }

    private static String textOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private static boolean labelMatches(String sourceLabel, String expectedLabelLowerCase) {
        if (sourceLabel == null) {
            return false;
        }
        String normalized = sourceLabel.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N} ]", " ").trim();
        return normalized.contains(expectedLabelLowerCase);
    }

    private static String normalizeSocket(String raw) {
        if (raw == null) {
            return null;
        }
        var am = Pattern.compile("\\b(am\\s*\\d)\\b", Pattern.CASE_INSENSITIVE).matcher(raw);
        if (am.find()) {
            return am.group(1).toUpperCase(Locale.ROOT).replace(" ", "");
        }
        var lga = Pattern.compile("\\b(lga\\s*\\d{3,4})\\b", Pattern.CASE_INSENSITIVE).matcher(raw);
        if (lga.find()) {
            return lga.group(1).toUpperCase(Locale.ROOT).replace(" ", "");
        }
        return null;
    }

    private record CpuSpecs(String socket, Integer coreCount, Integer threadCount, Double maxFrequencyGHz) {
        private static final CpuSpecs EMPTY = new CpuSpecs(null, null, null, null);
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
