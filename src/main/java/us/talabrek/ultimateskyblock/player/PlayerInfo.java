package us.talabrek.ultimateskyblock.player;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.*;

import java.util.*;

import org.bukkit.configuration.file.*;
import us.talabrek.ultimateskyblock.challenge.ChallengeCompletion;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.logging.*;
import java.io.*;

public class PlayerInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String playerName;
    private String displayName;
    private boolean hasIsland;
    private boolean hasParty;

    private Location islandLocation;
    private Location homeLocation;

    private HashMap<String, ChallengeCompletion> challenges;
    private FileConfiguration playerData;
    private File playerConfigFile;

    public PlayerInfo(final String playerName) {
        this.playerName = playerName;
        loadPlayer();
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
        return islandLocation != null && hasIsland ? islandLocation.clone() : null;
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

    public String getDisplayName() {
        return displayName != null ? displayName : playerName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    private String getPartyLocationString(Location l) {
        if (l == null) {
            return null;
        }
        return "" + l.getBlockX() + "," + l.getBlockZ();
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

    private void setupPlayer(final String player) {
        uSkyBlock.log(Level.INFO, "Creating player config Paths!");
        FileConfiguration playerConfig = getPlayerConfig(player);
        ConfigurationSection pSection = playerConfig.createSection("player");
        pSection.set("hasIsland", false);
        pSection.set("islandX", 0);
        pSection.set("islandY", 0);
        pSection.set("islandZ", 0);
        pSection.set("homeX", 0);
        pSection.set("homeY", 0);
        pSection.set("homeZ", 0);
        pSection.set("homeYaw", 0);
        pSection.set("homePitch", 0);
        pSection.set("kickWarning", false);
        final Iterator<String> ent = challenges.keySet().iterator();
        while (ent.hasNext()) {
            String currentChallenge = ent.next();
            ConfigurationSection cSection = playerConfig.createSection("player.challenges." + currentChallenge);
            cSection.set("firstCompleted", challenges.get(currentChallenge).getFirstCompleted());
            cSection.set("timesCompleted", challenges.get(currentChallenge).getTimesCompleted());
            cSection.set("timesCompletedSinceTimer", challenges.get(currentChallenge).getTimesCompletedSinceTimer());
        }
    }

    private PlayerInfo loadPlayer() {
        FileConfiguration playerConfig = getPlayerConfig(playerName);
        if (!playerConfig.contains("player.hasIsland")) {
            this.hasIsland = false;
            this.islandLocation = null;
            this.homeLocation = null;
            this.hasParty = false;
            this.buildChallengeList();
            createPlayerConfig();
            return this;
        }
        try {
            this.displayName = playerConfig.getString("player.displayName", playerName);
            this.hasIsland = playerConfig.getBoolean("player.hasIsland");
            this.islandLocation = new Location(uSkyBlock.getSkyBlockWorld(),
                    playerConfig.getInt("player.islandX"), playerConfig.getInt("player.islandY"), playerConfig.getInt("player.islandZ"));
            this.homeLocation = new Location(uSkyBlock.getSkyBlockWorld(),
                    playerConfig.getInt("player.homeX") + 0.5, playerConfig.getInt("player.homeY") + 0.2, playerConfig.getInt("player.homeZ") + 0.5,
                    playerConfig.getInt("player.homeYaw", 0), playerConfig.getInt("player.homePitch", 0));
            buildChallengeList();
            for (String currentChallenge : challenges.keySet()) {
                challenges.put(currentChallenge, new ChallengeCompletion(currentChallenge, playerConfig.getLong("player.challenges." + currentChallenge + ".firstCompleted"), playerConfig.getInt("player.challenges." + currentChallenge + ".timesCompleted"), playerConfig.getInt("player.challenges." + currentChallenge + ".timesCompletedSinceTimer")));
            }
            if (Bukkit.getPlayer(playerName) != null && playerConfig.getBoolean("player.kickWarning")) {
                Bukkit.getPlayer(playerName).sendMessage("\u00a7cYou were removed from your island since the last time you played!");
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
        playerConfigFile = new File(uSkyBlock.getInstance().directoryPlayers, player + ".yml");
        playerData = YamlConfiguration.loadConfiguration(playerConfigFile);
    }

    private void createPlayerConfig() {
        uSkyBlock.log(Level.INFO, "Creating new player config!");
        this.setupPlayer(playerName);
    }

    public FileConfiguration getPlayerConfig(final String player) {
        if (playerData == null) {
            reloadPlayerConfig(player);
        }
        return playerData;
    }

    public void save() {
        if (playerData == null) {
            uSkyBlock.log(Level.INFO, "Can't save player data!");
            return;
        }
        FileConfiguration playerConfig = playerData;
        playerConfig.set("player.hasIsland", getHasIsland());
        playerConfig.set("player.displayName", displayName);
        Location location = this.getIslandLocation();
        if (location != null) {
            playerConfig.set("player.islandX", location.getBlockX());
            playerConfig.set("player.islandY", location.getBlockY());
            playerConfig.set("player.islandZ", location.getBlockZ());
        } else {
            playerConfig.set("player.islandX", 0);
            playerConfig.set("player.islandY", 0);
            playerConfig.set("player.islandZ", 0);
        }
        Location home = this.getHomeLocation();
        if (home != null) {
            playerConfig.set("player.homeX", home.getBlockX());
            playerConfig.set("player.homeY", home.getBlockY());
            playerConfig.set("player.homeZ", home.getBlockZ());
            playerConfig.set("player.homeYaw", home.getYaw());
            playerConfig.set("player.homePitch", home.getPitch());
        } else {
            playerConfig.set("player.homeX", 0);
            playerConfig.set("player.homeY", 0);
            playerConfig.set("player.homeZ", 0);
            playerConfig.set("player.homeYaw", 0);
            playerConfig.set("player.homePitch", 0);
        }
        final Iterator<String> ent = challenges.keySet().iterator();
        String currentChallenge = "";
        while (ent.hasNext()) {
            currentChallenge = ent.next();
            playerConfig.set("player.challenges." + currentChallenge + ".firstCompleted", challenges.get(currentChallenge).getFirstCompleted());
            playerConfig.set("player.challenges." + currentChallenge + ".timesCompleted", challenges.get(currentChallenge).getTimesCompleted());
            playerConfig.set("player.challenges." + currentChallenge + ".timesCompletedSinceTimer", challenges.get(currentChallenge).getTimesCompletedSinceTimer());
        }
        playerConfigFile = new File(uSkyBlock.getInstance().directoryPlayers, playerName + ".yml");
        try {
            playerConfig.save(playerConfigFile);
            uSkyBlock.log(Level.INFO, "Player data saved!");
        } catch (IOException ex) {
            uSkyBlock.getInstance().getLogger().log(Level.SEVERE, "Could not save config to " + playerConfigFile, ex);
        }
    }
}
