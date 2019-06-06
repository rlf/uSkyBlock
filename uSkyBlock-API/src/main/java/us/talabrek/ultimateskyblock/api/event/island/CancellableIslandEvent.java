package us.talabrek.ultimateskyblock.api.event.island;

import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import us.talabrek.ultimateskyblock.api.IslandInfo;

/**
 * Represents a cancellable island related event.
 * @since 2.7.7
 */
public abstract class CancellableIslandEvent extends IslandEvent implements Cancellable {
    private boolean cancelled = false;

    public CancellableIslandEvent(@NotNull final IslandInfo islandInfo) {
        super(islandInfo);
    }

    CancellableIslandEvent(@NotNull final IslandInfo islandInfo, boolean async) {
        super(islandInfo, async);
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
