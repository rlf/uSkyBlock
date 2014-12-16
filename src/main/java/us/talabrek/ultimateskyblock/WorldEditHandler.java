package us.talabrek.ultimateskyblock;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.plugin.*;
import org.bukkit.*;
import com.sk89q.worldedit.schematic.*;
import com.sk89q.worldedit.bukkit.*;
import com.sk89q.worldedit.data.*;

import java.io.*;
import java.util.logging.Level;

import com.sk89q.worldedit.*;

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

    public static void clearIsland(final World skyWorld, final ProtectedRegion region) {
        long t = System.currentTimeMillis();
        long diff = 0;
        Region cube = getRegion(skyWorld, region);
        for (Vector2D chunk : cube.getChunks()) {
            skyWorld.regenerateChunk(chunk.getBlockX(), chunk.getBlockZ());
            skyWorld.refreshChunk(chunk.getBlockX(), chunk.getBlockZ());
        }
        diff = System.currentTimeMillis() - t;
        uSkyBlock.log(Level.INFO, String.format("Refreshed island in %d.%03d seconds", (diff / 1000), (diff % 1000)));
    }

    private static Region getRegion(World skyWorld, ProtectedRegion region) {
        return new CuboidRegion(new BukkitWorld(skyWorld), region.getMinimumPoint(), region.getMaximumPoint());
    }
}
