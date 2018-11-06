package us.talabrek.ultimateskyblock.island.task;

import java.util.Iterator;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.async.IncrementalRunnable;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;

/**
 * SetBiomeTask for incremental execution.
 */
public class SetBiomeTask extends IncrementalRunnable {
    private final World world;
    private final Biome biome;
    private final BlockVector3 minP;
    private final BlockVector3 maxP;
    private final Set<BlockVector2> chunks;

    public SetBiomeTask(uSkyBlock plugin, Location loc, Biome biome, Runnable onCompletion) {
        super(plugin, onCompletion);
        this.biome = biome;
        ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(loc);
        if (region != null) {
            minP = region.getMinimumPoint();
            maxP = region.getMaximumPoint();
        } else {
            minP = null;
            maxP = null;
        }
        world = loc.getWorld();
        chunks = WorldEditHandler.getChunks(new CuboidRegion(minP, maxP));
    }

    public SetBiomeTask(uSkyBlock plugin, World world, BlockVector3 minP, BlockVector3 maxP, Biome biome, Runnable onCompletion) {
        super(plugin, onCompletion);
        this.biome = biome;
        this.minP = minP;
        this.maxP = maxP;
        this.world = world;
        chunks = WorldEditHandler.getChunks(new CuboidRegion(minP, maxP));
    }

    @Override
    protected boolean execute() {
        if (minP == null || maxP == null) {
            return true;
        }
        Iterator<BlockVector2> it = chunks.iterator();
        while (it.hasNext()) {
            BlockVector2 chunk = it.next();
            it.remove();
            world.loadChunk(chunk.getBlockX(), chunk.getBlockZ());
            int cx = chunk.getBlockX() << 4;
            int cz = chunk.getBlockZ() << 4;
            int mx = cx + 15;
            int mz = cz + 15;
            if (cx < minP.getBlockX()) {
                cx = minP.getBlockX();
            }
            if (cz < minP.getBlockZ()) {
                cz = minP.getBlockZ();
            }
            if (mx > maxP.getBlockX()) {
                mx = maxP.getBlockX();
            }
            if (mz > maxP.getBlockZ()) {
                mz = maxP.getBlockZ();
            }
            for (int x = cx; x <= mx; x++) {
                for (int z = cz; z <= mz; z++) {
                    world.setBiome(x, z, biome);
                }
            }
            world.refreshChunk(chunk.getBlockX(), chunk.getBlockZ());
            if (!tick()) {
                return isDone();
            }
        }
        return isDone();
    }

    private boolean isDone() {
        return chunks.isEmpty();
    }
}
