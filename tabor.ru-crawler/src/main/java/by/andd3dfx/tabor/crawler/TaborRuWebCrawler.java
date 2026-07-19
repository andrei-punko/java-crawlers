package by.andd3dfx.tabor.crawler;

import by.andd3dfx.crawler.engine.WebCrawler;
import by.andd3dfx.tabor.dto.ProfileData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Crawler for dating profiles on <a href="https://tabor.ru">tabor.ru</a>
 * (women in Minsk, configurable age range, with cover photo).
 */
@Slf4j
public class TaborRuWebCrawler extends WebCrawler<ProfileData> {

    public static final int DEFAULT_MIN_AGE = 25;
    public static final int DEFAULT_MAX_AGE = 45;
    public static final String CITY = "Минск";
    public static final int COUNTRY_ID = 248;
    /** «Дети» values that exclude a profile after the detail page is parsed. */
    public static final Set<String> EXCLUDED_CHILDREN = Set.of(
            "есть, живем вместе",
            "есть, живем порознь",
            "нет, и не хочу"
    );
    /** «Отношение к курению» values that exclude a profile. */
    public static final Set<String> EXCLUDED_SMOKING = Set.of("курю");
    /**
     * Safety ceiling when {@code maxPagesCap == -1} («all pages»), to avoid infinite loops
     * if pagination never ends.
     */
    public static final int UNLIMITED_PAGES_HARD_CAP = 5000;

    private static final String BASE_URL = "https://tabor.ru";
    private static final Pattern PROFILE_ID_PATTERN = Pattern.compile("/id(\\d+)");

    private final int minAge;
    private final int maxAge;
    private final Set<String> seenProfileIds = new HashSet<>();

    public TaborRuWebCrawler() {
        this(DEFAULT_MIN_AGE, DEFAULT_MAX_AGE);
    }

    public TaborRuWebCrawler(int minAge, int maxAge) {
        if (minAge < 18) {
            throw new IllegalArgumentException("minAge must be >= 18, got " + minAge);
        }
        if (maxAge < minAge) {
            throw new IllegalArgumentException("maxAge (" + maxAge + ") must be >= minAge (" + minAge + ")");
        }
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    public int getMinAge() {
        return minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public String buildStartingUrl() {
        String cityEncoded = URLEncoder.encode(CITY, StandardCharsets.UTF_8);
        return BASE_URL + "/search"
                + "?search%5Bage%5D=" + minAge + "%3B" + maxAge
                + "&search%5Bfind_sex%5D=2"
                + "&search%5Bcity%5D=" + cityEncoded
                + "&search%5Bcountry_id%5D=" + COUNTRY_ID;
    }

    @Override
    public List<ProfileData> batchSearch(String pageUrl, int maxPagesCap, long throttlingDelayMs) {
        seenProfileIds.clear();
        int effectiveCap = maxPagesCap;
        if (effectiveCap == -1) {
            effectiveCap = UNLIMITED_PAGES_HARD_CAP;
            log.info("pagesCap=-1: applying hard cap of {} pages", UNLIMITED_PAGES_HARD_CAP);
        }
        return super.batchSearch(pageUrl, effectiveCap, throttlingDelayMs);
    }

    @Override
    protected Elements extractElements(Document document) {
        Elements matched = new Elements();
        for (Element element : document.select("li.search-list__item")) {
            Integer age = parseAge(element.select("[itemprop=description]").text());
            String city = StringUtils.trimToNull(element.select("[itemprop=addressLocality]").text());
            String profileId = extractProfileIdFromListCard(element);
            if (age != null && age >= minAge && age <= maxAge
                    && CITY.equalsIgnoreCase(city)
                    && hasCoverPhoto(element)
                    && profileId != null
                    && seenProfileIds.add(profileId)) {
                matched.add(element);
            } else if (profileId != null && seenProfileIds.contains(profileId)) {
                log.debug("Skip already seen profile id={}", profileId);
            }
        }
        return matched;
    }

    static String extractProfileIdFromListCard(Element element) {
        String attrId = StringUtils.trimToNull(element.attr("element"));
        if (attrId != null) {
            return attrId;
        }
        String href = element.select("a[itemprop=url]").attr("href");
        if (StringUtils.isBlank(href)) {
            href = element.select("a[href*=/id]").attr("href");
        }
        return extractId(href);
    }

    /**
     * Real cover photos are served from {@code /photos/}; placeholders/default avatars are skipped
     * so the profile page is not fetched.
     */
    static boolean hasCoverPhoto(Element element) {
        String src = element.select("img[itemprop=image], img.comment__avatar").attr("abs:src");
        if (StringUtils.isBlank(src)) {
            src = element.select("img[itemprop=image], img.comment__avatar").attr("src");
        }
        return StringUtils.isNotBlank(src) && src.contains("/photos/");
    }

    @Override
    protected String extractNextUrl(Document document) {
        Elements nextLinks = document.select("a[rel=next]");
        if (nextLinks.isEmpty()) {
            return null;
        }
        String href = nextLinks.first().attr("abs:href");
        return StringUtils.trimToNull(href);
    }

    @SneakyThrows
    @Override
    protected ProfileData mapElementToData(Element element, long throttlingDelayMs) {
        String href = element.select("a[itemprop=url]").attr("abs:href");
        if (StringUtils.isBlank(href)) {
            href = element.select("a[href^=/id]").attr("abs:href");
        }
        log.info("Retrieve profile details for {}", href);
        Document document = retrieveDocument(href, throttlingDelayMs);
        return mapProfileDocument(document);
    }

    ProfileData mapProfileDocument(Document document) {
        Map<String, String> about = extractAboutFields(document);

        String url = document.baseUri();
        String id = extractId(url);
        Integer age = parseAge(document.select("[itemprop=description]").first() != null
                ? document.select("[itemprop=description]").first().text()
                : null);

        String lookingFor = StringUtils.trimToNull(
                document.select("#meet .about__list__item").stream()
                        .map(Element::text)
                        .filter(t -> t.startsWith("Ищу "))
                        .findFirst()
                        .orElse(null)
        );

        return ProfileData.builder()
                .url(url)
                .id(id)
                .name(StringUtils.trimToNull(document.select("h1.user__name").text()))
                .age(age)
                .city(StringUtils.trimToNull(document.select("[itemprop=addressLocality]").text()))
                .country(StringUtils.trimToNull(document.select("[itemprop=addressCountry]").text()))
                .photoUrl(extractPhotoUrl(document))
                .lookingFor(lookingFor)
                .purpose(about.get("Цель знакомства"))
                .height(about.get("Рост"))
                .weight(about.get("Вес"))
                .bodyType(about.get("Телосложение"))
                .eyeColor(about.get("Цвет глаз"))
                .maritalStatus(about.get("Семейное положение"))
                .children(about.get("Дети"))
                .occupation(firstNonBlank(about.get("Профессия"), about.get("Сфера деятельности")))
                .smoking(about.get("Отношение к курению"))
                .aboutText(extractAboutText(document))
                .build();
    }

    private static String extractPhotoUrl(Document document) {
        String ogImage = document.select("meta[property=og:image]").attr("content");
        if (StringUtils.isNotBlank(ogImage)) {
            return ogImage.trim();
        }
        String img = document.select("img[itemprop=image]").attr("abs:src");
        return StringUtils.trimToNull(img);
    }

    private static Map<String, String> extractAboutFields(Document document) {
        java.util.HashMap<String, String> fields = new java.util.HashMap<>();
        for (Element item : document.select(".about__list__item")) {
            Elements terms = item.select(".about__list__term");
            Elements descs = item.select(".about__list__desc");
            int count = Math.min(terms.size(), descs.size());
            for (int i = 0; i < count; i++) {
                String key = normalizeTerm(terms.get(i).text());
                String value = StringUtils.trimToNull(descs.get(i).text());
                if (key != null && value != null) {
                    fields.put(key, value);
                }
            }
        }
        return fields;
    }

    private static String normalizeTerm(String term) {
        String trimmed = StringUtils.trimToNull(term);
        if (trimmed == null) {
            return null;
        }
        return trimmed.replace(":", "").trim();
    }

    private static String extractAboutText(Document document) {
        Element aboutBlock = document.selectFirst("#about .about__list, .about__content#about");
        if (aboutBlock == null) {
            return null;
        }
        String text = StringUtils.trimToNull(aboutBlock.text());
        return text;
    }

    private static String extractId(String url) {
        if (url == null) {
            return null;
        }
        Matcher matcher = PROFILE_ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    static Integer parseAge(String raw) {
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        Matcher matcher = Pattern.compile("(\\d+)").matcher(raw.trim());
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    private static String firstNonBlank(String first, String second) {
        if (StringUtils.isNotBlank(first)) {
            return first;
        }
        return StringUtils.trimToNull(second);
    }

    public boolean matchesSearchCriteria(ProfileData profile) {
        return rejectionReason(profile) == null;
    }

    /**
     * @return human-readable rejection reason, or {@code null} if the profile passes filters
     */
    public String rejectionReason(ProfileData profile) {
        if (profile == null) {
            return "null profile";
        }
        if (profile.getAge() == null || profile.getCity() == null || StringUtils.isBlank(profile.getPhotoUrl())) {
            return "incomplete (age/city/photo)";
        }
        if (!profile.getPhotoUrl().contains("/photos/")) {
            return "no cover photo";
        }
        if (!CITY.equalsIgnoreCase(profile.getCity())) {
            return "city not " + CITY + " (" + profile.getCity() + ")";
        }
        int age = profile.getAge();
        if (age < minAge || age > maxAge) {
            return "age out of range (" + age + ", expected " + minAge + "-" + maxAge + ")";
        }
        String children = StringUtils.trimToNull(profile.getChildren());
        if (children != null) {
            for (String excluded : EXCLUDED_CHILDREN) {
                if (excluded.equalsIgnoreCase(children)) {
                    return "children: " + children;
                }
            }
        }
        String smoking = StringUtils.trimToNull(profile.getSmoking());
        if (smoking != null) {
            for (String excluded : EXCLUDED_SMOKING) {
                if (excluded.equalsIgnoreCase(smoking)) {
                    return "smoking: " + smoking;
                }
            }
        }
        return null;
    }
}
