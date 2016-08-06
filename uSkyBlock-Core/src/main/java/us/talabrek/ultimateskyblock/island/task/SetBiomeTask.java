package us.talabrek.ultimateskyblock.island.task;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import us.talabrek.ultimateskyblock.async.IncrementalRunnable;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.handler.task.WorldEditClear;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Iterator;
import java.util.Set;

/**
 * SetBiomeTask for incremental execution.
 */
public class SetBiomeTask extends IncrementalRunnable {
    private final World world;
    private final Biome biome;
    private final BlockVector minP;
    private final BlockVector maxP;
    private final Set<Vector2D> chunks;

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

    public SetBiomeTask(uSkyBlock plugin, World world, BlockVector minP, BlockVector maxP, Biome biome, Runnable onCompletion) {
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
        Iterator<Vector2D> it = chunks.iterator();
        while (it.hasNext()) {
            Vector2D chunk = it.next();
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
