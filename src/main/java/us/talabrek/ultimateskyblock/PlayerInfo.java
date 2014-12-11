package us.talabrek.ultimateskyblock;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.*;

import java.util.*;

import org.bukkit.configuration.file.*;
import us.talabrek.ultimateskyblock.challenge.ChallengeCompletion;

import java.util.logging.*;
import java.io.*;

public class PlayerInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String playerName;
    private boolean hasIsland;
    private boolean hasParty;

    private Location islandLocation;
    private Location homeLocation;

    private HashMap<String, ChallengeCompletion> challenges;
    private Location partyIslandLocation;
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
        } else {
            this.islandLocation = new Location(uSkyBlock.getSkyBlockWorld(), (double) iX, (double) iY, (double) iZ);
        }
        if (hX == 0 && hY == 0 && hZ == 0) {
            this.homeLocation = null;
        } else {
            this.homeLocation = new Location(uSkyBlock.getSkyBlockWorld(), (double) hX, (double) hY, (double) hZ);
        }
        this.challenges = new HashMap<>();
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

    public boolean getHasIsland() {
        return this.hasIsland;
    }

    public String locationForParty() {
        return getPartyLocationString(this.islandLocation);
    }

    public String locationForPartyOld() {
        return getPartyLocationString(partyIslandLocation);
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
        this.islandLocation = l != null ? l.clone() : null;
    }

    public Location getIslandLocation() {
        return islandLocation != null ? islandLocation.clone() : null;
    }

    public void setHomeLocation(final Location l) {
        this.homeLocation = l != null ? l.clone() : null;
    }

    public Location getHomeLocation() {
        return homeLocation != null ? homeLocation.clone() : null;
    }

    public boolean getHasParty() {
        return this.hasParty;
    }

    public void setJoinParty(final Location l) {
        this.hasParty = true;
        this.islandLocation = l != null ? l.clone() : null;
        this.hasIsland = true;
    }

    public void setLeaveParty() {
        this.hasParty = false;
        this.islandLocation = null;
        this.hasIsland = false;
        if (Bukkit.getPlayer(this.playerName) == null) {
            getPlayerConfig(this.playerName).set("player.kickWarning", true);
        }
    }

    private Location getLocationString(final String s) {
        if (s == null || s.trim() == "") {
            return null;
        }
        final String[] parts = s.split(":");
        if (parts.length >= 4) {
            final World w = Bukkit.getServer().getWorld(parts[0]);
            final int x = Integer.parseInt(parts[1]);
            final int y = Integer.parseInt(parts[2]);
            final int z = Integer.parseInt(parts[3]);
            float yaw = parts.length == 6 ? Float.parseFloat(parts[4]) : 0;
            float pitch = parts.length == 6 ? Float.parseFloat(parts[5]) : 0;
            return new Location(w, (double) x, (double) y, (double) z, yaw, pitch);
        }
        return null;
    }

    private String getPartyLocationString(Location l) {
        if (l == null) {
            return null;
        }
        return "" + l.getX() + "," + l.getZ();
    }

    public void completeChallenge(final String challenge) {
        if (challenges.containsKey(challenge)) {
            if (!onChallengeCooldown(challenge)) {
                long now = System.currentTimeMillis();
                challenges.get(challenge).setFirstCompleted(now + uSkyBlock.getInstance().getChallengeLogic().getResetInMillis(challenge));

            }
            challenges.get(challenge).addTimesCompleted();
        }
    }

    public boolean onChallengeCooldown(final String challenge) {
        return getChallenge(challenge).isOnCooldown();
    }

    public void resetChallenge(final String challenge) {
        if (challenges.containsKey(challenge)) {
            challenges.get(challenge).setTimesCompleted(0);
            challenges.get(challenge).setFirstCompleted(0L);
        }
    }

    public int checkChallenge(final String challenge) {
        try {
            if (challenges.containsKey(challenge.toLowerCase())) {
                return challenges.get(challenge.toLowerCase()).getTimesCompleted();
            }
        } catch (ClassCastException ex) {
        }
        return 0;
    }

    public int checkChallengeSinceTimer(final String challenge) {
        try {
            String challengeKey = challenge.toLowerCase();
            if (onChallengeCooldown(challengeKey) && challenges.containsKey(challengeKey)) {
                return challenges.get(challengeKey).getTimesCompletedSinceTimer();
            }
        } catch (ClassCastException ex) {
        }
        return 0;
    }

    public ChallengeCompletion getChallenge(final String challenge) {
        return challenges.get(challenge.toLowerCase());
    }

    public boolean challengeExists(final String challenge) {
        return challenges.containsKey(challenge.toLowerCase());
    }

    public void resetAllChallenges() {
        this.challenges = null;
        this.buildChallengeList();
    }

    public void buildChallengeList() {
        if (this.challenges == null) {
            this.challenges = new HashMap<>();
        }
        uSkyBlock.getInstance().getChallengeLogic().populateChallenges(challenges);
    }

    private String getStringLocation(final Location l) {
        if (l == null) {
            return "";
        }
        return String.valueOf(l.getWorld().getName()) + ":"
                + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ()
                + ":" + l.getYaw() + ":" + l.getPitch();
    }

    public Location getPartyIslandLocation() {
        return partyIslandLocation != null ? partyIslandLocation.clone() : null;
    }

    public void setupPlayer(final String player) {
        uSkyBlock.log(Level.INFO, "Creating player config Paths!");
        FileConfiguration playerConfig = getPlayerConfig(player);
        playerConfig.createSection("player");
        ConfigurationSection playerSection = playerConfig.getConfigurationSection("player");
        FileConfiguration.createPath(playerSection, "hasIsland");
        FileConfiguration.createPath(playerSection, "islandX");
        FileConfiguration.createPath(playerSection, "islandY");
        FileConfiguration.createPath(playerSection, "islandZ");
        FileConfiguration.createPath(playerSection, "homeX");
        FileConfiguration.createPath(playerSection, "homeY");
        FileConfiguration.createPath(playerSection, "homeZ");
        FileConfiguration.createPath(playerSection, "homeYaw");
        FileConfiguration.createPath(playerSection, "homePitch");
        FileConfiguration.createPath(playerSection, "challenges");
        playerConfig.set("player.hasIsland", false);
        playerConfig.set("player.islandX", 0);
        playerConfig.set("player.islandY", 0);
        playerConfig.set("player.islandZ", 0);
        playerConfig.set("player.homeX", 0);
        playerConfig.set("player.homeY", 0);
        playerConfig.set("player.homeZ", 0);
        playerConfig.set("player.homeYaw", 0);
        playerConfig.set("player.homePitch", 0);
        playerConfig.set("player.kickWarning", false);
        final Iterator<String> ent = challenges.keySet().iterator();
        String currentChallenge = "";
        while (ent.hasNext()) {
            currentChallenge = ent.next();
            playerConfig.createSection("player.challenges." + currentChallenge);
            FileConfiguration.createPath(playerConfig.getConfigurationSection("player.challenges." + currentChallenge), "firstCompleted");
            FileConfiguration.createPath(playerConfig.getConfigurationSection("player.challenges." + currentChallenge), "timesCompleted");
            FileConfiguration.createPath(playerConfig.getConfigurationSection("player.challenges." + currentChallenge), "timesCompletedSinceTimer");
            playerConfig.set("player.challenges." + currentChallenge + ".firstCompleted", challenges.get(currentChallenge).getFirstCompleted());
            playerConfig.set("player.challenges." + currentChallenge + ".timesCompleted", challenges.get(currentChallenge).getTimesCompleted());
            playerConfig.set("player.challenges." + currentChallenge + ".timesCompletedSinceTimer", challenges.get(currentChallenge).getTimesCompletedSinceTimer());
        }
    }

    public PlayerInfo loadPlayer(final String player) {
        FileConfiguration playerConfig = getPlayerConfig(player);
        if (!playerConfig.contains("player.hasIsland")) {
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
            this.hasIsland = playerConfig.getBoolean("player.hasIsland");
            this.islandLocation = new Location(uSkyBlock.getSkyBlockWorld(),
                    playerConfig.getInt("player.islandX"), playerConfig.getInt("player.islandY"), playerConfig.getInt("player.islandZ"));
            this.homeLocation = new Location(uSkyBlock.getSkyBlockWorld(),
                    playerConfig.getInt("player.homeX"), playerConfig.getInt("player.homeY"), playerConfig.getInt("player.homeZ"),
                    playerConfig.getInt("player.homeYaw", 0), playerConfig.getInt("player.homePitch", 0));
            buildChallengeList();
            for (String currentChallenge : challenges.keySet()) {
                challenges.put(currentChallenge, new ChallengeCompletion(currentChallenge, playerConfig.getLong("player.challenges." + currentChallenge + ".firstCompleted"), playerConfig.getInt("player.challenges." + currentChallenge + ".timesCompleted"), playerConfig.getInt("player.challenges." + currentChallenge + ".timesCompletedSinceTimer")));
            }
            if (Bukkit.getPlayer(player) != null && playerConfig.getBoolean("player.kickWarning")) {
                Bukkit.getPlayer(player).sendMessage("\u00a7cYou were removed from your island since the last time you played!");
                playerConfig.set("player.kickWarning", false);
            }
            return this;
        } catch (Exception e) {
            e.printStackTrace();
            uSkyBlock.log(Level.INFO, "Returning null while loading, not good!");
            return null;
        }
    }

    // TODO: 09/12/2014 - R4zorax: All this should be made UUID
    public void reloadPlayerConfig(final String player) {
        this.playerConfigFile = new File(uSkyBlock.getInstance().directoryPlayers, player + ".yml");
        this.playerData = YamlConfiguration.loadConfiguration(this.playerConfigFile);
    }

    public void createPlayerConfig(final String player) {
        uSkyBlock.log(Level.INFO, "Creating new player config!");
        getPlayerConfig(player);
        this.setupPlayer(player);
    }

    public FileConfiguration getPlayerConfig(final String player) {
        if (this.playerData == null) {
            uSkyBlock.log(Level.INFO, "Reloading player data!");
            this.reloadPlayerConfig(player);
        }
        return this.playerData;
    }

    public void savePlayerConfig(final String player) {
        if (this.playerData == null) {
            uSkyBlock.log(Level.INFO, "Can't save player data!");
            return;
        }
        getPlayerConfig(player).set("player.hasIsland", this.getHasIsland());
        Location location = this.getIslandLocation();
        if (location != null) {
            getPlayerConfig(player).set("player.islandX", location.getBlockX());
            getPlayerConfig(player).set("player.islandY", location.getBlockY());
            getPlayerConfig(player).set("player.islandZ", location.getBlockZ());
        } else {
            getPlayerConfig(player).set("player.islandX", 0);
            getPlayerConfig(player).set("player.islandY", 0);
            getPlayerConfig(player).set("player.islandZ", 0);
        }
        Location home = this.getHomeLocation();
        if (home != null) {
            getPlayerConfig(player).set("player.homeX", home.getBlockX());
            getPlayerConfig(player).set("player.homeY", home.getBlockY());
            getPlayerConfig(player).set("player.homeZ", home.getBlockZ());
        } else {
            getPlayerConfig(player).set("player.homeX", 0);
            getPlayerConfig(player).set("player.homeY", 0);
            getPlayerConfig(player).set("player.homeZ", 0);
        }
        final Iterator<String> ent = challenges.keySet().iterator();
        String currentChallenge = "";
        while (ent.hasNext()) {
            currentChallenge = ent.next();
            getPlayerConfig(player).set("player.challenges." + currentChallenge + ".firstCompleted", challenges.get(currentChallenge).getFirstCompleted());
            getPlayerConfig(player).set("player.challenges." + currentChallenge + ".timesCompleted", challenges.get(currentChallenge).getTimesCompleted());
            getPlayerConfig(player).set("player.challenges." + currentChallenge + ".timesCompletedSinceTimer", challenges.get(currentChallenge).getTimesCompletedSinceTimer());
        }
        this.playerConfigFile = new File(uSkyBlock.getInstance().directoryPlayers, player + ".yml");
        try {
            getPlayerConfig(player).save(this.playerConfigFile);
            uSkyBlock.log(Level.INFO, "Player data saved!");
        } catch (IOException ex) {
            uSkyBlock.getInstance().getLogger().log(Level.SEVERE, "Could not save config to " + this.playerConfigFile, ex);
        }
    }
}
