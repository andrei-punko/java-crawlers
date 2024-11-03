package by.andd3dfx.rabotaby.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

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

        assertThat("Wrong amount of Java items", statisticsUtil.get("Java"), is(2));
        assertThat("Wrong amount of SQL items", statisticsUtil.get("SQL"), is(1));
        assertThat("Wrong amount of Spring items", statisticsUtil.get("Spring"), is(3));
        assertThat("Wrong amount of absent item", statisticsUtil.get("EJB"), is(nullValue()));

        final LinkedHashMap<String, Integer> map = statisticsUtil.buildSortedMap();
        final Set<Entry<String, Integer>> entries = map.entrySet();
        final Iterator<Entry<String, Integer>> iterator = entries.iterator();

        final Entry<String, Integer> entry1 = iterator.next();
        assertThat("Wrong first key", entry1.getKey(), is("Spring"));
        assertThat("Wrong first value", entry1.getValue(), is(3));

        final Entry<String, Integer> entry2 = iterator.next();
        assertThat("Wrong second key", entry2.getKey(), is("Java"));
        assertThat("Wrong second value", entry2.getValue(), is(2));

        final Entry<String, Integer> entry3 = iterator.next();
        assertThat("Wrong third key", entry3.getKey(), is("SQL"));
        assertThat("Wrong third value", entry3.getValue(), is(1));
    }
}
