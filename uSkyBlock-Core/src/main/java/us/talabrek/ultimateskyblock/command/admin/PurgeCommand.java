package us.talabrek.ultimateskyblock.command.admin;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import us.talabrek.ultimateskyblock.async.Callback;
import us.talabrek.ultimateskyblock.command.admin.task.PurgeScanTask;
import us.talabrek.ultimateskyblock.command.admin.task.PurgeTask;
import us.talabrek.ultimateskyblock.uSkyBlock;
import dk.lockfuglsang.minecraft.util.TimeUtil;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * The purge-command.
 */
public class PurgeCommand extends AbstractCommand {
    private final uSkyBlock plugin;

    private PurgeScanTask scanTask;
    private PurgeTask purgeTask;
    private String days = null;

    public PurgeCommand(uSkyBlock plugin) {
        super("purge", "usb.admin.purge", "time-in-days|stop ?force", tr("purges all abandoned islands"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(final CommandSender sender, String alias, Map<String, Object> data, String... args) {
        if (scanActive()) {
            tryConfirm(sender, args);
            return true;
        }
        if (args.length == 0 || !args[0].matches("[0-9]+")) {
            sender.sendMessage(tr("\u00a74You must provide the age in days to purge!"));
            return false;
        }
        days = args[0];
        final boolean force = args.length > 1 && args[1].equalsIgnoreCase("force");
        final int time = Integer.parseInt(days, 10) * 24;
        sender.sendMessage(tr("\u00a7eFinding all islands that has been abandoned for more than {0} days.", args[0]));
        scanTask = new PurgeScanTask(plugin, plugin.directoryIslands, time, sender, new Runnable() {
            @Override
            public void run() {
                if (force) {
                    doPurge(sender);
                } else {
                    int timeout = plugin.getConfig().getInt("options.advanced.purgeTimeout", 600000);
                    sender.sendMessage(tr("\u00a74PURGE:\u00a7e Repeat the command within {0} to accept.", TimeUtil.millisAsString(timeout)));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (scanTask.isActive()) {
                                sender.sendMessage("\u00a77purge timed out");
                                scanTask.stop();
                            }
                        }
                    }.runTaskLaterAsynchronously(plugin, TimeUtil.millisAsTicks(timeout));
                }
            }
        });
        scanTask.runTaskAsynchronously(plugin);
        return true;
    }

    private boolean scanActive() {
        return scanTask != null && scanTask.isActive() || purgeTask != null && purgeTask.isActive();
    }

    private void tryConfirm(CommandSender sender, String[] args) {
        if (purgeTask != null && purgeTask.isActive()) {
            if (args.length == 1 && args[0].equalsIgnoreCase("stop")) {
                sender.sendMessage(tr("\u00a74Trying to abort purge"));
                purgeTask.stop();
                return;
            }
        }
        if (scanTask != null && scanTask.isActive() && !scanTask.isDone() && args.length == 1 && args[0].equalsIgnoreCase("stop")) {
            sender.sendMessage(tr("\u00a74Trying to abort purge"));
            scanTask.stop();
            return;
        }
        if (scanTask != null && scanTask.isActive() && scanTask.isDone() && args.length == 1 && args[0].equalsIgnoreCase(days)) {
            doPurge(sender);
        } else {
            sender.sendMessage(tr("\u00a74A purge is already running, please wait for it to finish!"));
        }
    }

    private void doPurge(CommandSender sender) {
        sender.sendMessage(tr("\u00a74Starting purge..."));
        purgeTask = new PurgeTask(plugin, scanTask.getPurgeList(), sender);
        purgeTask.runTaskAsynchronously(plugin);
        scanTask.stop(); // Mark as inactive
    }
}
