package by.andd3dfx.tabor.dto;

import by.andd3dfx.crawler.dto.CrawlerData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileData implements CrawlerData {

    private String url;
    private String id;
    private String name;
    private Integer age;
    private String city;
    private String photoUrl;
    private String photoPath;
    /** Короткий статус со страницы анкеты. */
    private String statusText;
    private String lookingFor;
    private String purpose;
    private String importantInPartner;
    private String lifePriorities;
    private String characterTraits;
    private String interestsAndHobbies;
    private String height;
    private String weight;
    private String bodyType;
    private String eyeColor;
    private String appearance;
    private String maritalStatus;
    private String relationshipStatus;
    private String children;
    private String education;
    private String occupation;
    private String activity;
    private String housing;
    /** «Материальное положение». */
    private String materialStatus;
    private String materialSupport;
    private String smoking;
    private String alcohol;
    /** Текст поля «О себе». */
    private String aboutText;
}
