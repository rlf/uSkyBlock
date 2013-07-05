package us.talabrek.ultimateskyblock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class PlayerInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String playerName;
	private boolean hasIsland;
	private boolean hasParty;
	private List<String> members;
	private String partyLeader;
	private String partyIslandLocation;
	private String islandLocation;
	private String homeLocation;
	private String deathWorld;
	private HashMap<String, Boolean> challengeList;
	private float islandExp;
	private int islandLevel;

	public PlayerInfo(String playerName) {
		this.playerName = playerName;
		members = new ArrayList<String>();
		hasIsland = false;

		islandLocation = null;
		homeLocation = null;
		deathWorld = null;
		hasParty = false;
		partyLeader = null;
		partyIslandLocation = null;
		islandExp = 0.0F;
		challengeList = new HashMap<String, Boolean>();
		islandLevel = 0;
	}

	public void setPlayerName(String s) {
		playerName = s;
	}

	public void setPartyIslandLocation(Location l) {
		partyIslandLocation = getStringLocation(l);
	}

	public Location getPartyIslandLocation() {
		return getLocationString(partyIslandLocation);
	}

	public Player getPlayer() {
		return Bukkit.getPlayer(playerName);
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setHasIsland(boolean b) {
		hasIsland = b;
	}

	public boolean getHasIsland() {
		return hasIsland;
	}

	public String getDeathWorld() {
		return deathWorld;
	}

	public void setDeathWorld(String dw) {
		deathWorld = dw;
	}

	public void setIslandLocation(Location l) {
		islandLocation = getStringLocation(l);
	}

	public Location getIslandLocation() {
		return getLocationString(islandLocation);
	}

	public void setHomeLocation(Location l) {
		homeLocation = getStringLocation(l);
	}

	public Location getHomeLocation() {
		return getLocationString(homeLocation);
	}

	public boolean getHasParty() {
		if (members == null) {
			members = new ArrayList<String>();
		}
		return hasParty;
	}

	public void setJoinParty(String leader, Location l) {
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

	public List<String> getMembers() {
		return members;
	}

	public String getPartyLeader() {
		return partyLeader;
	}

	public void setPartyLeader(String leader) {
		partyLeader = leader;
	}

	public void setMembers(List<String> newMembers) {
		members = newMembers;
	}

	public void addMember(String member) {
		members.add(member);
	}

	public void removeMember(String member) {
		members.remove(member);
	}

	public void setIslandExp(float i) {
		islandExp = i;
	}

	public float getIslandExp() {
		return islandExp;
	}

	public void setIslandLevel(int i) {
		islandLevel = i;
	}

	public int getIslandLevel() {
		return islandLevel;
	}

	public void ListData() {
		System.out.println("uSkyblock "+"Player: " + getPlayerName());
		System.out.println("uSkyblock "+"Has an island?: " + getHasIsland());
		System.out.println("uSkyblock "+"Has a party?: " + getHasParty());
		if (getHasIsland())
			System.out.println("uSkyblock "+"Island Location: " + getStringLocation(getIslandLocation()));
		if (getHasParty() && getPartyIslandLocation() != null)
			System.out.println("uSkyblock "+"Island Location (party): " + getStringLocation(getPartyIslandLocation()));
		System.out.println("uSkyblock "+"Island Level: " + islandLevel);
	}

	private Location getLocationString(String s) {
		if (s == null || s.trim() == "") { return null; }
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

	public void completeChallenge(String challenge) {
		if (challengeList.containsKey(challenge)) {
			challengeList.remove(challenge);
			challengeList.put(challenge, Boolean.valueOf(true));
		}
	}

	public void resetChallenge(String challenge) {
		if (challengeList.containsKey(challenge)) {
			challengeList.remove(challenge);
			challengeList.put(challenge, Boolean.valueOf(false));
		}
	}

	public boolean checkChallenge(String challenge) {
		if (challengeList.containsKey(challenge.toLowerCase())) { return challengeList.get(challenge.toLowerCase()).booleanValue(); }

		return false;
	}

	public boolean challengeExists(String challenge) {
		if (challengeList.containsKey(challenge.toLowerCase())) { return true; }
		return false;
	}

	public void resetAllChallenges() {
		challengeList = null;
		buildChallengeList();
	}

	public void buildChallengeList() {
		if (challengeList == null)
			challengeList = new HashMap<String, Boolean>();
		final Iterator<?> itr = Settings.challenges_challengeList.iterator();
		while (itr.hasNext()) {
			final String current = (String) itr.next();
			if (!challengeList.containsKey(current.toLowerCase()))
				challengeList.put(current.toLowerCase(), Boolean.valueOf(false));
		}
		if (challengeList.size() > Settings.challenges_challengeList.size()) {
			final Object[] challengeArray = challengeList.keySet().toArray();
			for (int i = 0; i < challengeArray.length; i++) {
				if (!Settings.challenges_challengeList.contains(challengeArray[i].toString())) {
					challengeList.remove(challengeArray[i].toString());
				}
			}
		}
	}

	public void displayChallengeList() {
		final Iterator<String> itr = challengeList.keySet().iterator();
		System.out.println("uSkyblock "+"Displaying Challenge list for " + playerName);
		while (itr.hasNext()) {
			final String current = itr.next();
			System.out.println("uSkyblock "+current + ": " + challengeList.get(current));
		}
	}

	private String getStringLocation(Location l) {
		if (l == null) { return ""; }
		return l.getWorld().getName() + ":" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
	}
}