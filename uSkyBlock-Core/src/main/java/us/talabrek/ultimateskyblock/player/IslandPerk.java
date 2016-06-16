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
    private final double scoreMultiply;
    private final double scoreOffset;

    public IslandPerk(String schemeName, String permission, ItemStack displayItem, Perk perk) {
        this(schemeName, permission, displayItem, perk, 1d, 0d);
    }

    public IslandPerk(String schemeName, String permission, ItemStack displayItem, Perk perk, double scoreMultiply, double scoreOffset) {
        this.schemeName = schemeName;
        this.permission = permission;
        this.displayItem = displayItem;
        this.perk = perk;
        this.scoreMultiply = scoreMultiply;
        this.scoreOffset = scoreOffset;
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

    public double getScoreMultiply() {
        return scoreMultiply;
    }

    public double getScoreOffset() {
        return scoreOffset;
    }
}
