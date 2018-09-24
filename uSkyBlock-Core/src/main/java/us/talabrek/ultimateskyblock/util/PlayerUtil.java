package us.talabrek.ultimateskyblock.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.uuid.PlayerDB;

import java.util.List;

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

    public static String getMetadata(Player player, String key, String defaultValue) {
        if (player.hasMetadata(key) && !player.getMetadata(key).isEmpty()) {
            List<MetadataValue> metadata = player.getMetadata(key);
            return metadata.get(0).asString();
        }
        return defaultValue;
    }

    public static void setMetadata(Player player, String key, String value) {
        player.setMetadata(key, new FixedMetadataValue(uSkyBlock.getInstance(), value));
    }

    public static void loadConfig(PlayerDB playerDB, FileConfiguration config) {
        PlayerUtil.playerDB = playerDB;
        skipDisplayName = config.getBoolean("options.advanced.useDisplayNames", false);
    }
}
