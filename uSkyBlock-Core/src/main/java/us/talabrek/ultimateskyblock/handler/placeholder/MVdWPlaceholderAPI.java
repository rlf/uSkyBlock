package us.talabrek.ultimateskyblock.handler.placeholder;

import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * MVdWPlaceholder proxy
 */
public class MVdWPlaceholderAPI implements PlaceholderAPI {
    private static final Logger log = Logger.getLogger(MVdWPlaceholderAPI.class.getName());

    public boolean isAvailable() {
        // Might not be enabled yet...
        return Bukkit.getPluginManager().getPlugin("MVdWPlaceholderAPI") != null;
    }

    @Override
    public boolean registerPlaceholder(uSkyBlock plugin, final PlaceholderReplacer replacer) {
        if (isAvailable()) {
            be.maximvdw.placeholderapi.PlaceholderReplacer proxy = new be.maximvdw.placeholderapi.PlaceholderReplacer() {
                @Override
                public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
                    log.info("Placeholder: " + e.getPlaceholder());
                    if (replacer.getPlaceholders().contains(e.getPlaceholder())) {
                        return replacer.replace(e.getOfflinePlayer(), e.getPlayer(), e.getPlaceholder());
                    }
                    return null;
                }
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
        for (Iterator<String> it = be.maximvdw.placeholderapi.PlaceholderAPI.getCustomPlaceholders().keySet().iterator(); it.hasNext(); ) {
            if (it.next().startsWith("usb_")) {
                it.remove();
            }
        }
    }

    @Override
    public String replacePlaceholders(Player player, String message) {
        if (isAvailable()) {
            return be.maximvdw.placeholderapi.PlaceholderAPI.replacePlaceholders(player, message);
        }
        return message;
    }
}
