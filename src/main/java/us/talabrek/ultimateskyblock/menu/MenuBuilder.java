package us.talabrek.ultimateskyblock.menu;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder pattern for Menus
 */
public class MenuBuilder {
    private String title;
    private int size;
    private List<MenuItem> items = new ArrayList<>();

    public MenuBuilder() {
    }

    public MenuBuilder config(ConfigurationSection section) {
        String currentPath = section.getCurrentPath();
        title(currentPath.substring(currentPath.lastIndexOf(".")+1));
        size(section.getInt("size", 36));
        ConfigurationSection itemsSection = section.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String indexStr : itemsSection.getKeys(false)) {
                MenuItem menuItem = new MenuItemBuilder()
                        .config(itemsSection.getConfigurationSection(indexStr), Integer.parseInt(indexStr))
                        .build();
                items.add(menuItem);
            }
        }
        return this;
    }

    public MenuBuilder size(int size) {
        this.size = size;
        return this;
    }

    public MenuBuilder title(String title) {
        this.title = title;
        return this;
    }

    public MenuBuilder item(MenuItem item) {
        this.items.add(item);
        return this;
    }

    public Menu build() {
        return new Menu(title, size, items);
    }
}
