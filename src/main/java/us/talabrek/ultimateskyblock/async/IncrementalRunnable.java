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
    private double tStart;

    /**
     * The time of completion.
     */
    private double tCompleted;

    /**
     * Millis used in processing.
     */
    private double tUsed;

    /**
     * The time of the current incremental run.
     */
    private double tRunning;

    private volatile boolean isCancelled = false;

    private int consecutiveRuns = 0;

    /**
     * Number of ticks in total.
     */
    private int ticks = 0;

    private double lastTick = 0;

    public IncrementalRunnable(uSkyBlock plugin) {
        this(plugin, null, 20, 20, 15);
    }

    public IncrementalRunnable(uSkyBlock plugin, Runnable onCompletion) {
        this(plugin, onCompletion, 20, 20, 15);
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
    protected double millisActive() {
        return System.nanoTime()*1000d - tRunning;
    }

    protected double millisLeft() {
        return maxMs - millisActive();
    }

    public boolean stillTime() {
        double millisPerTick = getTimeUsed()/ticks;
        return millisPerTick < millisLeft();
    }

    public uSkyBlock getPlugin() {
        return plugin;
    }

    protected boolean tick() {
        ticks++;
        lastTick = System.nanoTime()*1000d;
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
        if (tCompleted != 0) {
            return Math.round(tCompleted - tStart);
        }
        if (tStart == 0) {
            return -1;
        }
        return Math.round(System.nanoTime()*1000d - tStart);
    }

    /**
     * Returns the number of ms the task has been actively executing.
     * @return the number of ms the task has been actively executing.
     */
    public double getTimeUsed() {
        return tUsed + ((tRunning != 0) ? System.nanoTime()*1000d-tRunning : 0);
    }

    public void cancel() {
        isCancelled = true;
    }

    @Override
    public final void run() {
        tRunning = System.nanoTime()*1000d;
        if (tStart == 0) {
            tStart = tRunning;
        }
        try {
            lastTick = tRunning;
            if (!execute() && !isCancelled) {
                // TODO: 28/09/2015 - R4zorax: Don't run back to back ALL the time
                Bukkit.getScheduler().runTaskLater(plugin, this, consecutiveRuns < maxConsecutive ? 0 : yieldDelay);
            } else {
                tCompleted = System.nanoTime()*1000d;
                if (onCompletion != null && !isCancelled) {
                    Bukkit.getScheduler().runTaskLater(plugin, onCompletion, 0);
                }
            }
        } finally {
            tUsed += (System.nanoTime()*1000d - tRunning);
            tRunning = 0;
            consecutiveRuns++;
            if (consecutiveRuns > maxConsecutive) {
                consecutiveRuns = 0;
            }
        }
    }

    protected void setOnCompletion(Runnable onCompletion) {
        this.onCompletion = onCompletion;
    }
}
