package us.talabrek.ultimateskyblock.api.event.island;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.talabrek.ultimateskyblock.api.IslandInfo;

/**
 * Fired when an island is locked.
 * @implNote Until more fundamental changes to uSkyBlock are made, the user gets *NO* feedback that
 * the lock event is cancelled from us! The cancelling plugin should send a message to the initializer
 * if feedback is expected.
 * @since 2.8.4
 */
public class IslandLockEvent extends CancellableIslandEvent {
    private static final HandlerList handlers = new HandlerList();
    private final OfflinePlayer initializer;

    public IslandLockEvent(@NotNull IslandInfo islandInfo, @Nullable OfflinePlayer initializer) {
        super(islandInfo);
        this.initializer = initializer;
    }

    /**
     * Gets the {@link OfflinePlayer} initializing the lock event, if available.
     * @return The {@link OfflinePlayer} initializing the trust event, or null if the initializer is unknown.
     */
    @Nullable
    public OfflinePlayer getInitializer() {
        return initializer;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
