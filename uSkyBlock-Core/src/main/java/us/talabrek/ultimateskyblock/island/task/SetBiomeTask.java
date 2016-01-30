package us.talabrek.ultimateskyblock.island.task;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import us.talabrek.ultimateskyblock.async.IncrementalRunnable;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

/**
 * SetBiomeTask for incremental execution.
 */
public class SetBiomeTask extends IncrementalRunnable {
    private final Location loc;
    private final Biome biome;
    private final BlockVector minP;
    private final BlockVector maxP;
    private final int maxX;
    private final int maxZ;
    private int x;
    private int z;

    public SetBiomeTask(uSkyBlock plugin, Location loc, Biome biome, Runnable onCompletion) {
        super(plugin, onCompletion);
        this.loc = loc;
        this.biome = biome;
        ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(loc);
        if (region != null) {
            minP = region.getMinimumPoint();
            maxP = region.getMaximumPoint();
            x = minP.getBlockX();
            z = minP.getBlockZ();
            maxX = maxP.getBlockX();
            maxZ = maxP.getBlockZ();
        } else {
            minP = null;
            maxP = null;
            maxX = 0;
            maxZ = 0;
        }
    }

    @Override
    protected boolean execute() {
        if (minP == null || maxP == null) {
            return true;
        }
        for (; x <= maxX; ) {
            for (; z <= maxZ; ) {
                if ((x % 16) == 0 && (z % 16) == 0) {
                    loc.getWorld().loadChunk(x, z);
                }
                // Set the biome in the world.
                loc.getWorld().setBiome(x, z, biome);
                // Refresh the chunks so players can see it without relogging!
                // Unfortunately, it doesn't work - though it should (We filed a bug report about it to SPIGOT)
                // See https://hub.spigotmc.org/jira/browse/SPIGOT-457
                //skyBlockWorld.refreshChunk(x, z);
                z++;
                if (!tick()) {
                    return isDone();
                }
            }
            x++;
            if (isDone()) {
                return true;
            }
            z = minP.getBlockZ();
        }
        return isDone();
    }

    private boolean isDone() {
        return x > maxX && z >= maxZ;
    }
}
