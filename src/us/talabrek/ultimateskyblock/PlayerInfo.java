package us.talabrek.ultimateskyblock;

import org.bukkit.entity.*;
import org.bukkit.*;
import java.util.*;
import org.bukkit.configuration.file.*;
import java.util.logging.*;
import java.io.*;

public class PlayerInfo implements Serializable
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
    
    public PlayerInfo(final String playerName) {
        super();
        this.loadPlayer(this.playerName = playerName);
    }
    
    public PlayerInfo(final String playerName, final boolean hasIsland, final int iX, final int iY, final int iZ, final int hX, final int hY, final int hZ) {
        super();
        this.playerName = playerName;
        this.hasIsland = hasIsland;
        if (iX == 0 && iY == 0 && iZ == 0) {
            this.islandLocation = null;
        }
        else {
            this.islandLocation = this.getStringLocation(new Location(uSkyBlock.getSkyBlockWorld(), (double)iX, (double)iY, (double)iZ));
        }
        if (hX == 0 && hY == 0 && hZ == 0) {
            this.homeLocation = null;
        }
        else {
            this.homeLocation = this.getStringLocation(new Location(uSkyBlock.getSkyBlockWorld(), (double)hX, (double)hY, (double)hZ));
        }
        this.challengeListNew = new HashMap<String, Challenge>();
        this.buildChallengeList();
    }
    
    public void startNewIsland(final Location l) {
        this.hasIsland = true;
        this.setIslandLocation(l);
        this.hasParty = false;
        this.homeLocation = null;
    }
    
    public void removeFromIsland() {
        this.hasIsland = false;
        this.setIslandLocation(null);
        this.hasParty = false;
        this.homeLocation = null;
    }
    
    public void setPlayerName(final String s) {
        this.playerName = s;
    }
    
    public boolean getHasIsland() {
        return this.hasIsland;
    }
    
    public String locationForParty() {
        return this.getPartyLocationString(this.islandLocation);
    }
    
    public String locationForPartyOld() {
        return this.getPartyLocationString(this.partyIslandLocation);
    }
    
    public Player getPlayer() {
        return Bukkit.getPlayer(this.playerName);
    }
    
    public String getPlayerName() {
        return this.playerName;
    }
    
    public void setHasIsland(final boolean b) {
        this.hasIsland = b;
    }
    
    public void setIslandLocation(final Location l) {
        this.islandLocation = this.getStringLocation(l);
    }
    
    public Location getIslandLocation() {
        return this.getLocationString(this.islandLocation);
    }
    
    public void setHomeLocation(final Location l) {
        this.homeLocation = this.getStringLocation(l);
    }
    
    public Location getHomeLocation() {
        return this.getLocationString(this.homeLocation);
    }
    
    public boolean getHasParty() {
        return this.hasParty;
    }
    
    public void setJoinParty(final Location l) {
        this.hasParty = true;
        this.islandLocation = this.getStringLocation(l);
        this.hasIsland = true;
    }
    
    public void setLeaveParty() {
        this.hasParty = false;
        this.islandLocation = null;
        this.hasIsland = false;
        if (Bukkit.getPlayer(this.playerName) == null) {
            this.getPlayerConfig(this.playerName).set("player.kickWarning", (Object)true);
        }
    }
    
    private Location getLocationString(final String s) {
        if (s == null || s.trim() == "") {
            return null;
        }
        final String[] parts = s.split(":");
        if (parts.length == 4) {
            final World w = Bukkit.getServer().getWorld(parts[0]);
            final int x = Integer.parseInt(parts[1]);
            final int y = Integer.parseInt(parts[2]);
            final int z = Integer.parseInt(parts[3]);
            return new Location(w, (double)x, (double)y, (double)z);
        }
        return null;
    }
    
    private String getPartyLocationString(final String s) {
        if (s == null || s.trim() == "") {
            return null;
        }
        final String[] parts = s.split(":");
        if (parts.length == 4) {
            return String.valueOf(parts[1]) + "," + parts[3];
        }
        return null;
    }
    
    public void completeChallenge(final String challenge) {
        if (this.challengeListNew.containsKey(challenge)) {
            if (!this.onChallengeCooldown(challenge)) {
                if (uSkyBlock.getInstance().getConfig().contains("options.challenges.challengeList." + challenge + ".resetInHours")) {
                    this.challengeListNew.get(challenge).setFirstCompleted(Calendar.getInstance().getTimeInMillis() + uSkyBlock.getInstance().getConfig().getInt("options.challenges.challengeList." + challenge + ".resetInHours") * 3600000);
                }
                else if (uSkyBlock.getInstance().getConfig().contains("options.challenges.defaultResetInHours")) {
                    this.challengeListNew.get(challenge).setFirstCompleted(Calendar.getInstance().getTimeInMillis() + uSkyBlock.getInstance().getConfig().getInt("options.challenges.defaultResetInHours") * 3600000);
                }
                else {
                    this.challengeListNew.get(challenge).setFirstCompleted(Calendar.getInstance().getTimeInMillis() + 518400000L);
                }
            }
            this.challengeListNew.get(challenge).addTimesCompleted();
        }
    }
    
    public long getChallengeCooldownTime(final String challenge) {
        if (this.getChallenge(challenge).getFirstCompleted() <= 0L) {
            return 0L;
        }
        if (this.getChallenge(challenge).getFirstCompleted() > Calendar.getInstance().getTimeInMillis()) {
            return this.getChallenge(challenge).getFirstCompleted() - Calendar.getInstance().getTimeInMillis();
        }
        return 0L;
    }
    
    public boolean onChallengeCooldown(final String challenge) {
        return this.getChallenge(challenge).getFirstCompleted() > 0L && this.getChallenge(challenge).getFirstCompleted() > Calendar.getInstance().getTimeInMillis();
    }
    
    public void resetChallenge(final String challenge) {
        if (this.challengeListNew.containsKey(challenge)) {
            this.challengeListNew.get(challenge).setTimesCompleted(0);
            this.challengeListNew.get(challenge).setFirstCompleted(0L);
        }
    }
    
    public int checkChallenge(final String challenge) {
        try {
            if (this.challengeListNew.containsKey(challenge.toLowerCase())) {
                return this.challengeListNew.get(challenge.toLowerCase()).getTimesCompleted();
            }
        }
        catch (ClassCastException ex) {}
        return 0;
    }
    
    public int checkChallengeSinceTimer(final String challenge) {
        try {
            if (this.challengeListNew.containsKey(challenge.toLowerCase())) {
                return this.challengeListNew.get(challenge.toLowerCase()).getTimesCompletedSinceTimer();
            }
        }
        catch (ClassCastException ex) {}
        return 0;
    }
    
    public Challenge getChallenge(final String challenge) {
        if (this.challengeListNew.containsKey(challenge.toLowerCase())) {
            return this.challengeListNew.get(challenge.toLowerCase());
        }
        return null;
    }
    
    public boolean challengeExists(final String challenge) {
        return this.challengeListNew.containsKey(challenge.toLowerCase());
    }
    
    public void resetAllChallenges() {
        this.challengeListNew = null;
        this.buildChallengeList();
    }
    
    public void displayData(final String player) {
        System.out.print(String.valueOf(player) + " has an island: " + this.getHasIsland());
        if (this.getIslandLocation() != null) {
            System.out.print(String.valueOf(player) + " island location: " + this.getIslandLocation().toString());
        }
        if (this.getHomeLocation() != null) {
            System.out.print(String.valueOf(player) + " home location: " + this.getHomeLocation().toString());
        }
    }
    
    public void buildChallengeList() {
        if (this.challengeListNew == null) {
            this.challengeListNew = new HashMap<String, Challenge>();
        }
        for (final String current : Settings.challenges_challengeList) {
            if (!this.challengeListNew.containsKey(current.toLowerCase())) {
                this.challengeListNew.put(current.toLowerCase(), new Challenge(current.toLowerCase(), 0L, 0, 0));
            }
        }
        if (this.challengeListNew.size() > Settings.challenges_challengeList.size()) {
            final Object[] challengeArray = this.challengeListNew.keySet().toArray();
            for (int i = 0; i < challengeArray.length; ++i) {
                if (!Settings.challenges_challengeList.contains(challengeArray[i].toString())) {
                    this.challengeListNew.remove(challengeArray[i].toString());
                }
            }
        }
    }
    
    public void displayChallengeList() {
        final Iterator<String> itr = this.challengeListNew.keySet().iterator();
        System.out.print("Displaying Challenge list for " + this.playerName);
        while (itr.hasNext()) {
            final String current = itr.next();
            System.out.print(String.valueOf(current) + ": " + this.challengeListNew.get(current));
        }
    }
    
    private String getStringLocation(final Location l) {
        if (l == null) {
            return "";
        }
        return String.valueOf(l.getWorld().getName()) + ":" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
    }
    
    public Location getPartyIslandLocation() {
        return this.getLocationString(this.partyIslandLocation);
    }
    
    public void setupPlayer(final String player) {
        System.out.println("Creating player config Paths!");
        this.getPlayerConfig(player).createSection("player");
        this.getPlayerConfig(player);
        FileConfiguration.createPath(this.getPlayerConfig(player).getConfigurationSection("player"), "hasIsland");
        this.getPlayerConfig(player);
        FileConfiguration.createPath(this.getPlayerConfig(player).getConfigurationSection("player"), "islandX");
        this.getPlayerConfig(player);
        FileConfiguration.createPath(this.getPlayerConfig(player).getConfigurationSection("player"), "islandY");
        this.getPlayerConfig(player);
        FileConfiguration.createPath(this.getPlayerConfig(player).getConfigurationSection("player"), "islandZ");
        this.getPlayerConfig(player);
        FileConfiguration.createPath(this.getPlayerConfig(player).getConfigurationSection("player"), "homeX");
        this.getPlayerConfig(player);
        FileConfiguration.createPath(this.getPlayerConfig(player).getConfigurationSection("player"), "homeY");
        this.getPlayerConfig(player);
        FileConfiguration.createPath(this.getPlayerConfig(player).getConfigurationSection("player"), "homeZ");
        this.getPlayerConfig(player);
        FileConfiguration.createPath(this.getPlayerConfig(player).getConfigurationSection("player"), "challenges");
        this.getPlayerConfig(player).set("player.hasIsland", (Object)false);
        this.getPlayerConfig(player).set("player.islandX", (Object)0);
        this.getPlayerConfig(player).set("player.islandY", (Object)0);
        this.getPlayerConfig(player).set("player.islandZ", (Object)0);
        this.getPlayerConfig(player).set("player.homeX", (Object)0);
        this.getPlayerConfig(player).set("player.homeY", (Object)0);
        this.getPlayerConfig(player).set("player.homeZ", (Object)0);
        this.getPlayerConfig(player).set("player.kickWarning", (Object)false);
        final Iterator<String> ent = this.challengeListNew.keySet().iterator();
        String currentChallenge = "";
        while (ent.hasNext()) {
            currentChallenge = ent.next();
            this.getPlayerConfig(player).createSection("player.challenges." + currentChallenge);
            this.getPlayerConfig(player);
            FileConfiguration.createPath(this.getPlayerConfig(player).getConfigurationSection("player.challenges." + currentChallenge), "firstCompleted");
            this.getPlayerConfig(player);
            FileConfiguration.createPath(this.getPlayerConfig(player).getConfigurationSection("player.challenges." + currentChallenge), "timesCompleted");
            this.getPlayerConfig(player);
            FileConfiguration.createPath(this.getPlayerConfig(player).getConfigurationSection("player.challenges." + currentChallenge), "timesCompletedSinceTimer");
            this.getPlayerConfig(player).set("player.challenges." + currentChallenge + ".firstCompleted", (Object)this.challengeListNew.get(currentChallenge).getFirstCompleted());
            this.getPlayerConfig(player).set("player.challenges." + currentChallenge + ".timesCompleted", (Object)this.challengeListNew.get(currentChallenge).getTimesCompleted());
            this.getPlayerConfig(player).set("player.challenges." + currentChallenge + ".timesCompletedSinceTimer", (Object)this.challengeListNew.get(currentChallenge).getTimesCompletedSinceTimer());
        }
    }
    
    public PlayerInfo loadPlayer(final String player) {
        if (!this.getPlayerConfig(player).contains("player.hasIsland")) {
            this.playerName = player;
            this.hasIsland = false;
            this.islandLocation = null;
            this.homeLocation = null;
            this.hasParty = false;
            this.buildChallengeList();
            this.createPlayerConfig(player);
            return this;
        }
        try {
            this.hasIsland = this.getPlayerConfig(player).getBoolean("player.hasIsland");
            this.islandLocation = this.getStringLocation(new Location(uSkyBlock.getSkyBlockWorld(), (double)this.getPlayerConfig(player).getInt("player.islandX"), (double)this.getPlayerConfig(player).getInt("player.islandY"), (double)this.getPlayerConfig(player).getInt("player.islandZ")));
            this.homeLocation = this.getStringLocation(new Location(uSkyBlock.getSkyBlockWorld(), (double)this.getPlayerConfig(player).getInt("player.homeX"), (double)this.getPlayerConfig(player).getInt("player.homeY"), (double)this.getPlayerConfig(player).getInt("player.homeZ")));
            this.buildChallengeList();
            final Iterator<String> ent = Settings.challenges_challengeList.iterator();
            String currentChallenge = "";
            this.challengeListNew = new HashMap<String, Challenge>();
            while (ent.hasNext()) {
                currentChallenge = ent.next();
                this.challengeListNew.put(currentChallenge, new Challenge(currentChallenge, this.getPlayerConfig(player).getLong("player.challenges." + currentChallenge + ".firstCompleted"), this.getPlayerConfig(player).getInt("player.challenges." + currentChallenge + ".timesCompleted"), this.getPlayerConfig(player).getInt("player.challenges." + currentChallenge + ".timesCompletedSinceTimer")));
            }
            if (Bukkit.getPlayer(player) != null && this.getPlayerConfig(player).getBoolean("player.kickWarning")) {
                Bukkit.getPlayer(player).sendMessage("§cYou were removed from your island since the last time you played!");
                this.getPlayerConfig(player).set("player.kickWarning", (Object)false);
            }
            return this;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Returning null while loading, not good!");
            return null;
        }
    }
    
    public void reloadPlayerConfig(final String player) {
        this.playerConfigFile = new File(uSkyBlock.getInstance().directoryPlayers, String.valueOf(player) + ".yml");
        this.playerData = (FileConfiguration)YamlConfiguration.loadConfiguration(this.playerConfigFile);
    }
    
    public void createPlayerConfig(final String player) {
        System.out.println("Creating new player config!");
        this.getPlayerConfig(player);
        this.setupPlayer(player);
    }
    
    public FileConfiguration getPlayerConfig(final String player) {
        if (this.playerData == null) {
            System.out.println("Reloading player data!");
            this.reloadPlayerConfig(player);
        }
        return this.playerData;
    }
    
    public void savePlayerConfig(final String player) {
        if (this.playerData == null) {
            System.out.println("Can't save player data!");
            return;
        }
        this.getPlayerConfig(player).set("player.hasIsland", (Object)this.getHasIsland());
        if (this.getIslandLocation() != null) {
            this.getPlayerConfig(player).set("player.islandX", (Object)this.getIslandLocation().getBlockX());
            this.getPlayerConfig(player).set("player.islandY", (Object)this.getIslandLocation().getBlockY());
            this.getPlayerConfig(player).set("player.islandZ", (Object)this.getIslandLocation().getBlockZ());
        }
        else {
            this.getPlayerConfig(player).set("player.islandX", (Object)0);
            this.getPlayerConfig(player).set("player.islandY", (Object)0);
            this.getPlayerConfig(player).set("player.islandZ", (Object)0);
        }
        if (this.getHomeLocation() != null) {
            this.getPlayerConfig(player).set("player.homeX", (Object)this.getHomeLocation().getBlockX());
            this.getPlayerConfig(player).set("player.homeY", (Object)this.getHomeLocation().getBlockY());
            this.getPlayerConfig(player).set("player.homeZ", (Object)this.getHomeLocation().getBlockZ());
        }
        else {
            this.getPlayerConfig(player).set("player.homeX", (Object)0);
            this.getPlayerConfig(player).set("player.homeY", (Object)0);
            this.getPlayerConfig(player).set("player.homeZ", (Object)0);
        }
        final Iterator<String> ent = this.challengeListNew.keySet().iterator();
        String currentChallenge = "";
        while (ent.hasNext()) {
            currentChallenge = ent.next();
            this.getPlayerConfig(player).set("player.challenges." + currentChallenge + ".firstCompleted", (Object)this.challengeListNew.get(currentChallenge).getFirstCompleted());
            this.getPlayerConfig(player).set("player.challenges." + currentChallenge + ".timesCompleted", (Object)this.challengeListNew.get(currentChallenge).getTimesCompleted());
            this.getPlayerConfig(player).set("player.challenges." + currentChallenge + ".timesCompletedSinceTimer", (Object)this.challengeListNew.get(currentChallenge).getTimesCompletedSinceTimer());
        }
        this.playerConfigFile = new File(uSkyBlock.getInstance().directoryPlayers, String.valueOf(player) + ".yml");
        try {
            this.getPlayerConfig(player).save(this.playerConfigFile);
            System.out.println("Player data saved!");
        }
        catch (IOException ex) {
            uSkyBlock.getInstance().getLogger().log(Level.SEVERE, "Could not save config to " + this.playerConfigFile, ex);
        }
    }
    
    public void deleteIslandConfig(final String player) {
        (this.playerConfigFile = new File(uSkyBlock.getInstance().directoryPlayers, String.valueOf(player) + ".yml")).delete();
    }
}
