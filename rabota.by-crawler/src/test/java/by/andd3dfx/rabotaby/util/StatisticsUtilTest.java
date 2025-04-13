package by.andd3dfx.rabotaby.util;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StatisticsUtilTest {

    private StatisticsUtil statisticsUtil;

    @Before
    public void setUp() {
        statisticsUtil = new StatisticsUtil();
    }

    @Test
    public void testStatisticsCollector() {
        statisticsUtil.putKeyword("Java");
        statisticsUtil.putKeyword("Spring");
        statisticsUtil.putKeyword("Java");
        statisticsUtil.putKeyword("Spring");
        statisticsUtil.putKeyword("SQL");
        statisticsUtil.putKeyword("Spring");

        assertThat(statisticsUtil.get("Java"))
                .as("Wrong amount of Java items").isEqualTo(2);
        assertThat(statisticsUtil.get("SQL"))
                .as("Wrong amount of SQL items").isEqualTo(1);
        assertThat(statisticsUtil.get("Spring"))
                .as("Wrong amount of Spring items").isEqualTo(3);
        assertThat(statisticsUtil.get("EJB"))
                .as("Wrong amount of absent item").isNull();

        final LinkedHashMap<String, Integer> map = statisticsUtil.buildSortedMap();
        final Set<Entry<String, Integer>> entries = map.entrySet();
        final Iterator<Entry<String, Integer>> iterator = entries.iterator();

        final Entry<String, Integer> entry1 = iterator.next();
        assertThat(entry1.getKey()).as("Wrong first key").isEqualTo("Spring");
        assertThat(entry1.getValue()).as("Wrong first value").isEqualTo(3);

        final Entry<String, Integer> entry2 = iterator.next();
        assertThat(entry2.getKey()).as("Wrong second key").isEqualTo("Java");
        assertThat(entry2.getValue()).as("Wrong second value").isEqualTo(2);

        final Entry<String, Integer> entry3 = iterator.next();
        assertThat(entry3.getKey()).as("Wrong third key").isEqualTo("SQL");
        assertThat(entry3.getValue()).as("Wrong third value").isEqualTo(1);
    }
}
