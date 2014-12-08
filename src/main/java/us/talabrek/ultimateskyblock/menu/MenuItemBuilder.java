package us.talabrek.ultimateskyblock.menu;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder pattern on MenuItem - Enables easy creation of menu-items
 */
public class MenuItemBuilder {
    String icon;
    String title;
    List<String> lore = new ArrayList<>();
    String subMenu;
    List<String> commands = new ArrayList<>();
    String permission;
    int index = -1;
    String enabled = "true";

    public MenuItemBuilder() {
    }

    public MenuItemBuilder config(ConfigurationSection section, int index) {
        icon(section.getString("icon", "387"));
        title(section.getString("title"));
        lore(section.getStringList("lore"));
        subMenu(section.getString("subMenu"));
        command(section.getString("command"));
        permission(section.getString("permission"));
        index(section.getInt("index", index)); // Any configured index wins
        enabled(section.getString("enabled"));
        return this;
    }

    public MenuItemBuilder enabled(String enabled) {
        this.enabled = enabled;
        return this;
    }

    public MenuItemBuilder index(int index) {
        this.index = index;
        return this;
    }

    public MenuItemBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }

    public MenuItemBuilder title(String title) {
        this.title = title;
        return this;
    }

    public MenuItemBuilder lore(String lore) {
        this.lore.add(lore);
        return this;
    }

    public MenuItemBuilder lore(List<String> lore) {
        this.lore.addAll(lore);
        return this;
    }

    public MenuItemBuilder subMenu(String subMenu) {
        this.subMenu = subMenu;
        return this;
    }

    public MenuItemBuilder command(String command) {
        this.commands.add(command);
        return this;
    }

    public MenuItemBuilder icon(String icon) {
        this.icon = icon;
        return this;
    }

    public MenuItem build() {
        return new MenuItem(icon, title, lore, subMenu, commands, index, enabled);
    }
}
