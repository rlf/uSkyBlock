package us.talabrek.ultimateskyblock.handler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.handler.actionbarapi.ActionBarAPIAdaptor;

/**
 * Static handler allowing for soft-depend.
 */
public enum ActionBarHandler {;
    public static boolean isEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("ActionBarAPI");
    }

    public static void sendActionBar(Player player, String message) {
        if (isEnabled()) {
            ActionBarAPIAdaptor.sendActionBar(player, message);
        }
    }
}
