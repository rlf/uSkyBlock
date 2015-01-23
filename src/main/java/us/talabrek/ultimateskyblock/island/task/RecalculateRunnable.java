package us.talabrek.ultimateskyblock.island.task;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.talabrek.ultimateskyblock.api.event.uSkyBlockEvent;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;

public class RecalculateRunnable extends BukkitRunnable {
    private final uSkyBlock plugin;

    public RecalculateRunnable(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        List<Player> recalcPlayers = new ArrayList<>();
        for (Player player : plugin.getWorld().getPlayers()) {
            if (player.isOnline() && plugin.playerIsOnIsland(player)) {
                recalcPlayers.add(player);
            }
        }
        if (!recalcPlayers.isEmpty()) {
            plugin.getExecutor().execute(plugin, new RecalculateTask(plugin, recalcPlayers), new Runnable() {
                @Override
                public void run() {
                    plugin.fireChangeEvent(new uSkyBlockEvent(null, plugin, uSkyBlockEvent.Cause.RANK_UPDATED));
                }
            }, 0.5f, 1);
        }
    }
}
