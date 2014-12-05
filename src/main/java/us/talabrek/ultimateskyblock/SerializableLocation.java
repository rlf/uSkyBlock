package us.talabrek.ultimateskyblock;

import java.io.*;

import org.bukkit.*;

public class SerializableLocation implements Serializable {
    private static final long serialVersionUID = 23L;
    private double x;
    private double y;
    private double z;
    private String world;

    public SerializableLocation(final Location loc) {
        super();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.world = loc.getWorld().getName();
    }

    public Location getLocation() {
        final World w = Bukkit.getWorld(this.world);
        if (w == null) {
            return null;
        }
        final Location toRet = new Location(w, this.x, this.y, this.z);
        return toRet;
    }
}
