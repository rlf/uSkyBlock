package us.talabrek.ultimateskyblock.async;

import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Represents an incremental task, that can be executed in chunks.
 */
public interface IncrementalTask {
    static final Logger log = Logger.getLogger(IncrementalTask.class.getName());
    /**
     * Executes the incremental task.
     *
     * @param offset The offset of the task.
     * @param length The length assigned to this task.
     * @return <code>true</code> if there are more work to be done.
     */
    boolean execute(Plugin plugin, int offset, int length);

    /**
     * Returns the number of incremental tasks supported by this Task.
     * @return A positive integer.
     */
    int getLength();

    /**
     * Whether or not the task has been completed.
     * @return Whether or not the task has been completed.
     */
    boolean isComplete();
}