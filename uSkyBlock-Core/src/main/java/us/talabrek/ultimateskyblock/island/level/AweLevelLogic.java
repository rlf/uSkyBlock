package us.talabrek.ultimateskyblock.island.level;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import us.talabrek.ultimateskyblock.api.async.Callback;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.HashSet;
import java.util.Set;

public class AweLevelLogic extends CommonLevelLogic {

    private Set<Location> running = new HashSet<>();

    public AweLevelLogic(uSkyBlock plugin, FileConfiguration config) {
        super(plugin, config);
    }

    @Override
    public void calculateScoreAsync(Location l, Callback<IslandScore> callback) {
        throw new UnsupportedOperationException("Not supported until WorldEdit releases a Bukkit 1.13 version");
            /*
        if (running.contains(l)) {
            return;
        }
        running.add(l);
        plugin.async(() -> {
            ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(l);
            if (region == null) {
                return;
            }
            BlockCountCollection counts = new BlockCountCollection(scoreMap);

            EditSession editSession = AsyncWorldEditHandler.getAWEAdaptor().createEditSession(new BukkitWorld(l.getWorld()), -1);
            List<Countable<BaseBlock>> distribution = editSession.getBlockDistributionWithData(WorldEditHandler.getRegion(plugin.getWorld(), region));
            // TODO: Rasmus - 19-07-2018: fix this for 1.13 - once AWE has Material instead of ID for block-distribution
            distribution.forEach((a) -> counts.add(Material.getMaterial(a.getID().getType()), (byte) (a.getID().getData()&0xff),a.getAmount()));

            IslandScore islandScore = createIslandScore(counts);
            Location netherLocation = getNetherLocation(l);
            ProtectedRegion netherRegion = WorldGuardHandler.getNetherRegionAt(netherLocation);
            if (netherRegion != null && islandScore.getScore() > activateNetherAtLevel && plugin.getSkyBlockNetherWorld() != null) {
                editSession = AsyncWorldEditHandler.getAWEAdaptor().createEditSession(new BukkitWorld(netherLocation.getWorld()), -1);
                distribution = editSession.getBlockDistributionWithData(WorldEditHandler.getRegion(plugin.getSkyBlockNetherWorld(), netherRegion));
                distribution.forEach((a) -> counts.add(Material.getMaterial(a.getID().getType()), (byte) (a.getID().getData()&0xff),a.getAmount()));
                islandScore = createIslandScore(counts);
            }
            callback.setState(islandScore);
            plugin.sync(callback);
            running.remove(l);
        });
            */
    }
}
