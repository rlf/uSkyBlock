package us.talabrek.ultimateskyblock.island;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Business logic regarding the calculation of level
 */
public class LevelLogic {
    private static final int MAX_BLOCK = 255;
    private final FileConfiguration config;

    private final int blockValue[] = new int[MAX_BLOCK];
    private final int blockLimit[] = new int[MAX_BLOCK];
    private final int blockDR[] = new int[MAX_BLOCK];

    public LevelLogic(FileConfiguration config) {
        this.config = config;
        load();
    }

    public void load() {
        int defaultValue = config.getInt("general.default", 10);
        int defaultLimit = config.getInt("general.limit", Integer.MAX_VALUE);
        int defaultDR = config.getInt("general.defaultScale", 10000);
        Arrays.fill(blockValue, defaultValue);
        Arrays.fill(blockLimit, defaultLimit);
        ConfigurationSection blockValueSection = config.getConfigurationSection("blockValues");
        for (String blockKey : blockValueSection.getKeys(false)) {
            int blockId = Integer.parseInt(blockKey, 10);
            blockValue[blockId] = blockValueSection.getInt(blockKey, defaultValue);
        }
        ConfigurationSection blockLimitSection = config.getConfigurationSection("blockLimits");
        for (String blockKey : blockLimitSection.getKeys(false)) {
            int blockId = Integer.parseInt(blockKey, 10);
            blockLimit[blockId] = blockLimitSection.getInt(blockKey, defaultLimit);
        }
        ConfigurationSection diminishingReturnSection = config.getConfigurationSection("diminishingReturns");
        for (String blockKey : diminishingReturnSection.getKeys(false)) {
            int blockId = Integer.parseInt(blockKey, 10);
            blockDR[blockId] = diminishingReturnSection.getInt(blockKey, defaultDR);
        }
    }

    public IslandScore calculateScore(PlayerInfo playerInfo) {
        final int radius = Settings.island_protectionRange / 2;
        int pointsPerLevel = config.getInt("general.pointsPerLevel");
        final Location l = playerInfo.getIslandLocation();
        final int px = l.getBlockX();
        final int pz = l.getBlockZ();
        final World w = l.getWorld();
        int typeId;
        final int[] values = new int[MAX_BLOCK];
        for (int x = -radius; x <= radius; ++x) {
            for (int y = 0; y <= 255; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    typeId = w.getBlockAt(px + x, y, pz + z).getTypeId();
                    values[typeId] += 1;
                }
            }
        }
        double score = 0;
        List<BlockScore> blocks = new ArrayList<>();
        for (int i = 1; i < MAX_BLOCK; ++i) {
            int count = values[i];
            if (count > 0 && blockValue[i] > 0) {
                double adjustedCount = count;
                if (count > blockLimit[i] && blockLimit[i] != -1) {
                    adjustedCount = blockLimit[i]; // Hard edge
                }
                if (blockDR[i] > 0) {
                    adjustedCount = dReturns(count, blockDR[i]);
                }
                double blockScore = adjustedCount * blockValue[i];
                score += blockScore;
                blocks.add(new BlockScore(i, count, blockScore/pointsPerLevel));
            }
        }
        return new IslandScore(score/pointsPerLevel, blocks);
    }

    double dReturns(final double val, final double scale) {
        if (val < 0.0) {
            return -this.dReturns(-val, scale);
        }
        final double mult = val / scale;
        final double trinum = (Math.sqrt(8.0 * mult + 1.0) - 1.0) / 2.0;
        return trinum * scale;
    }

}
