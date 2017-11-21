package us.talabrek.ultimateskyblock.island;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.api.model.BlockScore;
import us.talabrek.ultimateskyblock.api.model.BlockScore.State;
import us.talabrek.ultimateskyblock.async.Callback;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.task.ChunkSnapShotTask;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Business logic regarding the calculation of level
 */
public class LevelLogic {
    public static final String CN = LevelLogic.class.getName();
    private static final Logger log = Logger.getLogger(CN);

    private static final Pattern KEY_PATTERN = Pattern.compile("(?<id>[0-9]+)([/:](?<sub>(\\*|[0-9]+|[0-9]+-[0-9]+)))?");
    private static final int MAX_BLOCK = 0xfff;
    private static final int DATA_BITS = 4;
    private static final int MAX_INDEX = MAX_BLOCK << DATA_BITS;
    private static final int DATA_MASK = 0xf;
    private final uSkyBlock plugin;
    private final FileConfiguration config;

    private final float blockValue[] = new float[MAX_INDEX];
    private final int blockLimit[] = new int[MAX_INDEX];
    private final int blockDR[] = new int[MAX_INDEX];
    private final int blockNV[] = new int[MAX_INDEX];
    private int pointsPerLevel;
    private int activateNetherAtLevel;

    public LevelLogic(uSkyBlock plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
        load();
        pointsPerLevel = config.getInt("general.pointsPerLevel");
        activateNetherAtLevel = config.getInt("nether.activate-at.level", 100);
    }

    public void load() {
        float defaultValue = (float) config.getDouble("general.default", 10);
        int defaultLimit = config.getInt("general.limit", Integer.MAX_VALUE);
        int defaultDR = config.getInt("general.defaultScale", 10000);
        // Per default, let all blocks (regardless of data-value) share limit and score
        for (int b = 0; b < MAX_INDEX; b++) {
            if ((b & DATA_MASK) == 0) {
                blockValue[b] = defaultValue;
            } else {
                blockValue[b] = -1; // Share with the blockId:0 block
            }
        }
        Arrays.fill(blockLimit, defaultLimit);
        ConfigurationSection blockValueSection = config.getConfigurationSection("blockValues");
        for (String blockKey : blockValueSection.getKeys(false)) {
            int[] blockIds = getBlockIds(blockKey);
            float value = (float) blockValueSection.getDouble(blockKey, defaultValue);
            for (int blockId : blockIds) {
                blockValue[blockId] = value;
            }
        }
        ConfigurationSection blockLimitSection = config.getConfigurationSection("blockLimits");
        for (String blockKey : blockLimitSection.getKeys(false)) {
            int[] blockIds = getBlockIds(blockKey);
            int value = blockLimitSection.getInt(blockKey, defaultLimit);
            for (int blockId : blockIds) {
                blockLimit[blockId] = value;
            }
        }
        ConfigurationSection diminishingReturnSection = config.getConfigurationSection("diminishingReturns");
        for (String blockKey : diminishingReturnSection.getKeys(false)) {
            int[] blockIds = getBlockIds(blockKey);
            int value = diminishingReturnSection.getInt(blockKey, defaultDR);
            for (int blockId : blockIds) {
                blockDR[blockId] = value;
            }
        }
        
        ConfigurationSection negativeReturnSection = config.getConfigurationSection("negativeReturns");
        if (negativeReturnSection != null) {
            for (String blockKey : negativeReturnSection.getKeys(false)) {
                int[] blockIds = getBlockIds(blockKey);
                int value = negativeReturnSection.getInt(blockKey, 0);
                for (int blockId : blockIds) {
                    blockNV[blockId] = value;
                }
            }
        }
    }

    private int[] getBlockIds(String blockKey) {
        Matcher m = KEY_PATTERN.matcher(blockKey);
        if (m.matches()) {
            int blockId = Integer.parseInt(m.group("id"), 10);
            byte[] dataValues = getDataValues(m.group("sub"));
            int[] ids = new int[dataValues.length];
            for (int i = 0; i < ids.length; i++) {
                ids[i] = blockId << DATA_BITS | (dataValues[i] & 0xff);
            }
            return ids;
        } else {
            LogUtil.log(Level.WARNING, "Invalid key '" + blockKey + "' in levelConfig");
        }
        return new int[0];
    }

    private byte[] getDataValues(String sub) {
        if (sub == null) {
            return new byte[]{0};
        }
        if (sub.equalsIgnoreCase("*") || sub.equalsIgnoreCase("0-15")) {
            byte[] data = new byte[16];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) ((i) & 0xff);
            }
            return data;
        } else if (!sub.isEmpty()) {
            String[] split = sub.split("-");
            if (split.length == 1) {
                return new byte[]{(byte) (Integer.parseInt(split[0]) & 0xff)};
            } else {
                int min = Integer.parseInt(split[0]);
                int max = Integer.parseInt(split[1]);
                byte[] data = new byte[max - min];
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte) ((min + i) & 0xff);
                }
                return data;
            }
        }
        return new byte[0];
    }

    public void calculateScoreAsync(final Location l, final Callback<IslandScore> callback) {
        // TODO: 10/05/2015 - R4zorax: Ensure no overlapping calls to this one happen...
        log.entering(CN, "calculateScoreAsync");
        // is further threading needed here?
        final ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(l);
        if (region == null) {
            return;
        }
        new ChunkSnapShotTask(plugin, l, region, new Callback<List<ChunkSnapshot>>() {
            @Override
            public void run() {
                final List<ChunkSnapshot> snapshotsOverworld = getState();
                Location netherLoc = getNetherLocation(l);
                final ProtectedRegion netherRegion = WorldGuardHandler.getNetherRegionAt(netherLoc);
                new ChunkSnapShotTask(plugin, netherLoc, netherRegion, new Callback<List<ChunkSnapshot>>() {
                    @Override
                    public void run() {
                        final List<ChunkSnapshot> snapshotsNether = getState();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                final int[] counts = createBlockCountArray();
                                int minX = region.getMinimumPoint().getBlockX();
                                int maxX = region.getMaximumPoint().getBlockX();
                                int minZ = region.getMinimumPoint().getBlockZ();
                                int maxZ = region.getMaximumPoint().getBlockZ();
                                for (int x = minX; x <= maxX; ++x) {
                                    for (int z = minZ; z <= maxZ; ++z) {
                                        ChunkSnapshot chunk = getChunkSnapshot(x >> 4, z >> 4, snapshotsOverworld);
                                        if (chunk == null) {
                                            // This should NOT happen!
                                            log.log(Level.WARNING, "Missing chunk in snapshot for x,z = " + x + "," + z);
                                            continue;
                                        }
                                        int cx = (x & 0xf);
                                        int cz = (z & 0xf);
                                        for (int y = 0; y <= 255; y++) {
                                            int blockId = chunk.getBlockTypeId(cx, y, cz);
                                            if (blockId != 0) { // Ignore AIR
                                                blockId = blockId << DATA_BITS | (chunk.getBlockData(cx, y, cz) & 0xff);
                                                addBlockCount(blockId, counts);
                                            }
                                        }
                                    }
                                }
                                IslandScore islandScore = createIslandScore(counts);
                                if (islandScore.getScore() >= activateNetherAtLevel && netherRegion != null && snapshotsNether != null) {
                                    // Add nether levels
                                    minX = netherRegion.getMinimumPoint().getBlockX();
                                    maxX = netherRegion.getMaximumPoint().getBlockX();
                                    minZ = netherRegion.getMinimumPoint().getBlockZ();
                                    maxZ = netherRegion.getMaximumPoint().getBlockZ();
                                    for (int x = minX; x <= maxX; ++x) {
                                        for (int z = minZ; z <= maxZ; ++z) {
                                            ChunkSnapshot chunk = getChunkSnapshot(x >> 4, z >> 4, snapshotsNether);
                                            if (chunk == null) {
                                                // This should NOT happen!
                                                log.log(Level.WARNING, "Missing nether-chunk in snapshot for x,z = " + x + "," + z);
                                                continue;
                                            }
                                            int cx = (x & 0xf);
                                            int cz = (z & 0xf);
                                            for (int y = 5; y < 120; y++) {
                                                int blockId = chunk.getBlockTypeId(cx, y, cz);
                                                if (blockId != 0) { // Ignore AIR
                                                    blockId = blockId << DATA_BITS | (chunk.getBlockData(cx, y, cz) & 0xff);
                                                    addBlockCount(blockId, counts);
                                                }
                                            }
                                        }
                                    }
                                    islandScore = createIslandScore(counts);
                                }
                                callback.setState(islandScore);
                                plugin.sync(callback);
                                log.exiting(CN, "calculateScoreAsync");
                            }
                        }.runTaskAsynchronously(plugin);
                    }
                }).runTask(plugin);
            }
        }).runTask(plugin);
    }

    private Location getNetherLocation(Location l) {
        Location netherLoc = l.clone();
        netherLoc.setWorld(plugin.getSkyBlockNetherWorld());
        netherLoc.setY(Settings.nether_height);
        return netherLoc;
    }

    private ChunkSnapshot getChunkSnapshot(int x, int z, List<ChunkSnapshot> snapshots) {
        for (ChunkSnapshot chunk : snapshots) {
            if (chunk.getX() == x && chunk.getZ() == z) {
                return chunk;
            }
        }
        return null;
    }

    public IslandScore createIslandScore(int[] counts) {
        double score = 0;
        List<BlockScore> blocks = new ArrayList<>();
        for (int i = 1 << DATA_BITS; i < MAX_BLOCK << DATA_BITS; ++i) {
            int count = counts[i];
            if (count > 0 && blockValue[i] > 0) {
                State state = State.NORMAL;
                double adjustedCount = count;
                if (count > blockLimit[i] && blockLimit[i] != -1) {
                    adjustedCount = blockLimit[i]; // Hard edge
                    state = State.LIMIT;
                } else if (blockDR[i] > 0 && count > blockDR[i]) {
                    state = State.DIMINISHING;
                    adjustedCount = dReturns(count, blockDR[i]);
                }
                else if (blockNV[i] > 0 && count > blockNV[i]){
                	state = State.NEGATIVE;
                	adjustedCount = 2 * blockNV[i] - count;   
                }
                double blockScore = adjustedCount * blockValue[i];
                score += blockScore;
                blocks.add(new BlockScoreImpl(new ItemStack(i >> DATA_BITS, 1, (short) (i & DATA_MASK)), count, blockScore / pointsPerLevel, state));
            }
        }
        return new IslandScore(score / pointsPerLevel, blocks);
    }

    private void addBlockCount(int id, int[] counts) {
        int blockId = id;
        if (blockId < 0 || blockId >= MAX_INDEX) {
            return;
        }
        if (blockValue[blockId] == -1) {
            blockId = blockId & (~DATA_MASK); // remove sub-type
        } else if (blockValue[blockId] < -1) {
            // Direct addressing
            blockId = (-Math.round(blockValue[blockId])) << DATA_BITS;
        }
        if (blockId >= 0 && blockId < MAX_INDEX) {
            counts[blockId] += 1;
        } else {
            log.warning("Invalid blockId value from levelConfig.yml: " + id + " -> " + blockId);
        }
    }

    double dReturns(final double val, final double scale) {
        if (val < 0.0) {
            return -this.dReturns(-val, scale);
        }
        final double mult = val / scale;
        final double trinum = (Math.sqrt(8.0 * mult + 1.0) - 1.0) / 2.0;
        return trinum * scale;
    }

    public int[] createBlockCountArray() {
        return new int[MAX_BLOCK << DATA_BITS];
    }
}
