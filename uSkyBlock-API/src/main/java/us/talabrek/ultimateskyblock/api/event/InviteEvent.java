package us.talabrek.ultimateskyblock.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import us.talabrek.ultimateskyblock.api.IslandInfo;

/**
 * Fired when a player tries to invite another player to her island.
 * @since 2.7.0
 */
public class InviteEvent extends CancellablePlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Player guest;
    private final IslandInfo islandInfo;

    public InviteEvent(Player who, IslandInfo islandInfo, Player guest) {
        super(who);
        this.islandInfo = islandInfo;
        this.guest = guest;
    }

    public Player getGuest() {
        return guest;
    }

    public IslandInfo getIslandInfo() {
        return islandInfo;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
