package us.talabrek.ultimateskyblock.handler.task;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.async.IncrementalTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Do WorldEdit stuff in increments
 */
public class WorldEditRegenTask implements IncrementalTask {
    private static final BaseBlock AIR = new BaseBlock(0);
    private static final BaseBlock GLASS = new BaseBlock(20);
    public static final int INCREMENT = 4;
    private EditSession editSession;
    private final BukkitWorld bukkitWorld;
    private final List<Region> regions;
    private final int size;

    public WorldEditRegenTask(World world, Set<Region> borderRegions) {
        bukkitWorld = new BukkitWorld(world);
        // TODO: 21/09/2015 - R4zorax: Note this will NOT use AWE
        //editSession = new EditSession(bukkitWorld, 255 * Settings.island_protectionRange * Settings.island_protectionRange);
        // This one will get an AWE session...
        //editSession = WorldEditHandler.getWorldEdit().getWorldEdit().getEditSessionFactory().getEditSession(bukkitWorld, 255 * Settings.island_protectionRange * Settings.island_protectionRange);
        log.log(Level.FINE, "Planning regen of borders: " + borderRegions);
        regions = createRegions(borderRegions);
        size = regions.size();
        log.log(Level.FINE, "Planning regen of regions: " + regions);
    }

    private List<Region> createRegions(Set<Region> borderRegions) {
        List<Region> list = new ArrayList<>();
        for (Region region : borderRegions) {
            if (region.getLength() > region.getWidth())  {
                // Z-axis
                Vector min = region.getMinimumPoint();
                Vector max = region.getMaximumPoint();
                Vector pt = new Vector(max);
                pt = pt.setZ(min.getBlockZ());
                while (pt.getBlockZ() < max.getBlockZ()) {
                    int dz = Math.min(INCREMENT, Math.abs(max.getBlockZ()-pt.getBlockZ()));
                    pt = pt.add(0, 0, dz);
                    list.add(new CuboidRegion(min, pt));
                    min = min.setZ(pt.getZ());
                }
            } else {
                // X-axis
                Vector min = region.getMinimumPoint();
                Vector max = region.getMaximumPoint();
                Vector pt = new Vector(max);
                pt = pt.setX(min.getBlockX());
                while (pt.getBlockX() < max.getBlockX()) {
                    int dx = Math.min(INCREMENT, Math.abs(max.getBlockX()-pt.getBlockX()));
                    pt = pt.add(dx, 0, 0);
                    list.add(new CuboidRegion(min, pt));
                    min = min.setX(pt.getX());
                }
            }
        }
        return list;
    }

    @Override
    public boolean execute(Plugin plugin, int offset, int length) {
        log.log(Level.FINE, "Executing WorldEditRegen of regions " + offset + "-" + (offset+length) + " of " + regions.size() + " regions");
        for (int i = 0; i < length && !regions.isEmpty(); i++) {
            Region region = regions.remove(0);
            editSession = new EditSession(bukkitWorld, region.getArea()*255);
            editSession.setFastMode(true);
            editSession.enableQueue();
            bukkitWorld.regenerate(region, editSession);
            editSession.flushQueue();
            editSession.commit();
        }
        return isComplete();
    }

    @Override
    public int getLength() {
        return size;
    }

    @Override
    public boolean isComplete() {
        return regions.isEmpty();
    }
}
