package by.andd3dfx.onliner.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProcessorDescriptionParserTest {

    private static final String RYZEN_LINE =
            "2022 г., Vermeer (Zen 3), сокет AM4, 8 ядер, 16 потоков, частота 4.6/3.4 ГГц, кэш 4 МБ + 32 МБ, "
                    + "техпроцесс 7 нм, поддержка DDR4, TDP 65W, OEM";

    @Test
    public void parsesTypicalRyzenLine() {
        assertThat(ProcessorDescriptionParser.parseCoreCount(RYZEN_LINE)).isEqualTo(8);
        assertThat(ProcessorDescriptionParser.parseThreadCount(RYZEN_LINE)).isEqualTo(16);
        assertThat(ProcessorDescriptionParser.parseMaxFrequencyGHz(RYZEN_LINE)).isEqualTo(4.6);
    }

    @Test
    public void maxFrequency_isHigherOfPair() {
        var line = "частота 3.4/4.6 ГГц";
        assertThat(ProcessorDescriptionParser.parseMaxFrequencyGHz(line)).isEqualTo(4.6);
    }

    @Test
    public void acceptsCommaDecimal() {
        var line = "6 ядер, 12 потоков, частота 4,4/3,5 ГГц";
        assertThat(ProcessorDescriptionParser.parseMaxFrequencyGHz(line)).isEqualTo(4.4);
    }

    @Test
    public void singleFrequencyValue() {
        assertThat(ProcessorDescriptionParser.parseMaxFrequencyGHz("одна частота 4 ГГц")).isEqualTo(4.0);
        assertThat(ProcessorDescriptionParser.parseMaxFrequencyGHz("частота 3,8 ГГц")).isEqualTo(3.8);
    }

    @Test
    public void nullForMissingFragments() {
        assertThat(ProcessorDescriptionParser.parseCoreCount(null)).isNull();
        assertThat(ProcessorDescriptionParser.parseThreadCount("без потоковой строки")).isNull();
        assertThat(ProcessorDescriptionParser.parseMaxFrequencyGHz("без слова ггц")).isNull();
    }
}
