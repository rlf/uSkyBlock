package us.talabrek.ultimateskyblock.command.admin.task;

import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.async.IncrementalTask;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.List;
import java.util.logging.Level;

/**
 * The PurgeCommand as an IncrementtalTask.
 */
public class PurgeTask implements IncrementalTask {

    private final uSkyBlock skyBlock;
    private final List<String> removeList;
    private final int size;

    public PurgeTask(uSkyBlock plugin, List<String> removeList) {
        this.skyBlock = plugin;
        this.removeList = removeList;
        size = removeList.size();
    }
    @Override
    public boolean execute(Plugin plugin, int offset, int length) {
        for (int i = 0; i < length && !removeList.isEmpty(); i++) {
            final String playerName = removeList.remove(0);
            skyBlock.deletePlayerIsland(playerName, new Runnable() {
                @Override
                public void run() {
                    skyBlock.log(Level.INFO, "Purge: Removed " + playerName + "'s island");
                }
            });
        }
        return isComplete();
    }

    @Override
    public int getLength() {
        return size;
    }

    @Override
    public boolean isComplete() {
        return removeList.isEmpty();
    }
}
