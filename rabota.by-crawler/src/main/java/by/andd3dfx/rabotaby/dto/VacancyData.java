package by.andd3dfx.rabotaby.dto;

import by.andd3dfx.crawler.dto.CrawlerData;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class VacancyData implements CrawlerData {

    private String url;
    private String companyName;
    private String textContent;
    private String salary;
    private Set<String> keywords;
    private String address;
}
