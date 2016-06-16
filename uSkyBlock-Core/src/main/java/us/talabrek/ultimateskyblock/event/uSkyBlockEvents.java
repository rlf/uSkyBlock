package us.talabrek.ultimateskyblock.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.talabrek.ultimateskyblock.api.event.CreateIslandEvent;
import us.talabrek.ultimateskyblock.api.event.InviteEvent;
import us.talabrek.ultimateskyblock.api.event.RestartIslandEvent;
import us.talabrek.ultimateskyblock.uSkyBlock;

/**
 * Main event-handler for internal uSkyBlock events
 */
public class uSkyBlockEvents implements Listener {
    private final uSkyBlock plugin;

    public uSkyBlockEvents(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRestart(RestartIslandEvent e) {
        if (!e.isCancelled()) {
            plugin.restartPlayerIsland(e.getPlayer(), e.getIslandLocation(), e.getSchematic());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCreate(CreateIslandEvent e) {
        if (!e.isCancelled()) {
            plugin.createIsland(e.getPlayer(), e.getSchematic());
        }
    }
}
