package us.talabrek.ultimateskyblock.island.level;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import us.talabrek.ultimateskyblock.api.async.Callback;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.task.ChunkSnapShotTask;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.List;
import java.util.logging.Level;

/**
 * Business logic regarding the calculation of level
 */
public class ChunkSnapshotLevelLogic extends CommonLevelLogic {

    public ChunkSnapshotLevelLogic(uSkyBlock plugin, FileConfiguration config) {
        super(plugin, config);
    }

    @Override
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
                                calculateScoreAndCallback(region, snapshotsOverworld, netherRegion, snapshotsNether, callback);
                            }
                        }.runTaskAsynchronously(plugin);
                    }
                }).runTask(plugin);
            }
        }).runTask(plugin);
    }

    private void calculateScoreAndCallback(ProtectedRegion region, List<ChunkSnapshot> snapshotsOverworld, ProtectedRegion netherRegion, List<ChunkSnapshot> snapshotsNether, Callback<IslandScore> callback) {
        IslandScore islandScore = calculateScore(region, snapshotsOverworld, netherRegion, snapshotsNether);
        callback.setState(islandScore);
        plugin.sync(callback);
        log.exiting(CN, "calculateScoreAsync");
    }

    private IslandScore calculateScore(ProtectedRegion region, List<ChunkSnapshot> snapshotsOverworld, ProtectedRegion netherRegion, List<ChunkSnapshot> snapshotsNether) {
        final BlockCountCollection counts = new BlockCountCollection(scoreMap);
        int minX = region.getMinimumPoint().getBlockX();
        int maxX = region.getMaximumPoint().getBlockX();
        int minY = region.getMinimumPoint().getBlockY();
        int maxY = region.getMaximumPoint().getBlockY();
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
                for (int y = minY; y < maxY; y++) {
                    Material blockType = chunk.getBlockType(cx, y, cz);
                    if (blockType == Material.AIR) {
                        continue;
                    }
                    counts.add(blockType);
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
                    for (int y = 6; y < 120; y++) {
                        Material blockType = chunk.getBlockType(cx, y, cz);
                        if (blockType == Material.AIR) {
                            continue;
                        }
                        counts.add(blockType);
                    }
                }
            }
            islandScore = createIslandScore(counts);
        }
        return islandScore;
    }

    private static ChunkSnapshot getChunkSnapshot(int x, int z, List<ChunkSnapshot> snapshots) {
        for (ChunkSnapshot chunk : snapshots) {
            if (chunk.getX() == x && chunk.getZ() == z) {
                return chunk;
            }
        }
        return null;
    }
}
