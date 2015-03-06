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
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;
import us.talabrek.ultimateskyblock.util.UUIDUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import static us.talabrek.ultimateskyblock.util.FileUtil.readConfig;
import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * Data object for an island
 */
public class IslandInfo {
    private static final int YML_VERSION = 1;
    private static File directory = new File(".");

    private final File file;
    private final FileConfiguration config;
    private final String name;

    public IslandInfo(String islandName) {
        config = new YamlConfiguration();
        file = new File(directory, islandName + ".yml");
        name = islandName;
        if (file.exists()) {
            readConfig(config, file);
        }
        if (config.getInt("version", 0) < YML_VERSION) {
            updateConfig();
        }
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

    public void clearIslandConfig(final String leader) {
        config.set("general.level", 0);
        config.set("general.warpLocationX", 0);
        config.set("general.warpLocationY", 0);
        config.set("general.warpLocationZ", 0);
        config.set("general.warpActive", false);
        config.set("log.logPos", 1);
        config.set("version", YML_VERSION);
        setupPartyLeader(leader);
        sendMessageToIslandGroup("The island has been created.");
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
        config.set("party.currentSize", getMembers().size());
        save();
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
        WorldGuardHandler.addPlayerToOldRegion(name, member);
        save();
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            uSkyBlock.log(Level.SEVERE, "Unable to save island " + file, e);
        }
    }

    public void reload() {
        final InputStream defConfigStream = getClass().getClassLoader().getResourceAsStream("island.yml");
        if (defConfigStream != null) {
            try {
                config.load(new InputStreamReader(defConfigStream, "UTF-8"));
            } catch (IOException | InvalidConfigurationException e) {
                uSkyBlock.log(Level.SEVERE, "Unable to read island-defaults", e);
            }
        }
        save();
    }

    public void updatePartyNumber(final Player player) {
        if (config.getInt("party.maxSize") < 8 && VaultHandler.checkPerk(player.getName(), "usb.extra.partysize", player.getWorld())) {
            config.set("party.maxSize", 8);
        } else if (config.getInt("party.maxSize") < 7 && VaultHandler.checkPerk(player.getName(), "usb.extra.party3", player.getWorld())) {
            config.set("party.maxSize", 7);
        } else if (config.getInt("party.maxSize") < 6 && VaultHandler.checkPerk(player.getName(), "usb.extra.party2", player.getWorld())) {
            config.set("party.maxSize", 6);
        } else if (config.getInt("party.maxSize") < 5 && VaultHandler.checkPerk(player.getName(), "usb.extra.party1", player.getWorld())) {
            config.set("party.maxSize", 5);
        }
        save();
    }

    public String getLeader() {
        return config.getString("party.leader", "");
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

    public void log(String message) {
        int currentLogPos = config.getInt("log.logPos");
        config.set("log." + (++currentLogPos), message);
        if (currentLogPos <= 10) {
            config.set("log.logPos", currentLogPos);
        } else {
            // Wrap around
            config.set("log.logPos", 1);
        }
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
        sendMessageToIslandGroup("\u00a7b"+player.getName() + "\u00a7d locked the island.");
        if (hasWarp()) {
            config.set("general.warpActive", false);
            player.sendMessage(tr("\u00a74Since your island is locked, your incoming warp has been deactivated."));
            sendMessageToIslandGroup(tr("\u00a7b{0}\u00a7d deactivated the island warp.", player.getName()));
        }
        save();
    }

    public void unlock(Player player) {
        WorldGuardHandler.islandUnlock(player, name);
        config.set("general.locked", false);
        sendMessageToIslandGroup("\u00a7b"+player.getName() + "\u00a7d unlocked the island.");
        save();
    }

    public void sendMessageToIslandGroup(String message) {
        Date date = new Date();
        final String dateTxt = DateFormat.getDateInstance(3).format(date).toString();
        for (String player : getMembers()) {
            if (Bukkit.getPlayer(player) != null) {
                Bukkit.getPlayer(player).sendMessage("\u00a7d[skyblock] " + message);
            }
        }
        log("\u00a7d[" + dateTxt + "] " + message);
    }

    public int getMaxPartySize() {
        return config.getInt("party.maxSize", 4);
    }

    public boolean isBanned(Player player) {
        return isBanned(player.getName());
    }

    // TODO: 19/12/2014 - R4zorax: UUID
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

    public void removeMember(PlayerInfo member) {
        WorldGuardHandler.removePlayerFromRegion(name, member.getPlayerName());
        member.setHomeLocation(null);
        member.setLeaveParty();
        member.save();
        removeMember(member.getPlayerName());
    }

    public void removeMember(String playername) {
        config.set("party.members." + playername, null);
        config.set("party.currentSize", getPartySize() - 1);
        save();
        sendMessageToIslandGroup("\u00a7b" +playername + "\u00a7d has been removed from the island group.");
    }

    public void setLevel(double score) {
        config.set("general.level", score);
        save();
    }

    public double getLevel() {
        return getMembers().isEmpty() ? 0 : config.getDouble("general.level");
    }

    public void setRegionVersion(int version) {
        config.set("general.regionVersion", version);
        save();
    }

    public int getRegionVersion() {
        return config.getInt("general.regionVersion", 0);
    }

    public List<String> getLog() {
        List<String> log = new ArrayList<>();
        int cLog = config.getInt("log.logPos", 1);
        for (int i = 0; i < 10; i++) {
            String msg = config.getString("log." + (((cLog+i) % 10)+1), "");
            if (msg != null && !msg.trim().isEmpty()) {
                log.add(msg);
            }
        }
        return log;
    }

    public boolean isParty() {
        return getMembers().size() > 1;
    }

    public void setMaxPartySize(int size) {
        config.set("party.maxSize", size);
        save();
    }

    public Location getWarpLocation() {
        if (hasWarp()) {
            return new Location(uSkyBlock.getInstance().getSkyBlockWorld(),
                    config.getInt("general.warpLocationX", 0),
                    config.getInt("general.warpLocationY", 0),
                    config.getInt("general.warpLocationZ", 0));
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
        str += ChatColor.GRAY + "  - level: " + ChatColor.DARK_AQUA + String.format("%5.2f", getLevel()) +"\n";
        str += ChatColor.GRAY + "  - location: " + ChatColor.DARK_AQUA +  name + "\n";
        str += ChatColor.GRAY + "  - warp: " + ChatColor.DARK_AQUA +  hasWarp() + "\n";
        if (hasWarp()) {
            str += ChatColor.GRAY + "     loc: " + ChatColor.DARK_AQUA + LocationUtil.asString(getWarpLocation()) + "\n";
        }
        str += ChatColor.GRAY + "  - locked: " + ChatColor.DARK_AQUA +  isLocked() + "\n";
        str += ChatColor.DARK_AQUA + "Party:\n";
        str += ChatColor.GRAY + "  - leader: " + ChatColor.DARK_AQUA + getLeader() + "\n";
        str += ChatColor.GRAY + "  - members: " + ChatColor.DARK_AQUA + getMembers() + "\n";
        str += ChatColor.GRAY + "  - size: " + ChatColor.DARK_AQUA + getPartySize() + "\n";
        str += ChatColor.GRAY + "  - max: " + ChatColor.DARK_AQUA + getMaxPartySize() + "\n";
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
            if (uuid == null || uuid.equals(player.getUniqueId())) {
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
            if (uuid == null || uuid.equals(player.getUniqueId())) {
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
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUIDUtil.fromString(uuid));
                    if (offlinePlayer != null && offlinePlayer.isOnline()) {
                        return true;
                    }
                } else {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberName);
                    if (offlinePlayer != null && offlinePlayer.isOnline()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean contains(Location loc) {
        return name.equalsIgnoreCase(WorldGuardHandler.getIslandNameAt(loc));
    }
}
