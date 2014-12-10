package us.talabrek.ultimateskyblock.challenge;

import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * A reward record
 */
public class Reward {
    private final List<ItemStack> itemReward;
    private final String permissionReward;
    private final int currencyReward;
    private final int xpReward;
    private final String rewardText;

    public Reward(String rewardText, List<ItemStack> itemReward, String permissionReward, int currencyReward, int xpReward) {
        this.itemReward = itemReward;
        this.permissionReward = permissionReward;
        this.currencyReward = currencyReward;
        this.xpReward = xpReward;
        this.rewardText = rewardText;
    }

    public List<ItemStack> getItemReward() {
        return itemReward;
    }

    public String getPermissionReward() {
        return permissionReward;
    }

    public int getCurrencyReward() {
        return currencyReward;
    }

    public int getXpReward() {
        return xpReward;
    }

    public String getRewardText() {
        return rewardText;
    }

    @Override
    public String toString() {
        return "Reward{" +
                "itemReward=" + itemReward +
                ", permissionReward='" + permissionReward + '\'' +
                ", currencyReward=" + currencyReward +
                ", xpReward=" + xpReward +
                ", rewardText='" + rewardText + '\'' +
                '}';
    }
}

