package us.talabrek.ultimateskyblock.island;

import org.bukkit.Location;
import org.bukkit.World;
import us.talabrek.ultimateskyblock.uSkyBlock;

/**
 * Responsible for island creation, locating locations, purging, clearing etc.
 */
public class IslandLogic {
    public IslandLogic(uSkyBlock plugin) {
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

}
