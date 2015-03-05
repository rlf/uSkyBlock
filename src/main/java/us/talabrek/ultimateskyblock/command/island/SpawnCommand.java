package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;

import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Convernience to get to spawn.
 */
public class SpawnCommand extends RequirePlayerCommand {
    private final uSkyBlock plugin;

    public SpawnCommand(uSkyBlock plugin) {
        super("spawn", null, "teleports you to the skyblock spawn");
        this.plugin = plugin;
    }

    @Override
    protected boolean doExecute(String alias, Player player, Map<String, Object> data, String... args) {
    	if(player.hasPermission("usb.mod.bypassteleport") || (plugin.getConfig().getInt("options.island.islandTeleportDelay") == 0)) {
        	plugin.spawnTeleport(player);
        } else {
        	final Player p = player;
        	Timer t = new Timer();
			t.schedule(new TimerTask() {
				@Override
				public void run() {
					plugin.spawnTeleport(p);
				}
			}, (plugin.getConfig().getInt("options.island.islandTeleportDelay") * 1000));
        }
        return true;
    }
}
