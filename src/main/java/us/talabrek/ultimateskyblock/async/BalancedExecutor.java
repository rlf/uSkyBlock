package us.talabrek.ultimateskyblock.async;

import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Supports executing tasks in smaller increments, and balancing it against the server-load.
 */
public interface BalancedExecutor {
    /**
     * Executes the task with a max load of loadFactor.
     * @param task       The task to call continously until the task is completed.
     * @param completion A runnable to execute once the task is done.
     * @param loadFactor A load-factor ]0..1] of how many ticks should be consumed.
     * @param maxTicks   The maximum number of ticks to execute in one go.
     */
    void execute(Plugin plugin, IncrementalTask task, Runnable completion, float loadFactor, int maxTicks);

    /**
     * Cancels the task.
     * @param task The task to cancel.
     * @return <code>true</code> if it was possible to cancel the task.
     */
    boolean cancel(IncrementalTask task);

    /**
     * Return the currently completed progress of the given task.
     * @param task The task to query.
     * @return A number between [0..1].
     */
    TaskProgress getProgress(IncrementalTask task);

    /**
     * Returns a list of currently executing tasks.
     * @return a list of currently executing tasks.
     */
    List<TaskProgress> getTasks();
}
