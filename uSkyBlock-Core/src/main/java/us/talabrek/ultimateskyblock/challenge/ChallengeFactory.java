package us.talabrek.ultimateskyblock.challenge;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import us.talabrek.ultimateskyblock.util.FormatUtil;
import us.talabrek.ultimateskyblock.util.ItemStackUtil;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static us.talabrek.ultimateskyblock.util.ItemStackUtil.createItemStack;

/**
 * Builder for Challenges (Note:
 */
public class ChallengeFactory {
    private static final Pattern ENTITY_PATTERN = Pattern.compile("(?<type>[a-zA-Z0-9]+)(?<meta>:\\{.*\\})?(:(?<count>[0-9]+))?");
    private static Logger log = Logger.getLogger(ChallengeFactory.class.getName());

    public static void setLogger(Logger log) {
        ChallengeFactory.log = log;
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

    public static Challenge createChallenge(Rank rank, ConfigurationSection section, ChallengeDefaults defaults) {
        String name = section.getName().toLowerCase();
        if (section.getBoolean("disabled", false)) {
            return null; // Skip this challenge
        }
        String displayName = section.getString("name", name);
        Challenge.Type type = Challenge.Type.from(section.getString("type", "onPlayer"));
        String requiredItems = section.getString("requiredItems", "");
        List<EntityMatch> requiredEntities = createEntities(section.getStringList("requiredEntities"));
        int resetInHours = section.getInt("resetInHours", rank.getResetInHours());
        String description = section.getString("description");
        ItemStack displayItem = createItemStack(
                section.getString("displayItem", defaults.displayItem),
                FormatUtil.normalize(displayName), description);
        ItemStack lockedItem = section.isString("lockedDisplayItem") ? createItemStack(section.getString("lockedDisplayItem", "BARRIER"), displayName, description) : null;
        boolean takeItems = section.getBoolean("takeItems", true);
        int radius = section.getInt("radius", 10);
        Reward reward = createReward(section.getConfigurationSection("reward"));
        Reward repeatReward = createReward(section.getConfigurationSection("repeatReward"));
        return new Challenge(name, displayName, description, type, requiredItems, requiredEntities, rank, resetInHours, displayItem, section.getString("tool", null), lockedItem, takeItems, radius, reward, repeatReward);
    }

    private static List<EntityMatch> createEntities(List<String> requiredEntities) {
        List<EntityMatch> entities = new ArrayList<>();
        for (String entityString : requiredEntities) {
            Matcher m = ENTITY_PATTERN.matcher(entityString);
            if (m.matches()) {
                String type = m.group("type");
                String meta = m.group("meta");
                String countStr = m.group("count");
                int count = countStr != null ? Integer.parseInt(countStr, 10) : 1;
                EntityType entityType = EntityType.fromName(type);
                Map<String,Object> map = meta != null ? createMap(meta.substring(1)) : new HashMap<String,Object>(); // skip the leading ':'
                if (entityType != null && map != null) {
                    entities.add(new EntityMatch(entityType, map, count));
                } else {
                    throw new IllegalArgumentException("Malformed requiredEntities: " + entityString);
                }
            }
        }
        return entities;
    }

    private static Map<String,Object> createMap(String mapString) {
        try {
            Object parse = new JSONParser().parse(new StringReader(mapString));
            if (parse instanceof Map) {
                return (Map<String,Object>)parse;
            }
        } catch (IOException | ParseException e) {
            throw new IllegalArgumentException("Not a valid map: " + mapString, e);
        }
        throw new IllegalArgumentException("Not a map: " + mapString);
    }

    private static Reward createReward(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        List<String> items = new ArrayList<>();
        if (!section.getStringList("items").isEmpty()) {
            items.addAll(section.getStringList("items"));
        } else if (section.getString("items", null) != null) {
            items.addAll(Arrays.asList(section.getString("items").split(" ")));
        }
        return new Reward(
                section.getString("text", "\u00a74Unknown"),
                ItemStackUtil.createItemsWithProbabilty(items),
                section.getString("permission"),
                section.getInt("currency", 0),
                section.getInt("xp", 0),
                section.getStringList("commands"));
    }


    public static Map<String, Rank> createRankMap(ConfigurationSection ranksSection, ChallengeDefaults defaults) {
        LinkedHashMap<String, Rank> ranks = new LinkedHashMap<>();
        Rank previous = null;
        for (String rankName : ranksSection.getKeys(false)) {
            Rank rank = new Rank(ranksSection.getConfigurationSection(rankName), previous, defaults);
            ranks.put(rankName, rank);
            previous = rank;
        }
        return ranks;
    }
}
