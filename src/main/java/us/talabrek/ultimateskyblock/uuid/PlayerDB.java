package us.talabrek.ultimateskyblock.uuid;

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
     * Stores a new name in the DB.
     * @param uuid The UUID of the player.
     * @param name The current name.
     */
    void setName(UUID uuid, String name) throws IOException;
}
