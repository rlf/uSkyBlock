package us.talabrek.ultimateskyblock;

import java.io.Serializable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class SerializableLocation
  implements Serializable
{
  private static final long serialVersionUID = 23L;
  private double x;
  private double y;
  private double z;
  private String world;
  
  public SerializableLocation(Location loc)
  {
    this.x = loc.getX();
    this.y = loc.getY();
    this.z = loc.getZ();
    this.world = loc.getWorld().getName();
  }
  
  public Location getLocation()
  {
    World w = Bukkit.getWorld(this.world);
    if (w == null) {
      return null;
    }
    Location toRet = new Location(w, this.x, this.y, this.z);
    return toRet;
  }
}
