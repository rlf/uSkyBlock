package us.talabrek.ultimateskyblock.uuid;

import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.UUID;

/**
 * Simple abstraction, allowing for future DB support
 */
public interface PlayerDB {
    /**
     * Returns the current name from the DB.
     * @param uuid The UUID of the player.
     * @return the current name from the DB.
     */
    String getName(UUID uuid);

    /**
     * Returns the last known DisplayName of the player.
     * @param uuid The UUID of the player.
     * @return the last known DisplayName of the player.
     */
    String getDisplayName(UUID uuid);

    /**
     * Stores a new name in the DB.
     * @param name The current name.
     */
    void updatePlayer(Player name) throws IOException;
}
