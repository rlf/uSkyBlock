package us.talabrek.ultimateskyblock.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a player accepts an invite.
 * @since 2.7.0
 */
public class AcceptEvent extends CancellablePlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    public AcceptEvent(Player who) {
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
