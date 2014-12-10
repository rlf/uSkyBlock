package us.talabrek.ultimateskyblock.challenge;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import us.talabrek.ultimateskyblock.PlayerInfo;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Arrays;

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
    }

    public void load() {
        int defaultValue = config.getInt("general.default", 10);
        int defaultLimit = config.getInt("general.limit", Integer.MAX_VALUE);
        int defaultDR = config.getInt("general.defaultScale", 10000);
        Arrays.fill(blockValue, defaultValue);
        Arrays.fill(blockLimit, defaultLimit);
        ConfigurationSection blockValueSection = config.getConfigurationSection("blockValues");
        for (String blockKey : blockValueSection.getKeys(false)) {
            int blockId = Integer.parseInt(blockKey);
            blockValue[blockId] = blockValueSection.getInt(blockKey, defaultValue);
        }
        ConfigurationSection blockLimitSection = config.getConfigurationSection("blockLimits");
        for (String blockKey : blockLimitSection.getKeys(false)) {
            int blockId = Integer.parseInt(blockKey);
            blockLimit[blockId] = blockLimitSection.getInt(blockKey, defaultLimit);
        }
        ConfigurationSection diminishingReturnSection = config.getConfigurationSection("diminishingReturns");
        for (String blockKey : diminishingReturnSection.getKeys(false)) {
            int blockId = Integer.parseInt(blockKey);
            blockDR[blockId] = diminishingReturnSection.getInt(blockKey, defaultDR);
        }
    }

    public long calculateScore(PlayerInfo playerInfo) {
        final int[] values = new int[MAX_BLOCK];
        final Location l = playerInfo.getIslandLocation();
        final int px = l.getBlockX();
        final int py = l.getBlockY();
        final int pz = l.getBlockZ();
        final World w = l.getWorld();
        int radius = Settings.island_protectionRange / 2;
        int typeId;
        for (int x = -radius; x <= radius; ++x) {
            for (int y = 0; y <= 255; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    typeId = w.getBlockAt(px + x, py + y, pz + z).getTypeId();
                    values[typeId]++;
                }
            }
        }
        double score = 0;
        for (int i = 1; i <= MAX_BLOCK; ++i) {
            double count = values[i];
            if (count > blockLimit[i] && blockLimit[i] != -1) {
                count = blockLimit[i]; // Hard edge
            }
            if (blockDR[i] > 0) {
                count = dReturns(count, blockDR[i]);
            }
            score += count*blockValue[i];
        }
        long islandLevel = Math.round(score / config.getInt("general.pointsPerLevel"));
        uSkyBlock.getInstance().getIslandConfig(playerInfo).set("general.level", islandLevel);
        playerInfo.savePlayerConfig(playerInfo.getPlayerName());
        uSkyBlock.getInstance().saveIslandConfig(playerInfo.locationForParty());
        return islandLevel;
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
