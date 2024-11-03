package by.andd3dfx.pravtor.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SearchCriteria {

    private final String topic;
    private final String url;
}
