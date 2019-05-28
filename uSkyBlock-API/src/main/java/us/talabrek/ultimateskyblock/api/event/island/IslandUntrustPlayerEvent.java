package us.talabrek.ultimateskyblock.api.event.island;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.talabrek.ultimateskyblock.api.IslandInfo;

/**
 * Thrown when a player is untrusted on an island.
 * @implNote Until more fundamental changes to uSkyBlock are made, the user gets *NO* feedback that the untrust event is
 * cancelled from us! The cancelling plugin should send a message to the initializer if feedback is expected.
 * @since 2.7.7
 */
public class IslandUntrustPlayerEvent extends CancellableIslandEvent {
    private static final HandlerList handlers = new HandlerList();
    private final OfflinePlayer trustee;
    private final OfflinePlayer initializer;

    public IslandUntrustPlayerEvent(@NotNull final IslandInfo islandInfo,
                                    @NotNull final OfflinePlayer trustee,
                                    @Nullable final OfflinePlayer initializer)
    {
        super(islandInfo);
        this.trustee = trustee;
        this.initializer = initializer;
    }

    /**
     * Gets the {@link OfflinePlayer} associated with the new trustee.
     * @return The {@link OfflinePlayer} associated with the new trustee.
     */
    @NotNull
    public OfflinePlayer getTrustee() {
        return trustee;
    }

    /**
     * Gets the {@link OfflinePlayer} initializing the untrust event, if available.
     * @return The {@link OfflinePlayer} initializing the untrust event, or null if the initializer is unknown.
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
