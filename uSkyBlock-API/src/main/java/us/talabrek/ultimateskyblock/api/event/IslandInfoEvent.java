package us.talabrek.ultimateskyblock.api.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import us.talabrek.ultimateskyblock.api.async.Callback;
import us.talabrek.ultimateskyblock.api.model.IslandScore;

/**
 * @since v2.7.4
 */
public class IslandInfoEvent extends CancellableAsyncPlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Location islandLocation;
    private final Callback<IslandScore> callback;

    public IslandInfoEvent(Player who, Location islandLocation, Callback<IslandScore> callback) {
        super(who);
        this.islandLocation = islandLocation;
        this.callback = callback;
    }

    public Location getIslandLocation() {
        return islandLocation;
    }

    public Callback<IslandScore> getCallback() {
        return callback;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
