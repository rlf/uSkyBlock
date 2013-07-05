package us.talabrek.ultimateskyblock;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class SerializableLocation implements Serializable {
	private static final long serialVersionUID = 23L;
	private final double x;
	private final double y;
	private final double z;
	private final String world;

	public SerializableLocation(Location loc) {
		x = loc.getX();
		y = loc.getY();
		z = loc.getZ();
		world = loc.getWorld().getName();
	}

	public Location getLocation() {
		final World w = Bukkit.getWorld(world);
		if (w == null)
			return null;
		final Location toRet = new Location(w, x, y, z);
		return toRet;
	}
}