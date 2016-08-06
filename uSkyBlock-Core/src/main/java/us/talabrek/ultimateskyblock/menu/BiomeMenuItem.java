package us.talabrek.ultimateskyblock.menu;

import org.bukkit.inventory.ItemStack;

public class BiomeMenuItem {
    private final ItemStack icon;
    private final String name;
    private final String title;
    private final String description;

    public BiomeMenuItem(ItemStack icon, String name, String title, String description) {
        this.icon = icon;
        this.name = name;
        this.title = title;
        this.description = description;
    }

    public ItemStack getIcon() {
        return icon.clone();
    }

    public String getId() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
