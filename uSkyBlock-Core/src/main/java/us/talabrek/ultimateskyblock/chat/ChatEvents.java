package us.talabrek.ultimateskyblock.chat;

import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import us.talabrek.ultimateskyblock.api.event.IslandChatEvent;
import us.talabrek.ultimateskyblock.uSkyBlock;

/**
 * @see us.talabrek.ultimateskyblock.api.event.IslandChatEvent
 * @see IslandChatCommand
 */
public class ChatEvents implements Listener {
    private final ChatLogic logic;
    private final uSkyBlock plugin;

    public ChatEvents(ChatLogic logic, uSkyBlock plugin) {
        this.logic = logic;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandChatEvent(IslandChatEvent e) {
        if (!e.getPlayer().isOnline()
                || e.getType() == null
                || e.getMessage() == null
                )
        {
            return;
        }
        logic.sendMessage(e.getPlayer(), e.getType(), e.getMessage());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChatEvent(AsyncPlayerChatEvent e) {
        IslandChatEvent.Type toggle = logic.getToggle(e.getPlayer());
        if (toggle != null) {
            e.setCancelled(true);
            Server server = plugin.getServer();
            // Called via a sync task, because this listener might get called async.
            server.getScheduler().runTask(plugin, () ->
                    server.getPluginManager().callEvent(
                            new IslandChatEvent(e.getPlayer(), toggle, e.getMessage())));
        }
    }
}
