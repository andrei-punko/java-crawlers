package by.andd3dfx.onliner.util;

import java.util.regex.Pattern;

/**
 * Извлекает числовые характеристики из краткой строки описания процессора в каталоге Onliner.
 */
public final class ProcessorDescriptionParser {

    private static final int RE_FLAGS = Pattern.CASE_INSENSITIVE
            | Pattern.UNICODE_CASE
            | Pattern.UNICODE_CHARACTER_CLASS;

    private static final Pattern CORES = Pattern.compile(
            "(\\d+)\\s*(?:ядер|ядра|ядро)\\b", RE_FLAGS);

    private static final Pattern THREADS = Pattern.compile(
            "(\\d+)\\s*(?:потоков|потока|поток)\\b", RE_FLAGS);

    private static final Pattern FREQ_PAIR = Pattern.compile(
            "частота\\s*([\\d.,]+)\\s*/\\s*([\\d.,]+)\\s*ггц", RE_FLAGS);

    private static final Pattern FREQ_SINGLE = Pattern.compile(
            "частота\\s*([\\d.,]+)\\s*ггц", RE_FLAGS);

    private ProcessorDescriptionParser() {
    }

    public static Integer parseCoreCount(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        var m = CORES.matcher(description);
        return m.find() ? Integer.valueOf(m.group(1)) : null;
    }

    public static Integer parseThreadCount(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        var m = THREADS.matcher(description);
        return m.find() ? Integer.valueOf(m.group(1)) : null;
    }

    /**
     * Частота из «частота A/B ГГц» — большее из двух (turbo/base); из «частота X ГГц» — X.
     */
    public static Double parseMaxFrequencyGHz(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        var pair = FREQ_PAIR.matcher(description);
        if (pair.find()) {
            double a = parseDecimal(pair.group(1));
            double b = parseDecimal(pair.group(2));
            return Math.max(a, b);
        }
        var single = FREQ_SINGLE.matcher(description);
        if (single.find()) {
            return parseDecimal(single.group(1));
        }
        return null;
    }

    private static double parseDecimal(String raw) {
        return Double.parseDouble(raw.replace(',', '.').trim());
    }
}
