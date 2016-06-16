package us.talabrek.ultimateskyblock.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerEvent;

/**
 * Common event for PlayerEvents that can be cancelled.
 * @since 2.7.0
 */
public abstract class CancellablePlayerEvent extends PlayerEvent implements Cancellable {
    private boolean cancelled = false;

    public CancellablePlayerEvent(Player who) {
        super(who);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
