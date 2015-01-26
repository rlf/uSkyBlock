package us.talabrek.ultimateskyblock.async;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * A BalancedExecutor that executes tasks synchronously
 */
public class SyncBalancedExecutor extends AbstractBalancedExecutor {

    public SyncBalancedExecutor(BukkitScheduler scheduler) {
        super(scheduler);
    }

    @Override
    protected void doLater(Plugin plugin, Runnable runnable, long delay) {
        scheduler.runTaskLater(plugin, runnable, delay);
    }

}
