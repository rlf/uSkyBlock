package us.talabrek.ultimateskyblock.util;

import dk.lockfuglsang.minecraft.util.FormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;

public class GuiItemUtil {
    private GuiItemUtil() {
        // Uninstantiable static utility class
    }

    // TODO: Replace calls to this specific method with a more general one
    public static ItemStack createItemFromComponentString(String components) {
        if (components == null) {
            return null;
        }
        return Bukkit.getItemFactory().createItemStack(components);
    }

    public static ItemStack createGuiDisplayItem(String material, String name) {
        return createGuiDisplayItem(material, name, null);
    }

    public static ItemStack createGuiDisplayItem(String material, String name, String description) {
        Material type = Material.matchMaterial(material.toUpperCase(Locale.ROOT));
        if (type == null) {
            type = Material.BARRIER;
        }
        return createGuiDisplayItem(type, name, description);
    }

    public static ItemStack createGuiDisplayItem(Material material, String name) {
        return createGuiDisplayItem(material, name, null);
    }

    public static ItemStack createGuiDisplayItem(Material material, String name, String description) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.setDisplayName(FormatUtil.normalize(name));
            }
            if (description != null) {
                meta.setLore(FormatUtil.wordWrap(FormatUtil.normalize(description), 30, 30));
            }
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }
}
