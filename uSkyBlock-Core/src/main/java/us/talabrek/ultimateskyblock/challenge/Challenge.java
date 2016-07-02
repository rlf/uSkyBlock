package us.talabrek.ultimateskyblock.challenge;

import dk.lockfuglsang.minecraft.nbt.NBTUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.util.FormatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;
import static us.talabrek.ultimateskyblock.util.FormatUtil.*;
import static us.talabrek.ultimateskyblock.util.MetaUtil.createMap;

/**
 * The data-object for a challenge
 */
public class Challenge {
    public static final Pattern REQ_PATTERN = Pattern.compile("(?<type>[0-9]+)(:(?<subtype>[0-9]+))?:(?<amount>[0-9]+)(;(?<op>[+\\-*])(?<inc>[0-9]+))\\s*?(?<meta>\\{.*\\})?");
    public static final int MAX_DETAILS = 11;

    public enum Type { PLAYER, ISLAND, ISLAND_LEVEL;
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
    private final String displayName;
    private final Type type;
    private final List<String> requiredItems;
    private final List<EntityMatch> requiredEntities;
    private final Rank rank;
    private final int resetInHours;
    private final ItemStack displayItem;
    private final String tool;
    private final ItemStack lockedItem;
    private final boolean takeItems;
    private final int radius;
    private final Reward reward;
    private final Reward repeatReward;

    public Challenge(String name, String displayName, String description, Type type, List<String> requiredItems, List<EntityMatch> requiredEntities, Rank rank, int resetInHours, ItemStack displayItem, String tool, ItemStack lockedItem, boolean takeItems, int radius, Reward reward, Reward repeatReward) {
        this.name = name;
        this.displayName = displayName;
        this.type = type;
        this.requiredItems = requiredItems;
        this.requiredEntities = requiredEntities;
        this.rank = rank;
        this.resetInHours = resetInHours;
        this.displayItem = displayItem;
        this.tool = tool;
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

    public String getDisplayName() {
        return FormatUtil.normalize(displayName);
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
        if (type == Type.ISLAND_LEVEL && requiredItems.size() == 1) {
            return Integer.parseInt(requiredItems.get(0), 10);
        }
        return 0;
    }

    public List<ItemStack> getRequiredItems(int timesCompleted) {
        List<ItemStack> items = new ArrayList<>();
        for (String item : requiredItems) {
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
                mat.setItemMeta(meta);
                mat = NBTUtil.addNBTTag(mat, m.group("meta"));
                items.add(mat);
            }
        }
        return items;
    }

    public List<EntityMatch> getRequiredEntities() {
        return requiredEntities;
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
        lores.addAll(prefix(wordWrap(getDescription(), 30, 30), "\u00a77"));
        int timesCompleted = completion.getTimesCompletedInCooldown();
        Reward reward = getReward();
        if (completion.getTimesCompleted() > 0 && isRepeatable()) {
            if (completion.isOnCooldown()) {
                long cooldown = completion.getCooldownInMillis();
                if (cooldown >= ChallengeLogic.MS_DAY) {
                    final int days = (int) (cooldown / ChallengeLogic.MS_DAY);
                    lores.add(tr("\u00a74Requirements will reset in {0} days.", days));
                } else if (cooldown >= ChallengeLogic.MS_HOUR) {
                    final int hours = (int) (cooldown / ChallengeLogic.MS_HOUR);
                    lores.add(tr("\u00a74Requirements will reset in {0} hours.", hours));
                } else if (cooldown >= 0) {
                    final int minutes = Math.round(cooldown / ChallengeLogic.MS_MIN);
                    lores.add(tr("\u00a74Requirements will reset in {0} minutes.", minutes));
                }
            }
            reward = getRepeatReward();
        }
        List<ItemStack> reqItems = getRequiredItems(timesCompleted);
        if ((reqItems != null && !reqItems.isEmpty()) || (requiredEntities != null && !requiredEntities.isEmpty())) {
            lores.add(tr("\u00a7eThis challenge requires the following:"));
        }
        List<String> details = new ArrayList<>();
        if (reqItems != null && !reqItems.isEmpty()) {
            for (ItemStack item : reqItems) {
                if (wrappedDetails(details).size() >= MAX_DETAILS) {
                    details.add(tr("\u00a77and more..."));
                    break;
                }
                details.add(item.getAmount() > 1
                        ? tr("\u00a7f{0}x \u00a77{1}", item.getAmount(), VaultHandler.getItemName(item))
                        : tr("\u00a77{0}", VaultHandler.getItemName(item)));
            }
        }
        if (requiredEntities != null && !requiredEntities.isEmpty() && wrappedDetails(details).size() < MAX_DETAILS) {
            for (EntityMatch entityMatch : requiredEntities) {
                if (wrappedDetails(details).size() >= MAX_DETAILS) {
                    details.add(tr("\u00a77and more..."));
                    break;
                }
                details.add(entityMatch.getCount() > 1
                        ? tr("\u00a7f{0}x \u00a77{1}", entityMatch.getCount(), entityMatch.getDisplayName())
                        : tr("\u00a77{0}", entityMatch.getDisplayName()));
            }
        }
        lores.addAll(wrappedDetails(details));
        List<String> lines = wordWrap("\u00a7a" + reward.getRewardText(), 20, 30);
        lores.add(tr("\u00a76Item Reward: \u00a7a") + lines.get(0));
        for (String line : lines.subList(1, lines.size())) {
            lores.add(line);
        }
        if (withCurrency) {
            lores.add(tr("\u00a76Currency Reward: \u00a7a{0}", reward.getCurrencyReward()));
        }
        lores.add(tr("\u00a76Exp Reward: \u00a7a{0}", reward.getXpReward()));
        lores.add(tr("\u00a7dTotal times completed: \u00a7f{0}", completion.getTimesCompleted()));
        meta.setLore(lores);
        currentChallengeItem.setItemMeta(meta);
        return currentChallengeItem;
    }

    private List<String> wrappedDetails(List<String> details) {
        return wordWrap(join(details, ", "), 30, 30);
    }

    public ItemStack getDisplayItem() {
        // TODO: 10/12/2014 - R4zorax: Incorporate all the other goodies here...
        return new ItemStack(displayItem); // Copy
    }

    public String getTool() {
        return tool;
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
