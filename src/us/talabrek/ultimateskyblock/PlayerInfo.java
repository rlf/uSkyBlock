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
		/*  34 */this.playerName = playerName;
		/*  35 */this.members = new ArrayList<String>();
		/*  36 */this.hasIsland = false;

		/*  38 */this.islandLocation = null;
		/*  39 */this.homeLocation = null;
		/*  40 */this.deathWorld = null;
		/*  41 */this.hasParty = false;
		/*  42 */this.partyLeader = null;
		/*  43 */this.partyIslandLocation = null;
		/*  44 */this.islandExp = 0.0F;
		/*  45 */this.challengeList = new HashMap<String, Boolean>();
		/*  46 */this.islandLevel = 0;
	}

	public void setPlayerName(String s) {
		/*  50 */this.playerName = s;
	}

	public void setPartyIslandLocation(Location l) {
		/*  54 */this.partyIslandLocation = getStringLocation(l);
	}

	public Location getPartyIslandLocation() {
		/*  58 */return getLocationString(this.partyIslandLocation);
	}

	public Player getPlayer() {
		/*  62 */return Bukkit.getPlayer(this.playerName);
	}

	public String getPlayerName() {
		/*  66 */return this.playerName;
	}

	public void setHasIsland(boolean b) {
		/*  70 */this.hasIsland = b;
	}

	public boolean getHasIsland() {
		/*  74 */return this.hasIsland;
	}

	public String getDeathWorld() {
		/*  78 */return this.deathWorld;
	}

	public void setDeathWorld(String dw) {
		/*  81 */this.deathWorld = dw;
	}

	public void setIslandLocation(Location l) {
		/*  85 */this.islandLocation = getStringLocation(l);
	}

	public Location getIslandLocation() {
		/*  89 */return getLocationString(this.islandLocation);
	}

	public void setHomeLocation(Location l) {
		/*  93 */this.homeLocation = getStringLocation(l);
	}

	public Location getHomeLocation() {
		/*  97 */return getLocationString(this.homeLocation);
	}

	public boolean getHasParty() {
		/* 101 */if (this.members == null) {
			/* 103 */this.members = new ArrayList<String>();
		}
		/* 105 */return this.hasParty;
	}

	public void setJoinParty(String leader, Location l) {
		/* 109 */this.hasParty = true;
		/* 110 */this.partyLeader = leader;
		/* 111 */this.partyIslandLocation = getStringLocation(l);
	}

	public void setLeaveParty() {
		/* 115 */this.hasParty = false;
		/* 116 */this.partyLeader = null;
		/* 117 */this.islandLevel = 0;
		/* 118 */this.partyIslandLocation = null;
		/* 119 */this.members = new ArrayList<String>();
	}

	public List<String> getMembers() {
		/* 123 */return this.members;
	}

	public String getPartyLeader() {
		/* 127 */return this.partyLeader;
	}

	public void setPartyLeader(String leader) {
		/* 131 */this.partyLeader = leader;
	}

	public void setMembers(List<String> newMembers) {
		/* 135 */this.members = newMembers;
	}

	public void addMember(String member) {
		/* 140 */this.members.add(member);
	}

	public void removeMember(String member) {
		/* 145 */this.members.remove(member);
	}

	public void setIslandExp(float i) {
		/* 149 */this.islandExp = i;
	}

	public float getIslandExp() {
		/* 153 */return this.islandExp;
	}

	public void setIslandLevel(int i) {
		/* 157 */this.islandLevel = i;
	}

	public int getIslandLevel() {
		/* 161 */return this.islandLevel;
	}

	public void ListData() {
		/* 165 */System.out.print("Player: " + getPlayerName());
		/* 166 */System.out.print("Has an island?: " + getHasIsland());
		/* 167 */System.out.print("Has a party?: " + getHasParty());
		/* 168 */if (getHasIsland())
			/* 169 */System.out.print("Island Location: " + getStringLocation(getIslandLocation()));
		/* 170 */if ((getHasParty()) &&
		/* 171 */(getPartyIslandLocation() != null))
			/* 172 */System.out.print("Island Location (party): " + getStringLocation(getPartyIslandLocation()));
		/* 173 */System.out.print("Island Level: " + this.islandLevel);
	}

	private Location getLocationString(String s) {
		/* 177 */if ((s == null) || (s.trim() == "")) {
			/* 178 */return null;
		}
		/* 180 */String[] parts = s.split(":");
		/* 181 */if (parts.length == 4) {
			/* 182 */World w = Bukkit.getServer().getWorld(parts[0]);
			/* 183 */int x = Integer.parseInt(parts[1]);
			/* 184 */int y = Integer.parseInt(parts[2]);
			/* 185 */int z = Integer.parseInt(parts[3]);
			/* 186 */return new Location(w, x, y, z);
		}
		/* 188 */return null;
	}

	public void completeChallenge(String challenge) {
		/* 193 */if (this.challengeList.containsKey(challenge)) {
			/* 195 */this.challengeList.remove(challenge);
			/* 196 */this.challengeList.put(challenge, Boolean.valueOf(true));
		}
	}

	public void resetChallenge(String challenge) {
		/* 202 */if (this.challengeList.containsKey(challenge)) {
			/* 204 */this.challengeList.remove(challenge);
			/* 205 */this.challengeList.put(challenge, Boolean.valueOf(false));
		}
	}

	public boolean checkChallenge(String challenge) {
		/* 211 */if (this.challengeList.containsKey(challenge.toLowerCase())) {
			/* 213 */return ((Boolean) this.challengeList.get(challenge.toLowerCase())).booleanValue();
		}

		/* 216 */return false;
	}

	public boolean challengeExists(String challenge) {
		/* 221 */if (this.challengeList.containsKey(challenge.toLowerCase())) {
			/* 223 */return true;
		}
		/* 225 */return false;
	}

	public void resetAllChallenges() {
		/* 230 */this.challengeList = null;
		/* 231 */buildChallengeList();
	}

	public void buildChallengeList() {
		/* 236 */if (this.challengeList == null)
			/* 237 */this.challengeList = new HashMap<String, Boolean>();
		/* 238 */Iterator<?> itr = Settings.challenges_challengeList.iterator();
		/* 239 */while (itr.hasNext()) {
			/* 241 */String current = (String) itr.next();
			/* 242 */if (!this.challengeList.containsKey(current.toLowerCase()))
				/* 243 */this.challengeList.put(current.toLowerCase(), Boolean.valueOf(false));
		}
		/* 245 */if (this.challengeList.size() > Settings.challenges_challengeList.size()) {
			/* 247 */Object[] challengeArray = this.challengeList.keySet().toArray();
			/* 248 */for (int i = 0; i < challengeArray.length; i++) {
				/* 250 */if (!Settings.challenges_challengeList.contains(challengeArray[i].toString())) {
					/* 252 */this.challengeList.remove(challengeArray[i].toString());
				}
			}
		}
	}

	public void displayChallengeList() {
		/* 261 */Iterator<String> itr = this.challengeList.keySet().iterator();
		/* 262 */System.out.print("Displaying Challenge list for " + this.playerName);
		/* 263 */while (itr.hasNext()) {
			/* 265 */String current = (String) itr.next();
			/* 266 */System.out.print(current + ": " + this.challengeList.get(current));
		}
	}

	private String getStringLocation(Location l) {
		/* 271 */if (l == null) {
			/* 272 */return "";
		}
		/* 274 */return l.getWorld().getName() + ":" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
	}
}