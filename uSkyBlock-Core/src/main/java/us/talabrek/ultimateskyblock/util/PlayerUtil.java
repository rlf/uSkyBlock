package us.talabrek.ultimateskyblock.util;

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
        if (playerDB != null) {
            return playerDB.getDisplayName(playerName);
        }
        return playerName;
    }

    public static void loadConfig(PlayerDB playerDB, FileConfiguration config) {
        PlayerUtil.playerDB = playerDB;
        skipDisplayName = config.getBoolean("options.advanced.useDisplayNames", false);
    }
}
