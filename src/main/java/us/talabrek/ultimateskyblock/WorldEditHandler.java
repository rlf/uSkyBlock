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

    public static boolean clearIsland(World skyWorld, ProtectedRegion region) {
        Region cube = getRegion(skyWorld, region);
        EditSession session = new EditSession(new BukkitWorld(skyWorld), cube.getArea());
        try {
            session.setBlocks(cube, new BaseBlock(0));
            return true;
        } catch (MaxChangedBlocksException e) {
            uSkyBlock.log(Level.SEVERE, "Unable to clear island", e);
            return false;
        }
    }

    private static Region getRegion(World skyWorld, ProtectedRegion region) {
        return new CuboidRegion(new BukkitWorld(skyWorld), region.getMinimumPoint(), region.getMaximumPoint());
    }
}
