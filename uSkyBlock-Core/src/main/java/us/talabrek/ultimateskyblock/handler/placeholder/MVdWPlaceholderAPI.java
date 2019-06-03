package us.talabrek.ultimateskyblock.handler.placeholder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.uSkyBlock;

/**
 * MVdWPlaceholder proxy
 */
public class MVdWPlaceholderAPI implements PlaceholderAPI {
    public boolean isAvailable() {
        // Might not be enabled yet...
        return Bukkit.getPluginManager().getPlugin("MVdWPlaceholderAPI") != null;
    }

    @Override
    public boolean registerPlaceholder(uSkyBlock plugin, final PlaceholderReplacer replacer) {
        if (isAvailable()) {
            be.maximvdw.placeholderapi.PlaceholderReplacer proxy = e -> {
                if (replacer.getPlaceholders().contains(e.getPlaceholder())) {
                    return replacer.replace(e.getOfflinePlayer(), e.getPlayer(), e.getPlaceholder());
                }
                return null;
            };
            for (String placeholder : replacer.getPlaceholders()) {
                be.maximvdw.placeholderapi.PlaceholderAPI.registerPlaceholder(plugin, placeholder, proxy);
            }
            return true;
        }
        return false;
    }

    @Override
    public void unregisterPlaceholder(uSkyBlock plugin, PlaceholderReplacer placeholderReplacer) {
        // Not implemented.
    }

    @Override
    public String replacePlaceholders(Player player, String message) {
        if (isAvailable()) {
            return be.maximvdw.placeholderapi.PlaceholderAPI.replacePlaceholders(player, message);
        }
        return message;
    }
}
