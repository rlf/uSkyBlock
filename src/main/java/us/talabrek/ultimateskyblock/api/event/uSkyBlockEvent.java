package us.talabrek.ultimateskyblock.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import us.talabrek.ultimateskyblock.api.uSkyBlockAPI;

/**
 * Event fired when changes occur in the uSkyBlock plugin that might be of interest to
 * other plugins.
 *
 * <b>Note:</b> is fired asynchronously.
 */
public class uSkyBlockEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final uSkyBlockAPI api;
    private final Cause cause;

    public uSkyBlockEvent(Player player, uSkyBlockAPI api, Cause cause) {
        super(true);
        this.player = player;
        this.api = api;
        this.cause = cause;
    }

    /**
     * Returns the player involved/triggering the update.
     * <b>Note:</b> May be <code>null</code>.
     * @return the player involved/triggering the update.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns a handle to the API - can be used to retrieve state-changes.
     * <b>Note:</b> Cannot be null, but can be disabled.
     * @return a handle to the API - can be used to retrieve state-changes.
     */
    public uSkyBlockAPI getAPI() {
        return api;
    }

    /**
     * Returns the cause of the event.
     * May be used to decide whether any actions are required.
     * <b>Note:</b> Cannot be null.
     * @return the cause of the event.
     */
    public Cause getCause() {
        return cause;
    }

    /**
     * Returns the handlers listening to this event.
     * @return the handlers listening to this event.
     */
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

    public enum Cause { RANK_UPDATED, SCORE_CHANGED }
}
