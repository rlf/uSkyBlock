package us.talabrek.ultimateskyblock.async;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

/**
 * A BalancedExecutor that executes tasks synchronously
 */
public class SyncBalancedExecutor extends AbstractBalancedExecutor {

    public SyncBalancedExecutor(BukkitScheduler scheduler) {
        super(scheduler);
    }

    @Override
    protected void doLater(Plugin plugin, Runnable runnable, long delay) {
        if (plugin != null && plugin.isEnabled()) {
            scheduler.runTaskLater(plugin, runnable, delay);
        }
    }

}
