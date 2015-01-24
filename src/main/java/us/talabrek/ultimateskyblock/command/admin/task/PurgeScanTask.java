package us.talabrek.ultimateskyblock.command.admin.task;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.talabrek.ultimateskyblock.async.IncrementalTask;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Scans for all players on a list of islands.
 */
public class PurgeScanTask extends BukkitRunnable implements IncrementalTask {
    private final List<String> islandList;
    private final List<String> purgeList;
    private final int size;
    private final long cutOff;
    private final long now;
    private final uSkyBlock plugin;

    public PurgeScanTask(uSkyBlock plugin, File islandDir, int time) {
        this.plugin = plugin;
        now = System.currentTimeMillis();
        this.cutOff = now - (time * 3600000);
        String[] islandList = islandDir.list(FileUtil.createYmlFilenameFilter());
        this.islandList = Arrays.asList(islandList);
        size = islandList.length;
        purgeList = new ArrayList<>();
    }

    @Override
    public boolean execute(Plugin bukkitPlugin, int offset, int length) {
        for (int i = 0; i < length && !islandList.isEmpty(); i++) {
            String islandFile = islandList.remove(0);
            String islandName = FileUtil.getBasename(islandFile);
            IslandInfo islandInfo = plugin.getIslandInfo(islandName);
            if (islandInfo != null) {
                if (islandInfo.getLevel() < 10 && offlineSince(islandInfo.getMembers())) {
                    purgeList.add(islandName);
                }
            }
        }
        return isComplete();
    }

    private boolean offlineSince(Set<String> members) {
        for (String member : members) {
            OfflinePlayer oplayer = Bukkit.getOfflinePlayer(member);
            if (oplayer.isOnline() || oplayer.getLastPlayed() > cutOff) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getLength() {
        return size;
    }

    @Override
    public boolean isComplete() {
        return islandList.isEmpty();
    }

    @Override
    public void run() {
        final Runnable onPurgeCompletion = new Runnable() {
            @Override
            public void run() {
                if (plugin.isPurgeActive()) {
                    plugin.log(Level.INFO, "Finished purging marked inactive islands.");
                    plugin.deactivatePurge();
                }
            }
        };
        final Runnable onScanCompletion = new Runnable() {
            @Override
            public void run() {
                plugin.log(Level.INFO, "Done scanning - found " + purgeList.size()+ " candidates for purging.");
                plugin.getExecutor().execute(plugin, new PurgeTask(plugin, purgeList), onPurgeCompletion, 0.3f, 1);
            }
        };
        plugin.getExecutor().execute(plugin, this, onScanCompletion, 0.2f, 1);
    }
}
