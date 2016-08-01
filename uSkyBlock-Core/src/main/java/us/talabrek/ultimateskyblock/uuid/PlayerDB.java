package us.talabrek.ultimateskyblock.uuid;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Set;
import java.util.UUID;

/**
 * Simple abstraction, allowing for future DB support
 */
public interface PlayerDB extends Listener {
    UUID UNKNOWN_PLAYER_UUID = UUID.fromString("c1fc3ace-e6b2-37ed-a575-03e0d777d7f1");
    String UNKNOWN_PLAYER_NAME = "__UNKNOWN__";

    /**
     * Returns the UUID (if found) for the player we currently thinks has that name
     * @param name The name (not displayName) of a player.
     * @return
     */
    UUID getUUIDFromName(String name);

    /**
     * Returns the UUID (if found) for the player we currently thinks has that name
     * @param name The name (not displayName) of a player.
     * @param lookup Whether or not to ask Mojang for the UUID.
     * @return
     */
    UUID getUUIDFromName(String name, boolean lookup);

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

    /** Returns the last known UUID of the player with the given name.
     *
     * @param playerName The last known playername.
     * @return Either the displayName (if found) or the player-name.
     */
    String getDisplayName(String playerName);

    Set<String> getNames(String search);
    /**
     * Stores a new name in the DB.
     */
    void updatePlayer(UUID uuid, String name, String displayName);

    Player getPlayer(UUID uuid);
    Player getPlayer(String name);

    void shutdown();
}
