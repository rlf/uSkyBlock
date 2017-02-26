package us.talabrek.ultimateskyblock.handler;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.handler.asyncworldedit.AWEAdaptor;
import us.talabrek.ultimateskyblock.handler.task.WEPasteSchematic;
import us.talabrek.ultimateskyblock.player.PlayerPerk;
import us.talabrek.ultimateskyblock.uSkyBlock;
import dk.lockfuglsang.minecraft.util.VersionUtil;

import java.io.File;
import java.util.logging.Level;

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

    public static EditSession createEditSession(World world, int maxblocks) {
        return getAWEAdaptor().createEditSession(world, maxblocks);
    }

    public static void loadIslandSchematic(File file, Location origin, PlayerPerk playerPerk) {
        new WEPasteSchematic(file, origin, playerPerk).runTask(uSkyBlock.getInstance());
    }

    public static void regenerate(Region region, Runnable onCompletion) {
        getAWEAdaptor().regenerate(region, onCompletion);
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
                className = "us.talabrek.ultimateskyblock.handler.asyncworldedit.FAWEAdaptor";
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

    public static Plugin getFAWE() {
        return Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit");
    }

    public static Plugin getAWE() {
        return Bukkit.getPluginManager().getPlugin("AsyncWorldEdit");
    }

    public static final AWEAdaptor NULL_ADAPTOR = new AWEAdaptor() {
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

        @Override
        public void regenerate(final Region region, final Runnable onCompletion) {
            uSkyBlock.getInstance().sync(new Runnable() {
                @Override
                public void run() {
                    try {
                        final EditSession editSession = WorldEditHandler.createEditSession(region.getWorld(), region.getArea() * 255);
                        editSession.enableQueue();
                        editSession.setFastMode(true);
                        editSession.getWorld().regenerate(region, editSession);
                        editSession.flushQueue();
                    } finally {
                        onCompletion.run();
                    }
                }
            });
        }
    };


}
