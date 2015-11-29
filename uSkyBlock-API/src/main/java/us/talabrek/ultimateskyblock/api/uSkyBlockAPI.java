package us.talabrek.ultimateskyblock.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * The public API of the uSkyBlock plugin.
 *
 * This API will not be changed without prior notice - allowing other plugins a way to access
 * data from the uSkyBlock plugin.
 *
 * To get hold of an API object, make sure your plugin depends on uSkyBlock, and then do:
 * <pre>{@code
 *     uSkyBlockAPI api = Bukkit.getPluginManager().getPlugin("uSkyBlock");
 *     if (api != null && api.isEnabled()) {
 *         // Access the api here...
 *     } else {
 *         // Complain here
 *     }
 * }</pre>
 * @since v2.1.0
 */
@SuppressWarnings("UnusedDeclaration")
public interface uSkyBlockAPI extends Plugin {
    /**
     * Returns the currently registered score for the given player.
     * @param player The player to check.
     * @return The score, if available.
     * @since v2.1.0
     */
    double getIslandLevel(Player player);

    /**
     * Returns the current rank of the island of the supplied player.
     * @param player The player to query for
     * @since v2.3-HF2d
     * @return A rank-object for the given player, or <code>null</code> if none exist.
     */
    IslandRank getIslandRank(Player player);

    /**
     * Returns the rank-list.
     * @param offset A 0-based offset.
     * @param length The max number of records to return.
     * @return A list (possibly empty) of the entries within the designated range.
     * @since v2.1.0
     */
    List<IslandLevel> getRanks(int offset, int length);

    /**
     * Convenience method for #getRanks(0, 10).
     * @return The top-ten list of islands.
     * @since v2.1.0
     */
    List<IslandLevel> getTopTen();

    /**
     * Returns the island (or <code>null</code>) of the location supplied.
     * @param location A location in the skyworld.
     * @return The IslandRank or <code>null</code> of the given location.
     * @since v2.5.5
     */
    IslandRank getIslandRank(Location location);
}
