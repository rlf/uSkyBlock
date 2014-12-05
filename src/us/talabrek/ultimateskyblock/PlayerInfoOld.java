package us.talabrek.ultimateskyblock;

import java.io.*;
import org.bukkit.entity.*;
import org.bukkit.*;

public class PlayerInfoOld implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String playerName;
    private boolean hasIsland;
    private boolean hasParty;
    private String islandLocation;
    private String homeLocation;
    private String partyIslandLocation;
    
    public PlayerInfoOld(final String playerName) {
        super();
        this.hasIsland = false;
        this.hasParty = false;
        this.islandLocation = "";
        this.homeLocation = "";
    }
    
    public PlayerInfoOld(final String playerName, final boolean hasIsland, final int iX, final int iY, final int iZ, final int hX, final int hY, final int hZ) {
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
    
    public void displayData(final String player) {
        System.out.print(String.valueOf(player) + " has an island: " + this.getHasIsland());
        if (this.getIslandLocation() != null) {
            System.out.print(String.valueOf(player) + " island location: " + this.getIslandLocation().toString());
        }
        if (this.getHomeLocation() != null) {
            System.out.print(String.valueOf(player) + " home location: " + this.getHomeLocation().toString());
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
}
