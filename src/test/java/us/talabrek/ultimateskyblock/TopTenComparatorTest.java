package us.talabrek.ultimateskyblock;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class TopTenComparatorTest {

    @Test
    public void testCompare() throws Exception {
        Map<String, Double> testMap = new HashMap<>();
        testMap.put("test A", 10.2);
        testMap.put("test B", 10.3);
        testMap.put("test C", 123.4);
        testMap.put("test AB", 10.3);
        testMap.put("test D", 8.0);
        String expected = "{test C=123.4, test AB=10.3, test B=10.3, test A=10.2, test D=8.0}";

        TreeMap<String, Double> sorted = new TreeMap<>(new TopTenComparator(testMap));
        sorted.putAll(testMap);
        assertThat(sorted.toString(), is(expected));
        assertThat(new LinkedHashMap<>(sorted).toString(), is(expected));
    }
}