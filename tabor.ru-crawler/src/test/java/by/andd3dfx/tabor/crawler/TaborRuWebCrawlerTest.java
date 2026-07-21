package by.andd3dfx.tabor.crawler;

import by.andd3dfx.tabor.dto.ProfileData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TaborRuWebCrawlerTest {

    @Test
    public void buildStartingUrlContainsFilters() {
        var crawler = new TaborRuWebCrawler();
        String url = crawler.buildStartingUrl();

        assertThat(url).startsWith("https://tabor.ru/search?");
        assertThat(url).contains("search%5Bage%5D=" + TaborRuWebCrawler.DEFAULT_MIN_AGE
                + "%3B" + TaborRuWebCrawler.DEFAULT_MAX_AGE);
        assertThat(url).contains("search%5Bfind_sex%5D=2");
        assertThat(url).contains("search%5Bcountry_id%5D=248");
        assertThat(url).contains("search%5Bcity%5D=");
    }

    @Test
    public void batchSearchWithOfflineStubFiltersByCityAgeAndPhoto() {
        var stub = new TaborRuWebCrawlerOfflineStub();
        var result = stub.batchSearch(stub.buildStartingUrl(), 2, 1);

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(profile -> {
            assertThat(profile.getCity()).isEqualToIgnoringCase("Минск");
            assertThat(profile.getAge()).isBetween(TaborRuWebCrawler.DEFAULT_MIN_AGE, TaborRuWebCrawler.DEFAULT_MAX_AGE);
            assertThat(profile.getPhotoUrl()).contains("/photos/");
            assertThat(profile.getName()).isNotBlank();
        });
        assertThat(result.get(0).getId()).isEqualTo("1001");
        assertThat(result.get(1).getId()).isEqualTo("2001");
    }

    @Test
    public void mapProfileDocumentParsesAboutFields() {
        var crawler = new TaborRuWebCrawler();
        Document document = Jsoup.parse(PROFILE_HTML, "https://tabor.ru/id1001");

        ProfileData profile = crawler.mapProfileDocument(document);

        assertThat(profile.getId()).isEqualTo("1001");
        assertThat(profile.getName()).isEqualTo("Анна");
        assertThat(profile.getAge()).isEqualTo(30);
        assertThat(profile.getCity()).isEqualTo("Минск");
        assertThat(profile.getPhotoUrl()).contains("800x600.jpg");
        assertThat(profile.getStatusText()).isEqualTo("Привет всем");
        assertThat(profile.getPurpose()).isEqualTo("любовь и отношения");
        assertThat(profile.getImportantInPartner()).isEqualTo("ум");
        assertThat(profile.getLifePriorities()).isEqualTo("семья и карьера");
        assertThat(profile.getCharacterTraits()).isEqualTo("спокойная, целеустремленная");
        assertThat(profile.getInterestsAndHobbies()).isEqualTo("путешествия, спорт");
        assertThat(profile.getHeight()).isEqualTo("170 см.");
        assertThat(profile.getWeight()).isEqualTo("60 кг.");
        assertThat(profile.getBodyType()).isEqualTo("обычное");
        assertThat(profile.getEyeColor()).isEqualTo("зеленый");
        assertThat(profile.getAppearance()).isEqualTo("обыкновенная");
        assertThat(profile.getMaritalStatus()).isEqualTo("не замужем");
        assertThat(profile.getRelationshipStatus()).isEqualTo("в активном поиске");
        assertThat(profile.getChildren()).isEqualTo("нет");
        assertThat(profile.getEducation()).isEqualTo("высшее");
        assertThat(profile.getSmoking()).isEqualTo("не курю");
        assertThat(profile.getAlcohol()).isEqualTo("не пью");
        assertThat(profile.getOccupation()).isEqualTo("дизайнер");
        assertThat(profile.getActivity()).isEqualTo("IT");
        assertThat(profile.getHousing()).isEqualTo("своя квартира");
        assertThat(profile.getMaterialStatus()).isEqualTo("среднее");
        assertThat(profile.getMaterialSupport()).isEqualTo("не нуждаюсь в спонсоре");
        assertThat(profile.getAboutText()).contains("люблю путешествия");
        assertThat(profile.getLookingFor()).contains("Ищу");
    }

    @Test
    public void buildStartingUrlUsesCustomAgeRange() {
        var crawler = new TaborRuWebCrawler(30, 35);
        assertThat(crawler.buildStartingUrl()).contains("search%5Bage%5D=30%3B35");
    }

    @Test
    public void extractNextUrlFallsBackToIncrementingPage() {
        var crawler = new TaborRuWebCrawler();
        String html = """
                <html><body>
                <ul class="search-list">
                  <li class="search-list__item">x</li>
                </ul>
                </body></html>
                """;
        Document document = Jsoup.parse(html,
                "https://tabor.ru/search?page=80&search%5Bage%5D=25%3B45&search%5Bfind_sex%5D=2");

        assertThat(crawler.extractNextUrl(document))
                .isEqualTo("https://tabor.ru/search?page=81&search%5Bage%5D=25%3B45&search%5Bfind_sex%5D=2");
    }

    @Test
    public void extractNextUrlStopsOnEmptyListing() {
        var crawler = new TaborRuWebCrawler();
        Document document = Jsoup.parse("<html><body></body></html>",
                "https://tabor.ru/search?page=1001&search%5Bage%5D=25%3B45");
        assertThat(crawler.extractNextUrl(document)).isNull();
    }

    @Test
    public void incrementPageUrl() {
        assertThat(TaborRuWebCrawler.incrementPageUrl(
                "https://tabor.ru/search?page=80&search%5Bage%5D=25%3B45"))
                .isEqualTo("https://tabor.ru/search?page=81&search%5Bage%5D=25%3B45");
        assertThat(TaborRuWebCrawler.incrementPageUrl("https://tabor.ru/search?search%5Bage%5D=25%3B45"))
                .isEqualTo("https://tabor.ru/search?search%5Bage%5D=25%3B45&page=2");
    }

    @Test
    public void matchesSearchCriteria() {
        var crawler = new TaborRuWebCrawler();
        assertThat(crawler.matchesSearchCriteria(ProfileData.builder()
                .age(30).city("Минск")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build())).isTrue();
        assertThat(crawler.matchesSearchCriteria(ProfileData.builder()
                .city("Минск")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build())).isTrue();
        assertThat(crawler.matchesSearchCriteria(ProfileData.builder()
                .age(30).city("Минск").children("нет")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build())).isTrue();
        assertThat(crawler.matchesSearchCriteria(ProfileData.builder()
                .age(30).city("Минск").children("есть, живем порознь")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build())).isTrue();
        assertThat(crawler.matchesSearchCriteria(ProfileData.builder()
                .age(30).city("Минск").children("есть, живем вместе")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build())).isTrue();
        assertThat(crawler.matchesSearchCriteria(ProfileData.builder()
                .age(30).city("Минск").children("нет, и не хочу")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build())).isFalse();
        assertThat(crawler.matchesSearchCriteria(ProfileData.builder()
                .age(30).city("Минск").smoking("курю")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build())).isFalse();
        assertThat(crawler.matchesSearchCriteria(ProfileData.builder()
                .age(30).city("Минск").alcohol("люблю выпить")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build())).isFalse();
        assertThat(crawler.matchesSearchCriteria(ProfileData.builder()
                .age(30).city("Минск").materialSupport("ищу спонсора")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build())).isFalse();
        assertThat(crawler.matchesSearchCriteria(ProfileData.builder()
                .age(30).city("Минск").maritalStatus("замужем")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build())).isFalse();
        assertThat(crawler.matchesSearchCriteria(ProfileData.builder()
                .age(30).city("Минск").maritalStatus("не замужем")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build())).isTrue();
        assertThat(crawler.matchesSearchCriteria(ProfileData.builder()
                .age(30).city("Минск").weight("80 кг.")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build())).isTrue();
        assertThat(crawler.matchesSearchCriteria(ProfileData.builder()
                .age(30).city("Минск").weight("81 кг.")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build())).isFalse();
        assertThat(crawler.matchesSearchCriteria(ProfileData.builder()
                .age(30).city("Минск").smoking("не курю")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build())).isTrue();
        assertThat(crawler.rejectionReason(ProfileData.builder()
                .age(30).city("Минск").smoking("курю")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build()))
                .isEqualTo("smoking: курю");
        assertThat(crawler.rejectionReason(ProfileData.builder()
                .age(30).city("Минск").children("нет, и не хочу")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build()))
                .isEqualTo("children: нет, и не хочу");
        assertThat(crawler.rejectionReason(ProfileData.builder()
                .age(30).city("Минск").alcohol("люблю выпить")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build()))
                .isEqualTo("alcohol: люблю выпить");
        assertThat(crawler.rejectionReason(ProfileData.builder()
                .age(30).city("Минск").materialSupport("ищу спонсора")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build()))
                .isEqualTo("materialSupport: ищу спонсора");
        assertThat(crawler.rejectionReason(ProfileData.builder()
                .age(30).city("Минск").maritalStatus("замужем")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build()))
                .isEqualTo("maritalStatus: замужем");
        assertThat(crawler.rejectionReason(ProfileData.builder()
                .age(30).city("Минск").education("среднее")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build()))
                .isEqualTo("education: среднее");
        assertThat(crawler.rejectionReason(ProfileData.builder()
                .age(30).city("Минск").weight("85 кг.")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build()))
                .isEqualTo("weight is too high (max 80 кг)");
        assertThat(crawler.matchesSearchCriteria(ProfileData.builder()
                .age(30).city("Минск").education("среднее")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build())).isFalse();
        assertThat(crawler.matchesSearchCriteria(ProfileData.builder()
                .age(30).city("Минск").education("высшее")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build())).isTrue();
        assertThat(crawler.matchesSearchCriteria(ProfileData.builder()
                .age(24).city("Минск")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build())).isFalse();
        assertThat(crawler.matchesSearchCriteria(ProfileData.builder()
                .age(30).city("Гродно")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build())).isFalse();
        assertThat(crawler.matchesSearchCriteria(ProfileData.builder()
                .age(30).city("Минск").build())).isFalse();
        assertThat(crawler.matchesSearchCriteria(ProfileData.builder()
                .age(46).city("Минск")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build())).isFalse();
        assertThat(new TaborRuWebCrawler(30, 35).matchesSearchCriteria(ProfileData.builder()
                .age(28).city("Минск")
                .photoUrl("https://p7.tabor.ru/photos/x.jpg").build())).isFalse();
    }

    @Test
    public void batchSearchSkipsDuplicateProfilesAcrossPages() {
        var stub = new TaborRuWebCrawlerOfflineStub();
        stub.duplicateMatchingIdOnSecondPage = true;

        var result = stub.batchSearch(stub.buildStartingUrl(), 2, 1);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ProfileData::getId).containsExactly("1001", "2001");
        assertThat(stub.detailFetchCount).isEqualTo(2);
    }

    @Test
    public void batchSearchSkipsSeededKnownProfileIds() {
        var stub = new TaborRuWebCrawlerOfflineStub();
        stub.seedKnownProfileIds(List.of("1001"));

        var result = stub.batchSearch(stub.buildStartingUrl(), 2, 1);

        assertThat(result).extracting(ProfileData::getId).containsExactly("2001");
        assertThat(stub.detailFetchCount).isEqualTo(1);
    }

    private static final class TaborRuWebCrawlerOfflineStub extends TaborRuWebCrawler {

        private int listPageOrdinal;
        private int detailFetchCount;
        private boolean duplicateMatchingIdOnSecondPage;

        @Override
        protected Document retrieveDocument(String pageUrl, long throttlingDelayMs) {
            if (pageUrl.contains("/search")) {
                int page = listPageOrdinal++;
                return buildListPage(page, duplicateMatchingIdOnSecondPage, pageUrl);
            }
            detailFetchCount++;
            return Jsoup.parse(PROFILE_HTML.replace("1001", extractIdFromUrl(pageUrl)), pageUrl);
        }

        private static Document buildListPage(int pageIndex, boolean duplicateOnSecondPage, String pageUrl) {
            int matchingId;
            int youngId;
            int otherCityId;
            int noPhotoId;
            if (pageIndex == 0) {
                matchingId = 1001;
                youngId = 1002;
                otherCityId = 1003;
                noPhotoId = 1004;
            } else if (duplicateOnSecondPage) {
                matchingId = 1001; // already seen on page 1
                youngId = 2002;
                otherCityId = 2003;
                noPhotoId = 2004;
            } else {
                matchingId = 2001;
                youngId = 2002;
                otherCityId = 2003;
                noPhotoId = 2004;
            }
            if (pageIndex == 1 && !duplicateOnSecondPage) {
                matchingId = 2001;
            }
            Integer extraMatchingId = (pageIndex == 1 && duplicateOnSecondPage) ? 2001 : null;
            String html = listHtml(matchingId, youngId, otherCityId, noPhotoId, extraMatchingId, pageIndex == 0);
            return Jsoup.parse(html, pageUrl);
        }

        private static String listHtml(int matchingId, int youngId, int otherCityId, int noPhotoId,
                                      Integer extraMatchingId, boolean withNext) {
            StringBuilder cards = new StringBuilder();
            cards.append(card(matchingId, "Анна", 30, "Минск", true));
            cards.append(card(youngId, "Мария", 20, "Минск", true));
            cards.append(card(otherCityId, "Ольга", 35, "Гродно", true));
            cards.append(card(noPhotoId, "Безфото", 28, "Минск", false));
            if (extraMatchingId != null) {
                cards.append(card(extraMatchingId, "Елена", 32, "Минск", true));
            }
            String next = withNext
                    ? "<a rel=\"next\" href=\"/search?page=2&search%5Bage%5D=25%3B45\">next</a>"
                    : "";
            return "<html><body><ul class=\"search-list\">" + cards + "</ul>" + next + "</body></html>";
        }

        private static String card(int id, String name, int age, String city, boolean withPhoto) {
            String img = withPhoto
                    ? "<img class=\"comment__avatar\" itemprop=\"image\" src=\"https://p7.tabor.ru/photos/2026/"
                    + id + "/avatar_128x128.jpg\"/>"
                    : "<img class=\"comment__avatar\" src=\"https://im.tabor.ru/imgs/default-avatar.png\"/>";
            return """
                    <li class="search-list__item" element="%1$d" itemscope itemtype="http://schema.org/Person">
                      <a class="comment" href="/id%1$d" itemprop="url">
                        <span class="user__name" itemprop="name">%2$s</span>
                        <span itemprop="description">%3$d</span>
                        <span itemprop="addressLocality">%4$s</span>
                        %5$s
                      </a>
                    </li>
                    """.formatted(id, name, age, city, img);
        }

        private static String extractIdFromUrl(String url) {
            int idx = url.lastIndexOf("/id");
            return idx >= 0 ? url.substring(idx + 3) : "1001";
        }
    }

    private static final String PROFILE_HTML = """
            <html><head>
            <meta property="og:image" content="https://p7.tabor.ru/photos/2026/1001/photo_800x600.jpg"/>
            </head><body>
            <h1 class="user__name" itemprop="name">Анна</h1>
            <div class="user__status__text">Привет всем</div>
            <span itemprop="description">30</span>
            <span itemprop="addressCountry">Беларусь</span>
            <span itemprop="addressLocality">Минск</span>
            <div class="about__content" id="meet">
              <ul class="about__list">
                <li class="about__list__item">Ищу <strong>мужчину</strong> от 30 до 40 лет</li>
                <li class="about__list__item">
                  <span class="about__list__term">Цель знакомства:</span>
                  <span class="about__list__desc">любовь и отношения</span>
                </li>
                <li class="about__list__item">
                  <span class="about__list__term">Важное в партнере:</span>
                  <span class="about__list__desc">ум</span>
                </li>
                <li class="about__list__item">
                  <span class="about__list__term">Семейное положение:</span>
                  <span class="about__list__desc">не замужем</span>
                </li>
                <li class="about__list__item">
                  <span class="about__list__term">Статус отношений:</span>
                  <span class="about__list__desc">в активном поиске</span>
                </li>
                <li class="about__list__item">
                  <span class="about__list__term">Дети:</span>
                  <span class="about__list__desc">нет</span>
                </li>
              </ul>
            </div>
            <div class="about__content" id="type">
              <ul class="about__list">
                <li class="about__list__item">
                  <span class="about__list__term">Телосложение:</span>
                  <span class="about__list__desc">обычное</span>
                </li>
                <li class="about__list__item">
                  <span class="about__list__term">Рост:&nbsp;</span>
                  <span class="about__list__desc">170 см.</span>
                  <span class="about__list__term">Вес:&nbsp;</span>
                  <span class="about__list__desc">60 кг.</span>
                </li>
                <li class="about__list__item">
                  <span class="about__list__term">Цвет глаз:</span>
                  <span class="about__list__desc">зеленый</span>
                </li>
                <li class="about__list__item">
                  <span class="about__list__term">Моя внешность:</span>
                  <span class="about__list__desc">обыкновенная</span>
                </li>
                <li class="about__list__item">
                  <span class="about__list__term">О себе:</span>
                  <span class="about__list__desc">люблю путешествия</span>
                </li>
              </ul>
            </div>
            <div class="about__content" id="profile">
              <ul class="about__list">
                <li class="about__list__item">
                  <span class="about__list__term">Профессия:</span>
                  <span class="about__list__desc">дизайнер</span>
                </li>
                <li class="about__list__item">
                  <span class="about__list__term">Сфера деятельности:</span>
                  <span class="about__list__desc">IT</span>
                </li>
                <li class="about__list__item">
                  <span class="about__list__term">Образование:</span>
                  <span class="about__list__desc">высшее</span>
                </li>
                <li class="about__list__item">
                  <span class="about__list__term">Жилье:</span>
                  <span class="about__list__desc">своя квартира</span>
                </li>
                <li class="about__list__item">
                  <span class="about__list__term">Материальное положение:</span>
                  <span class="about__list__desc">среднее</span>
                </li>
                <li class="about__list__item">
                  <span class="about__list__term">Материальная поддержка:</span>
                  <span class="about__list__desc">не нуждаюсь в спонсоре</span>
                </li>
                <li class="about__list__item">
                  <span class="about__list__term">Жизненные приоритеты:</span>
                  <span class="about__list__desc">семья и карьера</span>
                </li>
                <li class="about__list__item">
                  <span class="about__list__term">Черты характера:</span>
                  <span class="about__list__desc">спокойная, целеустремленная</span>
                </li>
                <li class="about__list__item">
                  <span class="about__list__term">Интересы и увлечения:</span>
                  <span class="about__list__desc">путешествия, спорт</span>
                </li>
                <li class="about__list__item">
                  <span class="about__list__term">Отношение к курению:</span>
                  <span class="about__list__desc">не курю</span>
                </li>
                <li class="about__list__item">
                  <span class="about__list__term">Отношение к алкоголю:</span>
                  <span class="about__list__desc">не пью</span>
                </li>
              </ul>
            </div>
            </body></html>
            """;
}
