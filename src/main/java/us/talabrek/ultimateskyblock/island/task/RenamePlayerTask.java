package us.talabrek.ultimateskyblock.island.task;

import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.async.IncrementalTask;
import us.talabrek.ultimateskyblock.island.IslandLogic;
import us.talabrek.ultimateskyblock.util.FileUtil;
import us.talabrek.ultimateskyblock.uuid.PlayerNameChangedEvent;

/**
 * An async incremental task for renaming a player in all island-yml files.
 */
public class RenamePlayerTask implements IncrementalTask {
    private final String playerIsland;
    private final String[] files;
    private final IslandLogic logic;
    private final PlayerNameChangedEvent[] changes;
    private int index;

    public RenamePlayerTask(String file, String[] files, IslandLogic logic, PlayerNameChangedEvent... changes) {
        this.playerIsland = file;
        this.files = files;
        this.logic = logic;
        this.changes = changes;
        index = 0;
    }

    @Override
    public boolean execute(Plugin plugin, int offset, int length) {
        if (offset == 0 && playerIsland != null) {
            logic.renamePlayer(playerIsland, changes);
        }
        for (int i = offset; i < offset+length && i <= files.length; i++) {
            if (i != 0 && !files[i-1].equalsIgnoreCase(playerIsland + ".yml")) {
                logic.renamePlayer(FileUtil.getBasename(files[i - 1]), changes);
            }
        }
        return false;
    }

    @Override
    public int getLength() {
        return files.length;
    }

    @Override
    public boolean isComplete() {
        return index >= getLength();
    }
}
