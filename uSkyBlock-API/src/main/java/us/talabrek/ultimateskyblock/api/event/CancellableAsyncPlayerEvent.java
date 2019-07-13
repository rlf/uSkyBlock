package us.talabrek.ultimateskyblock.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/**
 * Common event for PlayerEvents that can be cancelled (async).
 * @since 2.7.4
 */
public abstract class CancellableAsyncPlayerEvent extends Event implements Cancellable {
    private boolean cancelled = false;
    private Player player;

    public CancellableAsyncPlayerEvent(Player player) {
        super(true);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
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
