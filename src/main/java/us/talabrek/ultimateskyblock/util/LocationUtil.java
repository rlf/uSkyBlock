package us.talabrek.ultimateskyblock.util;

import org.bukkit.Location;

/**
 * Responsible for various transformations of locations.
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
}
