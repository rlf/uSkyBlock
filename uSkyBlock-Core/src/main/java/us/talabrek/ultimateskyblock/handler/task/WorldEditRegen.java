package us.talabrek.ultimateskyblock.handler.task;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import us.talabrek.ultimateskyblock.async.IncrementalRunnable;
import us.talabrek.ultimateskyblock.handler.AsyncWorldEditHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runnable that can be run incrementally.
 */
public class WorldEditRegen extends IncrementalRunnable {
    private static final Logger log = Logger.getLogger(WorldEditRegen.class.getName());
    // The size of the "slices" in regions
    private static final int INCREMENT = AsyncWorldEditHandler.getFAWE() != null ? 128 : 2;

    private final List<Region> regions;
    private final int tasksToComplete;
    private volatile int tasksCompleted = 0;

    public WorldEditRegen(uSkyBlock plugin, Set<Region> borderRegions, Runnable onCompletion) {
        super(plugin, onCompletion);
        log.log(Level.FINE, "Planning regen of borders: " + borderRegions);
        regions = createRegions(borderRegions);
        tasksToComplete = regions.size();
        log.log(Level.FINE, "Planning regen of regions: " + regions);
    }

    private List<Region> createRegions(Set<Region> borderRegions) {
        List<Region> list = new ArrayList<>();
        for (Region region : borderRegions) {
            if (region.getLength() > region.getWidth()) {
                // Z-axis
                Vector min = region.getMinimumPoint();
                Vector max = region.getMaximumPoint();
                Vector pt = new Vector(max);
                pt = pt.setZ(min.getBlockZ());
                while (pt.getBlockZ() < max.getBlockZ()) {
                    int dz = Math.min(INCREMENT, Math.abs(max.getBlockZ() - pt.getBlockZ()));
                    pt = pt.add(0, 0, dz);
                    list.add(new CuboidRegion(region.getWorld(), min, pt));
                    min = min.setZ(pt.getZ());
                }
            } else {
                // X-axis
                Vector min = region.getMinimumPoint();
                Vector max = region.getMaximumPoint();
                Vector pt = new Vector(max);
                pt = pt.setX(min.getBlockX());
                while (pt.getBlockX() < max.getBlockX()) {
                    int dx = Math.min(INCREMENT, Math.abs(max.getBlockX() - pt.getBlockX()));
                    pt = pt.add(dx, 0, 0);
                    list.add(new CuboidRegion(region.getWorld(), min, pt));
                    min = min.setX(pt.getX());
                }
            }
        }
        return list;
    }

    @Override
    protected boolean execute() {
        while (!regions.isEmpty()) {
            final Region region = regions.remove(0);
            AsyncWorldEditHandler.regenerate(region, new Runnable() {
                @Override
                public void run() {
                    synchronized (WorldEditRegen.this) {
                        tasksCompleted++;
                        log.finer(Thread.currentThread().getName() + ": Completed " + tasksCompleted + " of " + tasksToComplete);
                    }
                }
            });
            if (!tick()) {
                break;
            }
        }
        // TODO: 17/06/2016 - R4zorax: Additional bail-out needed
        return regions.isEmpty() && tasksCompleted >= tasksToComplete;
    }
}
