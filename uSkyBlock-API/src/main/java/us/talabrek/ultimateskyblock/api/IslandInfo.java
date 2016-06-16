package us.talabrek.ultimateskyblock.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

/**
 * Public API for an island.
 * @since 2.6.2
 */
public interface IslandInfo {
    /**
     * Returns the maximum number of members this island can have.
     * @return the maximum number of members this island can have.
     */
    int getMaxPartySize();

    /**
     * Returns the maximum number of animals that can spawn on this island.
     *
     * Note: Only enforced if <pre>spawn-limits</pre> are enabled in <pre>config.yml</pre>
     * @return the maximum number of animals that can spawn on this island.
     */
    int getMaxAnimals();

    /**
     * Returns the maximum number of monsters (including squids) that can spawn on this island.
     *
     * Note: Only enforced if <pre>spawn-limits</pre> are enabled in <pre>config.yml</pre>
     * @return the maximum number of monsters that can spawn on this island.
     */
    int getMaxMonsters();

    /**
     * Returns the maximum number of villagers that can spawn on this island.
     *
     * Note: Only enforced if <pre>spawn-limits</pre> are enabled in <pre>config.yml</pre>
     * @return the maximum number of villagers that can spawn on this island.
     */
    int getMaxVillagers();

    /**
     * Returns the maximum number of golems (snowmen and iron-golems) that can spawn on this island.
     *
     * Note: Only enforced if <pre>spawn-limits</pre> are enabled in <pre>config.yml</pre>
     * @return the maximum number of golems that can spawn on this island.
     */
    int getMaxGolems();

    /**
     * Returns the player-name of the island-leader.
     * @return the player-name of the island-leader.
     */
    String getLeader();

    /**
     * The player-names of all the island-members (including the leader).
     * @return The player-names of all the island-members (including the leader).
     */
    Set<String> getMembers();

    /**
     * The name of the biome.
     * @return The name of the biome.
     */
    String getBiome();

    /**
     * The current party-size of the island.
     * @return The current party-size of the island.
     */
    int getPartySize();

    /**
     * True iff the player is the leader of this island.
     * @param player The player to query for
     * @return True iff the player is the leader of this island.
     */
    boolean isLeader(Player player);

    String getName();

    /**
     * True iff the player has been banned from this island.
     * @param player The player to query for
     * @return True iff the player has been banned from this island.
     */
    boolean isBanned(Player player);

    /**
     * List of players banned from this island.
     * @return List of players banned from this island.
     */
    List<String> getBans();

    /**
     * List of players trusted on this island.
     * @return List of players trusted on this island.
     */
    List<String> getTrustees();

    /**
     * The currently registered level of this island.
     * @return The currently registered level of this island.
     */
    double getLevel();

    /**
     * The latest event-log for the island.
     * Note: Sorted in inverse chronological order
     * @return The latest event-log for the island.
     */
    List<String> getLog();

    /**
     * True if this island constitutes a party (more than 1 member).
     * @return True if this island constitutes a party (more than 1 member).
     */
    boolean isParty();

    /**
     * The (possibly <code>null</code>) location of the island-warp.
     * @return The (possibly <code>null</code>) location of the island-warp.
     */
    Location getWarpLocation();

    /**
     * The location of the island.
     * Should never be null, but if data is corrupt, it might be.
     * @return The location of the island.
     */
    Location getIslandLocation();

    /**
     * True if at least one member of the island is online.
     * @return True if at least one member of the island is online.
     */
    boolean hasOnlineMembers();

    /**
     * List of members currently online.
     * @return List of members currently online.
     */
    List<Player> getOnlineMembers();

    /**
     * Checks whether a location is within the island borders.
     * @param loc The location to test for.
     * @return Checks whether a location is within the island borders.
     */
    boolean contains(Location loc);

    /**
     * Returns the current schematic-name for the island).
     * @return Returns the current schematic-name for the island).
     */
    String getSchematicName();

    /**
     * Returns a score-multiplier for this island
     * @return a score-multiplier for this island (default 1).
     * @since v2.6.13
     */
    double getScoreMultiplier();

    /**
     * Returns a score-offset for this island
     * @return a score-offset for this island (default 1).
     * @since v2.6.13
     */
    double getScoreOffset();
}
