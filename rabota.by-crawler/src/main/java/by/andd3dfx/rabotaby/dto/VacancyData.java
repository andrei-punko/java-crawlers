package by.andd3dfx.rabotaby.dto;

import java.util.Set;

import by.andd3dfx.crawler.dto.CrawlerData;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class VacancyData implements CrawlerData {

    private String url;
    private String companyName;
    private String textContent;
    private Set<String> keywords;
    private String addressString;

    @Override
    public String toString() {
        return "VacancyData{" +
            "url='" + url + '\'' +
            ", keywords=" + keywords +
            '}';
    }
}
