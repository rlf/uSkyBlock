package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.async.TaskProgress;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.command.common.CompositeUSBCommand;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.TimeUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Async Controller
 */
public class AsyncCommand extends CompositeUSBCommand {
    private final List<TaskProgress> taskList = new CopyOnWriteArrayList<>();
    private int asyncOffset = 0;

    public AsyncCommand(final uSkyBlock plugin) {
        super("async", "usb.admin", "controls the currently running async tasks");
        add(new AbstractUSBCommand("list", "lists currently running tasks") {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                synchronized (taskList) {
                    taskList.clear();
                    taskList.addAll(plugin.getAsyncExecutor().getTasks());
                    asyncOffset = taskList.size();
                    taskList.addAll(plugin.getExecutor().getTasks());
                    if (taskList.isEmpty()) {
                        sender.sendMessage("\u00a7cNo running tasks");
                        return true;
                    }
                    String msg = "\u00a7dTasks:\n";
                    int i = 1;
                    for (TaskProgress progress : taskList) {
                        msg += String.format("\u00a7e[%02d]\u00a77: \u00a73%s \u00a77%3.0f\u00a7e %s (%3.1f ticks)\n",
                                i++, progress.getName(), 100 * progress.getProgress(),
                                TimeUtil.millisAsString(progress.getMillis()), progress.getTicks());
                    }
                    sender.sendMessage(msg.split("\n"));
                    return true;
                }
            }
        });
        add(new AbstractTaskCommand("cancel", "cancels the task with the given number.") {
            @Override
            protected void performCommand(CommandSender sender, TaskProgress task, boolean isAsync) {
                if (doCancel(task, isAsync)) {
                    sender.sendMessage("\u00a7eSuccessfully cancelled task : "+ task);
                } else {
                    sender.sendMessage("\u00a74Could not cancel " + task + ".");
                }
            }

            private boolean doCancel(TaskProgress task, boolean isAsync) {
                if (isAsync) {
                    return plugin.getAsyncExecutor().cancel(task.getTask());
                } else {
                    return plugin.getExecutor().cancel(task.getTask());
                }
            }
        });
    }

    private abstract class AbstractTaskCommand extends AbstractUSBCommand {
        public AbstractTaskCommand(String name, String description) {
            super(name, null, "num", description);
        }

        protected abstract void performCommand(CommandSender sender, TaskProgress task, boolean isAsync);

        @Override
        public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
            if (args.length != 1) {
                return false;
            }
            if (!args[0].matches("[0-9]+")) {
                sender.sendMessage("\u00a74A valid number must be supplied as task-id.");
                return false;
            }
            synchronized (taskList) {
                if (taskList.isEmpty()) {
                    sender.sendMessage("\u00a74No tasks detected.\u00a7e Try \u00a7b/usb async list\u00a7e first.");
                    return true;
                }
                int index = Integer.parseInt(args[0]);
                if (index >= 1 && index <= taskList.size()) {
                    performCommand(sender, taskList.get(index), index <= asyncOffset);
                    return true;
                } else {
                    sender.sendMessage("\u00a74A number between 1 and " + taskList.size() + " expected.");
                    return false;
                }
            }
        }
    }
}
