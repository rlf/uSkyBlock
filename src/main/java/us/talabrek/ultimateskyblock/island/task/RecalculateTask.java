package us.talabrek.ultimateskyblock.island.task;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.api.event.uSkyBlockScoreChangedEvent;
import us.talabrek.ultimateskyblock.async.IncrementalTask;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.island.IslandScore;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Recalculates the listed players island-score
 */
public class RecalculateTask implements IncrementalTask {
    private final uSkyBlock plugin;
    private final List<String> locations;
    private final int islandArea;
    private final int size;

    private World world;
    private int radius;
    private int px = 0;
    private int pz = 0;
    private int x = 0;
    private int z = 0;
    private int[] blockCount = null;
    private String islandName = null;

    public RecalculateTask(uSkyBlock plugin, Set<String> locations) {
        this.plugin = plugin;
        this.locations = new ArrayList<>(locations);
        this.islandArea = Settings.island_protectionRange * Settings.island_protectionRange;
        this.size = locations.size() * islandArea;
        world = plugin.getWorld();
        radius = Settings.island_radius;
    }

    @Override
    public boolean execute(Plugin bukkitPlugin, int offset, int length) {
        for (int i = 0; i < length; i++) {
            try {
                int islandOffset = (offset + i) % islandArea;
                if (islandOffset == 0 && !locations.isEmpty()) {
                    // First coordinate in this island.
                    prepareNextIsland();
                }
                x++;
                if (x > radius) {
                    x = -radius;
                    z++;
                }
                plugin.getLevelLogic().addBlockCount(world, px + x, pz + z, blockCount);
                if (x == radius && z == radius) {
                    IslandScore islandScore = plugin.getLevelLogic().createIslandScore(blockCount);
                    IslandInfo islandInfo = plugin.getIslandInfo(islandName);
                    if (islandInfo != null) {
                        plugin.updateScore(null, islandInfo, islandScore);
                    }
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Error occurred during island-calculation of " + islandName, e);
                i += islandArea;
            }
        }
        return isComplete();
    }

    private void prepareNextIsland() {
        islandName = locations.remove(0);
        String[] xz = islandName.split(",");
        if (xz == null || xz.length != 2) {
            throw new IllegalStateException("Invalid island-name " + islandName + " found! Skipping.");
        }
        px = Integer.parseInt(xz[0]);
        pz = Integer.parseInt(xz[1]);
        x = -radius-1; // we do x++ in a sec
        z = -radius;
        blockCount = plugin.getLevelLogic().createBlockCountArray();
    }

    @Override
    public int getLength() {
        return size;
    }

    @Override
    public boolean isComplete() {
        return locations.isEmpty() && x >= radius && z >= radius;
    }
}
