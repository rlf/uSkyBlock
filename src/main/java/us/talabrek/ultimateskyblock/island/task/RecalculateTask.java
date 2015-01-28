package us.talabrek.ultimateskyblock.island.task;

import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.async.IncrementalTask;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Recalculates the listed players island-score
 */
public class RecalculateTask implements IncrementalTask {
    private final uSkyBlock plugin;
    private final List<String> locations;
    private final int size;

    public RecalculateTask(uSkyBlock plugin, Set<String> locations) {
        this.plugin = plugin;
        this.locations = new ArrayList<>(locations);
        this.size = locations.size();
    }

    @Override
    public boolean execute(Plugin bukkitPlugin, int offset, int length) {
        for (int i = 0; i < Math.min(locations.size(), length) && !locations.isEmpty(); i++) {
            plugin.recalculateScore(null, locations.remove(0));
        }
        return isComplete();
    }

    @Override
    public int getLength() {
        return size;
    }

    @Override
    public boolean isComplete() {
        return locations.isEmpty();
    }
}
