package us.talabrek.ultimateskyblock.challenge;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import dk.lockfuglsang.minecraft.util.FormatUtil;
import dk.lockfuglsang.minecraft.util.ItemStackUtil;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Data object of a rank.
 */
public class Rank {
    private final Rank previousRank;
    private final ChallengeDefaults defaults;
    private final List<Challenge> challenges;
    private ConfigurationSection config;

    public Rank(ConfigurationSection section, Rank previousRank, ChallengeDefaults defaults) {
        this.challenges = new ArrayList<>();
        this.previousRank = previousRank;
        this.defaults = defaults;
        this.config = section;
        ConfigurationSection challengeSection = section.getConfigurationSection("challenges");
        for (String challengeName : challengeSection.getKeys(false)) {
            Challenge challenge = ChallengeFactory.createChallenge(this, challengeSection.getConfigurationSection(challengeName), defaults);
            if (challenge != null) {
                challenges.add(challenge);
            }
        }
    }

    /**
     * Whether the rank is available for the player.
     *
     * @param playerInfo PlayerInfo of the player
     * @return
     */
    public boolean isAvailable(PlayerInfo playerInfo) {
        return getMissingRequirements(playerInfo).isEmpty();
    }

    public List<Challenge> getChallenges() {
        return challenges;
    }

    public ItemStack getDisplayItem() {
        String displayItem = config.getString("displayItem", "DIRT");
        return ItemStackUtil.asDisplayItem(ItemStackUtil.createItemStack(displayItem, getName(), null));
    }

    public String getName() {
        return FormatUtil.normalize(config.getString("name", config.getName()));
    }

    public String getRankKey() {
        return config.getName();
    }

    public Rank getPreviousRank() {
        return previousRank;
    }

    @Override
    public String toString() {
        return getName();
    }

    public List<String> getMissingRequirements(PlayerInfo playerInfo) {
        List<String> missing = new ArrayList<>();
        ConfigurationSection requires = config.getConfigurationSection("requires");
        if (requires != null) {
            if (previousRank != null) {
                int leeway = previousRank.getLeeway(playerInfo);
                int rankLeeway = requires.getInt("rankLeeway", defaults.rankLeeway);
                if (leeway > rankLeeway) {
                    missing.add(tr("\u00a77Complete {0} more {1} \u00a77challenges", (leeway - rankLeeway), previousRank));
                }
            }
            String missingChallenges = ChallengeFormat.getMissingRequirement(playerInfo, requires.getStringList("challenges"), uSkyBlock.getInstance().getChallengeLogic());
            if (missingChallenges != null) {
                missing.add(tr("\u00a77Complete {0}", missingChallenges));
            }
            if (!missing.isEmpty()) {
                missing.add("\u00a77" + tr("to unlock this rank"));
            }
        } else if (defaults.requiresPreviousRank) {
            if (previousRank != null) {
                int leeway = previousRank.getLeeway(playerInfo);
                if (leeway > defaults.rankLeeway) {
                    missing.add(tr("\u00a77Complete {0} more {1} \u00a77challenges", (leeway - defaults.rankLeeway), previousRank));
                }
            }
        }
        return missing;
    }

    private int getLeeway(PlayerInfo playerInfo) {
        int leeway = challenges.size();
        for (Challenge challenge : challenges) {
            if (playerInfo.getChallenge(challenge.getName()).getTimesCompleted() > 0) {
                leeway--;
            }
        }
        return leeway;
    }

    public int getResetInHours() {
        return config.getInt("resetInHours", defaults.resetInHours);
    }
}
