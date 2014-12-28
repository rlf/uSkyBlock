package us.talabrek.ultimateskyblock.handler;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiversePlugin;
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

    public static void importWorld(World skyWorld) {
        MultiverseCore core = getMultiverseCore();
        if (core != null) {
            if (!core.getMVWorldManager().isMVWorld(skyWorld)) {
                core.getMVWorldManager().addWorld(skyWorld.getName(), World.Environment.NORMAL, null, WorldType.NORMAL, false, "uSkyBlock");
            }
        }
    }
}
