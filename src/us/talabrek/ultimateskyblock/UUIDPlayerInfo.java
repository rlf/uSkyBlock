package us.talabrek.ultimateskyblock;

import java.io.Serializable;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class UUIDPlayerInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private UUID playerUUID;
    private boolean hasIsland;
    private boolean hasParty;
    private boolean warpActive;
    private List<UUID> members;
    private List<UUID> banned;
    private UUID partyLeader;
    private String partyIslandLocation;
    private String islandLocation;
    private String homeLocation;
    private String warpLocation;
    private String deathWorld;
    private HashMap<String, Boolean> challengeList;
    private float islandExp;
    private int islandLevel;

    public UUIDPlayerInfo(final UUID playerUUID) {
        this.playerUUID = playerUUID;
        members = new ArrayList<UUID>();
        banned = new ArrayList<UUID>();
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

    // constructor using OldPlayerInfo, for conversion
    public UUIDPlayerInfo(final PlayerInfo oldPlayerInfo) {
        playerUUID = Bukkit.getOfflinePlayer(oldPlayerInfo.playerName).getUniqueId();

        members = new ArrayList<UUID>();
        for (String memberName : oldPlayerInfo.members) {
            members.add(Bukkit.getOfflinePlayer(memberName).getUniqueId());
        }

        banned = new ArrayList<UUID>();
        for (String bannedName : oldPlayerInfo.banned) {
            banned.add(Bukkit.getOfflinePlayer(bannedName).getUniqueId());
        }

        hasIsland = oldPlayerInfo.hasIsland;

        hasParty = oldPlayerInfo.hasParty;

        warpActive = oldPlayerInfo.warpActive;

        if (oldPlayerInfo.partyLeader != null)
            partyLeader = Bukkit.getOfflinePlayer(oldPlayerInfo.partyLeader).getUniqueId();
        else
            partyLeader = null;

        partyIslandLocation = oldPlayerInfo.partyIslandLocation;

        islandLocation = oldPlayerInfo.islandLocation;

        homeLocation = oldPlayerInfo.homeLocation;

        warpLocation = oldPlayerInfo.warpLocation;

        deathWorld = oldPlayerInfo.deathWorld;

        challengeList = oldPlayerInfo.challengeList;

        islandExp = oldPlayerInfo.islandExp;

        islandLevel = oldPlayerInfo.islandLevel;

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
        members = new ArrayList<UUID>();
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
        members = new ArrayList<UUID>();
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

    public List<UUID> getBanned() {
        if (banned == null)
            banned = new ArrayList<UUID>();
        return banned;
    }

    public void addBan(UUID playerUUID) {
        getBanned().add(playerUUID);
    }

    public void removeBan(UUID playerUUID) {
        getBanned().remove(playerUUID);
    }

    public boolean isBanned(UUID playerUUID) {
        return getBanned().contains(playerUUID);
    }

    public void addMember(final UUID memberUUID) {
        members.add(memberUUID);
    }

    public void clearChallenges() {
        challengeList.clear();
    }

    public void buildChallengeList() {
        if (challengeList == null) {
            challengeList = new HashMap<String, Boolean>();
        }
        final Iterator<?> itr = Settings.challenges_challengeList.iterator();
        while (itr.hasNext()) {
            final String current = (String) itr.next();
            if (!challengeList.containsKey(current.toLowerCase())) {
                challengeList.put(current.toLowerCase(), Boolean.valueOf(false));
            }
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
        final Iterator<String> itr = challengeList.keySet().iterator();
        System.out.println("uSkyblock " + "Displaying Challenge list for " + Bukkit.getOfflinePlayer(playerUUID).getName());
        while (itr.hasNext()) {
            final String current = itr.next();
            System.out.println("uSkyblock " + current + ": " + challengeList.get(current));
        }
    }

    public String getDeathWorld() {
        return deathWorld;
    }

    public boolean getHasIsland() {
        return hasIsland;
    }

    public boolean getHasParty() {
        if (members == null) {
            members = new ArrayList<UUID>();
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

    public List<UUID> getMembers() {
        return members;
    }

    public Location getPartyIslandLocation() {
        return getLocationString(partyIslandLocation);
    }

    public UUID getPartyLeader() {
        return partyLeader;
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(playerUUID);
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    private String getStringLocation(final Location l) {
        if (l == null) {
            return "";
        }
        return l.getWorld().getName() + ":" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
    }

    public void ListData() {
        System.out.println("uSkyblock " + "Player: " + getPlayer().getName());
        System.out.println("uSkyblock " + "Has an island?: " + getHasIsland());
        System.out.println("uSkyblock " + "Has a party?: " + getHasParty());
        if (getHasIsland()) {
            System.out.println("uSkyblock " + "Island Location: " + getStringLocation(getIslandLocation()));
        }
        if (getHasParty() && getPartyIslandLocation() != null) {
            System.out.println("uSkyblock " + "Island Location (party): " + getStringLocation(getPartyIslandLocation()));
        }
        System.out.println("uSkyblock " + "Island Level: " + islandLevel);
    }

    public void removeMember(final UUID member) {
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

    public void setJoinParty(final UUID leader, final Location l) {
        hasParty = true;
        partyLeader = leader;
        partyIslandLocation = getStringLocation(l);
    }

    public void setLeaveParty() {
        hasParty = false;
        partyLeader = null;
        islandLevel = 0;
        partyIslandLocation = null;
        members = new ArrayList<UUID>();
    }

    public void setMembers(final List<UUID> newMembers) {
        members = newMembers;
    }

    public void setPartyIslandLocation(final Location l) {
        partyIslandLocation = getStringLocation(l);
    }

    public void setPartyLeader(final UUID leader) {
        partyLeader = leader;
    }

    public void setPlayerUUID(final UUID s) {
        playerUUID = s;
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
        Location target = getTeleportLocation();

        if (target == null)
            return false;

        if (getHomeLocation() == null)
            setHomeLocation(target);

        if (Misc.safeTeleport(player, target)) {
            uSkyBlock.getInstance().removeCreatures(target);
            return true;
        } else {
            player.teleport(target);
        }

        return false;
    }

    public boolean teleportWarp(Player player) {
        Location target = getWarpLocation();

        if (target == null) {
            if (getIslandLocation() == null && getHasParty())
                target = getPartyIslandLocation();
            else
                target = getIslandLocation();
        }

        if (target == null)
            return false;

        if (Misc.safeTeleport(player, target)) {
            uSkyBlock.getInstance().removeCreatures(target);
            return true;
        }

        return false;
    }

    public void recalculateLevel(final Runnable callback) {
        uSkyBlock.getInstance().getServer().getScheduler().runTask(uSkyBlock.getInstance(), new Runnable() {
            public void run() {

                try {
                    Location loc;
                    if (getHasParty())
                        loc = getPartyIslandLocation();
                    else
                        loc = getIslandLocation();

                    if (loc != null) {
                        int blockCount = 0;
                        int cobbleCount = 0;
                        int endCount = 0;
                        int px = loc.getBlockX();
                        int py = loc.getBlockY();
                        int pz = loc.getBlockZ();
                        int radius = (Settings.island_distance / 2);
                        for (int x = -radius; x <= radius; ++x) {
                            for (int y = 0; y <= 255; ++y) {
                                for (int z = -radius; z <= radius; ++z) {
                                    Block b = loc.getWorld().getBlockAt(px + x, py + y, pz + z);
                                    switch (b.getType()) {
                                        case DIAMOND_BLOCK:
                                        case EMERALD_BLOCK:
                                        case BEACON:
                                        case DRAGON_EGG:
                                            blockCount += 300;
                                            break;
                                        case GOLD_BLOCK:
                                        case ENCHANTMENT_TABLE:
                                            blockCount += 150;
                                            break;
                                        case OBSIDIAN:
                                        case IRON_BLOCK:
                                        case REDSTONE_BLOCK:
                                            blockCount += 10;
                                            break;
                                        case BOOKSHELF:
                                        case JUKEBOX:
                                        case HARD_CLAY:
                                        case STAINED_CLAY:
                                            blockCount += 5;
                                            break;
                                        case ICE:
                                        case CLAY:
                                        case NETHER_BRICK:
                                        case GRASS:
                                        case MYCEL:
                                        case GLOWSTONE:
                                        case NETHER_BRICK_STAIRS:
                                        case QUARTZ_BLOCK:
                                        case QUARTZ_STAIRS:
                                            blockCount += 3;
                                            break;
                                        case SMOOTH_BRICK:
                                        case BRICK:
                                        case WOOL:
                                        case SANDSTONE:
                                        case BRICK_STAIRS:
                                        case SMOOTH_STAIRS:
                                        case DOUBLE_STEP:
                                        case GLASS:
                                            blockCount += 2;
                                            break;
                                        case COBBLESTONE:
                                            if (cobbleCount < 10000) {
                                                ++cobbleCount;
                                                ++blockCount;
                                            }
                                            break;
                                        case ENDER_STONE:
                                            if (endCount < 10000) {
                                                ++endCount;
                                                ++blockCount;
                                            }
                                            break;

                                        // 0 pointers
                                        case WATER:
                                        case STATIONARY_WATER:
                                        case LAVA:
                                        case STATIONARY_LAVA:
                                        case AIR:
                                            break;

                                        default:
                                            ++blockCount;
                                            break;
                                    }
                                }
                            }
                        }
                        setIslandLevel(blockCount / 100);
                    }
                } catch (Exception e) {
                    uSkyBlock.getLog().severe("Error while calculating island level");
                    e.printStackTrace();
                }

                Bukkit.getScheduler().scheduleSyncDelayedTask(uSkyBlock.getInstance(), new Runnable() {
                    public void run() {
                        uSkyBlock.getInstance().updateTopIsland(UUIDPlayerInfo.this);
                    }
                }, 0L);

                if (callback != null)
                    Bukkit.getScheduler().runTask(uSkyBlock.getInstance(), callback);
            }
        });
    }

    public void save() {
        uSkyBlock.getInstance().savePlayer(this);
    }
}