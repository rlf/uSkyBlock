package us.talabrek.ultimateskyblock.api.event;

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

    public uSkyBlockScoreChangedEvent(Player player, uSkyBlockAPI api, IslandScore score) {
        super(player, api, Cause.SCORE_CHANGED);
        this.score = score;
    }

    /**
     * Returns the score that changed.
     * @return the score that changed.
     */
    public IslandScore getScore() {
        return score;
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
