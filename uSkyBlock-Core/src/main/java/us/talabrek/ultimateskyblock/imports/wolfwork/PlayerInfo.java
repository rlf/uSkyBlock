package us.talabrek.ultimateskyblock.imports.wolfwork;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Mirror of the PlayerInfo in wolfwork/uSkyBlock
 */
public class PlayerInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String playerName;
    private boolean hasIsland;
    private boolean hasParty;
    private boolean warpActive;
    private List<String> members;
    private List<String> banned;
    private String partyLeader;
    private String partyIslandLocation;
    private String islandLocation;
    private String homeLocation;
    private String warpLocation;
    private String deathWorld;
    private HashMap<String, Boolean> challengeList;
    private float islandExp;
    private int islandLevel;

    public PlayerInfo(final String playerName) {
        this.playerName = playerName;
        members = new ArrayList<String>();
        banned = new ArrayList<String>();
        hasIsland = false;
        warpActive = false;
        islandLocation = null;
        homeLocation = null;
        warpLocation = null;
        deathWorld = null;
        hasParty = false;
        partyLeader = null;
        partyIslandLocation = null;
        islandExp = 0.0F;
        challengeList = new HashMap<String, Boolean>();
        islandLevel = 0;
    }

    public void startNewIsland(Location l) {
        hasIsland = true;
        setIslandLocation(l);
        islandLevel = 0;
        islandExp = 0.0F;
        partyIslandLocation = null;
        partyLeader = null;
        hasParty = false;
        homeLocation = null;
        warpLocation = null;
        warpActive = false;
        members = new ArrayList<String>();
    }

    public void removeFromIsland() {
        hasIsland = false;
        setIslandLocation(null);
        islandLevel = 0;
        islandExp = 0.0F;
        partyIslandLocation = null;
        partyLeader = null;
        hasParty = false;
        homeLocation = null;
        warpLocation = null;
        warpActive = false;
        members = new ArrayList<String>();
    }

    public void toggleWarpActive() {
        if (!this.warpActive)
            warpActive = true;
        else
            warpActive = false;
    }

    public void warpOn() {
        warpActive = true;
    }

    public void warpOff() {
        warpActive = true;
    }

    public boolean isWarpActive() {
        return warpActive;
    }

    public void setWarpLocation(Location l) {
        warpLocation = getStringLocation(l);
    }

    public Location getWarpLocation() {
        return getLocationString(warpLocation);
    }

    public List<String> getBanned() {
        if (banned == null)
            banned = new ArrayList<String>();
        return banned;
    }

    public void addBan(String player) {
        getBanned().add(player);
    }

    public void removeBan(String player) {
        getBanned().remove(player);
    }

    public boolean isBanned(String player) {
        return getBanned().contains(player);
    }

    public void addMember(final String member) {
        members.add(member);
    }

    public void clearChallenges() {
        challengeList.clear();
    }

    public void buildChallengeList() {
        // Disabled
    }

    public boolean challengeExists(final String challenge) {
        if (challengeList.containsKey(challenge.toLowerCase())) {
            return true;
        }
        return false;
    }

    public boolean checkChallenge(final String challenge) {
        if (challengeList.containsKey(challenge.toLowerCase())) {
            return challengeList.get(challenge.toLowerCase()).booleanValue();
        }

        return false;
    }

    public void completeChallenge(final String challenge) {
        if (challengeList.containsKey(challenge)) {
            challengeList.remove(challenge);
            challengeList.put(challenge, Boolean.valueOf(true));
        }
    }

    public void displayChallengeList() {
        // Does nothing
    }

    public String getDeathWorld() {
        return deathWorld;
    }

    public boolean getHasIsland() {
        return hasIsland;
    }

    public boolean getHasParty() {
        if (members == null) {
            members = new ArrayList<String>();
        }
        return hasParty;
    }

    public Location getHomeLocation() {
        return getLocationString(homeLocation);
    }

    public float getIslandExp() {
        return islandExp;
    }

    public int getIslandLevel() {
        return islandLevel;
    }

    public Location getIslandLocation() {
        return getLocationString(islandLocation);
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
            return new Location(w, x, y, z);
        }
        return null;
    }

    public List<String> getMembers() {
        return members;
    }

    public Location getPartyIslandLocation() {
        return getLocationString(partyIslandLocation);
    }

    public String getPartyLeader() {
        return partyLeader;
    }

    public Player getPlayer() {
        return Bukkit.getPlayerExact(playerName);
    }

    public String getPlayerName() {
        return playerName;
    }

    private String getStringLocation(final Location l) {
        if (l == null) {
            return "";
        }
        return l.getWorld().getName() + ":" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
    }

    public void ListData() {
        // Do nothing
    }

    public void removeMember(final String member) {
        members.remove(member);
    }

    public void resetAllChallenges() {
        challengeList = null;
        buildChallengeList();
    }

    public void resetChallenge(final String challenge) {
        if (challengeList.containsKey(challenge)) {
            challengeList.remove(challenge);
            challengeList.put(challenge, Boolean.valueOf(false));
        }
    }

    public void setDeathWorld(final String dw) {
        deathWorld = dw;
    }

    public void setHasIsland(final boolean b) {
        hasIsland = b;
    }

    public void setHomeLocation(final Location l) {
        homeLocation = getStringLocation(l);
    }

    public void setIslandExp(final float i) {
        islandExp = i;
    }

    public void setIslandLevel(final int i) {
        islandLevel = i;
    }

    public void setIslandLocation(final Location l) {
        islandLocation = getStringLocation(l);
    }

    public void setJoinParty(final String leader, final Location l) {
        hasParty = true;
        partyLeader = leader;
        partyIslandLocation = getStringLocation(l);
    }

    public void setLeaveParty() {
        hasParty = false;
        partyLeader = null;
        islandLevel = 0;
        partyIslandLocation = null;
        members = new ArrayList<String>();
    }

    public void setMembers(final List<String> newMembers) {
        members = newMembers;
    }

    public void setPartyIslandLocation(final Location l) {
        partyIslandLocation = getStringLocation(l);
    }

    public void setPartyLeader(final String leader) {
        partyLeader = leader;
    }

    public void setPlayerName(final String s) {
        playerName = s;
    }

    public Location getTeleportLocation() {
        Location target = getHomeLocation();
        if (target == null) {
            if (getIslandLocation() == null && getHasParty())
                target = getPartyIslandLocation();
            else if (getIslandLocation() != null)
                target = getIslandLocation();
        }

        return target;
    }

    public boolean teleportHome(Player player) {
        // Do nothing
        return false;
    }

    public boolean teleportWarp(Player player) {
        // Do nothing
        return false;
    }

    public void recalculateLevel(final Runnable callback) {
        // Do nothing
    }

    public void save() {
        // Do nothing
    }

    @Override
    public String toString() {
        return "PlayerInfo{" +
                "playerName='" + playerName + '\'' +
                ", hasIsland=" + hasIsland +
                ", hasParty=" + hasParty +
                ", warpActive=" + warpActive +
                ", members=" + members +
                ", banned=" + banned +
                ", partyLeader='" + partyLeader + '\'' +
                ", partyIslandLocation='" + partyIslandLocation + '\'' +
                ", islandLocation='" + islandLocation + '\'' +
                ", homeLocation='" + homeLocation + '\'' +
                ", warpLocation='" + warpLocation + '\'' +
                ", deathWorld='" + deathWorld + '\'' +
                ", challengeList=" + challengeList +
                ", islandExp=" + islandExp +
                ", islandLevel=" + islandLevel +
                '}';
    }
}
