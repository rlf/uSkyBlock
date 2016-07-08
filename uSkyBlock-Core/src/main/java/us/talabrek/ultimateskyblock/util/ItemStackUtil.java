package us.talabrek.ultimateskyblock.util;

import dk.lockfuglsang.minecraft.nbt.NBTUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Conversion to ItemStack from strings.
 */
public enum ItemStackUtil {
    ;
    private static final Pattern ITEM_AMOUNT_PATTERN = Pattern.compile("(\\{p=(?<prob>0\\.[0-9]+)\\})?(?<id>[0-9A-Z_]+)(:(?<sub>[0-9]+))?:(?<amount>[0-9]+)\\s*(?<meta>\\{.*\\})?");
    private static final Pattern ITEM_PATTERN = Pattern.compile("(?<id>[0-9A-Z_]+)(:(?<sub>[0-9]+))?\\s*(?<meta>\\{.*\\})?");

    public static List<ItemProbability> createItemsWithProbabilty(List<String> items) {
        List<ItemProbability> itemProbs = new ArrayList<>();
        for (String reward : items) {
            Matcher m = ITEM_AMOUNT_PATTERN.matcher(reward);
            if (m.matches()) {
                double p = m.group("prob") != null ? Double.parseDouble(m.group("prob")) : 1;
                int id = getItemId(m);
                short sub = m.group("sub") != null ? (short) Integer.parseInt(m.group("sub"), 10) : 0;
                int amount = Integer.parseInt(m.group("amount"), 10);
                ItemStack itemStack = new ItemStack(id, amount, sub);
                itemStack = NBTUtil.addNBTTag(itemStack, m.group("meta"));
                itemProbs.add(new ItemProbability(p, itemStack));
            } else {
                throw new IllegalArgumentException("Unknown item: '" + reward + "' in '" + items + "'");
            }
        }
        return itemProbs;
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

    public static List<ItemStack> createItemList(String items, List<String> items2) {
        List<String> itemStrList = new ArrayList<>();
        if (items != null) {
            itemStrList.addAll(Arrays.asList(items.split(" ")));
        }
        if (items2 != null && !items2.isEmpty()) {
            itemStrList.addAll(items2);
        }
        return createItemList(itemStrList);
    }

    public static List<ItemStack> createItemList(List<String> items) {
        List<ItemStack> itemList = new ArrayList<>();
        for (String reward : items) {
            if (reward != null && !reward.isEmpty()) {
                itemList.add(createItemStackAmount(reward));
            }
        }
        return itemList;
    }

    private static ItemStack createItemStackAmount(String reward) {
        if (reward == null || reward.isEmpty()) {
            return null;
        }
        Matcher m = ITEM_AMOUNT_PATTERN.matcher(reward);
        if (m.matches()) {
            int id = getItemId(m);
            short sub = m.group("sub") != null ? (short) Integer.parseInt(m.group("sub"), 10) : 0;
            int amount = Integer.parseInt(m.group("amount"), 10);
            ItemStack itemStack = new ItemStack(id, amount, sub);
            if (m.group("meta") != null) {
                itemStack = NBTUtil.addNBTTag(itemStack, m.group("meta"));
            }
            return itemStack;
        } else {
            throw new IllegalArgumentException("Unknown item: '" + reward + "'");
        }
    }

    public static List<ItemStack> createItemList(String items) {
        List<ItemStack> itemList = new ArrayList<>();
        if (items != null && !items.trim().isEmpty()) {
            return createItemList(Arrays.asList(items.split(" ")));
        }
        return itemList;
    }

    public static ItemStack[] createItemArray(List<ItemStack> items) {
        return items != null ? items.toArray(new ItemStack[items.size()]) : new ItemStack[0];
    }

    public static ItemStack createItemStack(String displayItem) {
        return createItemStack(displayItem, null, null);
    }

    public static ItemStack createItemStack(String displayItem, String name, String description) {
        Material material = Material.DIRT;
        short subType = 0;
        String metaStr = null;
        if (displayItem != null) {
            Matcher matcher = ITEM_PATTERN.matcher(displayItem);
            if (matcher.matches()) {
                material = Material.getMaterial(getItemId(matcher));
                subType = matcher.group("sub") != null ? (short) Integer.parseInt(matcher.group("sub"), 10) : 0;
                metaStr = matcher.group("meta");
            }
        }
        if (material == null) {
            Bukkit.getLogger().warning("Invalid material " + displayItem + " supplied!");
            material = Material.BARRIER;
        }
        ItemStack itemStack = new ItemStack(material, 1, subType);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(FormatUtil.normalize(name));
            List<String> lore = new ArrayList<>();
            if (description != null) {
                lore.addAll(FormatUtil.wordWrap(FormatUtil.normalize(description), 30, 30));
            }
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        itemStack = NBTUtil.addNBTTag(itemStack, metaStr);
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

    public static boolean isValidInventoryItem(ItemStack itemStack) {
        Inventory inventory = Bukkit.createInventory(null, 9);
        inventory.setItem(0, itemStack);
        return inventory.getItem(0) != null && inventory.getItem(0).getItemMeta() != null && inventory.getItem(0).getData() != null && inventory.getItem(0).getData().toItemStack() != null;
    }

    public static Builder builder(ItemStack stack) {
        return new Builder(stack);
    }

    public static String asString(ItemStack item) {
        return item.getTypeId() + (item.getDurability() != 0 ? ":" + item.getDurability() : "") + ":" + item.getAmount();
    }

    /**
     * Builder for ItemStack
     */
    public static class Builder {
        private ItemStack itemStack;

        public Builder(ItemStack itemStack) {
            this.itemStack = itemStack != null ? itemStack.clone() : new ItemStack(0);
        }

        public Builder type(Material mat) {
            itemStack.setType(mat);
            return this;
        }

        public Builder type(int id) {
            itemStack.setTypeId(id);
            return this;
        }

        public Builder subType(int subType) {
            itemStack.setDurability((short) subType);
            return this;
        }

        public Builder amount(int amount) {
            itemStack.setAmount(amount);
            return this;
        }

        public Builder displayName(String name) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(name);
            itemStack.setItemMeta(itemMeta);
            return this;
        }

        public Builder enchant(Enchantment enchantment, int level) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.addEnchant(enchantment, level, false);
            itemStack.setItemMeta(itemMeta);
            return this;
        }

        public Builder select(boolean b) {
            return b ? select() : deselect();
        }

        public Builder select() {
            return enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).add(ItemFlag.HIDE_ENCHANTS);
        }

        public Builder deselect() {
            return remove(Enchantment.PROTECTION_ENVIRONMENTAL).remove(ItemFlag.HIDE_ENCHANTS);
        }

        public Builder add(ItemFlag... flags) {
            ItemMeta meta = itemStack.getItemMeta();
            meta.addItemFlags(flags);
            itemStack.setItemMeta(meta);
            return this;
        }

        public Builder remove(ItemFlag... flags) {
            ItemMeta meta = itemStack.getItemMeta();
            meta.removeItemFlags(flags);
            itemStack.setItemMeta(meta);
            return this;
        }

        private Builder remove(Enchantment enchantment) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.removeEnchant(enchantment);
            itemStack.setItemMeta(itemMeta);
            return this;
        }

        public Builder lore(String lore) {
            return lore(Collections.singletonList(lore));
        }

        public Builder lore(List<String> lore) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta.getLore() == null) {
                itemMeta.setLore(lore);
            } else {
                List<String> oldLore = itemMeta.getLore();
                oldLore.addAll(lore);
                itemMeta.setLore(oldLore);
            }
            itemStack.setItemMeta(itemMeta);
            return this;
        }

        public ItemStack build() {
            return itemStack;
        }
    }

    public static class ItemProbability {
        private final double probability;
        private final ItemStack item;

        public ItemProbability(double probability, ItemStack item) {
            this.probability = probability;
            this.item = item;
        }

        public double getProbability() {
            return probability;
        }

        public ItemStack getItem() {
            return item;
        }

        @Override
        public String toString() {
            return "ItemProbability{" +
                    "probability=" + probability +
                    ", item=" + item +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemProbability that = (ItemProbability) o;
            return Double.compare(that.probability, probability) == 0 &&
                    Objects.equals(item, that.item);
        }

        @Override
        public int hashCode() {
            return Objects.hash(probability, item);
        }
    }
}
