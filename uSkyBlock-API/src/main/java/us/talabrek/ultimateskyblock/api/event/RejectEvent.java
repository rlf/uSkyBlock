package us.talabrek.ultimateskyblock.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a player rejects an invite.
 * @since 2.7.0
 */
public class RejectEvent extends CancellablePlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    public RejectEvent(Player who) {
        super(who);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
