package us.talabrek.ultimateskyblock.island;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Compares two entries with each other.
 */
public class TopTenComparator implements Comparator<String> {
    private final Map<String, Double> dataMap;

    public TopTenComparator(Map<String,Double> map) {
        this.dataMap = map;
    }
    @Override
    public int compare(String o1, String o2) {
        int cmp = getValue(o2) - getValue(o1);
        if (cmp == 0) {
            cmp = o1 != null ? o1.compareTo(o2) : o2 == null ? 0 : -1;
        }
        return cmp;
    }

    private int getValue(String key) {
        Double d = dataMap.get(key);
        if (d != null) {
            return (int)(d.doubleValue()*10);
        }
        return 0;
    }
}
