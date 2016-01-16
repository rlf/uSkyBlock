package us.talabrek.ultimateskyblock.island.task;

import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.talabrek.ultimateskyblock.async.Callback;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;
import us.talabrek.ultimateskyblock.util.TimeUtil;

/**
 * A task that looks for a chest at an island location.
 */
public class LocateChestTask extends BukkitRunnable {
    private final uSkyBlock plugin;
    private final Player player;
    private final Location islandLocation;
    private final Runnable onCompletion;
    private final long timeout;
    private final long heartBeatTicks;

    private long tStart;

    public LocateChestTask(uSkyBlock plugin, Player player, Location islandLocation, Runnable onCompletion) {
        this.plugin = plugin;
        this.player = player;
        this.islandLocation = islandLocation;
        this.onCompletion = onCompletion;
        timeout = System.currentTimeMillis() + TimeUtil.stringAsMillis(plugin.getConfig().getString("asyncworldedit.watchDog.timeout", "5m"));
        heartBeatTicks = TimeUtil.millisAsTicks(plugin.getConfig().getInt("asyncworldedit.watchDog.heartBeatMs", 2000));
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        if (tStart == 0) {
            tStart = now;
        }
        // Check for completion
        Callback<Location> callback = new Callback<Location>() {
            @Override
            public void run() {
                Location chestLocation = getState();
                long now = System.currentTimeMillis();
                if (chestLocation == null && now < timeout) {
                    LocateChestTask.this.runTaskLater(plugin, heartBeatTicks);
                } else {
                    if (chestLocation == null && player != null && player.isOnline()) {
                        player.sendMessage(I18nUtil.tr("\u00a7cWatchdog!\u00a79 Unable to locate a chest within {0}, bailing out.", TimeUtil.millisAsString(timeout)));
                    }
                    if (onCompletion != null) {
                        Bukkit.getScheduler().runTask(plugin, onCompletion);
                    }
                }
            }
        };
        LocationUtil.findChestLocationAsync(plugin, islandLocation, callback);
    }
}
