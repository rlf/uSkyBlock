package us.talabrek.ultimateskyblock.island;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import us.talabrek.ultimateskyblock.PlayerInfo;
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

    public void clearIsland(PlayerInfo playerInfo) {
        FileConfiguration islandConfig = plugin.getIslandConfig(playerInfo);
        String regionName = islandConfig.getString("party.leader") + "Island";
        ProtectedRegion region = WorldGuardHandler.getWorldGuard().getRegionManager(plugin.getWorld()).getRegion(regionName);
        Region worldEditRegion = new CuboidRegion(plugin.getWorld(),
                region.getMinimumPoint(),
                region.getMaximumPoint());
    }
}
