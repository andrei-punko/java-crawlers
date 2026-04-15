package by.andd3dfx.onliner.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

/**
 * Извлекает числовые характеристики из краткой строки описания процессора в каталоге Onliner.
 * Если в описании нет «N ядер / потоков» (часто у Bristol Ridge и Athlon X4), для имени вида
 * {@code Athlon X4 …} подставляется число из суффикса X4 (ядра = потоки, без SMT).
 */
@UtilityClass
public final class ProcessorDescriptionParser {

    public static final int DEFAULT_CORE_COUNT = 1;
    public static final int DEFAULT_THREAD_COUNT = 1;

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

    /** Athlon X4 950 → 4 ядра (и 4 потока). */
    private static final Pattern ATHLON_X_CORES = Pattern.compile(
            "(?i)Athlon\\s+X(\\d+)", RE_FLAGS);

    public static Integer parseCoreCount(String description) {
        return parseCoreCount(description, null);
    }

    public static Integer parseCoreCount(String description, String productName) {
        Integer fromDesc = parseCoreCountFromDescription(description);
        if (fromDesc != DEFAULT_CORE_COUNT) {
            return fromDesc;
        }
        return athlonXCoreCount(productName);
    }

    public static Integer parseThreadCount(String description) {
        return parseThreadCount(description, null);
    }

    public static Integer parseThreadCount(String description, String productName) {
        Integer fromDesc = parseThreadCountFromDescription(description);
        if (fromDesc != DEFAULT_THREAD_COUNT) {
            return fromDesc;
        }
        Integer fromAthlonX = athlonXCoreCount(productName);
        if (fromAthlonX != DEFAULT_CORE_COUNT) {
            return fromAthlonX;
        }
        return DEFAULT_THREAD_COUNT;
    }

    private static Integer parseCoreCountFromDescription(String description) {
        if (description == null || description.isBlank()) {
            return DEFAULT_CORE_COUNT;
        }
        var m = CORES.matcher(description);
        return m.find() ? Integer.valueOf(m.group(1)) : DEFAULT_CORE_COUNT;
    }

    private static Integer parseThreadCountFromDescription(String description) {
        if (description == null || description.isBlank()) {
            return DEFAULT_THREAD_COUNT;
        }
        var m = THREADS.matcher(description);
        return m.find() ? Integer.valueOf(m.group(1)) : DEFAULT_THREAD_COUNT;
    }

    private static Integer athlonXCoreCount(String productName) {
        if (productName == null || productName.isBlank()) {
            return DEFAULT_CORE_COUNT;
        }
        var m = ATHLON_X_CORES.matcher(productName);
        return m.find() ? Integer.valueOf(m.group(1)) : DEFAULT_CORE_COUNT;
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
