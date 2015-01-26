package us.talabrek.ultimateskyblock.async;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * A non-blocking AsyncBalancedExecutor
 */
public class AsyncBalancedExecutor extends AbstractBalancedExecutor {
    public AsyncBalancedExecutor(BukkitScheduler scheduler) {
        super(scheduler);
    }

    @Override
    protected void doLater(Plugin plugin, Runnable runnable, long delay) {
        scheduler.runTaskLaterAsynchronously(plugin, runnable, delay);
    }
}
