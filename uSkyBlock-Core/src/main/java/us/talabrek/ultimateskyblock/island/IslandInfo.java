package us.talabrek.ultimateskyblock.island;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.player.Perk;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;
import us.talabrek.ultimateskyblock.util.TimeUtil;
import us.talabrek.ultimateskyblock.util.UUIDUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dk.lockfuglsang.minecraft.file.FileUtil.readConfig;
import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Data object for an island
 */
public class IslandInfo {
    private static final Logger log = Logger.getLogger(IslandInfo.class.getName());
    private static final Pattern OLD_LOG_PATTERN = Pattern.compile("\u00a7d\\[(?<date>[^\\]]+)\\]\u00a77 (?<msg>.*)");
    private static final int YML_VERSION = 1;
    private static File directory = new File(".");

    private final File file;
    private final FileConfiguration config;
    private final String name;
    private boolean dirty = false;
    private boolean toBeDeleted = false;

    public IslandInfo(String islandName) {
        config = new YamlConfiguration();
        file = new File(directory, islandName + ".yml");
        name = islandName;
        if (file.exists()) {
            readConfig(config, file);

            // Backwards compatibility.
            if (config.contains("maxSize")) {
                int oldMaxSize = config.getInt("maxSize");
                if (oldMaxSize > Settings.general_maxPartySize) {
                    ConfigurationSection leaderSection = config.getConfigurationSection("party.members." + getLeader());
                    if (leaderSection != null) {
                        leaderSection.set("maxPartySizePermission", oldMaxSize);
                    }
                }
                config.set("maxSize", null);
                save();
            }

            if (config.getInt("version", 0) < YML_VERSION) {
                updateConfig();
            }
        } else {
            log.fine("No file for " + islandName + " found, creating a fresh island!");
        }
    }

    public boolean exists() {
        return this.file.exists();
    }
    
    private void updateConfig() {
        int currentVersion = config.getInt("version", 0);
        if (currentVersion < 1) {
            // add ban-info to the individual player-configs.
            for (String banned : getBans()) {
                banPlayerInfo(banned);
            }
        }
        config.set("version", YML_VERSION);
        save();
    }

    public static void setDirectory(File dir) {
        directory = dir;
    }

    public void resetIslandConfig(final String leader) {
        config.set("general.level", 0);
        config.set("general.warpLocationX", 0);
        config.set("general.warpLocationY", 0);
        config.set("general.warpLocationZ", 0);
        config.set("general.warpActive", false);
        config.set("blocks.leafBreaks", 0);
        config.set("version", YML_VERSION);
        config.set("party", null);
        setupPartyLeader(leader);
        sendMessageToIslandGroup(false, marktr("The island has been created."));
    }

    public void setupPartyLeader(final String leader) {
        config.set("party.leader", leader);
        ConfigurationSection section = config.createSection("party.members." + leader);
        section.set("canChangeBiome", true);
        section.set("canToggleLock", true);
        section.set("canChangeWarp", true);
        section.set("canToggleWarp", true);
        section.set("canInviteOthers", true);
        section.set("canKickOthers", true);
        section.set("canBanOthers", true);
        config.set("party.currentSize", getMembers().size());

        Player onlinePlayer = Bukkit.getPlayer(leader);
        // The only time the onlinePlayer will be null is if it is being converted from another skyblock plugin.
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            updatePermissionPerks(onlinePlayer, uSkyBlock.getInstance().getPerkLogic().getPerk(onlinePlayer));
        }
        save();
    }

    public void addMember(final PlayerInfo playerInfo) {
        playerInfo.setJoinParty(getIslandLocation());
        setupPartyMember(playerInfo.getPlayerName());
    }

    public void setupPartyMember(final String member) {
        if (!getMembers().contains(member)) {
            config.set("party.currentSize", config.getInt("party.currentSize") + 1);
        }
        ConfigurationSection section = config.createSection("party.members." + member);
        section.set("canChangeBiome", false);
        section.set("canToggleLock", false);
        section.set("canChangeWarp", false);
        section.set("canToggleWarp", false);
        section.set("canInviteOthers", false);
        section.set("canKickOthers", false);
        section.set("canBanOthers", false);

        Player onlinePlayer = Bukkit.getPlayer(member);
        // The only time the onlinePlayer will be null is if it is being converted from another skyblock plugin.
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            updatePermissionPerks(onlinePlayer, uSkyBlock.getInstance().getPerkLogic().getPerk(onlinePlayer));
        }
        WorldGuardHandler.addPlayerToOldRegion(name, member);
        save();
    }

    public void updatePermissionPerks(final Player member, Perk perk) {
        ConfigurationSection section = config.getConfigurationSection("party.members." + member.getName());
        boolean dirty = false;
        if (section != null) {
            int maxParty = section.getInt("maxPartySizePermission", Settings.general_maxPartySize);
            if (perk.getMaxPartySize() != maxParty) {
                section.set("maxPartySizePermission", perk.getMaxPartySize());
                dirty = true;
            }
            int maxAnimals = section.getInt("maxAnimals", 0);
            if (perk.getAnimals() != maxAnimals) {
                section.set("maxAnimals", perk.getAnimals());
                dirty = true;
            }
            int maxMonsters = section.getInt("maxMonsters", 0);
            if (perk.getMonsters() != maxMonsters) {
                section.set("maxMonsters", perk.getMonsters());
                dirty = true;
            }
            int maxVillagers = section.getInt("maxVillagers", 0);
            if (perk.getVillagers() != maxVillagers) {
                section.set("maxVillagers", perk.getVillagers());
                dirty = true;
            }
            int maxGolems = section.getInt("maxGolems", 0);
            if (perk.getGolems() != maxGolems) {
                section.set("maxGolems", perk.getGolems());
                dirty = true;
            }
        }
        if (dirty) {
            save();
        }
    }

    public void save() {
        dirty = true;
        if (!file.exists()) {
            saveToFile(); // We use the file-existense a lot, so we need to touch it!
        }
    }

    public boolean isDirty() {
        return dirty || toBeDeleted;
    }

    public void saveToFile() {
        if (toBeDeleted) {
            log.fine("Deleting islandconfig: " + file);
            file.delete();
            toBeDeleted = false;
        } else {
            try {
                log.fine("Saving island-config: " + file);
                config.save(file);
            } catch (IOException e) {
                uSkyBlock.log(Level.SEVERE, "Unable to save island " + file, e);
            }
        }
    }

    public int getMaxPartySize() {
        return getMaxPartyIntValue("maxPartySizePermission", uSkyBlock.getInstance().getPerkLogic().getDefaultPerk().getMaxPartySize());
    }

    public int getMaxAnimals() {
        return getMaxPartyIntValue("maxAnimals", uSkyBlock.getInstance().getPerkLogic().getDefaultPerk().getAnimals());
    }

    public int getMaxMonsters() {
        return getMaxPartyIntValue("maxMonsters", uSkyBlock.getInstance().getPerkLogic().getDefaultPerk().getMonsters());
    }

    public int getMaxVillagers() {
        return getMaxPartyIntValue("maxVillagers", uSkyBlock.getInstance().getPerkLogic().getDefaultPerk().getVillagers());
    }

    public int getMaxGolems() {
        return getMaxPartyIntValue("maxGolems", uSkyBlock.getInstance().getPerkLogic().getDefaultPerk().getGolems());
    }

    private int getMaxPartyIntValue(String name, int defaultValue) {
        int value = defaultValue;
        ConfigurationSection membersSection = config.getConfigurationSection("party.members");
        if (membersSection != null) {
            for (String memberName : membersSection.getKeys(false)) {
                ConfigurationSection memberSection = membersSection.getConfigurationSection(memberName);
                if (memberSection != null) {
                    if (memberSection.isInt(name)) {
                        int memberValue = memberSection.getInt(name, value);
                        if (memberValue > value) {
                            value = memberValue;
                        }
                    }
                }
            }
        }
        return value;
    }

    public String getLeader() {
        return config.getString("party.leader", "");
    }

    public UUID getLeaderUniqueId() {
        String uuid = config.getString("party.leader-uuid", null);
        if (uuid == null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(getLeader());
            if (offlinePlayer != null) {
                uuid = offlinePlayer.getUniqueId().toString();
            }
        }
        return UUIDUtil.fromString(uuid);
    }

    public boolean hasPerm(Player player, String perm) {
        return hasPerm(player.getName(), perm);
    }

    // TODO: 19/12/2014 - R4zorax: UUID
    public boolean hasPerm(String name, String perm) {
        return name.equalsIgnoreCase(getLeader()) || config.getBoolean("party.members." + name + "." + perm);
    }

    public void setBiome(String biome) {
        config.set("general.biome", biome.toUpperCase());
        save();
    }

    public void setWarpLocation(Location loc) {
        if (loc == null) {
            return;
        }
        config.set("general.warpLocationX", loc.getBlockX());
        config.set("general.warpLocationY", loc.getBlockY());
        config.set("general.warpLocationZ", loc.getBlockZ());
        config.set("general.warpYaw", loc.getYaw());
        config.set("general.warpPitch", loc.getPitch());
        config.set("general.warpActive", true);
        save();
    }

    public void togglePerm(final String playername, final String perm) {
        if (!config.contains("party.members." + playername + "." + perm)) {
            return;
        }
        if (config.getBoolean("party.members." + playername + "." + perm)) {
            config.set("party.members." + playername + "." + perm, false);
        } else {
            config.set("party.members." + playername + "." + perm, true);
        }
        save();
    }

    public Set<String> getMembers() {
        ConfigurationSection memberSection = config.getConfigurationSection("party.members");
        return memberSection != null ? memberSection.getKeys(false) : Collections.<String>emptySet();
    }

    public String getBiome() {
        return config.getString("general.biome", "OCEAN").toUpperCase();
    }

    public void log(String message, Object[] args) {
        List<String> log = config.getStringList("log");
        StringBuilder sb = new StringBuilder();
        sb.append(System.currentTimeMillis());
        sb.append(";").append(message);
        for (Object arg : args) {
            sb.append(";").append(arg);
        }
        log.add(0, sb.toString());
        int logSize = uSkyBlock.getInstance().getConfig().getInt("options.island.log-size", 10);
        if (log.size() > logSize) {
            log = log.subList(0, logSize);
        }
        config.set("log", log);
        save();
    }

    public int getPartySize() {
        return config.getInt("party.currentSize", 1);
    }

    public boolean isLeader(Player player) {
        return isLeader(player.getName());
    }

    public boolean isLeader(String playerName) {
        return getLeader() != null && getLeader().equalsIgnoreCase(playerName);
    }

    public boolean hasWarp() {
        return config.getBoolean("general.warpActive");
    }

    public boolean isLocked() {
        return config.getBoolean("general.locked");
    }

    public String getName() {
        return name;
    }

    public void setWarpActive(boolean active) {
        config.set("general.warpActive", active);
        save();
    }

    public void lock(Player player) {
        WorldGuardHandler.islandLock(player, name);
        config.set("general.locked", true);
        sendMessageToIslandGroup(true, marktr("\u00a7b{0}\u00a7d locked the island."), player.getName());
        if (hasWarp()) {
            config.set("general.warpActive", false);
            player.sendMessage(tr("\u00a74Since your island is locked, your incoming warp has been deactivated."));
            sendMessageToIslandGroup(true, marktr("\u00a7b{0}\u00a7d deactivated the island warp."), player.getName());
        }
        save();
    }

    public void unlock(Player player) {
        WorldGuardHandler.islandUnlock(player, name);
        config.set("general.locked", false);
        sendMessageToIslandGroup(true, marktr("\u00a7b{0}\u00a7d unlocked the island."), player.getName());
        save();
    }

    public void sendMessageToIslandGroup(boolean broadcast, String message, Object... args) {
        if (broadcast) {
            for (String player : getMembers()) {
                if (Bukkit.getPlayer(player) != null) {
                    Bukkit.getPlayer(player).sendMessage(tr("\u00a7cSKY \u00a7f> \u00a77 {0}", tr(message, args)));
                }
            }
        }
        log(message, args);
    }

    public boolean isBanned(Player player) {
        return isBanned(player.getName());
    }

    public boolean isBanned(String player) {
        return config.getStringList("banned.list").contains(player);
    }

    public void banPlayer(String player) {
        List<String> stringList = config.getStringList("banned.list");
        if (!stringList.contains(player)) {
            stringList.add(player);
        }
        config.set("banned.list", stringList);
        save();
        banPlayerInfo(player);
    }

    private void banPlayerInfo(String player) {
        PlayerInfo playerInfo = uSkyBlock.getInstance().getPlayerInfo(player);
        if (playerInfo != null) {
            playerInfo.banFromIsland(name);
        }
    }

    public void unbanPlayer(String player) {
        List<String> stringList = config.getStringList("banned.list");
        while (stringList.contains(player)) {
            stringList.remove(player);
        }
        config.set("banned.list", stringList);
        save();
        unbanPlayerInfo(player);
    }

    private void unbanPlayerInfo(String player) {
        PlayerInfo playerInfo = uSkyBlock.getInstance().getPlayerInfo(player);
        if (playerInfo != null) {
            playerInfo.unbanFromIsland(name);
        }
    }

    public List<String> getBans() {
        return config.getStringList("banned.list");
    }

    public List<String> getTrustees() {
        return config.getStringList("trust.list");
    }

    public void trust(String playerName) {
        List<String> trustees = getTrustees();
        if (!trustees.contains(playerName)) {
            trustees.add(playerName);
            config.set("trust.list", trustees);
        }
        PlayerInfo playerInfo = uSkyBlock.getInstance().getPlayerInfo(playerName);
        if (playerInfo != null) {
            playerInfo.addTrust(this.name);
        }
        save();
    }

    public void untrust(String playerName) {
        List<String> trustees = getTrustees();
        trustees.remove(playerName);
        config.set("trust.list", trustees);
        PlayerInfo playerInfo = uSkyBlock.getInstance().getPlayerInfo(playerName);
        if (playerInfo != null) {
            playerInfo.removeTrust(this.name);
        }
        save();
    }

    public void removeMember(PlayerInfo member) {
        WorldGuardHandler.removePlayerFromRegion(name, member.getPlayerName());
        member.setHomeLocation(null);
        member.removeFromIsland();
        member.save();
        removeMember(member.getPlayerName());
    }

    public void removeMember(String playername) {
        config.set("party.members." + playername, null);
        config.set("party.currentSize", getPartySize() - 1);
        save();
        sendMessageToIslandGroup(true, marktr("\u00a7b{0}\u00a7d has been removed from the island group."), playername);
    }

    public void setLevel(double score) {
        config.set("general.level", score);
        save();
    }

    public double getLevel() {
        return getMembers().isEmpty() ? 0 : config.getDouble("general.level");
    }

    public void setRegionVersion(String version) {
        config.set("general.regionVersion", version);
        save();
    }

    public String getRegionVersion() {
        return config.getString("general.regionVersion", "");
    }

    public List<String> getLog() {
        List<String> log = new ArrayList<>();
        if (config.isInt("log.logPos")) {
            int cLog = config.getInt("log.logPos", 1);
            for (int i = 0; i < 10; i++) {
                String msg = config.getString("log." + (((cLog + i) % 10) + 1), "");
                if (msg != null && !msg.trim().isEmpty()) {
                    log.add(msg);
                }
            }
        } else {
            log.addAll(config.getStringList("log"));
        }
        List<String> convertedList = new ArrayList<>();
        long t = System.currentTimeMillis();
        for (String logEntry : log) {
            String[] split = logEntry.split(";");
            if (split.length >= 2) {
                long then = Long.parseLong(split[0]);
                String msg = split[1];
                Object[] args = new Object[split.length-2];
                System.arraycopy(split, 2, args, 0, args.length);
                convertedList.add(tr("\u00a79{1} \u00a77- {0}", TimeUtil.millisAsString(t-then), tr(msg, args)));
            } else {
                Matcher m = OLD_LOG_PATTERN.matcher(logEntry);
                if (m.matches()) {
                    String date = m.group("date");
                    Date parsedDate = null;
                    try {
                        parsedDate = DateFormat.getDateInstance(3).parse(date);
                    } catch (ParseException e) {
                        // Ignore
                    }
                    String msg = m.group("msg");
                    if (parsedDate != null) {
                        convertedList.add(tr("\u00a79{1} \u00a77- {0}", TimeUtil.millisAsString(t-parsedDate.getTime()), msg));
                    } else {
                        convertedList.add(logEntry);
                    }
                } else {
                    convertedList.add(logEntry);
                }
            }
        }
        return convertedList;
    }

    public boolean isParty() {
        return getMembers().size() > 1;
    }

    public Location getWarpLocation() {
        if (hasWarp()) {
            return new Location(uSkyBlock.getInstance().getSkyBlockWorld(),
                    config.getInt("general.warpLocationX", 0),
                    config.getInt("general.warpLocationY", 0),
                    config.getInt("general.warpLocationZ", 0),
                    (float) config.getDouble("general.warpYaw", 0),
                    (float) config.getDouble("general.warpPitch", 0));
        }
        return null;
    }

    public Location getIslandLocation() {
        World world = uSkyBlock.getInstance().getWorld();
        String[] cords = name.split(",");
        return new Location(world, Long.parseLong(cords[0], 10), Settings.island_height, Long.parseLong(cords[1], 10));
    }

    @Override
    public String toString() {
        String str = "\u00a7bIsland Info:\n";
        str += ChatColor.GRAY + "  - level: " + ChatColor.DARK_AQUA + String.format("%5.2f", getLevel()) + "\n";
        str += ChatColor.GRAY + "  - location: " + ChatColor.DARK_AQUA + name + "\n";
        str += ChatColor.GRAY + "  - biome: " + ChatColor.DARK_AQUA + getBiome() + "\n";
        str += ChatColor.GRAY + "  - warp: " + ChatColor.DARK_AQUA + hasWarp() + "\n";
        if (hasWarp()) {
            str += ChatColor.GRAY + "     loc: " + ChatColor.DARK_AQUA + LocationUtil.asString(getWarpLocation()) + "\n";
        }
        str += ChatColor.GRAY + "  - locked: " + ChatColor.DARK_AQUA + isLocked() + "\n";
        str += ChatColor.GRAY + "  - ignore: " + ChatColor.DARK_AQUA + ignore() + "\n";
        str += ChatColor.DARK_AQUA + "Party:\n";
        str += ChatColor.GRAY + "  - leader: " + ChatColor.DARK_AQUA + getLeader() + "\n";
        str += ChatColor.GRAY + "  - members: " + ChatColor.DARK_AQUA + getMembers() + "\n";
        str += ChatColor.GRAY + "  - size: " + ChatColor.DARK_AQUA + getPartySize() + "\n";
        str += ChatColor.DARK_AQUA + "Limits:\n";
        str += ChatColor.GRAY + "  - maxParty: " + ChatColor.DARK_AQUA + getMaxPartySize() + "\n";
        str += ChatColor.GRAY + "  - animals: " + ChatColor.DARK_AQUA + getMaxAnimals() + "\n";
        str += ChatColor.GRAY + "  - monsters: " + ChatColor.DARK_AQUA + getMaxMonsters() + "\n";
        str += ChatColor.GRAY + "  - villagers: " + ChatColor.DARK_AQUA + getMaxVillagers() + "\n";
        str += ChatColor.DARK_AQUA + "Bans:\n";
        for (String ban : getBans()) {
            str += ChatColor.GRAY + "  - " + ban + "\n";
        }
        str += ChatColor.DARK_AQUA + "Log:\n";
        for (String log : getLog()) {
            str += ChatColor.GRAY + "  - " + log + "\n";
        }
        return str;
    }

    public void renamePlayer(Player player, String oldName) {
        ConfigurationSection members = config.getConfigurationSection("party.members");
        ConfigurationSection section = config.getConfigurationSection("party.members." + oldName);
        String newName = player.getName();
        boolean dirty = false;
        if (section != null) {
            String uuid = section.getString("uuid", null);
            if (uuid == null || uuid.equals(UUIDUtil.asString(player.getUniqueId()))) {
                section = members.createSection(newName, section.getValues(true));
                section.set("uuid", UUIDUtil.asString(player.getUniqueId()));
                members.set(oldName, null); // remove existing section
                dirty = true;
            } else {
                throw new IllegalStateException("Member " + oldName + " has a different UUID than " + player);
            }
        }
        if (isLeader(oldName)) {
            String uuid = config.getString("party.leader-uuid", null);
            if (uuid == null || uuid.equals(UUIDUtil.asString(player.getUniqueId()))) {
                config.set("party.leader", newName);
                config.set("party.leader-uuid", UUIDUtil.asString(player.getUniqueId()));
                dirty = true;
            } else {
                throw new IllegalStateException("Leader " + oldName + " has a different UUID than " + player);
            }
        }
        List<String> bans = getBans();
        if (bans.contains(oldName)) {
            bans.remove(oldName);
            bans.add(newName);
            config.set("banned.list", bans);
            dirty = true;
        }
        List<String> trustees = getTrustees();
        if (trustees.contains(oldName)) {
            trustees.remove(oldName);
            trustees.add(newName);
            config.set("trust.list", trustees);
            dirty = true;
        }
        if (dirty) {
            WorldGuardHandler.updateRegion(player, this);
            save();
        }
    }

    public boolean hasOnlineMembers() {
        ConfigurationSection members = config.getConfigurationSection("party.members");
        if (members != null) {
            for (String memberName : members.getKeys(false)) {
                String uuid = members.getString(memberName + ".uuid", null);
                if (uuid != null) {
                    Player onlinePlayer = Bukkit.getPlayer(UUIDUtil.fromString(uuid));
                    if (onlinePlayer != null) {
                        return true;
                    }
                } else {
                    Player onlinePlayer = Bukkit.getPlayer(memberName);
                    if (onlinePlayer != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<Player> getOnlineMembers() {
        ConfigurationSection members = config.getConfigurationSection("party.members");
        List<Player> players = new ArrayList<>();
        if (members != null) {
            for (String memberName : members.getKeys(false)) {
                String uuid = members.getString(memberName + ".uuid", null);
                if (uuid != null) {
                    Player onlinePlayer = Bukkit.getPlayer(UUIDUtil.fromString(uuid));
                    if (onlinePlayer != null) {
                        players.add(onlinePlayer);
                    }
                } else {
                    Player onlinePlayer = Bukkit.getPlayer(memberName);
                    if (onlinePlayer != null) {
                        players.add(onlinePlayer);
                    }
                }
            }
        }
        return players;
    }
    public boolean contains(Location loc) {
        return name.equalsIgnoreCase(WorldGuardHandler.getIslandNameAt(loc));
    }

    public void sendMessageToOnlineMembers(String msg) {
        String message = tr("\u00a7cSKY \u00a7f> \u00a77 {0}", msg);
        for (Player player : getOnlineMembers()) {
            player.sendMessage(message);
        }
    }

    public void delete() {
        toBeDeleted = true;
    }

    public boolean ignore() {
        return config.getBoolean("general.ignore", false);
    }

    public void setIgnore(boolean b) {
        config.set("general.ignore", b);
        dirty = true;
    }

    public int getLeafBreaks() {
        return config.getInt("blocks.leafBreaks", 0);
    }

    public void setLeafBreaks(int breaks) {
        config.set("blocks.leafBreaks", breaks);
        dirty = true;
    }
}
