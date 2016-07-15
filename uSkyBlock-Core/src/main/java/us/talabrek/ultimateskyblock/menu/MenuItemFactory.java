package us.talabrek.ultimateskyblock.menu;

import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;
import static dk.lockfuglsang.minecraft.util.FormatUtil.wordWrap;

/**
 * A convenience factory for creating the itemstacks used in menus.
 */
public class MenuItemFactory {
    public static final String INT = "\u00a7b";
    public static final String FALSE = "\u00a7c";
    public static final String TRUE = "\u00a7a";
    public static final String STRING = "\u00a79";
    public static final int MAX_INT_VALUE = 64;
    public static final String READONLY = "\u00a77";

    public ItemStack createStringItem(String value, String path, YmlConfiguration config, boolean readonly) {
        return createLeafItem(new ItemStack(readonly ? Material.FLINT_AND_STEEL : Material.NAME_TAG, 1), value, path, config);
    }

    public ItemStack createLeafItem(ItemStack item, String value, String path, YmlConfiguration config) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("\u00a77\u00a7o" + path.substring(path.lastIndexOf('.')+1));
        List<String> lore = new ArrayList<>();
        lore.add(STRING + value);
        String comment = config.getComment(path);
        if (comment != null) {
            lore.addAll(wordWrap(comment.replaceAll("\n", " "), 20, 20));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createIntegerItem(int value, String path, YmlConfiguration config, boolean readonly) {
        ItemStack item = createIntegerIcon(value, readonly);
        return createLeafItem(
                item,
                INT + value,
                path, config);
    }

    public ItemStack createIntegerIcon(int value, boolean readonly) {
        return Math.abs(value) <= MAX_INT_VALUE
                ? (readonly ? new ItemStack(Material.DETECTOR_RAIL, value) : new ItemStack(Material.RAILS, value))
                : (readonly ? new ItemStack(Material.IRON_FENCE, 1, (short)1) : new ItemStack(Material.ACTIVATOR_RAIL, 1));
    }

    public ItemStack createBooleanItem(boolean value, String path, YmlConfiguration config, boolean readonly) {
        ItemStack icon = new ItemStack(Material.WOOL, 1, (short) (readonly ? (value ? 13 : 14) : (value ? 5 : 6)));
        return createLeafItem(icon, value ? TRUE + tr("true") : FALSE + tr("false"),
                path, config);
    }
}
