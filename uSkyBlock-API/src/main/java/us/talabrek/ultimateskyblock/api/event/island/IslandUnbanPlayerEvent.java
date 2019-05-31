package us.talabrek.ultimateskyblock.api.event.island;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.talabrek.ultimateskyblock.api.IslandInfo;

/**
 * Thrown when a player is unbanned from an island.
 * @implNote Until more fundamental changes to uSkyBlock are made, the user gets *NO* feedback that the unban event is
 * cancelled from us! The cancelling plugin should send a message to the initializer if feedback is expected.
 * @since 2.7.8
 */
public class IslandUnbanPlayerEvent extends CancellableIslandEvent {
    private static final HandlerList handlers = new HandlerList();
    private final OfflinePlayer target;
    private final OfflinePlayer initializer;

    public IslandUnbanPlayerEvent(@NotNull final IslandInfo islandInfo,
                                @NotNull final OfflinePlayer target,
                                @Nullable final OfflinePlayer initializer)
    {
        super(islandInfo);
        this.target = target;
        this.initializer = initializer;
    }

    /**
     * Gets the {@link OfflinePlayer} representing the unbanned player.
     * @return The {@link OfflinePlayer} representing the unbanned player.
     */
    @NotNull
    public OfflinePlayer getTarget() {
        return target;
    }

    /**
     * Gets the {@link OfflinePlayer} initializing the unban event, if available.
     * @return The {@link OfflinePlayer} initializing the unban event, or null if the initializer is unknown.
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
