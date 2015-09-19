package us.talabrek.ultimateskyblock.command.admin.task;

import org.bukkit.command.CommandSender;
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

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

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
    private final CommandSender sender;
    private final double purgeLevel;

    public PurgeScanTask(uSkyBlock plugin, File islandDir, int time, CommandSender sender) {
        this.plugin = plugin;
        this.sender = sender;
        now = System.currentTimeMillis();
        this.cutOff = now - (time * 3600000L);
        String[] islandList = islandDir.list(FileUtil.createIslandFilenameFilter());
        this.islandList = new ArrayList<>(Arrays.asList(islandList));
        size = islandList.length;
        purgeList = new ArrayList<>();
        purgeLevel = plugin.getConfig().getDouble("options.advanced.purgeLevel", 10);
    }

    @Override
    public boolean execute(Plugin bukkitPlugin, int offset, int length) {
        for (int i = 0; i < length && !islandList.isEmpty(); i++) {
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
        return isComplete();
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
                    sender.sendMessage(tr("\u00a74PURGE:\u00a79 Finished purging abandoned islands."));
                    plugin.deactivatePurge();
                }
            }
        };
        final Runnable onScanCompletion = new Runnable() {
            @Override
            public void run() {
                plugin.log(Level.INFO, "Done scanning - found " + purgeList.size()+ " candidates for purging.");
                sender.sendMessage(tr("\u00a74PURGE:\u00a79 Scanning done, found {0} candidates for purgatory.", purgeList.size()));
                plugin.getAsyncExecutor().execute(plugin, new PurgeTask(plugin, purgeList, sender), onPurgeCompletion, 0.3f, 1);
            }
        };
        plugin.getAsyncExecutor().execute(plugin, this, onScanCompletion, 1f, 1);
    }
}
