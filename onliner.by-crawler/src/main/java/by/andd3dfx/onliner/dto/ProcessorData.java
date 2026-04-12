package by.andd3dfx.onliner.dto;

import by.andd3dfx.crawler.dto.CrawlerData;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class ProcessorData implements CrawlerData {

    private String name;
    private String brand;
    private String url;
    private String price;
    private String currency;
    /** Краткая строка характеристик из каталога (в т.ч. сокет, ядра, частота). */
    private String description;
    private String socket;
    /** Вычисляется из {@link #description}. */
    private Integer coreCount;
    /** Вычисляется из {@link #description}. */
    private Integer threadCount;
    /** Вычисляется из {@link #description}, ГГц (максимум из пары turbo/base). */
    private Double maxFrequencyGHz;
}
