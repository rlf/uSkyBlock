package us.talabrek.ultimateskyblock.handler.placeholder;

import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import org.bukkit.Bukkit;
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
            be.maximvdw.placeholderapi.PlaceholderReplacer proxy = new be.maximvdw.placeholderapi.PlaceholderReplacer() {
                @Override
                public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
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
}
