package us.talabrek.ultimateskyblock.challenge;

import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.util.FormatUtil;
import us.talabrek.ultimateskyblock.util.I18nUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final List<EntityMatch> requiredEntities;
    private final Rank rank;
    private final int resetInHours;
    private final ItemStack displayItem;
    private final ItemStack lockedItem;
    private final boolean takeItems;
    private final int radius;
    private final Reward reward;
    private final Reward repeatReward;

    public Challenge(String name, String description, Type type, String requiredItems, List<EntityMatch> requiredEntities, Rank rank, int resetInHours, ItemStack displayItem, ItemStack lockedItem, boolean takeItems, int radius, Reward reward, Reward repeatReward) {
        this.name = name;
        this.type = type;
        this.requiredItems = requiredItems;
        this.requiredEntities = requiredEntities;
        this.rank = rank;
        this.resetInHours = resetInHours;
        this.displayItem = displayItem;
        this.lockedItem = lockedItem;
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

    public String getDescription() {
        return description;
    }

    public int getRadius() {
        return radius;
    }

    public int getRequiredLevel() {
        if (type == Type.ISLAND_LEVEL) {
            return Integer.parseInt(requiredItems, 10);
        }
        return 0;
    }

    public List<ItemStack> getRequiredItems(int timesCompleted) {
        List<ItemStack> items = new ArrayList<>();
        for (String item : requiredItems.split(" ")) {
            Matcher m = REQ_PATTERN.matcher(item);
            if (m.matches()) {
                int reqItem = Integer.parseInt(m.group("type"), 10);
                int subType = m.group("subtype") != null ? Integer.parseInt(m.group("subtype"), 10) : 0;
                int amount = Integer.parseInt(m.group("amount"), 10);
                char op = m.group("op") != null ? m.group("op").charAt(0) : 0;
                int inc = m.group("inc") != null ? Integer.parseInt(m.group("inc"), 10) : 0;
                amount = ChallengeLogic.calcAmount(amount, op, inc, timesCompleted);
                ItemStack mat = new ItemStack(reqItem, amount, (short) subType);
                ItemMeta meta = mat.getItemMeta();
                meta.setDisplayName("\u00a7f" + amount + " " + VaultHandler.getItemName(mat));
                mat.setItemMeta(meta);
                items.add(mat);
            }
        }
        return items;
    }

    public List<EntityMatch> getRequiredEntities() {
        return requiredEntities;
    }

    private String getName(ItemStack mat) {
        ItemInfo itemInfo = Items.itemByStack(mat);
        return itemInfo != null ? itemInfo.getName() : "" + mat.getType();
    }

    public Rank getRank() {
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
                if (cooldown >= ChallengeLogic.MS_DAY) {
                    final int days = (int) (cooldown / ChallengeLogic.MS_DAY);
                    lores.add(I18nUtil.tr("\u00a74Requirements will reset in {0} days.", days));
                } else if (cooldown >= ChallengeLogic.MS_HOUR) {
                    final int hours = (int) cooldown / ChallengeLogic.MS_HOUR;
                    lores.add(I18nUtil.tr("\u00a74Requirements will reset in {0} hours.", hours));
                } else {
                    final int minutes = Math.round(cooldown / ChallengeLogic.MS_MIN);
                    lores.add(I18nUtil.tr("\u00a74Requirements will reset in {0} minutes.", minutes));
                }
            }
            reward = getRepeatReward();
        }
        lores.add(I18nUtil.tr("\u00a7eThis challenge requires the following:"));
        for (ItemStack item : getRequiredItems(timesCompleted)) {
            lores.add(item.getItemMeta().getDisplayName());
        }
        List<String> lines = FormatUtil.wordWrap("\u00a7a" + reward.getRewardText(), 20, 30);
        lores.add(I18nUtil.tr("\u00a76Item Reward: \u00a7a") + lines.get(0));
        for (String line : lines.subList(1, lines.size())) {
            lores.add(line);
        }
        if (withCurrency) {
            lores.add(I18nUtil.tr("\u00a76Currency Reward: \u00a7a{0}", reward.getCurrencyReward()));
        }
        lores.add(I18nUtil.tr("\u00a76Exp Reward: \u00a7a{0}", reward.getXpReward()));
        lores.add(I18nUtil.tr("\u00a7dTotal times completed: \u00a7f{0}", completion.getTimesCompleted()));
        meta.setLore(lores);
        currentChallengeItem.setItemMeta(meta);
        return currentChallengeItem;
    }

    public ItemStack getDisplayItem() {
        // TODO: 10/12/2014 - R4zorax: Incorporate all the other goodies here...
        return new ItemStack(displayItem); // Copy
    }

    public ItemStack getLockedDisplayItem() {
        return lockedItem != null ? new ItemStack(lockedItem) : null;
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
