package us.talabrek.ultimateskyblock.challenge;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.talabrek.ultimateskyblock.player.PlayerInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Data object of a rank.
 */
public class Rank {
    private final Rank previousRank;
    private final ChallengeDefaults defaults;
    private final List<Challenge> challenges;
    private ConfigurationSection config;

    public Rank(ConfigurationSection section, @Nullable Rank previousRank, ChallengeDefaults defaults) {
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
     * @param playerInfo PlayerInfo of the player
     * @return
     */
    public boolean isAvailable(PlayerInfo playerInfo) {
        if (!defaults.requiresPreviousRank) {
            return true;
        }
        if (previousRank == null) {
            return true;
        }
        return getMissingRequirements(playerInfo).isEmpty();
    }

    public List<Challenge> getChallenges() {
        return challenges;
    }

    public ItemStack getDisplayItem() {
        String displayItem = config.getString("displayItem", "DIRT");
        Material material = Material.getMaterial(displayItem);
        ItemStack itemStack = new ItemStack(material, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(getName());
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public String getName() {
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
                    missing.add("\u00a77Complete " + (leeway-rankLeeway) + " more " + previousRank + " challenges");
                }
            }
            for (String challengeName : requires.getStringList("challenges")) {
                ChallengeCompletion challenge = playerInfo.getChallenge(challengeName);
                StringBuilder sb = new StringBuilder();
                if (challenge != null && challenge.getTimesCompleted() <= 0) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(challengeName);
                }
                if (sb.length() > 0) {
                    missing.add("\u00a77Complete " + sb.toString());
                }
            }
            if (!missing.isEmpty()) {
                missing.add("\u00a77to unlock this rank");
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
