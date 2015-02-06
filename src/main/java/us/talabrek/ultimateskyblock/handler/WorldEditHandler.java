package us.talabrek.ultimateskyblock.handler;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.async.CompositeIncrementalTask;
import us.talabrek.ultimateskyblock.handler.task.WorldEditRegenTask;
import us.talabrek.ultimateskyblock.handler.task.WorldRegenTask;
import us.talabrek.ultimateskyblock.handler.worldedit.WorldEditAdaptor;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorldEditHandler {
    private static final Logger log = Logger.getLogger(WorldEditHandler.class.getName());

    public static WorldEditPlugin getWorldEdit() {
        final Plugin plugin = uSkyBlock.getInstance().getServer().getPluginManager().getPlugin("WorldEdit");
        if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
            return null;
        }
        return (WorldEditPlugin) plugin;
    }

    public static boolean loadIslandSchematic(Player player, final World world, final File file, final Location origin) {
        return WorldEditAdaptor.Factory.create(getWorldEdit()).loadIslandSchematic(player, world, file, origin);
    }

    /**
     * Returns all the chunks that are fully contained within the region.
     */
    public static Set<Vector2D> getInnerChunks(Region region) {
        Set<Vector2D> chunks = region.getChunks();
        int by = (region.getMaximumPoint().getBlockY() + region.getMinimumPoint().getBlockY())/2;
        for (Iterator<Vector2D> it = chunks.iterator(); it.hasNext(); ) {
            Vector2D chunk = it.next();
            int bx = chunk.getBlockX()*16;
            int bz = chunk.getBlockZ()*16;
            Vector v1 = new Vector(bx, by, bz);
            Vector v2 = new Vector(bx+15, by, bz);
            Vector v3 = new Vector(bx+15, by, bz+15);
            Vector v4 = new Vector(bx, by, bz+15);
            if (!region.contains(v1) || !region.contains(v2) || !region.contains(v3) || !region.contains(v4)) {
                it.remove();
            }
        }
        return chunks;
    }

    /**
     * Returns a collection of regions covering the borders of the original region.
     * <b>Note:</b> complements the #getInnerChunks
     */
    public static Set<Region> getBorderRegions(Region region) {
        Set<Region> borders = new HashSet<>();
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();
        int minY = min.getBlockY();
        int maxY = max.getBlockY();
        int minX = min.getBlockX();
        int maxX = max.getBlockX();
        int minZ = min.getBlockZ();
        int maxZ = max.getBlockZ();

        int minModX = minX < 0 ? -(minX % 16) : minX % 16;
        int maxModX = maxX < 0 ? -(maxX % 16) : maxX % 16;
        int minModZ = minZ < 0 ? -(minZ % 16) : minZ % 16;
        int maxModZ = maxZ < 0 ? -(maxZ % 16) : maxZ % 16;
        int minChunkX = minX + (16-minModX);
        int maxChunkX = maxX - maxModX;
        int minChunkZ = minZ + (16-minModZ);
        int maxChunkZ = maxZ - maxModZ;
        // min < minChunk < maxChunk < max
        if ((minChunkX - minX) != 0) {
            borders.add(new CuboidRegion(region.getWorld(),
                    new Vector(minX, minY, minZ),
                    new Vector(minChunkX, maxY, maxZ)));
        }
        if ((maxZ - maxChunkZ) != 0) {
            borders.add(new CuboidRegion(region.getWorld(),
                    new Vector(minChunkX, minY, maxChunkZ),
                    new Vector(maxChunkX, maxY, maxZ)));
        }
        if ((maxX - maxChunkX) != 0) {
            borders.add(new CuboidRegion(region.getWorld(),
                    new Vector(maxChunkX, minY, minZ),
                    new Vector(maxX, maxY, maxZ)));
        }
        if ((minChunkZ - minZ) != 0) {
            borders.add(new CuboidRegion(region.getWorld(),
                    new Vector(minChunkX, minY, minZ),
                    new Vector(maxChunkX, maxY, minChunkZ)));
        }
        return borders;
    }

    public static void clearIsland(final World skyWorld, final ProtectedRegion region, final Runnable afterDeletion) {
        log.finer("Clearing island " + region);
        final long t = System.currentTimeMillis();
        final Region cube = getRegion(skyWorld, region);
        Set<Vector2D> innerChunks = getInnerChunks(cube);
        WorldRegenTask worldRegenTask = new WorldRegenTask(skyWorld, innerChunks);
        WorldEditRegenTask worldEditTask = new WorldEditRegenTask(skyWorld, getBorderRegions(cube));
        uSkyBlock.getInstance().getExecutor().execute(uSkyBlock.getInstance(), new CompositeIncrementalTask(worldRegenTask, worldEditTask), new Runnable() {
            @Override
            public void run() {
                long diff = System.currentTimeMillis() - t;
                uSkyBlock.log(Level.INFO, String.format("Cleared island in %d.%03d seconds", (diff / 1000), (diff % 1000)));
                if (afterDeletion != null) {
                    afterDeletion.run();
                }
            }
        }, 0.5f, 1); // 50% load, max. 10 ticks in a row
    }

    private static Region getRegion(World skyWorld, ProtectedRegion region) {
        return new CuboidRegion(new BukkitWorld(skyWorld), region.getMinimumPoint(), region.getMaximumPoint());
    }

    public static void loadRegion(Location location) {
        ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(location);
        World world = location.getWorld();
        Region cube = getRegion(world, region);
        for (Vector2D chunk : cube.getChunks()) {
            world.unloadChunk(chunk.getBlockX(), chunk.getBlockZ(), true, false);
            world.loadChunk(chunk.getBlockX(), chunk.getBlockZ(), false);
        }
    }

    public static void unloadRegion(Location location) {
        ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(location);
        World world = location.getWorld();
        Region cube = getRegion(world, region);
        for (Vector2D chunk : cube.getChunks()) {
            world.unloadChunk(chunk.getBlockX(), chunk.getBlockZ(), true);
        }
    }

    public static void refreshRegion(Location location) {
        ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(location);
        World world = location.getWorld();
        Region cube = getRegion(world, region);
        for (Vector2D chunk : cube.getChunks()) {
            world.refreshChunk(chunk.getBlockX(), chunk.getBlockZ());
        }
    }


}
