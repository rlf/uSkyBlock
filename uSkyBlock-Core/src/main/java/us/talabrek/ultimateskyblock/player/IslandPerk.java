package us.talabrek.ultimateskyblock.player;

import org.bukkit.inventory.ItemStack;

/**
 * The coupling between schemes and perks.
 */
public class IslandPerk {
    private String schemeName;
    private String permission;
    private ItemStack displayItem;
    private Perk perk;

    public IslandPerk(String schemeName, String permission, ItemStack displayItem, Perk perk) {
        this.schemeName = schemeName;
        this.permission = permission;
        this.displayItem = displayItem;
        this.perk = perk;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public String getPermission() {
        return permission;
    }

    public ItemStack getDisplayItem() {
        return new ItemStack(displayItem);
    }

    public Perk getPerk() {
        return perk;
    }
}
