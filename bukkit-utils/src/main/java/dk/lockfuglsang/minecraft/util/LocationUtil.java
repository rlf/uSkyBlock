package dk.lockfuglsang.minecraft.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static convenience class for location methods.
 */
public enum LocationUtil {
    ;
    private static final Pattern LOCATION_PATTERN = Pattern.compile("((?<world>[^:]+):)?(?<x>[\\-0-9\\.]+),(?<y>[\\-0-9\\.]+),(?<z>[\\-0-9\\.]+)(:(?<yaw>[\\-0-9\\.]+):(?<pitch>[\\-0-9\\.]+))?");

    public static String asString(Location loc) {
        if (loc == null) {
            return null;
        }
        String s = "";
        if (loc.getWorld() != null && loc.getWorld().getName() != null) {
            s += loc.getWorld().getName() + ":";
        }
        s += String.format("%.2f,%.2f,%.2f", loc.getX(), loc.getY(), loc.getZ());
        if (loc.getYaw() != 0f || loc.getPitch() != 0f) {
            s += String.format(":%.2f:%.2f", loc.getYaw(), loc.getPitch());
        }
        return s;
    }

    /**
     * Convenience method for when a location is needed as a yml key.
     */
    public static String asKey(Location loc) {
        return asString(loc).replaceAll(":", "-").replaceAll("\\.", "_");
    }

    public static Location fromString(String locString) {
        if (locString == null || locString.isEmpty()) {
            return null;
        }
        Matcher m = LOCATION_PATTERN.matcher(locString);
        if (m.matches()) {
            return new Location(Bukkit.getWorld(m.group("world")),
                    Double.parseDouble(m.group("x")),
                    Double.parseDouble(m.group("y")),
                    Double.parseDouble(m.group("z")),
                    m.group("yaw") != null ? Float.parseFloat(m.group("yaw")) : 0,
                    m.group("pitch") != null ? Float.parseFloat(m.group("pitch")) : 0
            );
        }
        return null;
    }

    public static Location centerOnBlock(Location loc) {
        if (loc == null) {
            return null;
        }
        return new Location(loc.getWorld(),
                loc.getBlockX() + 0.5, loc.getBlockY() + 0.1, loc.getBlockZ() + 0.5,
                loc.getYaw(), loc.getPitch());
    }

    public static Location centerInBlock(Location loc) {
        if (loc == null) {
            return null;
        }
        return new Location(loc.getWorld(),
                loc.getBlockX() + 0.5, loc.getBlockY() + 0.5, loc.getBlockZ() + 0.5,
                loc.getYaw(), loc.getPitch());
    }

}
