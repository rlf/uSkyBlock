package us.talabrek.ultimateskyblock.async;

import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import us.talabrek.ultimateskyblock.util.TimeUtil;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A non-blocking AsyncBalancedExecutor
 */
public class AsyncBalancedExecutor implements BalancedExecutor {
    private static final Logger log = Logger.getLogger(AsyncBalancedExecutor.class.getName());
    private final BukkitScheduler scheduler;

    public AsyncBalancedExecutor(BukkitScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void execute(final Plugin plugin, final IncrementalTask task, final Runnable completion, final float loadFactor, final int maxTicks) {
        log.log(Level.FINE, String.format("Scheduling task : %s for async-balanced execution with %3.0f%% load and %d max-ticks",
                task.toString(), loadFactor, maxTicks));
        final long tStart = System.currentTimeMillis();
        final AtomicInteger offset = new AtomicInteger(0);
        final AtomicInteger length = new AtomicInteger(1);
        final AtomicDouble usedTicks = new AtomicDouble(0);
        final Runnable runner = new Runnable() {
            @Override
            public void run() {
                if (!task.isComplete()) {
                    long t1 = System.currentTimeMillis();
                    int len = length.get();
                    int off = offset.get();
                    if (task.getLength() < (off + len)) {
                        len = task.getLength() - off;
                    }
                    try {
                        task.execute(plugin, off, len);
                    } finally {
                        offset.set(off + len);
                        long t2 = System.currentTimeMillis();
                        // TODO: 18/01/2015 - R4zorax: Show progress somewhere
                        float ticks = (t2 - t1) / 50;
                        usedTicks.addAndGet(ticks);
                        if (ticks < 1) {
                            ticks = 1;
                        }
                        // update length for next iteration
                        int newLength = Math.round(len * maxTicks / ticks);
                        if (newLength < 1) {
                            newLength = 1;
                        }
                        length.set(newLength);
                        long waitTime = (long) Math.ceil((1 - loadFactor) * ticks);
                        log.log(Level.FINE, "Executed " + len + " tasks in " + ticks + " ticks");
                        if (task.isComplete() || len == 0) {
                            log.log(Level.FINE,
                                    String.format("Async-balanced execution of %s completed in %s using %5.2f ticks",
                                            task.toString(), TimeUtil.millisAsString(System.currentTimeMillis()-tStart),
                                            usedTicks.get()));
                            scheduler.runTaskLater(plugin, completion, waitTime);
                        } else {
                            log.log(Level.FINE, "Scheduling next " + newLength + " tasks in " + waitTime + " ticks");
                            scheduler.runTaskLaterAsynchronously(plugin, this, waitTime);
                        }
                    }
                }
            }
        };
        scheduler.runTaskAsynchronously(plugin, runner);
    }
}
