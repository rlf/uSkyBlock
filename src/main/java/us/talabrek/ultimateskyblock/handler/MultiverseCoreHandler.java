package us.talabrek.ultimateskyblock.handler;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiversePlugin;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.plugin.Plugin;

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
            if (!core.getMVWorldManager().isMVWorld(skyWorld)) {
                core.getMVWorldManager().addWorld(skyWorld.getName(), World.Environment.NORMAL, "0", WorldType.NORMAL, false, "uSkyBlock");
            } else {
                MultiverseWorld mvWorld = core.getMVWorldManager().getMVWorld(skyWorld);
                mvWorld.setEnvironment(World.Environment.NORMAL);
                mvWorld.setGenerator("uSkyBlock");
            }
        } else if (hasMultiverse()) {
            if (!Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv import " + skyWorld.getName() + " NORMAL -g uSkyBlock")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv import " + skyWorld.getName() + " NORMAL uSkyBlock");
            }
        }
    }
}
