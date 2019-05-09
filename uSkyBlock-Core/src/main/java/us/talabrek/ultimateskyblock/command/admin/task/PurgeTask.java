package us.talabrek.ultimateskyblock.command.admin.task;

import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.ProgressTracker;
import dk.lockfuglsang.minecraft.util.TimeUtil;

import java.util.List;
import java.util.logging.Level;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static us.talabrek.ultimateskyblock.util.LogUtil.log;

/**
 * Scans for all players on a list of islands.
 */
public class PurgeTask extends BukkitRunnable {
    private final List<String> purgeList;
    private final uSkyBlock plugin;
    private final CommandSender sender;
    private final int feedbackEvery;
    private final ProgressTracker tracker;
    private final long tStart;
    private boolean active;

    public PurgeTask(uSkyBlock plugin, List<String> purgeList, CommandSender sender) {
        this.plugin = plugin;
        this.sender = sender;
        this.purgeList = purgeList;
        tStart = System.currentTimeMillis();
        feedbackEvery = plugin.getConfig().getInt("async.long.feedbackEvery", 30000);
        tracker = new ProgressTracker(sender, marktr("- PURGING: {0,number,##}% ({1}/{2}), elapsed {3}, estimated completion ~{4}"), 25, feedbackEvery);
        active = true;
    }

    private void doPurge() {
        int total = purgeList.size();
        int cnt = 0;
        while (!purgeList.isEmpty()) {
            if (!active) {
                break;
            }
            final String islandName = purgeList.remove(0);
            plugin.getIslandLogic().purge(islandName);
            cnt++;
            long elapsed = System.currentTimeMillis() - tStart;
            long eta = (elapsed / cnt) * (total - cnt);
            tracker.progressUpdate(cnt, total, TimeUtil.millisAsString(elapsed), TimeUtil.millisAsString(eta));
        }
        plugin.getOrphanLogic().save();
    }

    public boolean isActive() {
        return active;
    }

    public synchronized void stop() {
        active = false;
    }

    @Override
    public void run() {
        try {
            doPurge();
            log(Level.INFO, "Finished purging marked inactive islands.");
            if (active) {
                sender.sendMessage(I18nUtil.tr("\u00a74PURGE:\u00a79 Finished purging abandoned islands."));
            } else {
                sender.sendMessage(I18nUtil.tr("\u00a74PURGE:\u00a79 Aborted purging abandoned islands."));
            }
        } finally {
            active = false;
        }
    }
}
