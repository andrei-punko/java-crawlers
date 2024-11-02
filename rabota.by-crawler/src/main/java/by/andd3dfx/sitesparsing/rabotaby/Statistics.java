package by.andd3dfx.sitesparsing.rabotaby;

import by.andd3dfx.sitesparsing.rabotaby.dto.VacancyData;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class Statistics {

    private Map<String, Integer> keywordToFreqMap = new HashMap<>();

    public void putKeyword(String keyword) {
        if (!keywordToFreqMap.containsKey(keyword)) {
            keywordToFreqMap.put(keyword, 0);
        }
        keywordToFreqMap.put(keyword, keywordToFreqMap.get(keyword) + 1);
    }

    public Integer get(String keyword) {
        return keywordToFreqMap.get(keyword);
    }

    public LinkedHashMap<String, Integer> buildSortedMap() {
        return keywordToFreqMap
            .entrySet()
            .stream()
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .collect(toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e2,
                LinkedHashMap::new
            ));
    }

    public LinkedHashMap<String, Integer> collectStatistics(List<VacancyData> vacancyData) {
        final Statistics statistics = new Statistics();
        vacancyData.stream()
                .map(VacancyData::getKeywords)
                .forEach(keywords -> keywords.forEach(statistics::putKeyword));
        return statistics.buildSortedMap();
    }
}