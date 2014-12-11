package us.talabrek.ultimateskyblock.challenge;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builder for Challenges (Note:
 */
public class ChallengeFactory {
    private static final Pattern ITEM_PATTERN = Pattern.compile("(?<id>[0-9]+)(:(?<sub>[0-9]+))?");
    private static final Pattern ITEM_AMOUNT_PATTERN = Pattern.compile("(?<id>[0-9]+)(:(?<sub>[0-9]+))?:(?<amount>[0-9]+)");
    private static Logger log = Logger.getLogger(ChallengeFactory.class.getName());

    public static void setLogger(Logger log) {
        ChallengeFactory.log = log;
    }

    public static Map<String, Challenge> createChallengeMap(ConfigurationSection section, ChallengeDefaults defaults) {
        Map<String, Challenge> map = new LinkedHashMap<>();
        for (String challenge : section.getKeys(false)) {
            String key = challenge.toLowerCase();
            try {
                map.put(key, createChallenge(section.getConfigurationSection(challenge), defaults));
            } catch (Exception e) {
                log.log(Level.WARNING, "Unable to parse challenge " + challenge + ":" + e.getMessage());
            }
        }
        return map;
    }

    public static ChallengeDefaults createDefaults(ConfigurationSection section) {
        return new ChallengeDefaults(
                section.getInt("defaultResetInHours", 144),
                section.getBoolean("requiresPreviousRank", true),
                section.getString("repeatableColor", "\u00a7a"),
                section.getString("finishedColor", "\u00a72"),
                section.getString("challengeColor", "\u00a7e"),
                section.getInt("rankLeeway", 1),
                section.getBoolean("enableEconomyPlugin", true),
                section.getBoolean("broadcastCompletion", true),
                section.getInt("radius", 10));
    }

    public static Challenge createChallenge(ConfigurationSection section, ChallengeDefaults defaults) {
        String name = section.getName().toLowerCase();
        Challenge.Type type = Challenge.Type.from(section.getString("type", "onPlayer"));
        String requiredItems = section.getString("requiredItems");
        String rank = section.getString("rankLevel");
        int resetInHours = section.getInt("resetInHours", defaults.resetInHours);
        String description = section.getString("description");
        ItemStack displayItem = createItemStack(
                section.getString("displayItem", defaults.displayItem),
                name, description);
        boolean takeItems = section.getBoolean("takeItems", true);
        int radius = section.getInt("radius", 10);
        Reward reward = createReward(section);
        Reward repeatReward = section.getBoolean("repeatable", true) ? createRepeatReward(section) : null;
        return new Challenge(name, description, type, requiredItems, rank, resetInHours, displayItem, takeItems, radius, reward, repeatReward);
    }

    private static Reward createRepeatReward(ConfigurationSection section) {
        return new Reward(
                section.getString("repeatRewardText"),
                createItemList(section.getString("repeatItemReward")),
                "",
                section.getInt("repeatCurrencyReward", 0),
                section.getInt("repeatXpReward", 0));
    }

    private static Reward createReward(ConfigurationSection section) {
        return new Reward(
                section.getString("rewardText"),
                createItemList(section.getString("itemReward")),
                section.getString("permissionReward"),
                section.getInt("currencyReward", 0),
                section.getInt("xpReward", 0));
    }

    private static List<ItemStack> createItemList(String itemReward) {
        List<ItemStack> itemList = new ArrayList<>();
        if (itemReward != null && !itemReward.trim().isEmpty()) {
            for (String reward : itemReward.split(" ")) {
                Matcher m = ITEM_AMOUNT_PATTERN.matcher(reward);
                if (m.matches()) {
                    int id = Integer.parseInt(m.group("id"));
                    short sub = m.group("sub") != null ? (short) Integer.parseInt(m.group("sub")) : 0;
                    int amount = Integer.parseInt(m.group("amount"));
                    itemList.add(new ItemStack(id, amount, sub));
                } else {
                    throw new IllegalArgumentException("Unknown item: '" + reward + "' in '" + itemReward + "'");
                }
            }
        }
        return itemList;
    }

    private static ItemStack createItemStack(String displayItem, String name, String description) {
        Material material = Material.DIRT;
        short subType = 0;
        if (displayItem != null) {
            Matcher matcher = ITEM_PATTERN.matcher(displayItem);
            if (matcher.matches()) {
                material = Material.getMaterial(Integer.parseInt(matcher.group("id")));
                subType = matcher.group("sub") != null ? (short) Integer.parseInt(matcher.group("sub")) : 0;
            }
        }
        if (material == null) {
            throw new IllegalArgumentException("Unknown material " + displayItem);
        }
        ItemStack itemStack = new ItemStack(material, 1, subType);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        if (description != null) {
            lore.add(description);
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
