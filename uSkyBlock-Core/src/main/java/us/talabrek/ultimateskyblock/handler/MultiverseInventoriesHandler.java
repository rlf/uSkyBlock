package us.talabrek.ultimateskyblock.handler;

import org.bukkit.Bukkit;
import org.bukkit.World;
import us.talabrek.ultimateskyblock.handler.multiverseinventories.MultiverseInventoriesAdaptor;

/**
 * Handler for accessing Multiverse-Inventories, if enabled.
 */
public enum MultiverseInventoriesHandler {;
    public static void linkWorlds(World... worlds) {
        if (isMVInv()) {
            MultiverseInventoriesAdaptor.linkWorlds(worlds);
        }
    }
    public static boolean isMVInv() {
        return Bukkit.getPluginManager().isPluginEnabled("Multiverse-Inventories");
    }
}
