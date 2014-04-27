package us.talabrek.ultimateskyblock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

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
