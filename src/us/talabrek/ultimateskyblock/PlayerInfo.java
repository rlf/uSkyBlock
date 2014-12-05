package us.talabrek.ultimateskyblock;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class PlayerInfo
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  private String playerName;
  private boolean hasIsland;
  private boolean hasParty;
  private String islandLocation;
  private String homeLocation;
  private HashMap<String, Challenge> challengeListNew;
  private String partyIslandLocation;
  private FileConfiguration playerData;
  private File playerConfigFile;
  
  public PlayerInfo(String playerName)
  {
    this.playerName = playerName;
    loadPlayer(playerName);
  }
  
  public PlayerInfo(String playerName, boolean hasIsland, int iX, int iY, int iZ, int hX, int hY, int hZ)
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
    this.challengeListNew = new HashMap();
    buildChallengeList();
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
    if (Bukkit.getPlayer(this.playerName) == null) {
      getPlayerConfig(this.playerName).set("player.kickWarning", Boolean.valueOf(true));
    }
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
  
  public void completeChallenge(String challenge)
  {
    if (this.challengeListNew.containsKey(challenge))
    {
      if (!onChallengeCooldown(challenge)) {
        if (uSkyBlock.getInstance().getConfig().contains("options.challenges.challengeList." + challenge + ".resetInHours")) {
          ((Challenge)this.challengeListNew.get(challenge)).setFirstCompleted(Calendar.getInstance().getTimeInMillis() + uSkyBlock.getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".resetInHours") * 3600000);
        } else if (uSkyBlock.getInstance().getConfig().contains("options.challenges.defaultResetInHours")) {
          ((Challenge)this.challengeListNew.get(challenge)).setFirstCompleted(Calendar.getInstance().getTimeInMillis() + uSkyBlock.getInstance().getConfig().getInt("options.challenges.defaultResetInHours") * 3600000);
        } else {
          ((Challenge)this.challengeListNew.get(challenge)).setFirstCompleted(Calendar.getInstance().getTimeInMillis() + 518400000L);
        }
      }
      ((Challenge)this.challengeListNew.get(challenge)).addTimesCompleted();
    }
  }
  
  public long getChallengeCooldownTime(String challenge)
  {
    if (getChallenge(challenge).getFirstCompleted() > 0L)
    {
      if (getChallenge(challenge).getFirstCompleted() > Calendar.getInstance().getTimeInMillis()) {
        return getChallenge(challenge).getFirstCompleted() - Calendar.getInstance().getTimeInMillis();
      }
      return 0L;
    }
    return 0L;
  }
  
  public boolean onChallengeCooldown(String challenge)
  {
    if (getChallenge(challenge).getFirstCompleted() > 0L)
    {
      if (getChallenge(challenge).getFirstCompleted() > Calendar.getInstance().getTimeInMillis()) {
        return true;
      }
      return false;
    }
    return false;
  }
  
  public void resetChallenge(String challenge)
  {
    if (this.challengeListNew.containsKey(challenge))
    {
      ((Challenge)this.challengeListNew.get(challenge)).setTimesCompleted(0);
      ((Challenge)this.challengeListNew.get(challenge)).setFirstCompleted(0L);
    }
  }
  
  public int checkChallenge(String challenge)
  {
    try
    {
      if (this.challengeListNew.containsKey(challenge.toLowerCase())) {
        return ((Challenge)this.challengeListNew.get(challenge.toLowerCase())).getTimesCompleted();
      }
    }
    catch (ClassCastException localClassCastException) {}
    return 0;
  }
  
  public int checkChallengeSinceTimer(String challenge)
  {
    try
    {
      if (this.challengeListNew.containsKey(challenge.toLowerCase())) {
        return ((Challenge)this.challengeListNew.get(challenge.toLowerCase())).getTimesCompletedSinceTimer();
      }
    }
    catch (ClassCastException localClassCastException) {}
    return 0;
  }
  
  public Challenge getChallenge(String challenge)
  {
    if (this.challengeListNew.containsKey(challenge.toLowerCase())) {
      return (Challenge)this.challengeListNew.get(challenge.toLowerCase());
    }
    return null;
  }
  
  public boolean challengeExists(String challenge)
  {
    if (this.challengeListNew.containsKey(challenge.toLowerCase())) {
      return true;
    }
    return false;
  }
  
  public void resetAllChallenges()
  {
    this.challengeListNew = null;
    buildChallengeList();
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
  
  public void buildChallengeList()
  {
    if (this.challengeListNew == null) {
      this.challengeListNew = new HashMap();
    }
    Iterator<String> itr = Settings.challenges_challengeList.iterator();
    while (itr.hasNext())
    {
      String current = (String)itr.next();
      if (!this.challengeListNew.containsKey(current.toLowerCase())) {
        this.challengeListNew.put(current.toLowerCase(), new Challenge(current.toLowerCase(), 0L, 0, 0));
      }
    }
    if (this.challengeListNew.size() > Settings.challenges_challengeList.size())
    {
      Object[] challengeArray = this.challengeListNew.keySet().toArray();
      for (int i = 0; i < challengeArray.length; i++) {
        if (!Settings.challenges_challengeList.contains(challengeArray[i].toString())) {
          this.challengeListNew.remove(challengeArray[i].toString());
        }
      }
    }
  }
  
  public void displayChallengeList()
  {
    Iterator<String> itr = this.challengeListNew.keySet().iterator();
    System.out.print("Displaying Challenge list for " + this.playerName);
    while (itr.hasNext())
    {
      String current = (String)itr.next();
      System.out.print(current + ": " + this.challengeListNew.get(current));
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
  
  public void setupPlayer(String player)
  {
    System.out.println("Creating player config Paths!");
    getPlayerConfig(player).createSection("player");
    getPlayerConfig(player);FileConfiguration.createPath(getPlayerConfig(player).getConfigurationSection("player"), "hasIsland");
    getPlayerConfig(player);FileConfiguration.createPath(getPlayerConfig(player).getConfigurationSection("player"), "islandX");
    getPlayerConfig(player);FileConfiguration.createPath(getPlayerConfig(player).getConfigurationSection("player"), "islandY");
    getPlayerConfig(player);FileConfiguration.createPath(getPlayerConfig(player).getConfigurationSection("player"), "islandZ");
    getPlayerConfig(player);FileConfiguration.createPath(getPlayerConfig(player).getConfigurationSection("player"), "homeX");
    getPlayerConfig(player);FileConfiguration.createPath(getPlayerConfig(player).getConfigurationSection("player"), "homeY");
    getPlayerConfig(player);FileConfiguration.createPath(getPlayerConfig(player).getConfigurationSection("player"), "homeZ");
    getPlayerConfig(player);FileConfiguration.createPath(getPlayerConfig(player).getConfigurationSection("player"), "challenges");
    getPlayerConfig(player).set("player.hasIsland", Boolean.valueOf(false));
    getPlayerConfig(player).set("player.islandX", Integer.valueOf(0));
    getPlayerConfig(player).set("player.islandY", Integer.valueOf(0));
    getPlayerConfig(player).set("player.islandZ", Integer.valueOf(0));
    getPlayerConfig(player).set("player.homeX", Integer.valueOf(0));
    getPlayerConfig(player).set("player.homeY", Integer.valueOf(0));
    getPlayerConfig(player).set("player.homeZ", Integer.valueOf(0));
    getPlayerConfig(player).set("player.kickWarning", Boolean.valueOf(false));
    Iterator<String> ent = this.challengeListNew.keySet().iterator();
    String currentChallenge = "";
    while (ent.hasNext())
    {
      currentChallenge = (String)ent.next();
      getPlayerConfig(player).createSection("player.challenges." + currentChallenge);
      getPlayerConfig(player);FileConfiguration.createPath(getPlayerConfig(player).getConfigurationSection("player.challenges." + currentChallenge), "firstCompleted");
      getPlayerConfig(player);FileConfiguration.createPath(getPlayerConfig(player).getConfigurationSection("player.challenges." + currentChallenge), "timesCompleted");
      getPlayerConfig(player);FileConfiguration.createPath(getPlayerConfig(player).getConfigurationSection("player.challenges." + currentChallenge), "timesCompletedSinceTimer");
      getPlayerConfig(player).set("player.challenges." + currentChallenge + ".firstCompleted", Long.valueOf(((Challenge)this.challengeListNew.get(currentChallenge)).getFirstCompleted()));
      getPlayerConfig(player).set("player.challenges." + currentChallenge + ".timesCompleted", Integer.valueOf(((Challenge)this.challengeListNew.get(currentChallenge)).getTimesCompleted()));
      getPlayerConfig(player).set("player.challenges." + currentChallenge + ".timesCompletedSinceTimer", Integer.valueOf(((Challenge)this.challengeListNew.get(currentChallenge)).getTimesCompletedSinceTimer()));
    }
  }
  
  public PlayerInfo loadPlayer(String player)
  {
    if (!getPlayerConfig(player).contains("player.hasIsland"))
    {
      this.playerName = player;
      this.hasIsland = false;
      this.islandLocation = null;
      this.homeLocation = null;
      this.hasParty = false;
      buildChallengeList();
      createPlayerConfig(player);
      return this;
    }
    try
    {
      this.hasIsland = getPlayerConfig(player).getBoolean("player.hasIsland");
      this.islandLocation = getStringLocation(new Location(uSkyBlock.getSkyBlockWorld(), getPlayerConfig(player).getInt("player.islandX"), getPlayerConfig(player).getInt("player.islandY"), getPlayerConfig(player).getInt("player.islandZ")));
      this.homeLocation = getStringLocation(new Location(uSkyBlock.getSkyBlockWorld(), getPlayerConfig(player).getInt("player.homeX"), getPlayerConfig(player).getInt("player.homeY"), getPlayerConfig(player).getInt("player.homeZ")));
      buildChallengeList();
      
      Iterator<String> ent = Settings.challenges_challengeList.iterator();
      String currentChallenge = "";
      this.challengeListNew = new HashMap();
      while (ent.hasNext())
      {
        currentChallenge = (String)ent.next();
        this.challengeListNew.put(currentChallenge, new Challenge(currentChallenge, getPlayerConfig(player).getLong("player.challenges." + currentChallenge + ".firstCompleted"), getPlayerConfig(player).getInt("player.challenges." + currentChallenge + ".timesCompleted"), getPlayerConfig(player).getInt("player.challenges." + currentChallenge + ".timesCompletedSinceTimer")));
      }
      if (Bukkit.getPlayer(player) != null) {
        if (getPlayerConfig(player).getBoolean("player.kickWarning"))
        {
          Bukkit.getPlayer(player).sendMessage("§cYou were removed from your island since the last time you played!");
          getPlayerConfig(player).set("player.kickWarning", Boolean.valueOf(false));
        }
      }
      return this;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      
      System.out.println("Returning null while loading, not good!");
    }
    return null;
  }
  
  public void reloadPlayerConfig(String player)
  {
    this.playerConfigFile = new File(uSkyBlock.getInstance().directoryPlayers, player + ".yml");
    this.playerData = YamlConfiguration.loadConfiguration(this.playerConfigFile);
  }
  
  public void createPlayerConfig(String player)
  {
    System.out.println("Creating new player config!");
    getPlayerConfig(player);
    setupPlayer(player);
  }
  
  public FileConfiguration getPlayerConfig(String player)
  {
    if (this.playerData == null)
    {
      System.out.println("Reloading player data!");
      reloadPlayerConfig(player);
    }
    return this.playerData;
  }
  
  public void savePlayerConfig(String player)
  {
    if (this.playerData == null)
    {
      System.out.println("Can't save player data!");
      return;
    }
    getPlayerConfig(player).set("player.hasIsland", Boolean.valueOf(getHasIsland()));
    if (getIslandLocation() != null)
    {
      getPlayerConfig(player).set("player.islandX", Integer.valueOf(getIslandLocation().getBlockX()));
      getPlayerConfig(player).set("player.islandY", Integer.valueOf(getIslandLocation().getBlockY()));
      getPlayerConfig(player).set("player.islandZ", Integer.valueOf(getIslandLocation().getBlockZ()));
    }
    else
    {
      getPlayerConfig(player).set("player.islandX", Integer.valueOf(0));
      getPlayerConfig(player).set("player.islandY", Integer.valueOf(0));
      getPlayerConfig(player).set("player.islandZ", Integer.valueOf(0));
    }
    if (getHomeLocation() != null)
    {
      getPlayerConfig(player).set("player.homeX", Integer.valueOf(getHomeLocation().getBlockX()));
      getPlayerConfig(player).set("player.homeY", Integer.valueOf(getHomeLocation().getBlockY()));
      getPlayerConfig(player).set("player.homeZ", Integer.valueOf(getHomeLocation().getBlockZ()));
    }
    else
    {
      getPlayerConfig(player).set("player.homeX", Integer.valueOf(0));
      getPlayerConfig(player).set("player.homeY", Integer.valueOf(0));
      getPlayerConfig(player).set("player.homeZ", Integer.valueOf(0));
    }
    Iterator<String> ent = this.challengeListNew.keySet().iterator();
    String currentChallenge = "";
    while (ent.hasNext())
    {
      currentChallenge = (String)ent.next();
      getPlayerConfig(player).set("player.challenges." + currentChallenge + ".firstCompleted", Long.valueOf(((Challenge)this.challengeListNew.get(currentChallenge)).getFirstCompleted()));
      getPlayerConfig(player).set("player.challenges." + currentChallenge + ".timesCompleted", Integer.valueOf(((Challenge)this.challengeListNew.get(currentChallenge)).getTimesCompleted()));
      getPlayerConfig(player).set("player.challenges." + currentChallenge + ".timesCompletedSinceTimer", Integer.valueOf(((Challenge)this.challengeListNew.get(currentChallenge)).getTimesCompletedSinceTimer()));
    }
    this.playerConfigFile = new File(uSkyBlock.getInstance().directoryPlayers, player + ".yml");
    try
    {
      getPlayerConfig(player).save(this.playerConfigFile);
      System.out.println("Player data saved!");
    }
    catch (IOException ex)
    {
      uSkyBlock.getInstance().getLogger().log(Level.SEVERE, "Could not save config to " + this.playerConfigFile, ex);
    }
  }
  
  public void deleteIslandConfig(String player)
  {
    this.playerConfigFile = new File(uSkyBlock.getInstance().directoryPlayers, player + ".yml");
    this.playerConfigFile.delete();
  }
}
