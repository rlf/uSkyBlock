package us.talabrek.ultimateskyblock.handler.task;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.World;
import us.talabrek.ultimateskyblock.async.IncrementalRunnable;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runnable that can be run incrementally.
 */
public class WorldEditClear extends IncrementalRunnable {
    private static final Logger log = Logger.getLogger(WorldEditClear.class.getName());
    // The size of the "slices" in regions
    private static final int INCREMENT = 2;
    private final World world;
    private final List<Region> regions;

    public WorldEditClear(uSkyBlock plugin, World world, Set<Region> borderRegions, Runnable onCompletion) {
        super(plugin, onCompletion);
        this.world = world;
        log.log(Level.FINE, "Planning regen of borders: " + borderRegions);
        regions = createRegions(borderRegions);
        log.log(Level.FINE, "Planning regen of regions: " + regions);
    }

    private List<Region> createRegions(Set<Region> borderRegions) {
        List<Region> list = new ArrayList<>();
        for (Region region : borderRegions) {
            if (region.getLength() > region.getWidth())  {
                // Z-axis
                BlockVector3 min = region.getMinimumPoint();
                BlockVector3 max = region.getMaximumPoint();
                BlockVector3 pt = BlockVector3.at(max.getX(), max.getY(), max.getZ());
                pt = pt.withZ(min.getBlockZ());
                while (pt.getBlockZ() < max.getBlockZ()) {
                    int dz = Math.min(INCREMENT, Math.abs(max.getBlockZ()-pt.getBlockZ()));
                    pt = pt.add(0, 0, dz);
                    list.add(new CuboidRegion(min, pt));
                    min = min.withZ(pt.getZ());
                }
            } else {
                // X-axis
                BlockVector3 min = region.getMinimumPoint();
                BlockVector3 max = region.getMaximumPoint();
                BlockVector3 pt = BlockVector3.at(max.getX(), max.getY(), max.getZ());
                pt = pt.withX(min.getBlockX());
                while (pt.getBlockX() < max.getBlockX()) {
                    int dx = Math.min(INCREMENT, Math.abs(max.getBlockX()-pt.getBlockX()));
                    pt = pt.add(dx, 0, 0);
                    list.add(new CuboidRegion(min, pt));
                    min = min.withX(pt.getX());
                }
            }
        }
        return list;
    }

    @Override
    protected boolean execute() {
        while (!regions.isEmpty()) {
            final Region region = regions.remove(0);
            final EditSession editSession = WorldEditHandler.createEditSession(
                    new BukkitWorld(world), region.getArea() * 255);
            editSession.setReorderMode(EditSession.ReorderMode.MULTI_STAGE);
            editSession.setFastMode(true);
            try {
                editSession.setBlocks(region, BlockTypes.AIR.getDefaultState());
            } catch (MaxChangedBlocksException e) {
                log.log(Level.INFO, "Warning: we got MaxChangedBlocks from WE, please increase it!");
            }
            editSession.flushSession();
            if (!tick()) {
                break;
            }
        }
        return regions.isEmpty();
    }
}
