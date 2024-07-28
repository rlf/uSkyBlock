package us.talabrek.ultimateskyblock.challenge;

import dk.lockfuglsang.minecraft.util.FormatUtil;
import dk.lockfuglsang.minecraft.util.ItemRequirement;
import dk.lockfuglsang.minecraft.util.ItemStackUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;
import static dk.lockfuglsang.minecraft.util.FormatUtil.*;

/**
 * The data-object for a challenge
 */
public class Challenge {
    public static final int MAX_DETAILS = 11;
    public static final int MAX_LINE = 30;

    public enum Type {
        PLAYER, ISLAND, ISLAND_LEVEL;

        static Type from(String s) {
            if (s == null || s.trim().isEmpty() || s.trim().equalsIgnoreCase("onplayer")) {
                return PLAYER;
            } else if (s.equalsIgnoreCase("islandlevel")) {
                return ISLAND_LEVEL;
            }
            return ISLAND;
        }
    }

    private final String name;
    private final String description;
    private final String displayName;
    private final Type type;
    private final List<ItemRequirement> requiredItems;
    private final List<EntityMatch> requiredEntities;
    private final List<String> requiredChallenges;
    private final double requiredLevel;
    private final Rank rank;
    private final int resetInHours;
    private final ItemStack displayItem;
    private final String tool;
    private final ItemStack lockedItem;
    private final int offset;
    private final boolean takeItems;
    private final int radius;
    private final Reward reward;
    private final Reward repeatReward;
    private final int repeatLimit;

    public Challenge(String name, String displayName, String description, Type type, List<ItemRequirement> requiredItems,
                     List<EntityMatch> requiredEntities, List<String> requiredChallenges, double requiredLevel, Rank rank,
                     int resetInHours, ItemStack displayItem, String tool, ItemStack lockedItem, int offset,
                     boolean takeItems, int radius, Reward reward, Reward repeatReward, int repeatLimit) {
        this.name = name;
        this.displayName = displayName;
        this.type = type;
        this.requiredItems = requiredItems;
        this.requiredEntities = requiredEntities;
        this.requiredChallenges = requiredChallenges;
        this.requiredLevel = requiredLevel;
        this.rank = rank;
        this.resetInHours = resetInHours;
        this.displayItem = displayItem;
        this.tool = tool;
        this.lockedItem = lockedItem;
        this.offset = offset;
        this.takeItems = takeItems;
        this.radius = radius;
        this.reward = reward;
        this.repeatReward = repeatReward;
        this.description = description;
        this.repeatLimit = repeatLimit;
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
        return description != null ? description : "";
    }

    public int getRadius() {
        return radius;
    }

    public double getRequiredLevel() {
        return requiredLevel;
    }

    @NotNull
    public Map<ItemStack, Integer> getRequiredItems(int timesCompleted) {
        return this.requiredItems.stream().collect(Collectors.toUnmodifiableMap(
            item -> item.type().clone(),
            item -> item.amountForRepetitions(timesCompleted)
        ));
    }

    public List<EntityMatch> getRequiredEntities() {
        return requiredEntities;
    }

    public List<String> getRequiredChallenges() {
        return requiredChallenges;
    }

    public Rank getRank() {
        return rank;
    }

    public int getResetInHours() {
        return resetInHours;
    }

    public ItemStack getDisplayItem(ChallengeCompletion completion, boolean withCurrency) {
        int timesCompleted = completion.getTimesCompletedInCooldown();
        ItemStack currentChallengeItem = getDisplayItem();
        ItemMeta meta = currentChallengeItem.getItemMeta();
        List<String> lores = new ArrayList<>(prefix(wordWrap(getDescription(), MAX_LINE), "\u00a77"));
        Reward reward = getReward();
        if (completion.getTimesCompleted() > 0 && isRepeatable()) {
            currentChallengeItem.setAmount(completion.getTimesCompleted() < currentChallengeItem.getMaxStackSize() ? completion.getTimesCompleted() : currentChallengeItem.getMaxStackSize());
            if (completion.isOnCooldown()) {
                long cooldown = completion.getCooldownInMillis();
                if (timesCompleted < getRepeatLimit() || getRepeatLimit() <= 0) {
                    if (getRepeatLimit() > 0) {
                        lores.add(tr("\u00a74You can complete this {0} more time(s).", getRepeatLimit() - timesCompleted));
                    }
                    if (cooldown >= ChallengeLogic.MS_DAY) {
                        final int days = (int) (cooldown / ChallengeLogic.MS_DAY);
                        lores.add(tr("\u00a74Requirements will reset in {0} days.", days));
                    } else if (cooldown >= ChallengeLogic.MS_HOUR) {
                        final int hours = (int) (cooldown / ChallengeLogic.MS_HOUR);
                        lores.add(tr("\u00a74Requirements will reset in {0} hours.", hours));
                    } else if (cooldown >= 0) {
                        final int minutes = Math.round((float) cooldown / ChallengeLogic.MS_MIN);
                        lores.add(tr("\u00a74Requirements will reset in {0} minutes.", minutes));
                    }
                } else {
                    lores.add(tr("\u00a74This challenge is currently unavailable."));
                    if (cooldown >= ChallengeLogic.MS_DAY) {
                        final int days = (int) (cooldown / ChallengeLogic.MS_DAY);
                        lores.add(tr("\u00a74You can complete this again in {0} days.", days));
                    } else if (cooldown >= ChallengeLogic.MS_HOUR) {
                        final int hours = (int) (cooldown / ChallengeLogic.MS_HOUR);
                        lores.add(tr("\u00a74You can complete this again in {0} hours.", hours));
                    } else if (cooldown >= 0) {
                        final int minutes = Math.round((float) cooldown / ChallengeLogic.MS_MIN);
                        lores.add(tr("\u00a74You can complete this again in {0} minutes.", minutes));
                    }
                }
            }
            reward = getRepeatReward();
        }
        Map<ItemStack, Integer> requiredItemsForChallenge = getRequiredItems(timesCompleted);
        if (!requiredItemsForChallenge.isEmpty() || requiredEntities != null && !requiredEntities.isEmpty()) {
            lores.add(tr("\u00a7eThis challenge requires:"));
        }
        List<String> details = new ArrayList<>();
        if (!requiredItemsForChallenge.isEmpty()) {
            for (Map.Entry<ItemStack, Integer> requiredItem : requiredItemsForChallenge.entrySet()) {
                if (wrappedDetails(details).size() >= MAX_DETAILS) {
                    details.add(tr("\u00a77and more..."));
                    break;
                }
                int requiredAmount = requiredItem.getValue();
                ItemStack requiredType = requiredItem.getKey();
                details.add(requiredAmount > 1
                    ? tr("\u00a7f{0}x \u00a77{1}", requiredAmount, ItemStackUtil.getItemName(requiredType))
                    : tr("\u00a77{0}", ItemStackUtil.getItemName(requiredType)));
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
        if (type == Challenge.Type.PLAYER) {
            if (takeItems) {
                lores.add(tr("\u00a7eItems will be traded for reward."));
            }
        } else if (type == Challenge.Type.ISLAND) {
            lores.add(tr("\u00a7eMust be within {0} meters.", getRadius()));
        }
        List<String> lines = wordWrap("\u00a7a" + reward.getRewardText(), 20, MAX_LINE);
        lores.add(tr("\u00a76Item Reward: \u00a7a") + lines.get(0));
        lores.addAll(lines.subList(1, lines.size()));
        if (withCurrency) {
            lores.add(tr("\u00a76Currency Reward: \u00a7a{0}", reward.getCurrencyReward()));
        }
        lores.add(tr("\u00a76Exp Reward: \u00a7a{0}", reward.getXpReward()));
        lores.add(tr("\u00a7dTotal times completed: \u00a7f{0}", completion.getTimesCompleted()));

        meta.setLore(lores);
        currentChallengeItem.setItemMeta(meta);
        return currentChallengeItem;
    }

    public int getOffset() {
        return offset;
    }

    private List<String> wrappedDetails(List<String> details) {
        return wordWrap(join(details, ", "), MAX_LINE);
    }

    public ItemStack getDisplayItem() {
        return ItemStackUtil.asDisplayItem(displayItem); // Copy
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

    public int getRepeatLimit() {
        return repeatLimit;
    }

    public List<String> getMissingRequirements(PlayerInfo playerInfo) {
        String missingRequirement = ChallengeFormat.getMissingRequirement(playerInfo, requiredChallenges, uSkyBlock.getInstance().getChallengeLogic());
        if (missingRequirement != null) {
            return wordWrap(tr("\u00a77Requires {0}", missingRequirement), MAX_LINE);
        }
        return Collections.emptyList();
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
            ", repeatLimit=" + repeatLimit +
            '}';
    }
}
