package us.talabrek.ultimateskyblock.handler;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiversePlugin;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.Settings;

/**
 * Wrapper for the MVCore plugin.
 */
public enum MultiverseCoreHandler {;
    public static MultiverseCore getMultiverseCore() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        if (plugin instanceof MultiversePlugin) {
            return ((MultiversePlugin)plugin).getCore();
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
            Location worldSpawn = new Location(skyWorld, 0, Settings.island_height, 0);
            if (!core.getMVWorldManager().isMVWorld(skyWorld)) {
                core.getMVWorldManager().addWorld(skyWorld.getName(), World.Environment.NORMAL, "0", WorldType.NORMAL, false, "uSkyBlock");
            }
            MultiverseWorld mvWorld = core.getMVWorldManager().getMVWorld(skyWorld);
            mvWorld.setEnvironment(World.Environment.NORMAL);
            mvWorld.setGenerator("uSkyBlock");
            if (Settings.general_spawnSize > 0) {
                if (isEmptyLocation(mvWorld.getSpawnLocation())) {
                    mvWorld.setSpawnLocation(worldSpawn);
                } else {
                    worldSpawn = mvWorld.getSpawnLocation();
                }
                Block spawnBlock = skyWorld.getBlockAt(worldSpawn);
                if (!spawnBlock.getType().isSolid()) {
                    spawnBlock.setType(Material.BEDROCK);
                }
            }
        } else if (hasMultiverse()) {
            if (!Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv import " + skyWorld.getName() + " NORMAL -g uSkyBlock")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv import " + skyWorld.getName() + " NORMAL uSkyBlock");
            }
        }
    }

    private static boolean isEmptyLocation(Location location) {
        return location == null || (location.getBlockX() == 0 && location.getBlockZ() == 0 && location.getBlockY() == 0);
    }
}
