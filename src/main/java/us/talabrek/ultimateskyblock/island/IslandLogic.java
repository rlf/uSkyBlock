package us.talabrek.ultimateskyblock.island;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import us.talabrek.ultimateskyblock.WorldEditHandler;
import us.talabrek.ultimateskyblock.WorldGuardHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

/**
 * Responsible for island creation, locating locations, purging, clearing etc.
 */
public class IslandLogic {
    private final uSkyBlock plugin;

    public IslandLogic(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    public void loadIslandChunks(Location l, int radius) {
        World world = l.getWorld();
        final int px = l.getBlockX();
        final int pz = l.getBlockZ();
        for (int x = -radius-16; x <= radius+16; x += 16) {
            for (int z = -radius-16; z <= radius+16; z += 16) {
                world.loadChunk((px + x) / 16, (pz + z) / 16);
            }
        }
    }

    public void clearIsland(Location loc) {
        World skyBlockWorld = plugin.getWorld();
        ApplicableRegionSet applicableRegions = WorldGuardHandler.getWorldGuard().getRegionManager(skyBlockWorld).getApplicableRegions(loc);
        for (ProtectedRegion region : applicableRegions) {
            if (!region.getId().equalsIgnoreCase("__global__")) {
                WorldEditHandler.clearIsland(skyBlockWorld, region);
            }
        }
    }

}
