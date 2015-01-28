package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.command.admin.task.PurgeScanTask;
import us.talabrek.ultimateskyblock.command.admin.task.PurgeTask;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * The purge-command.
 */
public class PurgeCommand extends AbstractUSBCommand {
    private final uSkyBlock plugin;

    public PurgeCommand(uSkyBlock plugin) {
        super("purge", "usb.admin.purge", "time-in-days", "purges all abandoned islands");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(final CommandSender sender, String alias, Map<String, Object> data, String... args) {
        if (plugin.isPurgeActive()) {
            sender.sendMessage("\u00a74A purge is already running, please wait for it to finish!");
            return true;
        }
        if (args.length == 0 || !args[0].matches("[0-9]+")) {
            sender.sendMessage("\u00a74You must provide the age in days to purge!");
            return false;
        }
        plugin.activatePurge();
        final int time = Integer.parseInt(args[0], 10) * 24;
        sender.sendMessage("\u00a7eMarking all islands inactive for more than " + args[1] + " days.");
        new PurgeScanTask(plugin, plugin.directoryIslands, time).runTask(plugin);
        return true;
    }
}
