package us.talabrek.ultimateskyblock.async;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A BalancedExecutor that executes tasks synchronously
 */
public class SyncBalancedExecutor implements BalancedExecutor {
    private static final Logger log = Logger.getLogger(SyncBalancedExecutor.class.getName());
    private final BukkitScheduler scheduler;

    public SyncBalancedExecutor(BukkitScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void execute(final Plugin plugin, final IncrementalTask task, final Runnable completion, final float loadFactor, final int maxTicks) {
        final AtomicInteger offset = new AtomicInteger(0);
        final AtomicInteger length = new AtomicInteger(5);
        final Runnable runner = new Runnable() {
            @Override
            public void run() {
                if (!task.isComplete()) {
                    long t1 = System.currentTimeMillis();
                    int len = length.get();
                    int off = offset.get();
                    if (task.getLength() < (off+len)) {
                        len = task.getLength()-off;
                    }
                    try {
                        task.execute(plugin, off, len);
                    } finally {
                        offset.set(off+len);
                        long t2 = System.currentTimeMillis();
                        // TODO: 18/01/2015 - R4zorax: Show progress somewhere
                        float ticks = (t2-t1)/50;
                        if (ticks < 1) {
                            ticks = 1;
                        }
                        // update length for next iteration
                        int newLength = Math.round(len * maxTicks / ticks);
                        if (newLength < 1) {
                            newLength = 1;
                        }
                        length.set(newLength);
                        long waitTime = (long) Math.ceil((1-loadFactor)*ticks);
                        log.log(Level.FINE, "Executed " + len + " tasks in " + ticks + " ticks");
                        if (task.isComplete() || len == 0) {
                            scheduler.runTaskLater(plugin, completion, waitTime);
                        } else {
                            log.log(Level.FINE, "Scheduling next " + newLength + " tasks in " + waitTime + " ticks");
                            scheduler.runTaskLater(plugin, this, waitTime);
                        }
                    }
                }
            }
        };
        scheduler.runTask(plugin, runner);
    }
}
