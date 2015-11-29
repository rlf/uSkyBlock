package us.talabrek.ultimateskyblock.async;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import us.talabrek.ultimateskyblock.uSkyBlock;

/**
 * Convenience template class for executing heavy tasks on the main thread.
 *
 * <h2>Usage</h2>
 * <pre>
 *     new IncrementalRunnable() {
 *         boolean execute() {
 *             while (!isDone()) {
 *                 // Do something incrementially synchronously
 *                 if (!tick()) {
 *                     break;
 *                 }
 *             }
 *             return isDone();
 *         }
 *     }
 * </pre>
 */
public abstract class IncrementalRunnable extends BukkitRunnable {
    private final uSkyBlock plugin;
    private Runnable onCompletion;
    /**
     * The maximum number of consecutive ms to execute a task.
     */
    private final int maxMs;
    private final int maxConsecutive;
    private final int yieldDelay;


    /**
     * The time of creation
     */
    private double tStart = 0;

    /**
     * The time of completion.
     */
    private double tCompleted = 0;

    /**
     * Millis used in processing.
     */
    private double tUsed = 0;

    /**
     * The time of the current incremental run.
     */
    private double tRunning = 0;

    private volatile boolean isCancelled = false;

    private int consecutiveRuns = 0;

    /**
     * Number of iterations in total (calls to tick())
     */
    private volatile int iterations = 0;

    /**
     * Number of server-ticks consumed.
     */
    private volatile int ticks = 0;

    public IncrementalRunnable(uSkyBlock plugin) {
        this(plugin, null,
                plugin.getConfig().getInt("async.maxMs", 22),
                plugin.getConfig().getInt("async.maxConsecutiveTicks", 20),
                plugin.getConfig().getInt("async.yieldDelay", 15)
        );
    }

    public IncrementalRunnable(uSkyBlock plugin, Runnable onCompletion) {
        this(plugin, onCompletion,
                plugin.getConfig().getInt("async.maxMs", 22),
                plugin.getConfig().getInt("async.maxConsecutiveTicks", 20),
                plugin.getConfig().getInt("async.yieldDelay", 15)
        );
    }

    public IncrementalRunnable(uSkyBlock plugin, Runnable onCompletion, int maxMs, int maxConsecutive, int yieldDelay) {
        this.plugin = plugin;
        this.onCompletion = onCompletion;
        this.maxMs = maxMs;
        this.maxConsecutive = maxConsecutive;
        this.yieldDelay = yieldDelay;
    }

    protected boolean hasTime() {
        return millisActive() < maxMs && !isCancelled;
    }

    /**
     * Used by sub-classes to see how much time they have left.
     * @return The number of ms the current #execute() has been running.
     */
    protected long millisActive() {
        return Math.round(t() - tRunning);
    }

    protected double millisLeft() {
        return maxMs - millisActive();
    }

    public boolean stillTime() {
        double millisPerTick = getTimeUsed()/(iterations != 0 ? iterations : 1);
        return millisPerTick < millisLeft();
    }

    public uSkyBlock getPlugin() {
        return plugin;
    }

    protected boolean tick() {
        iterations++;
        return stillTime();
    }

    /**
     * Executes a potentially heavy task
     * @return <code>true</code> if done, <code>false</code> otherwise.
     */
    protected abstract boolean execute();

    /**
     * Returns the number of ms the task has been active.
     * @return the number of ms the task has been active.
     */
    public long getTimeElapsed() {
        if (tCompleted != 0d) {
            return Math.round(tCompleted - tStart);
        }
        if (tStart == 0) {
            return -1;
        }
        return Math.round(t() - tStart);
    }

    /**
     * Returns the number of ms the task has been actively executing.
     * @return the number of ms the task has been actively executing.
     */
    public double getTimeUsed() {
        return tUsed + (tRunning != 0 ? t()-tRunning : 0);
    }

    public void cancel() {
        isCancelled = true;
    }

    @Override
    public final void run() {
        tRunning = t();
        if (tStart == 0d) {
            tStart = tRunning;
            JobManager.addJob(this);
        }
        try {
            if (!execute() && !isCancelled) {
                // TODO: 28/09/2015 - R4zorax: Don't run back to back ALL the time
                Bukkit.getScheduler().runTaskLater(plugin, (Runnable)this, consecutiveRuns < maxConsecutive ? 0 : yieldDelay);
            } else {
                if (onCompletion != null && !isCancelled) {
                    Bukkit.getScheduler().runTaskLater(plugin, onCompletion, 0);
                }
                complete();
            }
        } finally {
            tUsed += (t() - tRunning);
            tRunning = 0;
            consecutiveRuns++;
            if (consecutiveRuns > maxConsecutive) {
                consecutiveRuns = 0;
            }
            ticks++;
        }
    }

    private static double t() {
        return System.nanoTime()/1000000d;
    }

    protected void setOnCompletion(Runnable onCompletion) {
        this.onCompletion = onCompletion;
    }

    private void complete() {
        tCompleted = t();
        JobManager.completeJob(this);
    }

    public int getTicks() {
        return ticks;
    }
}
