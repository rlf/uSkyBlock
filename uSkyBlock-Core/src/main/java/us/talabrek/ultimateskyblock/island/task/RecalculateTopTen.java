package us.talabrek.ultimateskyblock.island.task;

import org.bukkit.scheduler.BukkitRunnable;
import us.talabrek.ultimateskyblock.api.event.uSkyBlockEvent;
import us.talabrek.ultimateskyblock.api.async.Callback;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 */
public class RecalculateTopTen extends BukkitRunnable {
    private final List<String> locations;
    private final uSkyBlock plugin;

    public RecalculateTopTen(uSkyBlock plugin, Set<String> locations) {
        this.plugin = plugin;
        this.locations = new ArrayList<>(locations);
    }

    @Override
    public void run() {
        if (!locations.isEmpty()) {
            String islandName = locations.remove(0);
            plugin.calculateScoreAsync(null, islandName, new Callback<us.talabrek.ultimateskyblock.api.model.IslandScore>() {
                @Override
                public void run() {
                    // We use the deprecated on purpose (the other would fail).
                    plugin.async(RecalculateTopTen.this);
                }
            });
        } else {
            plugin.fireAsyncEvent(new uSkyBlockEvent(null, plugin.getAPI(), uSkyBlockEvent.Cause.RANK_UPDATED));
        }
    }
}
