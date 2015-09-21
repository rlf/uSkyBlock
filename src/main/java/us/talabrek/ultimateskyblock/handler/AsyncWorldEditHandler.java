package us.talabrek.ultimateskyblock.handler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.handler.asyncworldedit.AsyncWorldEditAdaptor;
import us.talabrek.ultimateskyblock.uSkyBlock;

/**
 * Handles integration with AWE - note must be put done in a way, that can handle if it's not available!
 */
public enum AsyncWorldEditHandler {;

    public static void onEnable(uSkyBlock plugin) {
        if (isAWE()) {
            AsyncWorldEditAdaptor.onEnable(plugin);
        }
    }

    public static void onDisable(uSkyBlock plugin) {
        if (isAWE()) {
            AsyncWorldEditAdaptor.onDisable(plugin);
        }
    }

    public static boolean isAWE() {
        return Bukkit.getPluginManager().isPluginEnabled("AsyncWorldEdit");
    }

    public static void registerCompletion(Player player, Runnable onCompletion) {
        if (isAWE()) {
            AsyncWorldEditAdaptor.registerCompletion(player, onCompletion);
        } else {
            Bukkit.getScheduler().runTaskLater(uSkyBlock.getInstance(), onCompletion, 5);
        }
    }
}
