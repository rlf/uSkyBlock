package us.talabrek.ultimateskyblock.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import us.talabrek.ultimateskyblock.uuid.PlayerDB;

/**
 * Wrappers for most player-related functionality.
 */
public enum PlayerUtil {;
    private static boolean skipDisplayName = false;
    private static PlayerDB playerDB;

    public static String getPlayerDisplayName(String playerName) {
        if (skipDisplayName) {
            return playerName;
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (offlinePlayer != null && offlinePlayer.getPlayer() != null && offlinePlayer.getPlayer().getDisplayName() != null) {
            return offlinePlayer.getPlayer().getDisplayName();
        } else if (offlinePlayer != null && playerDB != null) {
            String displayName = playerDB.getDisplayName(offlinePlayer.getUniqueId());
            if (displayName != null) {
                return displayName;
            }
        }
        return playerName;
    }

    public static void loadConfig(PlayerDB playerDB, FileConfiguration config) {
        PlayerUtil.playerDB = playerDB;
        skipDisplayName = config.getBoolean("options.advanced.disableDisplayNames", false);
    }
}
