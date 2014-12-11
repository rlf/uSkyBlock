package us.talabrek.ultimateskyblock.challenge;

import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.talabrek.ultimateskyblock.VaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static us.talabrek.ultimateskyblock.challenge.ChallengeLogic.*;

/**
 * The data-object for a challenge
 */
public class Challenge {
    public static final Pattern REQ_PATTERN = Pattern.compile("(?<type>[0-9]+)(:(?<subtype>[0-9]+))?:(?<amount>[0-9]+)(;(?<op>[+\\-*])(?<inc>[0-9]+))?");

    enum Type { PLAYER, ISLAND, ISLAND_LEVEL;
        static Type from(String s) {
            if (s == null || s.trim().isEmpty() || s.trim().toLowerCase().equals("onplayer")) {
                return PLAYER;
            } else if (s != null && s.equalsIgnoreCase("islandlevel")) {
                return ISLAND_LEVEL;
            }
            return ISLAND;
        }
    }
    private final String name;
    private final String description;
    private final Type type;
    private final String requiredItems;
    private final String rank;
    private final int resetInHours;
    private final ItemStack displayItem;
    private final boolean takeItems;
    private final int radius;
    private final Reward reward;
    private final Reward repeatReward;

    public Challenge(String name, String description, Type type, String requiredItems, String rank, int resetInHours, ItemStack displayItem, boolean takeItems, int radius, Reward reward, Reward repeatReward) {
        this.name = name;
        this.type = type;
        this.requiredItems = requiredItems;
        this.rank = rank;
        this.resetInHours = resetInHours;
        this.displayItem = displayItem;
        this.takeItems = takeItems;
        this.radius = radius;
        this.reward = reward;
        this.repeatReward = repeatReward;
        this.description = description;
    }

    public boolean isRepeatable() {
        return repeatReward != null;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getRequiredItems() {
        return requiredItems;
    }

    public String getDescription() {
        return description;
    }

    public int getRadius() {
        return radius;
    }

    public int getRequiredLevel() {
        // TODO: 10/12/2014 - R4zorax: Somehow ensure this is correct?
        return Integer.parseInt(requiredItems);
    }

    public List<ItemStack> getRequiredItems(int timesCompleted) {
        List<ItemStack> items = new ArrayList<>();
        for (String item : requiredItems.split(" ")) {
            Matcher m = REQ_PATTERN.matcher(item);
            if (m.matches()) {
                int reqItem = Integer.parseInt(m.group("type"));
                int subType = m.group("subtype") != null ? Integer.parseInt(m.group("subtype")) : 0;
                int amount = Integer.parseInt(m.group("amount"));
                char op = m.group("op") != null ? m.group("op").charAt(0) : 0;
                int inc = m.group("inc") != null ? Integer.parseInt(m.group("inc")) : 0;
                amount = calcAmount(amount, op, inc, timesCompleted);
                ItemStack mat = new ItemStack(reqItem, amount, (short) subType);
                ItemMeta meta = mat.getItemMeta();
                meta.setDisplayName("\u00a7f" + amount + " " + VaultHandler.getItemName(mat));
                mat.setItemMeta(meta);
                items.add(mat);
            }
        }
        return items;
    }

    private String getName(ItemStack mat) {
        ItemInfo itemInfo = Items.itemByStack(mat);
        return itemInfo != null ? itemInfo.getName() : "" + mat.getType();
    }

    public String getRank() {
        return rank;
    }

    public int getResetInHours() {
        return resetInHours;
    }

    public ItemStack getDisplayItem(ChallengeCompletion completion, boolean withCurrency) {
        ItemStack currentChallengeItem = getDisplayItem();
        ItemMeta meta = currentChallengeItem.getItemMeta();
        List<String> lores = new ArrayList<>();
        lores.add("\u00a77" + getDescription());
        int timesCompleted = completion.getTimesCompletedSinceTimer();
        Reward reward = getReward();
        if (completion.getTimesCompleted() > 0 && isRepeatable()) {
            if (completion.isOnCooldown()) {
                long cooldown = completion.getCooldownInMillis();
                if (cooldown >= MS_DAY) {
                    final int days = (int) (cooldown / MS_DAY);
                    lores.add("\u00a74Requirements will reset in " + days + " days.");
                } else if (cooldown >= MS_HOUR) {
                    final int hours = (int) cooldown / MS_HOUR;
                    lores.add("\u00a74Requirements will reset in " + hours + " hours.");
                } else {
                    final int minutes = Math.round(cooldown / MS_MIN);
                    lores.add("\u00a74Requirements will reset in " + minutes + " minutes.");
                }
            }
            reward = getRepeatReward();
        }
        lores.add("\u00a7eThis challenge requires the following:");
        for (ItemStack item : getRequiredItems(timesCompleted)) {
            lores.add(item.getItemMeta().getDisplayName());
        }
        List<String> lines = wordWrap(reward.getRewardText(), 20, 30);
        lores.add("\u00a76Item Reward: \u00a7a" + lines.get(0));
        for (String line : lines.subList(1, lines.size())) {
            lores.add("\u00a7a" + line);
        }
        if (withCurrency) {
            lores.add("\u00a76Currency Reward: \u00a7a" + reward.getCurrencyReward());
        }
        lores.add("\u00a76Exp Reward: \u00a7a" + reward.getXpReward());
        lores.add("\u00a7dTotal times completed: \u00a7f" + completion.getTimesCompleted());
        meta.setLore(lores);
        currentChallengeItem.setItemMeta(meta);
        return currentChallengeItem;
    }

    public ItemStack getDisplayItem() {
        // TODO: 10/12/2014 - R4zorax: Incorporate all the other goodies here...
        return new ItemStack(displayItem); // Copy
    }

    public boolean isTakeItems() {
        return takeItems;
    }

    public Reward getReward() {
        return reward;
    }

    public Reward getRepeatReward() {
        return repeatReward;
    }

    @Override
    public String toString() {
        return "Challenge{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", requiredItems='" + requiredItems + '\'' +
                ", rank='" + rank + '\'' +
                ", resetInHours=" + resetInHours +
                ", displayItem=" + displayItem +
                ", takeItems=" + takeItems +
                ", reward=" + reward +
                ", repeatReward=" + repeatReward +
                '}';
    }
}
