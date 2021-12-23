package by.andd3dfx.sitesparsing.tutby.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SingleSearchResult {

    private final List<VacancyData> dataItems;
    private final String nextPageUrl;
}
