package us.talabrek.ultimateskyblock.menu;

import org.bukkit.inventory.ItemStack;

/**
 * Holds the state for the menu reg. permission control.
 */
public class PartyPermissionMenuItem {
    private final ItemStack icon;
    private final String perm;
    private final String title;
    private final String shortDescription;
    private final String description;

    public PartyPermissionMenuItem(ItemStack icon, String perm, String title, String description) {
        this(icon, perm, title, description, description);
    }

    public PartyPermissionMenuItem(ItemStack icon, String perm, String title, String shortDescription, String description) {
        this.icon = icon;
        this.perm = perm;
        this.title = title;
        this.shortDescription = shortDescription;
        this.description = description;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public String getPerm() {
        return perm;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getShortDescription() {
        return shortDescription;
    }
}
