/*    */package us.talabrek.ultimateskyblock;

/*    */
/*    */import java.io.Serializable;
/*    */
import org.bukkit.Bukkit;
/*    */
import org.bukkit.Location;
/*    */
import org.bukkit.World;

/*    */
/*    */public class SerializableLocation
/*    */implements Serializable
/*    */{
	/*    */private static final long serialVersionUID = 23L;
	/*    */private double x;
	/*    */private double y;
	/*    */private double z;
	/*    */private String world;

	/*    */
	/*    */public SerializableLocation(Location loc)
	/*    */{
		/* 12 */this.x = loc.getX();
		/* 13 */this.y = loc.getY();
		/* 14 */this.z = loc.getZ();
		/* 15 */this.world = loc.getWorld().getName();
		/*    */}

	/*    */public Location getLocation() {
		/* 18 */World w = Bukkit.getWorld(this.world);
		/* 19 */if (w == null)
			/* 20 */return null;
		/* 21 */Location toRet = new Location(w, this.x, this.y, this.z);
		/* 22 */return toRet;
		/*    */}
	/*    */
}