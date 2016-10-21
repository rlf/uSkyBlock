package us.talabrek.ultimateskyblock.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import us.talabrek.ultimateskyblock.api.IslandInfo;
import us.talabrek.ultimateskyblock.api.PlayerInfo;

/**
 * Fired when a player joins an island (async, not cancellable).
 * @since 2.7.3
 */
public class MemberJoinedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final IslandInfo islandInfo;
    private final PlayerInfo playerInfo;

    public MemberJoinedEvent(IslandInfo islandInfo, PlayerInfo playerInfo) {
        super(true);
        this.islandInfo = islandInfo;
        this.playerInfo = playerInfo;
    }

    public IslandInfo getIslandInfo() {
        return islandInfo;
    }

    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
