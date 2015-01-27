package us.talabrek.ultimateskyblock.command.admin.task;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.talabrek.ultimateskyblock.async.IncrementalTask;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.FileUtil;
import us.talabrek.ultimateskyblock.util.TimeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * An incremental (synchroneous) task for protecting all islands.
 */
public class ProtectAllTask extends BukkitRunnable implements IncrementalTask {
    private final uSkyBlock plugin;
    private final CommandSender sender;

    private List<String> islandNames = new ArrayList<>();
    private int size;
    private int success = 0;

    public ProtectAllTask(uSkyBlock plugin, CommandSender sender) {
        this.plugin = plugin;
        this.sender = sender;
    }

    @Override
    public boolean execute(Plugin javaPlugin, int offset, int length) {
        for (int i = 0; i < length && !islandNames.isEmpty(); i++) {
            String fileName = islandNames.remove(0);
            String islandName = FileUtil.getBasename(fileName);
            IslandInfo islandInfo = plugin.getIslandInfo(islandName);
            try {
                if (WorldGuardHandler.protectIsland(plugin, sender, islandInfo)) {
                    success++;
                }
            } catch (Exception e) {
                log.log(Level.INFO, "Error occurred trying to process " + fileName, e);
            }
        }
        return isComplete();
    }

    @Override
    public int getLength() {
        return size;
    }

    @Override
    public boolean isComplete() {
        return islandNames.isEmpty();
    }

    @Override
    public void run() {
        String[] list = plugin.directoryIslands.list(FileUtil.createIslandFilenameFilter());
        islandNames.addAll(Arrays.asList(list));
        size = list.length;
        final long tStart = System.currentTimeMillis();
        plugin.getExecutor().execute(plugin, this, new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                String message = String.format("\u00a7eCompleted protect-all in %s, %d new regions were created!", TimeUtil.millisAsString(now-tStart), success);
                if (sender instanceof Player && ((Player)sender).isOnline()) {
                    sender.sendMessage(message);
                }
                plugin.log(Level.INFO, plugin.stripFormatting(message));
                plugin.setProtectAllActive(false);
            }
        }, 0.2f, 1);
    }
}
