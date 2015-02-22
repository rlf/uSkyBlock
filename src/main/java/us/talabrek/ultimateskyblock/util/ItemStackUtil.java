package us.talabrek.ultimateskyblock.util;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Conversion to ItemStack from strings.
 */
public enum ItemStackUtil {;
    private static final Pattern ITEM_AMOUNT_PATTERN = Pattern.compile("(\\{p=(?<prob>0\\.[0-9]+)\\})?(?<id>[0-9]+)(:(?<sub>[0-9]+))?(:(?<meta>\\{.*\\}))?:(?<amount>[0-9]+)");

    public static Map<ItemStack, Double> createItemsWithProbabilty(List<String> items) {
        Map<ItemStack, Double> map = new HashMap<>();
            for (String reward : items) {
                Matcher m = ITEM_AMOUNT_PATTERN.matcher(reward);
                if (m.matches()) {
                    double p = m.group("prob") != null ? Double.parseDouble(m.group("prob")) : 1;
                    int id = Integer.parseInt(m.group("id"), 10);
                    short sub = m.group("sub") != null ? (short) Integer.parseInt(m.group("sub"), 10) : 0;
                    int amount = Integer.parseInt(m.group("amount"), 10);
                    ItemStack itemStack = new ItemStack(id, amount, sub);
                    map.put(itemStack, p);
                } else {
                    throw new IllegalArgumentException("Unknown item: '" + reward + "' in '" + items + "'");
                }
        }
        return map;
    }

    public static List<ItemStack> createItemList(String items) {
        List<ItemStack> itemList = new ArrayList<>();
        if (items != null && !items.trim().isEmpty()) {
            for (String reward : items.split(" ")) {
                Matcher m = ITEM_AMOUNT_PATTERN.matcher(reward);
                if (m.matches()) {
                    int id = Integer.parseInt(m.group("id"), 10);
                    short sub = m.group("sub") != null ? (short) Integer.parseInt(m.group("sub"), 10) : 0;
                    int amount = Integer.parseInt(m.group("amount"), 10);
                    itemList.add(new ItemStack(id, amount, sub));
                } else if (!reward.isEmpty()) {
                    throw new IllegalArgumentException("Unknown item: '" + reward + "' in '" + items + "'");
                }
            }
        }
        return itemList;
    }

    public static ItemStack[] createItemArray(String items) {
        return createItemList(items).toArray(new ItemStack[0]);
    }

}
