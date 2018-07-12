package us.talabrek.ultimateskyblock.island;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import us.talabrek.ultimateskyblock.async.Callback;
import us.talabrek.ultimateskyblock.handler.AsyncWorldEditHandler;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.List;

public class AweLevelLogic extends CommonLevelLogic {

    public AweLevelLogic(uSkyBlock plugin, FileConfiguration config) {
        super(plugin, config);
    }

    @Override
    public void calculateScoreAsync(Location l, Callback<IslandScore> callback) {
        plugin.async(() -> {
            ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(l);
            if (region == null) {
                return;
            }
            int[] blockCountArray = createBlockCountArray();
            EditSession editSession = AsyncWorldEditHandler.getAWEAdaptor().createEditSession(new BukkitWorld(l.getWorld()), -1);
            List<Countable<BaseBlock>> distribution = editSession.getBlockDistributionWithData(WorldEditHandler.getRegion(plugin.getWorld(), region));
            distribution.forEach((a) -> incBlockCount(a.getID().getType(), a.getID().getData(), blockCountArray, a.getAmount()));

            IslandScore islandScore = createIslandScore(blockCountArray);
            Location netherLocation = getNetherLocation(l);
            ProtectedRegion netherRegion = WorldGuardHandler.getNetherRegionAt(netherLocation);
            if (netherRegion != null && islandScore.getScore() > activateNetherAtLevel && plugin.getSkyBlockNetherWorld() != null) {
                editSession = AsyncWorldEditHandler.getAWEAdaptor().createEditSession(new BukkitWorld(netherLocation.getWorld()), -1);
                distribution = editSession.getBlockDistributionWithData(WorldEditHandler.getRegion(plugin.getSkyBlockNetherWorld(), netherRegion));
                distribution.forEach((a) -> incBlockCount(a.getID().getType(), a.getID().getData(), blockCountArray, a.getAmount()));
                islandScore = createIslandScore(blockCountArray);
            }

            callback.setState(islandScore);
            plugin.sync(callback);
        });
    }
}
