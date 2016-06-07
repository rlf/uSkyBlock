package us.talabrek.ultimateskyblock.handler;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.task.WorldEditClear;
import us.talabrek.ultimateskyblock.handler.task.WorldEditRegen;
import us.talabrek.ultimateskyblock.handler.task.WorldRegen;
import us.talabrek.ultimateskyblock.player.PlayerPerk;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public static void loadIslandSchematic(final File file, final Location origin, PlayerPerk playerPerk) {
        log.finer("Trying to load schematic " + file);
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            BukkitWorld bukkitWorld = new BukkitWorld(origin.getWorld());
            ClipboardReader reader = ClipboardFormat.SCHEMATIC.getReader(in);

            WorldData worldData = bukkitWorld.getWorldData();
            Clipboard clipboard = reader.read(worldData);
            ClipboardHolder holder = new ClipboardHolder(clipboard, worldData);

            Player player = Bukkit.getPlayer(playerPerk.getPlayerInfo().getUniqueId());
            int maxBlocks = (255 * Settings.island_protectionRange * Settings.island_protectionRange);
            final EditSession editSession = AsyncWorldEditHandler.createEditSession(bukkitWorld, maxBlocks);
            editSession.enableQueue();
            editSession.setFastMode(true);
            Vector to = new Vector(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
            final Operation operation = holder
                    .createPaste(editSession, worldData)
                    .to(to)
                    .ignoreAirBlocks(true)
                    .build();
            AsyncWorldEditHandler.registerCompletion(player);
            Operations.completeBlindly(operation);
            editSession.flushQueue();
        } catch (IOException e) {
            uSkyBlock.log(Level.WARNING, "Unable to load schematic " + file, e);
        }
    }

    /**
     * Returns all the chunks that are fully contained within the region.
     */
    public static Set<Vector2D> getInnerChunks(Region region) {
        Set<Vector2D> chunks = new HashSet<>();
        int minX = region.getMinimumPoint().getBlockX();
        int minZ = region.getMinimumPoint().getBlockZ();
        int maxX = region.getMaximumPoint().getBlockX();
        int maxZ = region.getMaximumPoint().getBlockZ();
        int cx = minX & 0xF;
        int cz = minZ & 0xF;
        minX = cx != 0 ? minX - cx + 16 : minX;
        minZ = cz != 0 ? minZ - cz + 16 : minZ;
        for (int x = minX; x <= maxX; x+=16) {
            for (int z = minZ; z <= maxZ; z+=16) {
                chunks.add(new Vector2D(x >> 4, z >> 4));
            }
        }
        return chunks;
    }

    public static Set<Vector2D> getOuterChunks(Region region) {
        Set<Vector2D> chunks = new HashSet<>();
        int minX = region.getMinimumPoint().getBlockX();
        int minZ = region.getMinimumPoint().getBlockZ();
        int maxX = region.getMaximumPoint().getBlockX();
        int maxZ = region.getMaximumPoint().getBlockZ();
        int cx = minX & 0xF;
        int cz = minZ & 0xF;
        minX = minX - cx;
        minZ = minZ - cz;
        cx = maxX & 0xF;
        cz = maxZ & 0xF;
        maxX = cx != 15 ? maxX - cx + 16 : maxX;
        maxZ = cz != 15 ? maxZ - cz + 16 : maxZ;
        for (int x = minX; x < maxX; x+=16) {
            for (int z = minZ; z < maxZ; z+=16) {
                chunks.add(new Vector2D(x >> 4, z >> 4));
            }
        }
        return chunks;
    }

    public static Set<Vector2D> getChunks(Region region) {
        Set<Vector2D> chunks = new HashSet<>();
        int minX = region.getMinimumPoint().getBlockX();
        int minZ = region.getMinimumPoint().getBlockZ();
        int maxX = region.getMaximumPoint().getBlockX();
        int maxZ = region.getMaximumPoint().getBlockZ();
        int cx = minX & 0xF;
        int cz = minZ & 0xF;
        minX = (minX - cx) >> 4;
        minZ = (minZ - cz) >> 4;
        maxX = (maxX - (maxX & 0xF) + 16) >> 4;
        maxZ = (maxZ - (maxZ & 0xF) + 16) >> 4;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                chunks.add(new Vector2D(x, z));
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
        Set<Vector2D> innerChunks;
        Set<Region> borderRegions = new HashSet<>();
        if (isOuterPossible()) {
            if (Settings.island_protectionRange == Settings.island_distance) {
                innerChunks = getInnerChunks(cube);
            } else {
                innerChunks = getOuterChunks(cube);
            }
        } else {
            innerChunks = getInnerChunks(cube);
            borderRegions = getBorderRegions(cube);
        }
        Runnable onCompletion = new Runnable() {
            @Override
            public void run() {
                long diff = System.currentTimeMillis() - t;
                uSkyBlock.log(Level.FINE, String.format("Cleared island in %d.%03d seconds", (diff / 1000), (diff % 1000)));
                if (afterDeletion != null) {
                    afterDeletion.run();
                }
            }
        };
        WorldEditRegen weRegen = new WorldEditRegen(uSkyBlock.getInstance(), skyWorld, borderRegions, onCompletion);
        WorldRegen regen = new WorldRegen(uSkyBlock.getInstance(), skyWorld, innerChunks, weRegen);
        regen.runTask(uSkyBlock.getInstance());
    }

    public static void clearNetherIsland(final World skyWorld, final ProtectedRegion region, final Runnable afterDeletion) {
        log.finer("Clearing island " + region);
        final long t = System.currentTimeMillis();
        final Region cube = getRegion(skyWorld, region);
        Set<Vector2D> innerChunks;
        Set<Region> borderRegions = new HashSet<>();
        if (isOuterPossible()) {
            if (Settings.island_protectionRange == Settings.island_distance) {
                innerChunks = getInnerChunks(cube);
            } else {
                innerChunks = getOuterChunks(cube);
            }
        } else {
            innerChunks = getInnerChunks(cube);
            borderRegions = getBorderRegions(cube);
        }
        Runnable onCompletion = new Runnable() {
            @Override
            public void run() {
                long diff = System.currentTimeMillis() - t;
                uSkyBlock.log(Level.FINE, String.format("Cleared nether-island in %d.%03d seconds", (diff / 1000), (diff % 1000)));
                if (afterDeletion != null) {
                    afterDeletion.run();
                }
            }
        };
        WorldEditRegen weRegen = new WorldEditRegen(uSkyBlock.getInstance(), skyWorld, borderRegions, onCompletion);
        WorldRegen regen = new WorldRegen(uSkyBlock.getInstance(), skyWorld, innerChunks, weRegen);
        regen.runTask(uSkyBlock.getInstance());
    }

    private static Region getRegion(World skyWorld, ProtectedRegion region) {
        return new CuboidRegion(new BukkitWorld(skyWorld), region.getMinimumPoint(), region.getMaximumPoint());
    }

    public static boolean isOuterPossible() {
        return Settings.island_distance >= Settings.island_protectionRange &&
                ((Settings.island_distance % 32) == 0 || (Settings.island_distance - Settings.island_protectionRange) > 32);
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

    public static EditSession createEditSession(BukkitWorld bukkitWorld, int maxBlocks) {
        return new EditSession(bukkitWorld, maxBlocks);
    }
}
