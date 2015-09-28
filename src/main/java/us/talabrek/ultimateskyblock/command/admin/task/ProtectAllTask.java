package us.talabrek.ultimateskyblock.command.admin.task;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.async.IncrementalRunnable;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.FileUtil;
import us.talabrek.ultimateskyblock.util.TimeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static us.talabrek.ultimateskyblock.util.FormatUtil.stripFormatting;

/**
 * An incremental (synchroneous) task for protecting all islands.
 */
public class ProtectAllTask extends IncrementalRunnable {
    private static final Logger log = Logger.getLogger(ProtectAllTask.class.getName());
    private final CommandSender sender;

    private List<String> islandNames = null;
    private int success = 0;

    public ProtectAllTask(final uSkyBlock plugin, final CommandSender sender) {
        super(plugin);
        setOnCompletion(new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                String message = String.format("\u00a7eCompleted protect-all in %s, %d new regions were created!", TimeUtil.millisAsString(getTimeElapsed()), success);
                if (sender instanceof Player && ((Player)sender).isOnline()) {
                    sender.sendMessage(message);
                }
                plugin.log(Level.INFO, stripFormatting(message));
                plugin.setProtectAllActive(false);
            }
        });
        this.sender = sender;
    }

    private void init() {
        if (islandNames == null) {
            String[] list = getPlugin().directoryIslands.list(FileUtil.createIslandFilenameFilter());
            islandNames = new ArrayList<>(Arrays.asList(list));
        }
    }
    @Override
    public boolean execute() {
        init();
        while (!isComplete()) {
            String fileName = islandNames.remove(0);
            String islandName = FileUtil.getBasename(fileName);
            IslandInfo islandInfo = getPlugin().getIslandInfo(islandName);
            try {
                if (WorldGuardHandler.protectIsland(getPlugin(), sender, islandInfo)) {
                    success++;
                }
            } catch (Exception e) {
                log.log(Level.INFO, "Error occurred trying to process " + fileName, e);
            }
            if (!tick()) {
                break;
            }
        }
        return isComplete();
    }

    public boolean isComplete() {
        return islandNames.isEmpty();
    }
}
