package us.talabrek.ultimateskyblock.async;

import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Supports listing IncrementalTasks sequentially.
 */
public class CompositeIncrementalTask implements IncrementalTask {

    private final List<InternalTask> tasks;
    private int length;

    public CompositeIncrementalTask(IncrementalTask... tasks) {
        this.tasks = new ArrayList<>();
        length = 0;
        for (IncrementalTask task : tasks) {
            this.tasks.add(new InternalTask(task, length));
            length += task.getLength();
        }
    }

    @Override
    public boolean execute(Plugin plugin, int offset, int length) {
        InternalTask task = getTask(offset);
        if (task != null) {
            int innerOffset = offset - task.getStartOffset();
            int diff = Math.min(length, task.getTask().getLength() - innerOffset);
            boolean subTaskComplete = task.getTask().execute(plugin, innerOffset, diff);
            if (subTaskComplete) {
                diff = length - diff;
                if (diff > 0) {
                    return execute(plugin, task.getEndOffset(), diff);
                }
            }
        }
        return isComplete();
    }

    private InternalTask getTask(int offset) {
        for (InternalTask task : tasks) {
            if (task.getStartOffset() <= offset && task.getEndOffset() > offset) {
                return task;
            }
        }
        return null;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public boolean isComplete() {
        return tasks.get(tasks.size()-1).getTask().isComplete();
    }
    private static class InternalTask {
        private final IncrementalTask task;
        private final int startOffset;
        public InternalTask(IncrementalTask task, int startOffset) {
            this.task = task;
            this.startOffset = startOffset;
        }

        public IncrementalTask getTask() {
            return task;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return startOffset + task.getLength();
        }
    }
}
