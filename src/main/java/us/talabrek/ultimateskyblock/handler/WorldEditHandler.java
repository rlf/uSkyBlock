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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

    public static boolean loadIslandSchematic(final World world, final File file, final Location origin) {
        return WorldEditAdaptor.Factory.create(getWorldEdit()).loadIslandSchematic(world, file, origin);
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
     *
     * <pre>
     *     ^
     *     |           +---+---+---+---+---+---+
     *     |           |   |   |   |   |   |   |
     *     |           |   |   |   |   |   |   |
     *     |           +---+---+---+---+---+---+
     *     |         l |   | D=======C |   |   |
     *     |           |   | I |   | I |   |   |
     *     |         k +---+-I-R---Q-I-+---+---+
     *     |           |   | I |   | I |   |   |
     *     |           |   | I |   | I |   |   |
     *     |         j +---+-I-O---P-I-+---+---+
     *     |           |   | I |   | I |   |   |
     *     |         i |   | A=======B |   |   |
     *     |           +---+---+---+---+---+---+
     *     |                 a b   c d
     *     +----------------------------------------->
     *
     * Points:
     *     A = (a,i)
     *     B = (d,i)
     *     C = (d,l)
     *     D = (a,l)
     *
     *     M(x) = X mod 16, i.e. Mc = C mod 16.
     *
     * Borders:
     *     O = A + 16 - Ma   | A > 0
     *       = A - Ma        | A <= 0
     *
     *     Q = C - Mc - 1    | C > 0 && Mc != 15
     *       = C + Mc - 16   | C < 0 && Mc != -1
     * </pre>
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

        int minModX = minX % 16;
        int maxModX = maxX % 16;
        int minModZ = minZ % 16;
        int maxModZ = maxZ % 16;
        // Negative values are aligned differently than positive values
        int minChunkX = minModX > 0 ? minX + 16 - minModX : minX - minModX;
        int maxChunkX = maxModX >= 0 && maxModX != 15 ? maxX - maxModX - 1 : maxModX < 0 && maxModX != -1 ? maxX - 16 + maxModX : maxX;
        int minChunkZ = minModZ > 0 ? minZ + 16 - minModZ : minZ - minModZ;
        int maxChunkZ = maxModZ >= 0 && maxModZ != 15 ? maxZ - maxModZ - 1 : maxModZ < 0 && maxModZ != -1 ? maxZ - 16 + maxModZ : maxZ;
        // min < minChunk < maxChunk < max
        if (minModX != 0) {
            borders.add(new CuboidRegion(region.getWorld(),
                    new Vector(minX, minY, minZ),
                    new Vector(minChunkX-1, maxY, maxZ)));
        }
        if (maxModZ != 15 && maxModZ != -1) {
            borders.add(new CuboidRegion(region.getWorld(),
                    new Vector(minChunkX, minY, maxChunkZ+1),
                    new Vector(maxChunkX, maxY, maxZ)));
        }
        if (maxModX != 15 && maxModX != -1) {
            borders.add(new CuboidRegion(region.getWorld(),
                    new Vector(maxChunkX+1, minY, minZ),
                    new Vector(maxX, maxY, maxZ)));
        }
        if (minModZ != 0) {
            borders.add(new CuboidRegion(region.getWorld(),
                    new Vector(minChunkX, minY, minZ),
                    new Vector(maxChunkX, maxY, minChunkZ-1)));
        }
        return borders;
    }

    public static void clearIsland(final World skyWorld, final ProtectedRegion region, final Runnable afterDeletion) {
        log.finer("Clearing island " + region);
        final long t = System.currentTimeMillis();
        final Region cube = getRegion(skyWorld, region);
        Set<Vector2D> innerChunks = getInnerChunks(cube);
        WorldRegenTask worldRegenTask = new WorldRegenTask(skyWorld, innerChunks);
        Set<Region> borderRegions = getBorderRegions(cube);
        WorldEditRegenTask worldEditTask = new WorldEditRegenTask(skyWorld, borderRegions);
        uSkyBlock.getInstance().getExecutor().execute(uSkyBlock.getInstance(), new CompositeIncrementalTask(worldEditTask, worldRegenTask), new Runnable() {
            @Override
            public void run() {
                long diff = System.currentTimeMillis() - t;
                uSkyBlock.log(Level.INFO, String.format("Cleared island in %d.%03d seconds", (diff / 1000), (diff % 1000)));
                if (afterDeletion != null) {
                    afterDeletion.run();
                }
            }
        }, 0.5f, 1); // 50% load, max. 1 ticks in a row
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

    public static List<Player> getPlayersInRegion(World world, ProtectedRegion region) {
        // Note: This might be heavy - for large servers...
        List<Player> players = new ArrayList<>();
        for (Player player : world.getPlayers()) {
            if (player != null && player.isOnline()) {
                Location p = player.getLocation();
                if (region.contains(p.getBlockX(), p.getBlockY(), p.getBlockZ())) {
                    players.add(player);
                }
            }
        }
        return players;
    }
}
