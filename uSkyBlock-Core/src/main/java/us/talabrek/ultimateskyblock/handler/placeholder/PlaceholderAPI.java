package us.talabrek.ultimateskyblock.handler.placeholder;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Set;

/**
 *
 */
public interface PlaceholderAPI {
    interface PlaceholderReplacer {
        Set<String> getPlaceholders();

        String replace(OfflinePlayer offlinePlayer, Player player, String placeholder);
    }

    String replacePlaceholders(Player player, String message);

    boolean registerPlaceholder(uSkyBlock plugin, PlaceholderReplacer replacer);

    void unregisterPlaceholder(uSkyBlock plugin, PlaceholderReplacer placeholderReplacer);
}
