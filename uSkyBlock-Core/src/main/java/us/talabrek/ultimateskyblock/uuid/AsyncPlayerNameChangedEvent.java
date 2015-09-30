package us.talabrek.ultimateskyblock.uuid;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import us.talabrek.ultimateskyblock.player.PlayerInfo;

/**
 * Event fired when a player has changed his name.
 */
public class AsyncPlayerNameChangedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final PlayerInfo playerInfo;
    private final String oldName;
    private final String newName;

    public AsyncPlayerNameChangedEvent(Player who, PlayerInfo playerInfo, String oldName, String newName) {
        super(true); // Harry - True boolean is important, otherwise will synchronize with the event factory causing server lock-ups.
        this.player = who;
        this.playerInfo = playerInfo;
        this.oldName = oldName;
        this.newName = newName;
    }
    
    public Player getPlayer() {
        return player;
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
