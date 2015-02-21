package us.talabrek.ultimateskyblock.challenge;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A reward record
 */
public class Reward {
    private static final Random RND = new Random(System.currentTimeMillis());

    private final Map<ItemStack, Double> itemReward;
    private final String permissionReward;
    private final int currencyReward;
    private final int xpReward;
    private final String rewardText;
    private final List<String> commands;

    public Reward(String rewardText, Map<ItemStack, Double> itemReward, String permissionReward, int currencyReward, int xpReward, List<String> commands) {
        this.itemReward = itemReward;
        this.permissionReward = permissionReward;
        this.currencyReward = currencyReward;
        this.xpReward = xpReward;
        this.rewardText = rewardText;
        this.commands = commands;
    }

    public List<ItemStack> getItemReward() {
        List<ItemStack> copy = new ArrayList<>();
        for (Map.Entry<ItemStack,Double> e : itemReward.entrySet()) {
            if (RND.nextDouble() < e.getValue()) {
                copy.add(e.getKey());
            }
        }
        return copy;
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

