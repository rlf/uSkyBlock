package us.talabrek.ultimateskyblock.command.admin.task;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.FileUtil;
import us.talabrek.ultimateskyblock.util.I18nUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Scans for all players on a list of islands.
 */
public class PurgeScanTask extends BukkitRunnable {
    private final List<String> islandList;
    private final List<String> purgeList;
    private final long cutOff;
    private final long now;
    private final uSkyBlock plugin;
    private final CommandSender sender;
    private final double purgeLevel;
    private final int feedbackEvery;
    private long lastContact;

    public PurgeScanTask(uSkyBlock plugin, File islandDir, int time, CommandSender sender) {
        this.plugin = plugin;
        this.sender = sender;
        now = System.currentTimeMillis();
        this.cutOff = now - (time * 3600000L);
        String[] islandList = islandDir.list(FileUtil.createIslandFilenameFilter());
        this.islandList = new ArrayList<>(Arrays.asList(islandList));
        purgeList = new ArrayList<>();
        purgeLevel = plugin.getConfig().getDouble("options.advanced.purgeLevel", 10);
        feedbackEvery = plugin.getConfig().getInt("async.feedbackEvery", 5000);
        lastContact = System.currentTimeMillis();
    }

    private void generatePurgeList() {
        while (!islandList.isEmpty()) {
            String islandFile = islandList.remove(0);
            String islandName = FileUtil.getBasename(islandFile);
            IslandInfo islandInfo = plugin.getIslandInfo(islandName);
            if (islandInfo != null) {
                Set<String> members = islandInfo.getMembers();
                if (!islandInfo.ignore() && islandInfo.getLevel() < purgeLevel && abandonedSince(members)) {
                    purgeList.add(islandName);
                }
            }
        }
    }

    private boolean abandonedSince(Set<String> members) {
        for (String member : members) {
            PlayerInfo playerInfo = plugin.getPlayerInfo(member);
            if (playerInfo != null && playerInfo.getLastSaved() > cutOff) {
                return false;
            }
        }
        return true;
    }

    private void doPurge() {
        int total = purgeList.size();
        int cnt = 0;
        while (!purgeList.isEmpty()) {
            final String islandName = purgeList.remove(0);
            plugin.getIslandLogic().purge(islandName);
            long now = System.currentTimeMillis();
            if (now >= (lastContact + feedbackEvery)) {
                lastContact = now;
                sender.sendMessage(I18nUtil.tr("\u00a7cPURGE:\u00a79 Purged {0}/{1} {2,number,###}%", cnt, total, 100f * (cnt) / total));
            }
            cnt++;
        }
        plugin.getOrphanLogic().save();
    }

    @Override
    public void run() {
        try {
            generatePurgeList();
            plugin.log(Level.INFO, "Done scanning - found " + purgeList.size() + " candidates for purging.");
            sender.sendMessage(I18nUtil.tr("\u00a74PURGE:\u00a79 Scanning done, found {0} candidates for purgatory.", purgeList.size()));
            doPurge();
            plugin.log(Level.INFO, "Finished purging marked inactive islands.");
            sender.sendMessage(I18nUtil.tr("\u00a74PURGE:\u00a79 Finished purging abandoned islands."));
        } finally {
            plugin.deactivatePurge();
        }
    }
}
