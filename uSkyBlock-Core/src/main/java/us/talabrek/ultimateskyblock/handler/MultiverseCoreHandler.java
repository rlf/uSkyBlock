package us.talabrek.ultimateskyblock.handler;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.util.LocationUtil;

import static us.talabrek.ultimateskyblock.util.LocationUtil.centerOnBlock;

/**
 * Wrapper for the MVCore plugin.
 */
public enum MultiverseCoreHandler {;

    public static MultiverseCore getMultiverseCore() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        if (plugin instanceof MultiverseCore) {
            return (MultiverseCore) plugin;
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
                core.getMVWorldManager().addWorld(skyWorld.getName(), World.Environment.NORMAL, "0", WorldType.NORMAL, false, "uSkyBlock", false);
            }
            MultiverseWorld mvWorld = core.getMVWorldManager().getMVWorld(skyWorld);
            mvWorld.setEnvironment(World.Environment.NORMAL);
            mvWorld.setScaling(1);
            mvWorld.setGenerator("uSkyBlock");
            if (Settings.general_spawnSize > 0) {
                if (LocationUtil.isEmptyLocation(mvWorld.getSpawnLocation())) {
                    mvWorld.setAdjustSpawn(false);
                    Location spawn = centerOnBlock(worldSpawn);
                    mvWorld.setSpawnLocation(spawn);
                    skyWorld.setSpawnLocation(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());
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

    public static void importNetherWorld(World skyNetherWorld) {
        MultiverseCore core = getMultiverseCore();
        if (core != null) {
            Location worldSpawn = new Location(skyNetherWorld, 0.5, Settings.island_height / 2.0 + 0.1, 0.5);
            if (!core.getMVWorldManager().isMVWorld(skyNetherWorld)) {
                core.getMVWorldManager().addWorld(skyNetherWorld.getName(), World.Environment.NETHER, "0", WorldType.NORMAL, false, "uSkyBlock", false);
            }
            MultiverseWorld mvWorld = core.getMVWorldManager().getMVWorld(skyNetherWorld);
            mvWorld.setEnvironment(World.Environment.NETHER);
            mvWorld.setScaling(1.0);
            mvWorld.setGenerator("uSkyBlock");
            if (Settings.general_spawnSize > 0) {
                if (LocationUtil.isEmptyLocation(mvWorld.getSpawnLocation())) {
                    mvWorld.setAdjustSpawn(false);
                    mvWorld.setSpawnLocation(centerOnBlock(worldSpawn));
                    skyNetherWorld.setSpawnLocation(worldSpawn.getBlockX(), worldSpawn.getBlockY(), worldSpawn.getBlockZ());
                }
            }
            if (!Settings.extras_sendToSpawn) {
                mvWorld.setRespawnToWorld(Settings.general_worldName);
            }
        } else if (hasMultiverse()) {
            if (!Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv import " + skyNetherWorld.getName() + " NETHER -g uSkyBlock")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv import " + skyNetherWorld.getName() + " NETHER uSkyBlock");
            }
        }
    }
}
