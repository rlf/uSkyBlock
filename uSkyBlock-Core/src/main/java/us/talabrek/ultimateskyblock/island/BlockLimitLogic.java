package us.talabrek.ultimateskyblock.island;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import us.talabrek.ultimateskyblock.api.model.BlockScore;
import us.talabrek.ultimateskyblock.island.level.IslandScore;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class BlockLimitLogic {
    public enum CanPlace { YES, UNCERTAIN, NO};

    private static final Logger log = Logger.getLogger(BlockLimitLogic.class.getName());
    private Map<Material, Integer> blockLimits = new HashMap<>();
    // TODO: R4zorax - 13-07-2018: Persist this somehow - and use a guavacache
    private Map<Location, Map<Material,Integer>> blockCounts = new HashMap<>();

    private final boolean limitsEnabled;

    public BlockLimitLogic(uSkyBlock plugin) {
        FileConfiguration config = plugin.getConfig();
        limitsEnabled = config.getBoolean("options.island.block-limits.enabled", false);
        if (limitsEnabled) {
            ConfigurationSection section = config.getConfigurationSection("options.island.block-limits");
            Set<String> keys = section.getKeys(false);
            keys.remove("enabled");
            for (String key : keys) {
                Material material = Material.getMaterial(key.toUpperCase());
                int limit = section.getInt(key, -1);
                if (material != null && limit >= 0) {
                    blockLimits.put(material, limit);
                } else {
                    log.warning("Unknown material " + key + " supplied for block-limit, or value not an integer");
                }
            }
        }
    }

    public int getLimit(Material type) {
        return blockLimits.getOrDefault(type, Integer.MAX_VALUE);
    }

    public Map<Material,Integer> getLimits() {
        return Collections.unmodifiableMap(blockLimits);
    }

    public void updateBlockCount(Location islandLocation, IslandScore score) {
        if (!limitsEnabled) {
            return;
        }
        Map<Material, Integer> countMap = asBlockCount(score);
        blockCounts.put(islandLocation, countMap);
    }

    private Map<Material,Integer> asBlockCount(IslandScore score) {
        Map<Material, Integer> countMap = new ConcurrentHashMap<>();
        for (BlockScore blockScore : score.getTop()) {
            Material type = blockScore.getBlock().getType();
            if (blockLimits.containsKey(type)) {
                int initalValue = countMap.getOrDefault(type, 0);
                initalValue += blockScore.getCount();
                countMap.put(type, initalValue);
            }
        }
        return countMap;
    }

    public int getCount(Material type, Location islandLocation) {
        if (!limitsEnabled || !blockLimits.containsKey(type)) {
            return -1;
        }
        Map<Material, Integer> islandCount = blockCounts.getOrDefault(islandLocation, null);
        if (islandCount == null) {
            return -2;
        }
        return islandCount.getOrDefault(type, 0);
    }

    public CanPlace canPlace(Material type, IslandInfo islandInfo) {
        int count = getCount(type, islandInfo.getIslandLocation());
        if (count == -1) {
            return CanPlace.YES;
        } else if (count == -2) {
            return CanPlace.UNCERTAIN;
        }
        return count < blockLimits.getOrDefault(type, Integer.MAX_VALUE) ? CanPlace.YES : CanPlace.NO;
    }

    public void incBlockCount(Location islandLocation, Material type) {
        if (!limitsEnabled || !blockLimits.containsKey(type)) {
            return;
        }
        Map<Material, Integer> islandCount = blockCounts.getOrDefault(islandLocation, new ConcurrentHashMap<>());
        int blockCount = islandCount.getOrDefault(type, 0);
        islandCount.put(type, blockCount+1);
        blockCounts.put(islandLocation, islandCount);
    }

    public void decBlockCount(Location islandLocation, Material type) {
        if (!limitsEnabled || !blockLimits.containsKey(type)) {
            return;
        }
        Map<Material, Integer> islandCount = blockCounts.getOrDefault(islandLocation, new ConcurrentHashMap<>());
        int blockCount = islandCount.getOrDefault(type, 0);
        islandCount.put(type, blockCount-1);
        blockCounts.put(islandLocation, islandCount);
    }
}
