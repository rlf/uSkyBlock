package us.talabrek.ultimateskyblock.handler.asyncworldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.player.PlayerPerk;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;

import static us.talabrek.ultimateskyblock.util.LogUtil.log;

/**
 * Adaptor depending on FAWE 3.5.0 classes
 */
public class FAWE350Adaptor implements AWEAdaptor {
    private Plugin plugin;
    private Object fawe;

    @Override
    public void onEnable(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onDisable(Plugin plugin) {
    }

    @Override
    public void registerCompletion(Player player) {
    }

    @Override
    public void loadIslandSchematic(final File file, final Location origin, final PlayerPerk playerPerk) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                boolean noAir = false;
                boolean entities = true;
                Vector to = new Vector(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
                EditSession editSession = createEditSession(BukkitUtil.getLocalWorld(origin.getWorld()), -1);
                try {
                    SchematicFormat.getFormat(file)
                            .load(file)
                            .paste(editSession, to, noAir, entities);
                    editSession.flushQueue();
                } catch (MaxChangedBlocksException | IOException | DataException e) {
                    log(Level.INFO, "Unable to paste schematic " + file, e);
                }
            }
        });
    }

    public EditSession createEditSession(World bukkitWorld, int maxBlocks) {
        // 3.5.0 comes in both a static and non-static version... sigh
        try {
            Object api = getAPI();
            Method newSessionMethod = api.getClass().getMethod("getNewEditSession", World.class);
            if (Modifier.isStatic(newSessionMethod.getModifiers())) {
                return (EditSession) newSessionMethod.invoke(null, bukkitWorld);
            } else {
                return (EditSession) newSessionMethod.invoke(api, bukkitWorld);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log(Level.WARNING, "Incompatible FAWE detected!", e);
        }
        return WorldEditHandler.createEditSession(bukkitWorld, maxBlocks);
    }

    private Object getAPI() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit");
        if (plugin != null && plugin.isEnabled()) {
            if (fawe == null) {
                try {
                    fawe = Class.forName("com.boydti.fawe.FaweAPI").newInstance();
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                    log(Level.WARNING, "Incompatible FAWE detected!", e);
                }
            }
            return fawe;
        }
        return null;
    }
}
