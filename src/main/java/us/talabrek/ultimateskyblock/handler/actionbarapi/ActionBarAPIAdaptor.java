package us.talabrek.ultimateskyblock.handler.actionbarapi;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import org.bukkit.entity.Player;

/**
 * Runtime adaptor.
 */
public enum ActionBarAPIAdaptor {;
    public static void sendActionBar(Player player, String message) {
        ActionBarAPI.sendActionBar(player, message);
    }
}
