package us.talabrek.ultimateskyblock.handler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.handler.actionbarapi.ActionBarAPIAdaptor;

/**
 * Static handler allowing for soft-depend.
 */
public enum ActionBarHandler {;

    public static boolean isEnabled() {
        return isActionBarAPI();
    }

    public static boolean isActionBarAPI() {
        return Bukkit.getPluginManager().isPluginEnabled("ActionBarAPI");
    }

    public static void sendActionBar(Player player, String message) {
        try {
            if (isActionBarAPI()) {
                ActionBarAPIAdaptor.sendActionBar(player, message);
            } else {
                sendFallback(player, message);
            }
        } catch (Exception e) {
            // Suppress incompatibilities - this is just a "best-effort" approach.
            sendFallback(player, message);
        }
    }

    private static void sendFallback(Player player, String message) {
        if (player != null) {
            player.sendMessage(message);
        }
    }
}
