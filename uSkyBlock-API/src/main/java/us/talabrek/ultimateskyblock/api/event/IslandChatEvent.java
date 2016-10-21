package us.talabrek.ultimateskyblock.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * An Event indicating a chat-event for either ISLAND or PARTY.
 * @since v2.7.2
 */
public class IslandChatEvent extends CancellablePlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    public enum Type { ISLAND, PARTY }

    private final Type type;

    private String message;

    public IslandChatEvent(Player player, Type type, String message) {
        super(player);
        this.player = player;
        this.type = type;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null");
        }
        this.message = message;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
