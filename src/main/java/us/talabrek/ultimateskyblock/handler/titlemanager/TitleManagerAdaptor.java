package us.talabrek.ultimateskyblock.handler.titlemanager;

import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import org.bukkit.entity.Player;

/**
 * Adaptor for runtime (soft-depend) dependency on TitleManager
 */
public enum TitleManagerAdaptor {;
    public static void sendActionBar(Player player, String message) {
        new ActionbarTitleObject(message).send(player);
    }
}
