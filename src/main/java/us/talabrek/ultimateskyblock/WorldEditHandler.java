package us.talabrek.ultimateskyblock;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class WorldEditHandler {
    public static WorldEditPlugin getWorldEdit() {
        final Plugin plugin = uSkyBlock.getInstance().getServer().getPluginManager().getPlugin("WorldEdit");
        if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
            return null;
        }
        return (WorldEditPlugin) plugin;
    }

    public static boolean loadIslandSchematic(final World world, final File file, final Location origin) throws DataException, IOException, MaxChangedBlocksException {
        final Vector v = new Vector(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
        final SchematicFormat format = SchematicFormat.getFormat(file);
        if (format == null) {
            return false;
        }
        final EditSession es = new EditSession(new BukkitWorld(world), 999999999);
        final CuboidClipboard cc = format.load(file);
        cc.paste(es, v, false);
        return true;
    }

    public static void reloadIsland(final World skyWorld, final ProtectedRegion region) {
        long t = System.currentTimeMillis();
        final Region cube = getRegion(skyWorld, region);
        for (Vector2D chunk : cube.getChunks()) {
            skyWorld.loadChunk(chunk.getBlockX()/16, chunk.getBlockZ()/16, true);
            skyWorld.refreshChunk(chunk.getBlockX()/16, chunk.getBlockZ()/16);
        }
        long diff = System.currentTimeMillis() - t;
        uSkyBlock.log(Level.INFO, String.format("Reloaded island in %d.%03d seconds", (diff / 1000), (diff % 1000)));
    }

    public static void clearIsland(final World skyWorld, final ProtectedRegion region) {
        long t = System.currentTimeMillis();
        final Region cube = getRegion(skyWorld, region);
        for (Vector2D chunk : cube.getChunks()) {
            skyWorld.loadChunk(chunk.getBlockX() / 16, chunk.getBlockZ() / 16, true);
            skyWorld.regenerateChunk(chunk.getBlockX(), chunk.getBlockZ());
        }
        long diff = System.currentTimeMillis() - t;
        uSkyBlock.log(Level.INFO, String.format("Cleared island in %d.%03d seconds", (diff / 1000), (diff % 1000)));
    }

    private static Region getRegion(World skyWorld, ProtectedRegion region) {
        return new CuboidRegion(new BukkitWorld(skyWorld), region.getMinimumPoint(), region.getMaximumPoint());
    }
}
