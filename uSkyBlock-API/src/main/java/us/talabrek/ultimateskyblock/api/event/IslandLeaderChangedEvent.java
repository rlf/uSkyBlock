package us.talabrek.ultimateskyblock.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import us.talabrek.ultimateskyblock.api.IslandInfo;
import us.talabrek.ultimateskyblock.api.PlayerInfo;

/**
 * Fired on island leader change (e.g. when a leader uses /island makeleader). Async, not cancellable.
 * @since 2.7.5
 */
public class IslandLeaderChangedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final IslandInfo islandInfo;
    private final PlayerInfo originalLeaderInfo;
    private final PlayerInfo newLeaderInfo;

    public IslandLeaderChangedEvent(IslandInfo islandInfo, PlayerInfo originalLeaderInfo, PlayerInfo newLeaderInfo) {
        super(true);
        this.islandInfo = islandInfo;
        this.originalLeaderInfo = originalLeaderInfo;
        this.newLeaderInfo = newLeaderInfo;
    }

    public IslandInfo getIslandInfo() {
        return islandInfo;
    }

    public PlayerInfo getOriginalLeaderInfo() {
        return originalLeaderInfo;
    }

    public PlayerInfo getNewLeaderInfo() {
        return newLeaderInfo;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
