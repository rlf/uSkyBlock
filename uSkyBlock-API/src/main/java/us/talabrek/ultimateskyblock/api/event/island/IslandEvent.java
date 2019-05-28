package us.talabrek.ultimateskyblock.api.event.island;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.talabrek.ultimateskyblock.api.IslandInfo;

/**
 * Represents an island related event.
 * @since 2.7.7
 */
public abstract class IslandEvent extends Event {
    protected IslandInfo islandInfo;

    public IslandEvent(@NotNull final IslandInfo islandInfo) {
        this.islandInfo = islandInfo;
    }

    IslandEvent(@NotNull final IslandInfo islandInfo, boolean async) {
        super(async);
        this.islandInfo = islandInfo;
    }

    /**
     * Returns the IslandInfo involved in this event.
     * @return IslandInfo involved in this event.
     */
    @NotNull
    public final IslandInfo getIslandInfo() {
        return islandInfo;
    }
}
