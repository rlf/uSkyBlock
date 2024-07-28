package us.talabrek.ultimateskyblock.menu;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.talabrek.ultimateskyblock.util.GuiItemUtil;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Created by R4zorax on 03/10/2015.
 */
public class AbstractConfigMenu {
    private final FileConfiguration menuConfig;

    public AbstractConfigMenu(FileConfiguration menuConfig) {
        this.menuConfig = menuConfig;
    }

    public FileConfiguration getMenuConfig() {
        return menuConfig;
    }

    protected static void ensureCapacity(List<ItemStack> menuList, int index) {
        if (index >= menuList.size()) {
            menuList.addAll(Arrays.asList(new ItemStack[Math.max(index - menuList.size(), 9)]));
        }
    }

    protected static int getIndex(int row, int col) {
        return row * 9 + col;
    }

    boolean isBlackListed(String file, String currentPath) {
        return menuConfig.getStringList(file + ".blacklist").contains(currentPath);
    }

    boolean isReadonly(String file, String path) {
        return menuConfig.getStringList(file + ".readonly").contains(path);
    }

    protected int getPage(String s) {
        String format = tr("\u00a77Page {0}");
        try {
            Object[] parsed = new MessageFormat(format).parse(s);
            if (parsed != null && parsed.length > 0) {
                return Integer.parseInt("" + parsed[0]);
            }
        } catch (ParseException e) {
            // Ignore
        }
        return 1;
    }

    protected ItemStack createItemFromComponentString(String components) {
        return GuiItemUtil.createItemFromComponentString(components);
    }

    protected ItemStack createItem(Material icon, String title, List<String> lore) {
        ItemStack itemStack = new ItemStack(icon, 1);
        ItemMeta meta = Objects.requireNonNull(itemStack.getItemMeta());
        meta.setDisplayName(title);
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
