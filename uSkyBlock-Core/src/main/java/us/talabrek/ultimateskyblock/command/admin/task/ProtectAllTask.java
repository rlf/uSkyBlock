package us.talabrek.ultimateskyblock.command.admin.task;

import dk.lockfuglsang.minecraft.file.FileUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.IslandUtil;
import us.talabrek.ultimateskyblock.util.LogUtil;
import us.talabrek.ultimateskyblock.util.ProgressTracker;
import dk.lockfuglsang.minecraft.util.TimeUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * An incremental (synchroneous) task for protecting all islands.
 */
public class ProtectAllTask extends BukkitRunnable {
    private static final Logger log = Logger.getLogger(ProtectAllTask.class.getName());
    private final CommandSender sender;
    private final uSkyBlock plugin;
    private final ProgressTracker tracker;

    private volatile boolean active;

    public ProtectAllTask(final uSkyBlock plugin, final CommandSender sender, ProgressTracker tracker) {
        this.plugin = plugin;
        this.tracker = tracker;
        this.sender = sender;
    }

    public boolean isActive() {
        return active;
    }

    public void stop() {
        active = false;
    }

    @Override
    public void run() {
        active = true;
        long failed = 0;
        long success = 0;
        long skipped = 0;
        long tStart = System.currentTimeMillis();
        try {
            String[] list = plugin.directoryIslands.list(IslandUtil.createIslandFilenameFilter());
            long total = list != null ? list.length : 0;
            if (list != null) {
                for (String fileName : list) {
                    if (!active) {
                        break;
                    }
                    String islandName = FileUtil.getBasename(fileName);
                    IslandInfo islandInfo = plugin.getIslandInfo(islandName);
                    try {
                        if (WorldGuardHandler.protectIsland(plugin, sender, islandInfo)) {
                            success++;
                        } else {
                            skipped++;
                        }
                    } catch (Exception e) {
                        log.log(Level.INFO, "Error occurred trying to process " + fileName, e);
                        failed++;
                    }
                    tracker.progressUpdate(success + failed + skipped, total, failed, skipped, getElapsed(tStart));
                }
            }
        } finally {
            if (!active) {
                sender.sendMessage(tr("\u00a7cABORTED:\u00a7e Protect-All was aborted!"));
            }
            active = false;
        }
        String message = tr("\u00a7eCompleted protect-all in {0}, {1} new regions were created!", getElapsed(tStart), success);
        if (sender instanceof Player && ((Player)sender).isOnline()) {
            sender.sendMessage(message);
        }
        LogUtil.log(Level.INFO, message);
    }

    private String getElapsed(long tStart) {
        return TimeUtil.millisAsString(System.currentTimeMillis() - tStart);
    }
}
