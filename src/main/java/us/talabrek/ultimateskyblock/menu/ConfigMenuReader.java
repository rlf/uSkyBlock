package us.talabrek.ultimateskyblock.menu;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder pattern on Menu
 */
public enum ConfigMenuReader {;
    public static List<Menu> readMenus(ConfigurationSection section) {
        List<Menu> menus = new ArrayList<>();
        for (String menuName : section.getKeys(false)) {
            Menu menu = new MenuBuilder().config(section.getConfigurationSection(menuName)).build();
            menus.add(menu);
        }
        return menus;
    }
}
