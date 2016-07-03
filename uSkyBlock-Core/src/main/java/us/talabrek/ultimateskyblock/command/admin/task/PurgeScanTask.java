package us.talabrek.ultimateskyblock.command.admin.task;

import dk.lockfuglsang.minecraft.file.FileUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.IslandUtil;
import us.talabrek.ultimateskyblock.util.ProgressTracker;
import us.talabrek.ultimateskyblock.util.TimeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;
import static us.talabrek.ultimateskyblock.util.LogUtil.log;

/**
 * Scans for all players on a list of islands.
 */
public class PurgeScanTask extends BukkitRunnable {
    private final List<String> islandList;
    private final List<String> purgeList;
    private final long cutOff;
    private final uSkyBlock plugin;
    private final CommandSender sender;
    private final double purgeLevel;
    private final ProgressTracker tracker;
    private final long tStart;
    private volatile boolean active;
    private boolean done;

    public PurgeScanTask(uSkyBlock plugin, File islandDir, int time, CommandSender sender) {
        this.plugin = plugin;
        this.sender = sender;
        this.cutOff = System.currentTimeMillis() - (time * 3600000L);
        String[] islandList = islandDir.list(IslandUtil.createIslandFilenameFilter());
        this.islandList = new ArrayList<>(Arrays.asList(islandList));
        purgeList = new ArrayList<>();
        purgeLevel = plugin.getConfig().getDouble("options.advanced.purgeLevel", 10);
        int feedbackEvery = plugin.getConfig().getInt("async.long.feedbackEvery", 30000);
        tStart = System.currentTimeMillis();
        tracker = new ProgressTracker(sender, marktr("\u00a77- SCANNING: {0,number,##}% ({1}/{2} failed: {3}) ~ {4}"), 25, feedbackEvery);
        active = true;
    }

    private void generatePurgeList() {
        int progress = 0;
        int failed = 0;
        int total = islandList.size();
        while (!islandList.isEmpty()) {
            if (!active) {
                break;
            }
            String islandFile = islandList.remove(0);
            String islandName = FileUtil.getBasename(islandFile);
            try {
                IslandInfo islandInfo = plugin.getIslandInfo(islandName);
                if (islandInfo != null) {
                    Set<UUID> members = islandInfo.getMemberUUIDs();
                    if (!islandInfo.ignore() && islandInfo.getLevel() < purgeLevel && abandonedSince(members)) {
                        purgeList.add(islandName);
                    }
                }
            } catch (Exception e) {
                failed++;
            }
            progress++;
            tracker.progressUpdate(progress, total, failed, TimeUtil.millisAsString(System.currentTimeMillis()-tStart));
        }
    }

    public boolean isActive() {
        return active;
    }

    public boolean isDone() {
        return done;
    }

    public void stop() {
        active = false;
    }

    public List<String> getPurgeList() {
        return purgeList;
    }

    private boolean abandonedSince(Set<UUID> members) {
        for (UUID member : members) {
            PlayerInfo playerInfo = plugin.getPlayerInfo(member);
            if (playerInfo != null && playerInfo.getLastSaved() > cutOff) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void run() {
        generatePurgeList();
        if (!active) {
            sender.sendMessage(tr("\u00a74PURGE:\u00a79 Scanning aborted."));
            return;
        }
        log(Level.INFO, "Done scanning - found " + purgeList.size() + " candidates for purging.");
        sender.sendMessage(tr("\u00a74PURGE:\u00a79 Scanning done, found {0} candidates for purgatory.", purgeList.size()));
        done = true;
        if (!purgeList.isEmpty()) {
            int timeout = plugin.getConfig().getInt("options.advanced.purgeTimeout", 600000);
            sender.sendMessage(tr("\u00a74PURGE:\u00a7e Repeat the command within {0} to accept.", TimeUtil.millisAsString(timeout)));
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (active) {
                        sender.sendMessage("\u00a77purge timed out");
                        active = false;
                    }
                }
            }.runTaskLaterAsynchronously(plugin, TimeUtil.millisAsTicks(timeout));
        } else {
            active = false;
        }
    }
}
