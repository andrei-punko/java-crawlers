package by.andd3dfx.tabor.dto;

import by.andd3dfx.crawler.dto.CrawlerData;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class ProfileData implements CrawlerData {

    private String url;
    private String id;
    private String name;
    private Integer age;
    private String city;
    private String country;
    private String photoUrl;
    private String photoPath;
    private String lookingFor;
    private String purpose;
    private String height;
    private String weight;
    private String bodyType;
    private String eyeColor;
    private String maritalStatus;
    private String children;
    private String occupation;
    /** Значение поля «Отношение к курению» с анкеты. */
    private String smoking;
    private String aboutText;
}
