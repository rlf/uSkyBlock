package us.talabrek.ultimateskyblock.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
    private static final Pattern ITEM_AMOUNT_PATTERN = Pattern.compile("(\\{p=(?<prob>0\\.[0-9]+)\\})?(?<id>[0-9A-Z_]+)(:(?<sub>[0-9]+))?(:(?<meta>\\{.*\\}))?:(?<amount>[0-9]+)");
    private static final Pattern ITEM_PATTERN = Pattern.compile("(?<id>[0-9A-Z_]+)(:(?<sub>[0-9]+))?");
    private static final Pattern ITEM_NAME_PATTERN = Pattern.compile("(?<id>[A-Z_0-9]+)(:(?<sub>[0-9]+))?");

    public static Map<ItemStack, Double> createItemsWithProbabilty(List<String> items) {
        Map<ItemStack, Double> map = new HashMap<>();
            for (String reward : items) {
                Matcher m = ITEM_AMOUNT_PATTERN.matcher(reward);
                if (m.matches()) {
                    double p = m.group("prob") != null ? Double.parseDouble(m.group("prob")) : 1;
                    int id = getItemId(m);
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

    private static int getItemId(Matcher m) {
        String id = m.group("id");
        if (id != null && id.matches("[0-9]*")) {
            return Integer.parseInt(id, 10);
        } else if (id != null) {
            Material material = Material.getMaterial(id);
            if (material != null) {
                return material.getId();
            }
        }
        return Material.BARRIER.getId();
    }

    public static List<ItemStack> createItemList(String items) {
        List<ItemStack> itemList = new ArrayList<>();
        if (items != null && !items.trim().isEmpty()) {
            for (String reward : items.split(" ")) {
                Matcher m = ITEM_AMOUNT_PATTERN.matcher(reward);
                if (m.matches()) {
                    int id = getItemId(m);
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
        return createItemArray(createItemList(items));
    }

    public static ItemStack[] createItemArray(List<ItemStack> items) {
        return items.toArray(new ItemStack[0]);
    }

    public static ItemStack createItemStack(String displayItem, String name, String description) {
        Material material = Material.DIRT;
        short subType = 0;
        if (displayItem != null) {
            Matcher matcher = ITEM_PATTERN.matcher(displayItem);
            Matcher nameMatcher = ITEM_NAME_PATTERN.matcher(displayItem);
            if (matcher.matches()) {
                material = Material.getMaterial(getItemId(matcher));
                subType = matcher.group("sub") != null ? (short) Integer.parseInt(matcher.group("sub"), 10) : 0;
            } else if (nameMatcher.matches()) {
                material = Material.getMaterial(nameMatcher.group("id"));
                subType = nameMatcher.group("sub") != null ? (short) Integer.parseInt(nameMatcher.group("sub"), 10) : 0;
            }
        }
        if (material == null) {
            Bukkit.getLogger().warning("Invalid material " + displayItem + " supplied!");
            material = Material.BARRIER;
        }
        ItemStack itemStack = new ItemStack(material, 1, subType);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        if (description != null) {
            lore.add(description);
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static List<ItemStack> clone(List<ItemStack> items) {
        if (items == null) {
            return null;
        }
        List<ItemStack> copy = new ArrayList<>();
        for (ItemStack item : items) {
            copy.add(item.clone());
        }
        return copy;
    }
}
