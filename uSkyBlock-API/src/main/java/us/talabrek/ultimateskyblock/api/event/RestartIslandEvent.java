package us.talabrek.ultimateskyblock.api.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * Fired when a player wishes to restart his island.
 * @since 2.7.0
 */
public class RestartIslandEvent extends CancellablePlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Location islandLocation;
    private final String schematic;

    public RestartIslandEvent(Player who, Location islandLocation, String schematic) {
        super(who);
        this.islandLocation = islandLocation;
        this.schematic = schematic;
    }

    public Location getIslandLocation() {
        return islandLocation;
    }

    public String getSchematic() {
        return schematic;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
