package us.talabrek.ultimateskyblock;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import java.io.File;
import java.io.IOException;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class WorldEditHandler
{
  public WorldEditHandler() {}
  
  public static WorldEditPlugin getWorldEdit()
  {
    Plugin plugin = uSkyBlock.getInstance().getServer().getPluginManager().getPlugin("WorldEdit");
    if ((plugin == null) || (!(plugin instanceof WorldEditPlugin))) {
      return null;
    }
    return (WorldEditPlugin)plugin;
  }
  
  public static boolean loadIslandSchematic(World world, File file, Location origin)
    throws DataException, IOException, MaxChangedBlocksException
  {
    Vector v = new Vector(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
    SchematicFormat format = SchematicFormat.getFormat(file);
    if (format == null) {
      return false;
    }
    EditSession es = new EditSession(new BukkitWorld(world), 999999999);
    CuboidClipboard cc = format.load(file);
    cc.paste(es, v, false);
    return true;
  }
}
