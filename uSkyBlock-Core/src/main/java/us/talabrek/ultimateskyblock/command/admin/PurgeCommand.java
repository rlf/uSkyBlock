package us.talabrek.ultimateskyblock.command.admin;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.command.admin.task.PurgeScanTask;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

/**
 * The purge-command.
 */
public class PurgeCommand extends AbstractCommand {
    private final uSkyBlock plugin;

    public PurgeCommand(uSkyBlock plugin) {
        super("purge", "usb.admin.purge", "time-in-days", I18nUtil.tr("purges all abandoned islands"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(final CommandSender sender, String alias, Map<String, Object> data, String... args) {
        if (plugin.isPurgeActive()) {
            sender.sendMessage(I18nUtil.tr("\u00a74A purge is already running, please wait for it to finish!"));
            return true;
        }
        if (args.length == 0 || !args[0].matches("[0-9]+")) {
            sender.sendMessage(I18nUtil.tr("\u00a74You must provide the age in days to purge!"));
            return false;
        }
        plugin.activatePurge();
        final int time = Integer.parseInt(args[0], 10) * 24;
        sender.sendMessage(I18nUtil.tr("\u00a7eFinding all islands that has been abandoned for more than {0} days.", args[0]));
        new PurgeScanTask(plugin, plugin.directoryIslands, time, sender).runTaskAsynchronously(plugin);
        return true;
    }
}
