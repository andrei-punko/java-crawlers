package by.andd3dfx.sitesparsing.rabotaby.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SingleSearchResult {

    private List<VacancyData> dataItems;
    private String nextPageUrl;
}
