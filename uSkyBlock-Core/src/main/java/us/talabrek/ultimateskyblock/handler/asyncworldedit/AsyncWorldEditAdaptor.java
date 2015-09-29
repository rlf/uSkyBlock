package us.talabrek.ultimateskyblock.handler.asyncworldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.player.PlayerPerk;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.VersionUtil;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AsyncWorldEditAdaptor {
    private static final Logger log = Logger.getLogger(AsyncWorldEditAdaptor.class.getName());

    public static void onEnable(uSkyBlock plugin) {
        getAWEAdaptor().onEnable(plugin);
    }

    public static void onDisable(uSkyBlock plugin) {
        getAWEAdaptor().onDisable(plugin);
    }

    private static AsyncWorldEditMain getAWE() {
        return (AsyncWorldEditMain) Bukkit.getPluginManager().getPlugin("AsyncWorldEdit");
    }

    public static void registerCompletion(Player player) {
        getAWEAdaptor().registerCompletion(player);
    }

    public static EditSession createSession(BukkitWorld world, int maxblocks) {
        return WorldEditHandler.getWorldEdit().getWorldEdit().getEditSessionFactory().getEditSession(world, maxblocks);
    }

    public static void loadIslandSchematic(File file, Location origin, PlayerPerk playerPerk) {
        getAWEAdaptor().loadIslandSchematic(file, origin, playerPerk);
    }

    public static AWEAdaptor getAWEAdaptor() {
        AsyncWorldEditMain awe = getAWE();
        VersionUtil.Version version = VersionUtil.getVersion(awe.getDescription().getVersion());
        String className = null;
        if (version.isLT("3.0")) {
            className = "us.talabrek.ultimateskyblock.handler.asyncworldedit.AWE211Adaptor";
        } else {
            className = "us.talabrek.ultimateskyblock.handler.asyncworldedit.AWE311Adaptor";
        }
        try {
            return (AWEAdaptor) Class.forName(className).<AWEAdaptor>newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            log.log(Level.WARNING, "Unable to locate AWE adaptor for version " + version);
            return NULL_ADAPTOR;
        }
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

        }

        @Override
        public void onDisable(Plugin plugin) {

        }
    };
}
