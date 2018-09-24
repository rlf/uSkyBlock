package us.talabrek.ultimateskyblock.api.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import us.talabrek.ultimateskyblock.api.model.IslandScore;
import us.talabrek.ultimateskyblock.api.uSkyBlockAPI;

/**
 * Fired when a player updates his island-score.
 */
public class uSkyBlockScoreChangedEvent extends uSkyBlockEvent {
    private static final HandlerList handlers = new HandlerList();

    private final IslandScore score;
    private final Location islandLocation;

    public uSkyBlockScoreChangedEvent(Player player, uSkyBlockAPI api, IslandScore score, Location islandLocation) {
        super(player, api, Cause.SCORE_CHANGED);
        this.score = score;
        this.islandLocation = islandLocation;
    }

    /**
     * Returns the score that changed.
     * @return the score that changed.
     */
    public IslandScore getScore() {
        return score;
    }

    /**
     * Returns the island location
     * @return the island location
     * @since v2.7.4
     */
    public Location getIslandLocation() {
        return islandLocation;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Returns the handlers listening to this event.
     * Required for Bukkit-events.
     * @return the handlers listening to this event.
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
