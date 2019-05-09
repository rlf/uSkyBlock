package us.talabrek.ultimateskyblock.chat;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import us.talabrek.ultimateskyblock.api.event.IslandChatEvent;

/**
 * @see us.talabrek.ultimateskyblock.api.event.IslandChatEvent
 * @see IslandChatCommand
 */
public class ChatEvents implements Listener {

    private final ChatLogic logic;

    public ChatEvents(ChatLogic logic) {
        this.logic = logic;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onIslandChatEvent(IslandChatEvent e) {
        if (e.isCancelled()
                || e.getPlayer() == null
                || !e.getPlayer().isOnline()
                || e.getType() == null
                || e.getMessage() == null
        ) {
            return;
        }
        logic.sendMessage(e.getPlayer(), e.getType(), e.getMessage());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatEvent(AsyncPlayerChatEvent e) {
        if (e.isCancelled()
                || e.getMessage() == null
                || e.getPlayer() == null
        ) {
            return;
        }
        IslandChatEvent.Type toggle = logic.getToggle(e.getPlayer());
        if (toggle != null) {
            e.setCancelled(true);
            Bukkit.getPluginManager().callEvent(new IslandChatEvent(e.getPlayer(), toggle, e.getMessage()));
        }
    }
}
