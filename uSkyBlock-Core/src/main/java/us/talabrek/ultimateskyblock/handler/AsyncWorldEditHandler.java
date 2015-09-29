package us.talabrek.ultimateskyblock.handler;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.handler.asyncworldedit.AWEAdaptor;
import us.talabrek.ultimateskyblock.player.PlayerPerk;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.VersionUtil;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles integration with AWE.
 * Very HACKY and VERY unstable.
 *
 * Only kept as a cosmetic measure, to at least try to give the players some feedback.
 */
public enum AsyncWorldEditHandler {;
    private static final Logger log = Logger.getLogger(AsyncWorldEditHandler.class.getName());
    private static AWEAdaptor adaptor = null;

    public static void onEnable(uSkyBlock plugin) {
        getAWEAdaptor().onEnable(plugin);
    }

    public static void onDisable(uSkyBlock plugin) {
        getAWEAdaptor().onDisable(plugin);
    }

    public static void registerCompletion(Player player) {
        getAWEAdaptor().registerCompletion(player);
    }

    public static EditSession createEditSession(BukkitWorld world, int maxblocks) {
        return getAWEAdaptor().createEditSession(world, maxblocks);
    }

    public static void loadIslandSchematic(File file, Location origin, PlayerPerk playerPerk) {
        getAWEAdaptor().loadIslandSchematic(file, origin, playerPerk);
    }

    public static AWEAdaptor getAWEAdaptor() {
        if (adaptor == null) {
            Plugin awe = getAWE();
            VersionUtil.Version version = VersionUtil.getVersion(awe.getDescription().getVersion());
            String className = null;
            if (version.isLT("3.0")) {
                className = "us.talabrek.ultimateskyblock.handler.asyncworldedit.AWE211Adaptor";
            } else {
                className = "us.talabrek.ultimateskyblock.handler.asyncworldedit.AWE311Adaptor";
            }
            try {
                adaptor = (AWEAdaptor) Class.forName(className).<AWEAdaptor>newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                log.log(Level.WARNING, "Unable to locate AWE adaptor for version " + version);
                adaptor = NULL_ADAPTOR;
            }
        }
        return adaptor;
    }

    private static Plugin getAWE() {
        return Bukkit.getPluginManager().getPlugin("AsyncWorldEdit");
    }

    private static final AWEAdaptor NULL_ADAPTOR = new AWEAdaptor() {
        @Override
        public void onEnable(Plugin plugin) {

        }

        @Override
        public void registerCompletion(Player player) {

        }

        @Override
        public void loadIslandSchematic(File file, Location origin, PlayerPerk playerPerk) {
            WorldEditHandler.loadIslandSchematic(file, origin, playerPerk);
        }

        @Override
        public void onDisable(Plugin plugin) {

        }

        @Override
        public EditSession createEditSession(BukkitWorld world, int maxBlocks) {
            return WorldEditHandler.createEditSession(world, maxBlocks);
        }
    };
}
