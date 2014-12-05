package us.talabrek.ultimateskyblock;

import org.bukkit.Location;
import org.bukkit.plugin.*;
import org.bukkit.*;
import com.sk89q.worldedit.schematic.*;
import com.sk89q.worldedit.bukkit.*;
import com.sk89q.worldedit.data.*;
import java.io.*;
import com.sk89q.worldedit.*;

public class WorldEditHandler
{
    public static WorldEditPlugin getWorldEdit() {
        final Plugin plugin = uSkyBlock.getInstance().getServer().getPluginManager().getPlugin("WorldEdit");
        if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
            return null;
        }
        return (WorldEditPlugin)plugin;
    }
    
    public static boolean loadIslandSchematic(final World world, final File file, final Location origin) throws DataException, IOException, MaxChangedBlocksException {
        final Vector v = new Vector(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
        final SchematicFormat format = SchematicFormat.getFormat(file);
        if (format == null) {
            return false;
        }
        final EditSession es = new EditSession((LocalWorld)new BukkitWorld(world), 999999999);
        final CuboidClipboard cc = format.load(file);
        cc.paste(es, v, false);
        return true;
    }
}
