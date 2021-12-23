package by.andd3dfx.sitesparsing.onlinerby.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class CpuSearchResult {

    private final List<CpuItem> cpuItems;
    private final Integer pagesAmount;

    @Override
    public String toString() {
        return "CpuSearchResult{" +
                "cpuItems=" + cpuItems +
                ", pagesAmount=" + pagesAmount +
                '}';
    }
}
