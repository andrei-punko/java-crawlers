package by.andd3dfx.onliner;

import by.andd3dfx.onliner.crawler.OnlinerByCpuCrawler;
import by.andd3dfx.onliner.dto.ProcessorData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

@Slf4j
public class MainApp {

    private static final OnlinerByCpuCrawler crawler = new OnlinerByCpuCrawler();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Собирает процессоры с сокетом AM4 из каталога Onliner и сохраняет результат в JSON.
     *
     * @param args путь к выходному файлу; опционально — лимит страниц, задержка между запросами (мс)
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            throw new IllegalArgumentException("Укажите путь к выходному JSON-файлу.");
        }
        Path path = Paths.get(args[0]);

        int pagesCap = -1;
        if (args.length >= 2) {
            pagesCap = Integer.parseInt(args[1]);
        }

        int timeoutMs = 250;
        if (args.length >= 3) {
            timeoutMs = Integer.parseInt(args[2]);
        }

        if (pagesCap != -1) {
            log.info("Лимит страниц: {}. При неполном списке увеличьте лимит или укажите -1 для обхода всех страниц выдачи.",
                    pagesCap);
        }

        var pageUrl = crawler.buildAm4StartingUrl();
        var processors = crawler.batchSearch(pageUrl, pagesCap, timeoutMs);
        var sorted = processors.stream()
                .filter(p -> p.getName() != null && !p.getName().isBlank())
                .sorted(Comparator
                        .comparing(ProcessorData::getBrand, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(ProcessorData::getName, String::compareToIgnoreCase))
                .toList();
        log.info("После фильтрации к сохранению: {} записей", sorted.size());

        var jsonString = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(sorted);
        Files.write(path, jsonString.getBytes());
    }
}
