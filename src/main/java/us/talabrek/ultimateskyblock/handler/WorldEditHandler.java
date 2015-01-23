package us.talabrek.ultimateskyblock.handler;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.registry.WorldData;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.async.CompositeIncrementalTask;
import us.talabrek.ultimateskyblock.handler.task.WorldEditRegenTask;
import us.talabrek.ultimateskyblock.handler.task.WorldRegenTask;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

public class WorldEditHandler {
    public static WorldEditPlugin getWorldEdit() {
        final Plugin plugin = uSkyBlock.getInstance().getServer().getPluginManager().getPlugin("WorldEdit");
        if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
            return null;
        }
        return (WorldEditPlugin) plugin;
    }

    public static boolean loadIslandSchematic(Player player, final World world, final File file, final Location origin) {
        WorldEdit worldEdit = getWorldEdit().getWorldEdit();
        BukkitPlayer wePlayer = getWorldEdit().wrapPlayer(player);
        LocalSession session = worldEdit.getSession(wePlayer);
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            BukkitWorld bukkitWorld = new BukkitWorld(world);
            ClipboardReader reader = ClipboardFormat.SCHEMATIC.getReader(in);

            WorldData worldData = bukkitWorld.getWorldData();
            Clipboard clipboard = reader.read(worldData);
            ClipboardHolder holder = new ClipboardHolder(clipboard, worldData);
            session.setClipboard(holder);

            EditSession editSession = new EditSession(bukkitWorld, 255 * Settings.island_protectionRange * Settings.island_protectionRange);
            editSession.enableQueue();
            Vector to = new Vector(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
            Operation operation = holder
                    .createPaste(editSession, worldData)
                    .to(to)
                    .ignoreAirBlocks(false)
                    .build();
            Operations.completeLegacy(operation);
            editSession.flushQueue();
            editSession.commit();
            return true;
        } catch (IOException|WorldEditException e) {
            uSkyBlock.log(Level.WARNING, "Unable to load schematic " + file, e);
        }
        return false;
    }

    /**
     * Returns all the chunks that are fully contained within the region.
     */
    private static Set<Vector2D> getInnerChunks(Region region) {
        Set<Vector2D> chunks = region.getChunks();
        for (Iterator<Vector2D> it = chunks.iterator(); it.hasNext(); ) {
            Vector2D chunk = it.next();
            int bx = chunk.getBlockX()*16;
            int bz = chunk.getBlockZ()*16;
            Vector v1 = new Vector(bx, 100, bz);
            Vector v2 = new Vector(bx+16, 100, bz);
            Vector v3 = new Vector(bx+16, 100, bz+16);
            Vector v4 = new Vector(bx, 100, bz+16);
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
    private static Set<Region> getBorderRegions(Region region) {
        Set<Region> borders = new HashSet<>();
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();
        int minY = Math.min(min.getBlockY(), max.getBlockY());
        int maxY = Math.max(min.getBlockY(), max.getBlockY());
        int minX = Math.min(min.getBlockX(), max.getBlockX());
        int maxX = Math.max(min.getBlockX(), max.getBlockX());
        int minZ = Math.min(min.getBlockZ(), max.getBlockZ());
        int maxZ = Math.max(min.getBlockZ(), max.getBlockZ());

        // Java and modulo is weird - and we need the right sign for the arithmetic below to work.
        int minModX = minX < 0 ? -(minX % 16) : minX % 16;
        int maxModX = maxX < 0 ? -(maxX % 16) : maxX % 16;
        int minModZ = minZ < 0 ? -(minZ % 16) : minZ % 16;
        int maxModZ = maxZ < 0 ? -(maxZ % 16) : maxZ % 16;
        int minChunkX = minModX == 0 ? minX : minX + (16- minModX);
        int maxChunkX = maxModX == 0 ? maxX : maxX - maxModX;
        int minChunkZ = minModZ == 0 ? minZ : minZ + (16- minModZ);
        int maxChunkZ = maxModZ == 0 ? maxZ : maxZ - maxModZ;
        // min < minChunk < maxChunk < max
        if ((minChunkX - minX) > 0) {
            borders.add(new CuboidRegion(region.getWorld(),
                    new Vector(minX, minY, minZ),
                    new Vector(minChunkX, maxY, maxZ)));
        }
        if ((maxZ - maxChunkZ) > 0) {
            borders.add(new CuboidRegion(region.getWorld(),
                    new Vector(minChunkX, minY, maxChunkZ),
                    new Vector(maxChunkX, maxY, maxZ)));
        }
        if ((maxX - maxChunkX) > 0) {
            borders.add(new CuboidRegion(region.getWorld(),
                    new Vector(maxChunkX, minY, minZ),
                    new Vector(maxX, maxY, maxZ)));
        }
        if ((minChunkZ - minZ) > 0) {
            borders.add(new CuboidRegion(region.getWorld(),
                    new Vector(minChunkX, minY, minZ),
                    new Vector(maxChunkX, maxY, minChunkZ)));
        }
        return borders;
    }

    public static void clearIsland(final World skyWorld, final ProtectedRegion region, final Runnable afterDeletion) {
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

    public static void refreshRegion(Location location) {
        ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(location);
        World world = location.getWorld();
        Region cube = getRegion(world, region);
        for (Vector2D chunk : cube.getChunks()) {
            world.unloadChunk(chunk.getBlockX(), chunk.getBlockZ(), true, false);
            world.loadChunk(chunk.getBlockX(), chunk.getBlockZ(), false);
            world.refreshChunk(chunk.getBlockX(), chunk.getBlockZ());
        }
    }
}
