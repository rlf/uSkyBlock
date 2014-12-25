package us.talabrek.ultimateskyblock;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
//import com.sk89q.worldedit.extent.clipboard.Clipboard;
//import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
//import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
//import com.sk89q.worldedit.function.operation.Operation;
//import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.schematic.SchematicFormat;
//import com.sk89q.worldedit.session.ClipboardHolder;
//import com.sk89q.worldedit.world.registry.WorldData;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import org.bukkit.Location;
import org.bukkit.World;
//import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.logging.Level;

public class WorldEditHandler {
    public static WorldEditPlugin getWorldEdit() {
        final Plugin plugin = uSkyBlock.getInstance().getServer().getPluginManager().getPlugin("WorldEdit");
        if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
            return null;
        }
        return (WorldEditPlugin) plugin;
    }

/*    public static boolean loadIslandSchematic(Player player, final World world, final File file, final Location origin) {
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
    }*/
    
//	@SuppressWarnings("deprecation")
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
