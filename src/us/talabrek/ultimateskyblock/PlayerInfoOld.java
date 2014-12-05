package us.talabrek.ultimateskyblock;

import java.io.PrintStream;
import java.io.Serializable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class PlayerInfoOld
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  private String playerName;
  private boolean hasIsland;
  private boolean hasParty;
  private String islandLocation;
  private String homeLocation;
  private String partyIslandLocation;
  
  public PlayerInfoOld(String playerName)
  {
    this.hasIsland = false;
    this.hasParty = false;
    this.islandLocation = "";
    this.homeLocation = "";
  }
  
  public PlayerInfoOld(String playerName, boolean hasIsland, int iX, int iY, int iZ, int hX, int hY, int hZ)
  {
    this.playerName = playerName;
    this.hasIsland = hasIsland;
    if ((iX == 0) && (iY == 0) && (iZ == 0)) {
      this.islandLocation = null;
    } else {
      this.islandLocation = getStringLocation(new Location(uSkyBlock.getSkyBlockWorld(), iX, iY, iZ));
    }
    if ((hX == 0) && (hY == 0) && (hZ == 0)) {
      this.homeLocation = null;
    } else {
      this.homeLocation = getStringLocation(new Location(uSkyBlock.getSkyBlockWorld(), hX, hY, hZ));
    }
  }
  
  public void startNewIsland(Location l)
  {
    this.hasIsland = true;
    setIslandLocation(l);
    this.hasParty = false;
    this.homeLocation = null;
  }
  
  public void removeFromIsland()
  {
    this.hasIsland = false;
    setIslandLocation(null);
    this.hasParty = false;
    this.homeLocation = null;
  }
  
  public void setPlayerName(String s)
  {
    this.playerName = s;
  }
  
  public boolean getHasIsland()
  {
    return this.hasIsland;
  }
  
  public String locationForParty()
  {
    return getPartyLocationString(this.islandLocation);
  }
  
  public String locationForPartyOld()
  {
    return getPartyLocationString(this.partyIslandLocation);
  }
  
  public Player getPlayer()
  {
    return Bukkit.getPlayer(this.playerName);
  }
  
  public String getPlayerName()
  {
    return this.playerName;
  }
  
  public void setHasIsland(boolean b)
  {
    this.hasIsland = b;
  }
  
  public void setIslandLocation(Location l)
  {
    this.islandLocation = getStringLocation(l);
  }
  
  public Location getIslandLocation()
  {
    return getLocationString(this.islandLocation);
  }
  
  public void setHomeLocation(Location l)
  {
    this.homeLocation = getStringLocation(l);
  }
  
  public Location getHomeLocation()
  {
    return getLocationString(this.homeLocation);
  }
  
  public boolean getHasParty()
  {
    return this.hasParty;
  }
  
  public void setJoinParty(Location l)
  {
    this.hasParty = true;
    this.islandLocation = getStringLocation(l);
    this.hasIsland = true;
  }
  
  public void setLeaveParty()
  {
    this.hasParty = false;
    this.islandLocation = null;
    this.hasIsland = false;
  }
  
  private Location getLocationString(String s)
  {
    if ((s == null) || (s.trim() == "")) {
      return null;
    }
    String[] parts = s.split(":");
    if (parts.length == 4)
    {
      World w = Bukkit.getServer().getWorld(parts[0]);
      int x = Integer.parseInt(parts[1]);
      int y = Integer.parseInt(parts[2]);
      int z = Integer.parseInt(parts[3]);
      return new Location(w, x, y, z);
    }
    return null;
  }
  
  private String getPartyLocationString(String s)
  {
    if ((s == null) || (s.trim() == "")) {
      return null;
    }
    String[] parts = s.split(":");
    if (parts.length == 4) {
      return parts[1] + "," + parts[3];
    }
    return null;
  }
  
  public void displayData(String player)
  {
    System.out.print(player + " has an island: " + getHasIsland());
    if (getIslandLocation() != null) {
      System.out.print(player + " island location: " + getIslandLocation().toString());
    }
    if (getHomeLocation() != null) {
      System.out.print(player + " home location: " + getHomeLocation().toString());
    }
  }
  
  private String getStringLocation(Location l)
  {
    if (l == null) {
      return "";
    }
    return l.getWorld().getName() + ":" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
  }
  
  public Location getPartyIslandLocation()
  {
    return getLocationString(this.partyIslandLocation);
  }
}
