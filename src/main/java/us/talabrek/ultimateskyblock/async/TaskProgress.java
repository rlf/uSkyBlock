package us.talabrek.ultimateskyblock.async;

import us.talabrek.ultimateskyblock.util.TaskUtil;
import us.talabrek.ultimateskyblock.util.TimeUtil;

/**
 * POJO for holding progress of an executing task.
 */
public class TaskProgress {
    private final IncrementalTask task;
    private final double progress;
    private final double ticks;
    private final long millis;

    public TaskProgress(IncrementalTask task, double progress, double ticks, long millis) {
        this.task = task;
        this.progress = progress;
        this.ticks = ticks;
        this.millis = millis;
    }

    public IncrementalTask getTask() {
        return task;
    }

    public double getProgress() {
        return progress;
    }

    public double getTicks() {
        return ticks;
    }

    public long getMillis() {
        return millis;
    }

    public String getName() {
        return TaskUtil.getTaskName(task);
    }

    @Override
    public String toString() {
        return String.format("§3%s §7%3.0f%%§e %s (%3.1f ticks)", getName(), getProgress() * 100, TimeUtil.millisAsString(getMillis()), getTicks());
    }
}
