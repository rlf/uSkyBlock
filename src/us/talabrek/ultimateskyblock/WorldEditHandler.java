package us.talabrek.ultimateskyblock;

import java.io.File;
import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;

@SuppressWarnings("deprecation")
public class WorldEditHandler {
	public static WorldEditPlugin getWorldEdit() {
		final Plugin plugin = uSkyBlock.getInstance().getServer().getPluginManager().getPlugin("WorldEdit");

		if (plugin == null || !(plugin instanceof WorldEditPlugin)) { return null; }

		return (WorldEditPlugin) plugin;
	}

	@SuppressWarnings("deprecation")
	public static boolean loadIslandSchematic(final World world, final File file, final Location origin) throws DataException, IOException,
			MaxChangedBlocksException {
		final Vector v = new Vector(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
		final SchematicFormat format = SchematicFormat.getFormat(file);
		if (format == null) { return false; }
		final EditSession es = new EditSession(new BukkitWorld(world), 999999999);
		final CuboidClipboard cc = format.load(file);
		cc.paste(es, v, false);
		return true;
	}
}