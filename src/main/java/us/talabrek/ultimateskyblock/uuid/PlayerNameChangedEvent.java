package us.talabrek.ultimateskyblock.uuid;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import us.talabrek.ultimateskyblock.player.PlayerInfo;

/**
 * Event fired when a player has changed his name.
 */
public class PlayerNameChangedEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    private final PlayerInfo playerInfo;
    private final String oldName;
    private final String newName;

    public PlayerNameChangedEvent(Player who, PlayerInfo playerInfo, String oldName, String newName) {
        super(who);
        this.playerInfo = playerInfo;
        this.oldName = oldName;
        this.newName = newName;
    }

    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
