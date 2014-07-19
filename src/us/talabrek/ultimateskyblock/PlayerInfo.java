package us.talabrek.ultimateskyblock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class PlayerInfo implements Serializable {
    public static final long serialVersionUID = 1L;
    public String playerName;
    public boolean hasIsland;
    public boolean hasParty;
    public boolean warpActive;
    public List<String> members;
    public List<String> banned;
    public String partyLeader;
    public String partyIslandLocation;
    public String islandLocation;
    public String homeLocation;
    public String warpLocation;
    public String deathWorld;
    public HashMap<String, Boolean> challengeList;
    public float islandExp;
    public int islandLevel;

}
