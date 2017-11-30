package us.talabrek.ultimateskyblock.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.talabrek.ultimateskyblock.api.event.CreateIslandEvent;
import us.talabrek.ultimateskyblock.api.event.MemberJoinedEvent;
import us.talabrek.ultimateskyblock.api.event.MemberLeftEvent;
import us.talabrek.ultimateskyblock.api.event.RestartIslandEvent;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

/**
 * Main event-handler for internal uSkyBlock events
 */
public class InternalEvents implements Listener {
    private final uSkyBlock plugin;

    public InternalEvents(uSkyBlock plugin) {
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
            plugin.createIsland(e.getPlayer(), e.getSchematic(), e.getX(), e.getZ());
        }
    }

    @EventHandler
    public void onMemberJoin(MemberJoinedEvent e) {
        PlayerInfo playerInfo = (PlayerInfo) e.getPlayerInfo();
        playerInfo.execCommands(plugin.getConfig().getStringList("options.party.join-commands"));
    }

    @EventHandler
    public void onMemberLeft(MemberLeftEvent e) {
        PlayerInfo playerInfo = (PlayerInfo) e.getPlayerInfo();
        playerInfo.execCommands(plugin.getConfig().getStringList("options.party.leave-commands"));
    }
}
