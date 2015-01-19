package us.talabrek.ultimateskyblock.uuid;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Event fired when a player has changed his name.
 */
public class PlayerNameChangedEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    private final String oldName;

    public PlayerNameChangedEvent(Player who, String oldName) {
        super(who);
        this.oldName = oldName;
    }

    public String getOldName() {
        return oldName;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
