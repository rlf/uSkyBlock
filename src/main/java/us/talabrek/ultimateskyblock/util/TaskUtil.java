package us.talabrek.ultimateskyblock.util;

import us.talabrek.ultimateskyblock.async.IncrementalTask;

public enum TaskUtil {
    ;
    public static String getTaskName(IncrementalTask task) {
        String taskName = task != null ? task.toString() : "<none>";
        if (taskName.indexOf(".") != -1) {
            taskName = taskName.substring(taskName.lastIndexOf(".") + 1);
        }
        return taskName;
    }
}
