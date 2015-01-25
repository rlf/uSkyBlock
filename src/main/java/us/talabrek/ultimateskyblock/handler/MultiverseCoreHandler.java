package us.talabrek.ultimateskyblock.handler;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiversePlugin;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.util.LocationUtil;

/**
 * Wrapper for the MVCore plugin.
 */
public enum MultiverseCoreHandler {;
    public static MultiverseCore getMultiverseCore() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        if (plugin instanceof MultiverseCore) {
            return (MultiverseCore)plugin;
        }
        return null;
    }

    public static boolean hasMultiverse() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        return plugin != null && plugin.isEnabled();
    }

    public static void importWorld(World skyWorld) {
        MultiverseCore core = getMultiverseCore();
        if (core != null) {
            Location worldSpawn = new Location(skyWorld, 0.5, Settings.island_height + 0.1, 0.5);
            if (!core.getMVWorldManager().isMVWorld(skyWorld)) {
                core.getMVWorldManager().addWorld(skyWorld.getName(), World.Environment.NORMAL, "0", WorldType.NORMAL, false, "uSkyBlock");
            }
            MultiverseWorld mvWorld = core.getMVWorldManager().getMVWorld(skyWorld);
            mvWorld.setEnvironment(World.Environment.NORMAL);
            mvWorld.setGenerator("uSkyBlock");
            if (Settings.general_spawnSize > 0) {
                if (LocationUtil.isEmptyLocation(mvWorld.getSpawnLocation())) {
                    mvWorld.setAdjustSpawn(false);
                    mvWorld.setSpawnLocation(worldSpawn);
                }
            }
            if (!Settings.extras_sendToSpawn) {
                mvWorld.setRespawnToWorld(mvWorld.getName());
            }
        } else if (hasMultiverse()) {
            if (!Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv import " + skyWorld.getName() + " NORMAL -g uSkyBlock")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv import " + skyWorld.getName() + " NORMAL uSkyBlock");
            }
        }
    }

}
