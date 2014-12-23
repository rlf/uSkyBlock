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
    private final List<String> commands;

    public Reward(String rewardText, List<ItemStack> itemReward, String permissionReward, int currencyReward, int xpReward, List<String> commands) {
        this.itemReward = itemReward;
        this.permissionReward = permissionReward;
        this.currencyReward = currencyReward;
        this.xpReward = xpReward;
        this.rewardText = rewardText;
        this.commands = commands;
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

    public List<String> getCommands() {
        return commands;
    }

    @Override
    public String toString() {
        return "Reward{" +
                "itemReward=" + itemReward +
                ", permissionReward='" + permissionReward + '\'' +
                ", currencyReward=" + currencyReward +
                ", xpReward=" + xpReward +
                ", rewardText='" + rewardText + '\'' +
                ", commands=" + commands +
                '}';
    }
}

