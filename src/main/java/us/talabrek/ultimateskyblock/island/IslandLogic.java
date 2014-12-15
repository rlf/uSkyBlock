package us.talabrek.ultimateskyblock.island;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.WorldEditHandler;
import us.talabrek.ultimateskyblock.WorldGuardHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Responsible for island creation, locating locations, purging, clearing etc.
 */
public class IslandLogic {
    private final uSkyBlock plugin;

    private final Map<String, IslandInfo> islands = new ConcurrentHashMap<>();

    public IslandLogic(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    public IslandInfo getIslandInfo(String islandName) {
        if (!islands.containsKey(islandName)) {
            islands.put(islandName, new IslandInfo(islandName));
        }
        return islands.get(islandName);
    }

    public IslandInfo getIslandInfo(PlayerInfo playerInfo) {
        if (playerInfo.getHasIsland()) {
            return getIslandInfo(playerInfo.locationForParty());
        }
        return null;
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
