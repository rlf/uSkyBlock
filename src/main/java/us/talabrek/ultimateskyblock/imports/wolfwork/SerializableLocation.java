package us.talabrek.ultimateskyblock.imports.wolfwork;

import java.io.Serializable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class SerializableLocation implements Serializable {
    private static final long serialVersionUID = 23L;
    private final String world;
    private final double x;
    private final double y;
    private final double z;

    public SerializableLocation(final Location loc) {
        x = loc.getX();
        y = loc.getY();
        z = loc.getZ();
        world = loc.getWorld().getName();
    }

    public Location getLocation() {
        final World w = Bukkit.getWorld(world);
        if (w == null) {
            return null;
        }
        final Location toRet = new Location(w, x, y, z);
        return toRet;
    }
}