package us.talabrek.ultimateskyblock.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.MaterialData;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.logging.Level;

import static us.talabrek.ultimateskyblock.util.BlockUtil.isBreathable;

/**
 * Responsible for various transformations and queries of locations.
 */
public enum LocationUtil {;
    public static String asString(Location loc) {
        if (loc == null) {
            return null;
        }
        String s = "";
        if (loc.getWorld() != null && loc.getWorld().getName() != null) {
            s += loc.getWorld().getName() + ":";
        }
        s += String.format("%5.2f,%5.2f,%5.2f", loc.getX(), loc.getY(), loc.getZ());
        if (loc.getYaw() != 0f || loc.getPitch() != 0f) {
            s += String.format(":%3.2f:%3.2f", loc.getYaw(), loc.getPitch());
        }
        return s;
    }

    public static boolean isEmptyLocation(Location location) {
        return location == null || (location.getBlockX() == 0 && location.getBlockZ() == 0 && location.getBlockY() == 0);
    }

    public static String getIslandName(Location location) {
        if (location == null) {
            return null;
        }
        return location.getBlockX() + "," + location.getBlockZ();
    }

    public static Location centerOnBlock(Location loc) {
        if (loc == null) {
            return null;
        }
        return new Location(loc.getWorld(),
                loc.getBlockX() + 0.5, loc.getBlockY() + 0.1, loc.getBlockZ() + 0.5,
                loc.getYaw(), loc.getPitch());
    }

    public static boolean isSafeLocation(final Location l) {
        if (l == null) {
            return false;
        }
        final Block ground = l.getBlock().getRelative(BlockFace.DOWN);
        final Block air1 = l.getBlock();
        final Block air2 = l.getBlock().getRelative(BlockFace.UP);
        return ground.getType().isSolid() && isBreathable(air1) && isBreathable(air2);
    }

    public static void loadChunkAt(Location homeSweetHome) {
        if (!homeSweetHome.getWorld().isChunkLoaded(homeSweetHome.getBlockX() >> 4, homeSweetHome.getBlockZ() >> 4)) {
            homeSweetHome.getWorld().loadChunk(homeSweetHome.getBlockX() >> 4, homeSweetHome.getBlockZ() >> 4);
        }
    }

    /**
     * Finds the nearest block to loc that is a chest.
     *
     * @param loc The location to scan for a chest.
     * @return The location of the chest
     */
    public static Location findChestLocation(final Location loc) {
        loadChunkAt(loc);
        World world = loc.getWorld();
        int px = loc.getBlockX();
        int pz = loc.getBlockZ();
        int py = loc.getBlockY();
        for (int dy = 1; dy <= 30; dy++) {
            for (int dx = 1; dx <= 30; dx++) {
                for (int dz = 1; dz <= 30; dz++) {
                    // Scans from the center and out
                    int x = px + (dx % 2 == 0 ? dx / 2 : -dx / 2);
                    int z = pz + (dz % 2 == 0 ? dz / 2 : -dz / 2);
                    int y = py + (dy % 2 == 0 ? dy / 2 : -dy / 2);
                    if (world.getBlockAt(x, y, z).getType() == Material.CHEST) {
                        return new Location(world, x, y, z);
                    }
                }
            }
        }
        return null;
    }

    public static Location findNearestSpawnLocation(Location loc) {
        loadChunkAt(loc);
        World world = loc.getWorld();
        int px = loc.getBlockX();
        int pz = loc.getBlockZ();
        int py = loc.getBlockY();
        Block chestBlock = world.getBlockAt(loc);
        if (chestBlock.getType() == Material.CHEST) {
            BlockFace primaryDirection = null;
            // Start by checking in front of the chest.
            MaterialData data = chestBlock.getState().getData();
            if (data instanceof org.bukkit.material.Chest) {
                primaryDirection = ((org.bukkit.material.Chest) data).getFacing();
            }
            if (primaryDirection == BlockFace.NORTH) {
                // Neg Z
                pz -= 1; // start one block in the north dir.
            } else if (primaryDirection == BlockFace.SOUTH) {
                // Pos Z
                pz += 1; // start one block in the south dir
            } else if (primaryDirection == BlockFace.WEST) {
                // Neg X
                px -= 1; // start one block in the west dir
            } else if (primaryDirection == BlockFace.EAST) {
                // Pos X
                px += 1; // start one block in the east dir
            }
        }
        return findNearestSafeLocation(new Location(loc.getWorld(), px, py, pz), loc);
    }

    public static Location findNearestSafeLocation(Location loc, Location lookAt) {
        if (loc == null) {
            return null;
        }
        loadChunkAt(loc);
        World world = loc.getWorld();
        int px = loc.getBlockX();
        int pz = loc.getBlockZ();
        int py = loc.getBlockY();
        for (int dy = 1; dy <= 30; dy++) {
            for (int dx = 1; dx <= 30; dx++) {
                for (int dz = 1; dz <= 30; dz++) {
                    // Scans from the center and out
                    int x = px + (dx % 2 == 0 ? dx / 2 : -dx / 2);
                    int z = pz + (dz % 2 == 0 ? dz / 2 : -dz / 2);
                    int y = py + (dy % 2 == 0 ? dy / 2 : -dy / 2);
                    Location spawnLocation = new Location(world, x, y, z);
                    if (isSafeLocation(spawnLocation)) {
                        // look at the old location
                        spawnLocation = centerOnBlock(spawnLocation);
                        if (lookAt != null) {
                            Location d = centerOnBlock(lookAt).subtract(spawnLocation);
                            spawnLocation.setDirection(d.toVector());
                        } else {
                            spawnLocation.setYaw(loc.getYaw());
                            spawnLocation.setPitch(loc.getPitch());
                        }
                        uSkyBlock.log(Level.FINER, "found safe location " + spawnLocation + " near " + loc + ", looking at " + lookAt);
                        return spawnLocation;
                    }
                }
            }
        }
        return null;
    }
}
