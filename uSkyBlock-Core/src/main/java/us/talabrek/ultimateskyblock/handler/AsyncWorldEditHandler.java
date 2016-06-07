package us.talabrek.ultimateskyblock.handler;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.handler.asyncworldedit.AWEAdaptor;
import us.talabrek.ultimateskyblock.handler.task.WEPasteSchematic;
import us.talabrek.ultimateskyblock.player.PlayerPerk;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.VersionUtil;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import static us.talabrek.ultimateskyblock.util.LogUtil.log;

/**
 * Handles integration with AWE.
 * Very HACKY and VERY unstable.
 *
 * Only kept as a cosmetic measure, to at least try to give the players some feedback.
 */
public enum AsyncWorldEditHandler {;
    private static AWEAdaptor adaptor = null;

    public static void onEnable(uSkyBlock plugin) {
        getAWEAdaptor().onEnable(plugin);
    }

    public static void onDisable(uSkyBlock plugin) {
        getAWEAdaptor().onDisable(plugin);
        adaptor = null;
    }

    public static void registerCompletion(Player player) {
        getAWEAdaptor().registerCompletion(player);
    }

    public static EditSession createEditSession(BukkitWorld world, int maxblocks) {
        return getAWEAdaptor().createEditSession(world, maxblocks);
    }

    public static void loadIslandSchematic(File file, Location origin, PlayerPerk playerPerk) {
        new WEPasteSchematic(file, origin, playerPerk).runTask(uSkyBlock.getInstance());
    }

    public static AWEAdaptor getAWEAdaptor() {
        if (adaptor == null) {
            if (!uSkyBlock.getInstance().getConfig().getBoolean("asyncworldedit.enabled", true)) {
                return NULL_ADAPTOR;
            }
            Plugin fawe = getFAWE();
            Plugin awe = getAWE();
            String className = null;
            if (fawe != null) {
                VersionUtil.Version version = VersionUtil.getVersion(fawe.getDescription().getVersion());
                if (version.isLT("3.5.0")) {
                    className = "us.talabrek.ultimateskyblock.handler.asyncworldedit.FAWEAdaptor";
                } else {
                    className = "us.talabrek.ultimateskyblock.handler.asyncworldedit.FAWE350Adaptor";
                }
                try {
                    adaptor = (AWEAdaptor) Class.forName(className).<AWEAdaptor>newInstance();
                    log(Level.INFO, "Hooked into FAWE " + version);
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoClassDefFoundError e) {
                    log(Level.WARNING, "Unable to locate FAWE adaptor for version " + version + ": " + e);
                    adaptor = NULL_ADAPTOR;
                }
            } else if (awe != null) {
                VersionUtil.Version version = VersionUtil.getVersion(awe.getDescription().getVersion());
                if (version.isLT("3.0")) {
                    className = "us.talabrek.ultimateskyblock.handler.asyncworldedit.AWE211Adaptor";
                } else if (version.isLT("3.2.0")) {
                    className = "us.talabrek.ultimateskyblock.handler.asyncworldedit.AWE311Adaptor";
                } else if (version.isLT("3.3.0")) {
                    className = "us.talabrek.ultimateskyblock.handler.asyncworldedit.AWE321Adaptor";
                } else { // Just HOPE to GOD it soon becomes backward compatible...
                    className = "us.talabrek.ultimateskyblock.handler.asyncworldedit.AWE330Adaptor";
                }
                try {
                    adaptor = (AWEAdaptor) Class.forName(className).<AWEAdaptor>newInstance();
                    log(Level.INFO, "Hooked into AWE " + version);
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoClassDefFoundError e) {
                    log(Level.WARNING, "Unable to locate AWE adaptor for version " + version + ": " + e);
                    adaptor = NULL_ADAPTOR;
                }
            } else {
                adaptor = NULL_ADAPTOR;
            }
        }
        return adaptor;
    }

    public static boolean isAWE() {
        return getAWEAdaptor() != NULL_ADAPTOR;
    }

    private static Plugin getFAWE() {
        return Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit");
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
        public EditSession createEditSession(World world, int maxBlocks) {
            return WorldEditHandler.createEditSession(world, maxBlocks);
        }
    };
}
