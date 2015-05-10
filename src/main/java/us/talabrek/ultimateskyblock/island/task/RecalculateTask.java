package us.talabrek.ultimateskyblock.island.task;

import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.api.event.uSkyBlockEvent;
import us.talabrek.ultimateskyblock.async.Callback;
import us.talabrek.ultimateskyblock.async.IncrementalTask;
import us.talabrek.ultimateskyblock.island.IslandScore;
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
        size = locations.size();
    }

    @Override
    public boolean execute(Plugin bukkitPlugin, int offset, int length) {
        for (int i = 0; i < length && !locations.isEmpty(); i++) {
            plugin.calculateScoreAsync(null, locations.remove(0), new Callback<IslandScore>() {
                @Override
                public void run() {
                    if (locations.isEmpty()) {
                        plugin.fireChangeEvent(new uSkyBlockEvent(null, plugin.getAPI(), uSkyBlockEvent.Cause.RANK_UPDATED));
                    }
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
        return locations.isEmpty();
    }
}
