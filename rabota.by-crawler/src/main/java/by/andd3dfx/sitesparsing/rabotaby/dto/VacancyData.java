package by.andd3dfx.sitesparsing.rabotaby.dto;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class VacancyData {

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
