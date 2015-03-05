package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class HomeCommand extends RequireIslandCommand {
    public HomeCommand(uSkyBlock plugin) {
        super(plugin, "home|h", "usb.island.sethome", "teleport to the island home");
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (pi.getHomeLocation() == null) {
            pi.setHomeLocation(pi.getIslandLocation());
        }
        int maxParty = plugin.getConfig().getInt("options.general.maxPartySize", 4);
        if (maxParty > island.getMaxPartySize()) {
            island.setMaxPartySize(maxParty);
        }
        island.updatePartyNumber(player);
        
        if(player.hasPermission("usb.mod.bypassteleport") || (plugin.getConfig().getInt("options.island.islandTeleportDelay") == 0)) {
        	plugin.homeTeleport(player);
        } else {
        	final Player p = player;
        	Timer t = new Timer();
			t.schedule(new TimerTask() {
				@Override
				public void run() {
					plugin.homeTeleport(p);
				}
			}, (plugin.getConfig().getInt("options.island.islandTeleportDelay") * 1000));
        }
        return true;
    }
}
