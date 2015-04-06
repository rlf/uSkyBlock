package us.talabrek.ultimateskyblock.async;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import us.talabrek.ultimateskyblock.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Common executor using template pattern
 */
public abstract class AbstractBalancedExecutor implements BalancedExecutor {
    private static final Logger log = Logger.getLogger(SyncBalancedExecutor.class.getName());
    protected final BukkitScheduler scheduler;
    protected final Map<IncrementalTask, IncrementalExecution> tasks = new ConcurrentHashMap<>();

    public AbstractBalancedExecutor(BukkitScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void execute(final Plugin plugin, final IncrementalTask task, final Runnable completion, final float loadFactor, final int maxTicks) {
        log.log(Level.FINE, String.format("Scheduling task : %s for sync-balanced execution with %3.0f%% load and %d max-ticks",
                task.toString(), loadFactor*100, maxTicks));
        IncrementalExecution execution = new IncrementalExecution(plugin, task, completion, loadFactor, maxTicks);
        tasks.put(task, execution);
        scheduler.runTask(plugin, execution);
    }

    protected abstract void doLater(Plugin plugin, Runnable runnable, long delay);

    @Override
    public synchronized boolean cancel(IncrementalTask task) {
        if (tasks.containsKey(task)) {
            IncrementalExecution execution = tasks.remove(task);
            execution.stop();
            return true;
        }
        return false;
    }

    @Override
    public TaskProgress getProgress(IncrementalTask task) {
        IncrementalExecution execution = tasks.get(task);
        if (execution != null) {
            return execution.getProgress();
        }
        return null;
    }

    @Override
    public List<TaskProgress> getTasks() {
        List<TaskProgress> progress = new ArrayList<>();
        for (IncrementalExecution execution : tasks.values()) {
            if (execution != null) {
                progress.add(execution.getProgress());
            }
        }
        return progress;
    }

    private class IncrementalExecution implements Runnable {
        final int[] offset = new int[] { 0 };
        final int[] length = new int[] { 1 }; // Smallest increment to start with
        final double[] usedTicks = new double[] { 0 };
        private final Plugin plugin;
        private final IncrementalTask task;
        private final Runnable completion;
        private final float loadFactor;
        private final int maxTicks;
        private final long tStart = System.currentTimeMillis();
        private boolean stopped = false;

        public IncrementalExecution(Plugin plugin, IncrementalTask task, Runnable completion, float loadFactor, int maxTicks) {
            this.plugin = plugin;
            this.task = task;
            this.completion = completion;
            this.loadFactor = loadFactor;
            this.maxTicks = maxTicks;
        }
        @Override
        public void run() {
            if (stopped) {
                return;
            }
            if (!task.isComplete()) {
                long t1 = System.currentTimeMillis();
                int len = length[0];
                int off = offset[0];
                if (task.getLength() < (off+len)) {
                    len = task.getLength()-off;
                }
                try {
                    task.execute(plugin, off, len);
                } finally {
                    offset[0] += len;
                    long t2 = System.currentTimeMillis();
                    // TODO: 18/01/2015 - R4zorax: Show progress somewhere
                    double ticks = (t2-t1)/50d;
                    usedTicks[0] += ticks;
                    if (ticks < 0.5) {
                        ticks = 0.5;
                    }
                    // update length for next iteration
                    int newLength = (int) Math.round(len * maxTicks / ticks);
                    if (newLength < 1) {
                        newLength = 1;
                    }
                    if (newLength > len*2) {
                        newLength = len*2; // Max double the items.
                    }
                    length[0] = newLength;
                    long waitTime = (long) Math.ceil((1-loadFactor)*ticks);
                    log.log(Level.FINE, "Executed " + len + " tasks in " + ticks + " ticks (next length = " + newLength + ")");
                    if (task.isComplete() || len == 0) {
                        log.log(Level.FINE,
                                String.format("Balanced execution of %s completed in %s using %5.2f ticks",
                                        task.toString(), TimeUtil.millisAsString(System.currentTimeMillis() - tStart),
                                        usedTicks[0]));
                        doLater(plugin, completion, waitTime);
                        tasks.remove(task);
                    } else {
                        log.log(Level.FINE, "Scheduling next " + newLength + " tasks in " + waitTime + " ticks.");
                        doLater(plugin, IncrementalExecution.this, waitTime);
                    }
                }
            } else {
                doLater(plugin, completion, 0);
                tasks.remove(task);
            }
        }
        public synchronized void stop() {
            stopped = true;
        }

        public TaskProgress getProgress() {
            return new TaskProgress(task, 1f*task.getLength() / offset[0], usedTicks[0], System.currentTimeMillis()-tStart);
        }
    }
}
