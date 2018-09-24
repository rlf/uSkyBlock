package us.talabrek.ultimateskyblock.island;

import dk.lockfuglsang.minecraft.util.TimeUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.player.Perk;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.IslandUtil;
import us.talabrek.ultimateskyblock.util.LocationUtil;
import us.talabrek.ultimateskyblock.util.LogUtil;
import us.talabrek.ultimateskyblock.util.UUIDUtil;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dk.lockfuglsang.minecraft.file.FileUtil.readConfig;
import static dk.lockfuglsang.minecraft.perm.PermissionUtil.hasPermission;
import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Data object for an island
 */
public class IslandInfo implements us.talabrek.ultimateskyblock.api.IslandInfo {
    private static final Logger log = Logger.getLogger(IslandInfo.class.getName());
    private static final Pattern OLD_LOG_PATTERN = Pattern.compile("\u00a7d\\[(?<date>[^\\]]+)\\]\u00a77 (?<msg>.*)");
    private static final int YML_VERSION = 3;
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
                    ConfigurationSection leaderSection = config.getConfigurationSection("party.members." + UUIDUtil.asString(getLeaderUniqueId()));
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
            for (String banned : config.getStringList("banned.list")) {
                banPlayerInfo(uSkyBlock.getInstance().getPlayerDB().getUUIDFromName(banned));
            }
            config.set("version", 1);
        }
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
        config.set("general.scoreMultiply", null);
        config.set("general.scoreOffset", null);
        config.set("blocks.hopperCount", 0);
        setupPartyLeader(leader);
        sendMessageToIslandGroup(false, marktr("The island has been created."));
    }

    public void setupPartyLeader(final String leader) {
        UUID uuid = uSkyBlock.getInstance().getPlayerDB().getUUIDFromName(leader);
        String uuidString = UUIDUtil.asString(uuid);
        config.set("party.leader", leader);
        config.set("party.leader-uuid", uuidString);
        ConfigurationSection section = config.createSection("party.members." + uuidString);
        section.set("canChangeBiome", true);
        section.set("canToggleLock", true);
        section.set("canChangeWarp", true);
        section.set("canToggleWarp", true);
        section.set("canInviteOthers", true);
        section.set("canKickOthers", true);
        section.set("canBanOthers", true);
        config.set("party.currentSize", getMembers().size());

        Player onlinePlayer = uSkyBlock.getInstance().getPlayerDB().getPlayer(uuid);
        // The only time the onlinePlayer will be null is if it is being converted from another skyblock plugin.
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            updatePermissionPerks(onlinePlayer, uSkyBlock.getInstance().getPerkLogic().getPerk(onlinePlayer));
        }
        save();
    }

    public void addMember(final PlayerInfo playerInfo) {
        playerInfo.setJoinParty(getIslandLocation());
        setupPartyMember(playerInfo);
        uSkyBlock.getInstance().fireMemberJoinedEvent(this, playerInfo);
    }

    public void setupPartyMember(final PlayerInfo member) {
        if (!getMemberUUIDs().contains(member.getUniqueId())) {
            config.set("party.currentSize", config.getInt("party.currentSize") + 1);
        }
        ConfigurationSection section = config.createSection("party.members." + UUIDUtil.asString(member.getUniqueId()));
        section.set("name", member.getPlayerName());
        section.set("canChangeBiome", false);
        section.set("canToggleLock", false);
        section.set("canChangeWarp", false);
        section.set("canToggleWarp", false);
        section.set("canInviteOthers", false);
        section.set("canKickOthers", false);
        section.set("canBanOthers", false);

        Player onlinePlayer = member.getPlayer();
        // The only time the onlinePlayer will be null is if it is being converted from another skyblock plugin.
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            updatePermissionPerks(onlinePlayer, uSkyBlock.getInstance().getPerkLogic().getPerk(onlinePlayer));
        }
        WorldGuardHandler.updateRegion(this);
        save();
    }

    public void updatePermissionPerks(final Player member, Perk perk) {
        boolean updateRegion = false;
        if (isLeader(member)) {
            String oldLeaderName = getLeader();
            config.set("party.leader", member.getName());
            updateRegion |= !oldLeaderName.equals(member.getName());
        }
        ConfigurationSection section = config.getConfigurationSection("party.members." + member.getUniqueId());
        boolean dirty = false;
        if (section != null) {
            section.set("name", member.getName());
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
            if (section.isConfigurationSection("maxBlocks")) {
                dirty = true;
                Map<Material, Integer> blockLimits = perk.getBlockLimits();
                section.set("blockLimits ", null);
                if (!blockLimits.isEmpty()) {
                    ConfigurationSection maxBlocks = section.createSection("blockLimits");
                    for (Map.Entry<Material,Integer> limit : blockLimits.entrySet()) {
                        maxBlocks.set(limit.getKey().name(), limit.getValue());
                    }
                }
            }
        }
        if (dirty) {
            save();
        }
        if (updateRegion) {
            WorldGuardHandler.updateRegion(this);
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
        } else if (dirty) {
            try {
                log.fine("Saving island-config: " + file);
                config.save(file);
            } catch (IOException e) {
                LogUtil.log(Level.SEVERE, "Unable to save island " + file, e);
            }
        }
    }

    @Override
    public int getMaxPartySize() {
        return getMaxPartyIntValue("maxPartySizePermission", uSkyBlock.getInstance().getPerkLogic().getIslandPerk(getSchematicName()).getPerk().getMaxPartySize());
    }

    @Override
    public int getMaxAnimals() {
        return getMaxPartyIntValue("maxAnimals", uSkyBlock.getInstance().getPerkLogic().getIslandPerk(getSchematicName()).getPerk().getAnimals());
    }

    @Override
    public int getMaxMonsters() {
        return getMaxPartyIntValue("maxMonsters", uSkyBlock.getInstance().getPerkLogic().getIslandPerk(getSchematicName()).getPerk().getMonsters());
    }

    @Override
    public int getMaxVillagers() {
        return getMaxPartyIntValue("maxVillagers", uSkyBlock.getInstance().getPerkLogic().getIslandPerk(getSchematicName()).getPerk().getVillagers());
    }

    @Override
    public int getMaxGolems() {
        return getMaxPartyIntValue("maxGolems", uSkyBlock.getInstance().getPerkLogic().getIslandPerk(getSchematicName()).getPerk().getGolems());
    }

    @Override
    public Map<Material, Integer> getBlockLimits() {
        Map<Material, Integer> blockLimitMap = new HashMap<>();
        ConfigurationSection membersSection = config.getConfigurationSection("party.members");
        if (membersSection != null) {
            for (String memberName : membersSection.getKeys(false)) {
                ConfigurationSection memberSection = membersSection.getConfigurationSection(memberName);
                if (memberSection != null) {
                    if (memberSection.isConfigurationSection("blockLimits")) {
                        ConfigurationSection blockLimits = memberSection.getConfigurationSection("blockLimits");
                        for (String material : blockLimits.getKeys(false)) {
                            Material type = Material.matchMaterial(material);
                            if (type != null) {
                                blockLimitMap.computeIfAbsent(type, (k) -> {
                                    int memberMax = blockLimits.getInt(material, 0);
                                    return blockLimitMap.compute(type, (key, oldValue) ->
                                            oldValue != null && memberMax > 0 ? Math.max(memberMax, oldValue) : null);
                                });
                            }
                        }
                    }
                }
            }
        }
        return blockLimitMap;
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

    @Override
    public String getLeader() {
        return config.getString("party.leader", "");
    }

    public UUID getLeaderUniqueId() {
        String uuidStr = config.getString("party.leader-uuid", null);
        if (uuidStr == null) {
            UUID uuid = uSkyBlock.getInstance().getPlayerDB().getUUIDFromName(getLeader());
            if (uuid != null) {
                uuidStr = uuid.toString();
                config.set("party.leader-uuid", uuidStr);
                dirty = true;
            }
        }
        return UUIDUtil.fromString(uuidStr);
    }

    public boolean hasPerm(Player player, String perm) {
        return hasPerm(player.getUniqueId(), perm);
    }
    @Deprecated
    public boolean hasPerm(String playerName, String perm) {
        return hasPerm(uSkyBlock.getInstance().getPlayerDB().getUUIDFromName(playerName), perm);
    }

    public boolean hasPerm(UUID uuid, String perm) {
        return uuid.equals(getLeaderUniqueId()) || config.getBoolean("party.members." + UUIDUtil.asString(uuid) + "." + perm);
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

    public boolean togglePerm(final String playername, final String perm) {
        String uuidString = UUIDUtil.asString(uSkyBlock.getInstance().getPlayerDB().getUUIDFromName(playername));
        ConfigurationSection memberSection = config.getConfigurationSection("party.members." + uuidString);
        try {
            if (memberSection == null) {
                log.info("Perms for " + playername + " failed to toggle because player is not a part of that island!");
                return false;
            }
            if (memberSection.getBoolean(perm, false)) {
                memberSection.set(perm, false);
            } else {
                memberSection.set(perm, true);
            }
            save();
            return true;
        } catch (NullPointerException e) {
            log.info("Perms for " + playername + " failed to toggle because player is not a part of that island!");
            return false;
        }
    }

    @Override
    public Set<String> getMembers() {
        ConfigurationSection memberSection = config.getConfigurationSection("party.members");
        Set<String> members = new LinkedHashSet<>();
        if (memberSection != null) {
            for (String uuid : memberSection.getKeys(false)) {
                UUID id = UUIDUtil.fromString(uuid);
                if (id != null) {
                    String nm = uSkyBlock.getInstance().getPlayerDB().getName(id);
                    if (nm != null) {
                        members.add(nm);
                    } else {
                        log.info("Island " + name + " has unknown member-section " + uuid);
                        // Remove broken UUID from island file
                        config.set("party.members." + uuid, null);
                        config.set("party.currentSize", getPartySize() - 1);
                        save();
                    }
                } else {
                    log.info("Island " + name + " has invalid member-section " + uuid);
                }
            }
        }
        return members;
    }

    public Set<UUID> getMemberUUIDs() {
        ConfigurationSection memberSection = config.getConfigurationSection("party.members");
        Set<UUID> members = new HashSet<>();
        if (memberSection != null) {
            for (String uuid : memberSection.getKeys(false)) {
                try {
                    UUID id = UUIDUtil.fromString(uuid);
                    if (id == null) {
                        throw new IllegalArgumentException();
                    }
                    members.add(id);
                } catch (IllegalArgumentException e) {
                    log.info("Island " + name + " has invalid member-section " + uuid);
                }
            }
        }
        return members;
    }

    @Override
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

    @Override
    public int getPartySize() {
        return config.getInt("party.currentSize", 1);
    }

    @Override
    public boolean isLeader(Player player) {
        return player.getUniqueId().equals(getLeaderUniqueId());
    }

    public boolean isLeader(String playerName) {
        return getLeaderUniqueId().equals(uSkyBlock.getInstance().getPlayerDB().getUUIDFromName(playerName));
    }

    public boolean hasWarp() {
        return config.getBoolean("general.warpActive");
    }

    public boolean isLocked() {
        return config.getBoolean("general.locked");
    }

    @Override
    public String getName() {
        return name;
    }

    public void setWarp(boolean active) {
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
            for (UUID uuid : getMemberUUIDs()) {
                Player player = uSkyBlock.getInstance().getPlayerDB().getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    player.sendMessage(tr("\u00a7cSKY \u00a7f> \u00a77 {0}", tr(message, args)));
                }
            }
        }
        log(message, args);
    }

    @Override
    public boolean isBanned(Player player) {
        return isBanned(player.getUniqueId()) && !hasPermission(player, "usb.mod.bypassprotection");
    }

    public boolean isBanned(String name) {
        return isBanned(uSkyBlock.getInstance().getPlayerDB().getUUIDFromName(name));
    }

    public boolean isBanned(UUID uuid) {
        return config.getStringList("banned.list").contains(UUIDUtil.asString(uuid));
    }

    public void banPlayer(UUID uuid) {
        String uuidString = UUIDUtil.asString(uuid);
        List<String> stringList = config.getStringList("banned.list");
        if (!stringList.contains(uuidString)) {
            stringList.add(uuidString);
        }
        config.set("banned.list", stringList);
        save();
        banPlayerInfo(uuid);
    }

    private void banPlayerInfo(UUID uuid) {
        PlayerInfo playerInfo = uSkyBlock.getInstance().getPlayerLogic().getPlayerInfo(uuid);
        if (playerInfo != null) {
            playerInfo.banFromIsland(name);
        }
    }

    public void unbanPlayer(UUID uuid) {
        String uuidString = UUIDUtil.asString(uuid);
        List<String> stringList = config.getStringList("banned.list");
        while (stringList.contains(uuidString)) {
            stringList.remove(uuidString);
        }
        config.set("banned.list", stringList);
        save();
        unbanPlayerInfo(uuid);
    }

    private void unbanPlayerInfo(UUID uuid) {
        PlayerInfo playerInfo = uSkyBlock.getInstance().getPlayerLogic().getPlayerInfo(uuid);
        if (playerInfo != null) {
            playerInfo.unbanFromIsland(name);
        }
    }

    @Override
    public List<String> getBans() {
        List<String> uuidList = config.getStringList("banned.list");
        List<String> nameList = new ArrayList<>();
        for (String uuid : uuidList) {
            UUID id = UUIDUtil.fromString(uuid);
            if (id != null) {
                String name = uSkyBlock.getInstance().getPlayerDB().getName(id);
                nameList.add(name);
            } else {
                log.info("Island " + name + " has invalid ban-value " + uuid);
            }
        }
        return nameList;
    }

    @Deprecated
    @Override
    public List<String> getTrustees() {
        List<String> uuidList = config.getStringList("trust.list");
        List<String> nameList = new ArrayList<>();
        for (String uuid : uuidList) {
            UUID id = UUIDUtil.fromString(uuid);
            if (id != null) {
                nameList.add(uSkyBlock.getInstance().getPlayerDB().getName(id));
            } else {
                log.info("Island " + name + " has invalid trustee-value " + uuid);
            }
        }
        return nameList;
    }

    public List<UUID> getTrusteeUUIDs() {
        List<String> list = config.getStringList("trust.list");
        List<UUID> uuidList = new ArrayList<>();
        for (String uuid : list) {
            UUID id = UUIDUtil.fromString(uuid);
            if (id != null) {
                uuidList.add(id);
            } else {
                log.info("Island " + name + " has invalid trustee-value " + uuid);
            }
        }
        return uuidList;
    }

    public void trust(UUID uuid) {
        String uuidString = UUIDUtil.asString(uuid);
        List<String> trustees = config.getStringList("trust.list");
        if (!trustees.contains(uuidString)) {
            trustees.add(uuidString);
            config.set("trust.list", trustees);
        }
        PlayerInfo playerInfo = uSkyBlock.getInstance().getPlayerInfo(uuid);
        if (playerInfo != null) {
            playerInfo.addTrust(this.name);
        }
        save();
    }

    public void untrust(UUID uuid) {
        String uuidString = UUIDUtil.asString(uuid);
        List<String> trustees = config.getStringList("trust.list");
        trustees.remove(uuidString);
        config.set("trust.list", trustees);
        PlayerInfo playerInfo = uSkyBlock.getInstance().getPlayerInfo(uuid);
        if (playerInfo != null) {
            playerInfo.removeTrust(this.name);
        }
        save();
    }

    public void removeMember(PlayerInfo member) {
        member.setHomeLocation(null);
        member.removeFromIsland();
        member.save();
        config.set("party.members." + UUIDUtil.asString(member.getUniqueId()), null);
        config.set("party.currentSize", getPartySize() - 1);

        sendMessageToIslandGroup(true, marktr("\u00a7b{0}\u00a7d has been removed from the island group."), member.getPlayerName());
        WorldGuardHandler.updateRegion(this);
        uSkyBlock.getInstance().fireMemberLeftEvent(this, member);
        save();
    }

    public void setLevel(double score) {
        config.set("general.level", score);
        save();
    }

    @Override
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

    @Override
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
                Object[] args = new Object[split.length - 2];
                System.arraycopy(split, 2, args, 0, args.length);
                convertedList.add(tr("\u00a79{1} \u00a77- {0}", TimeUtil.millisAsString(t - then), tr(msg, args)));
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
                        convertedList.add(tr("\u00a79{1} \u00a77- {0}", TimeUtil.millisAsString(t - parsedDate.getTime()), msg));
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

    @Override
    public boolean isParty() {
        return getMembers().size() > 1;
    }

    @Override
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

    @Override
    public Location getIslandLocation() {
        return IslandUtil.getIslandLocation(name);
    }

    @Override
    public String toString() {
        String str = "\u00a7bIsland Info:\n";
        str += ChatColor.GRAY + "  - level: " + ChatColor.DARK_AQUA + String.format("%5.2f", getLevel()) + "\n";
        str += ChatColor.GRAY + "  - location: " + ChatColor.DARK_AQUA + name + "\n";
        str += ChatColor.GRAY + "  - biome: " + ChatColor.DARK_AQUA + getBiome() + "\n";
        str += ChatColor.GRAY + "  - schematic: " + ChatColor.DARK_AQUA + getSchematicName() + "\n";
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

    @Override
    public boolean hasOnlineMembers() {
        ConfigurationSection members = config.getConfigurationSection("party.members");
        if (members != null) {
            for (String uuid : members.getKeys(false)) {
                if (uuid != null) {
                    UUID id = UUIDUtil.fromString(uuid);
                    if (id != null) {
                        Player onlinePlayer = uSkyBlock.getInstance().getPlayerDB().getPlayer(id);
                        if (onlinePlayer != null) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<Player> getOnlineMembers() {
        ConfigurationSection members = config.getConfigurationSection("party.members");
        List<Player> players = new ArrayList<>();
        if (members != null) {
            for (String uuid : members.getKeys(false)) {
                if (uuid != null) {
                    UUID id = UUIDUtil.fromString(uuid);
                    if (id != null) {
                        Player onlinePlayer = uSkyBlock.getInstance().getPlayerDB().getPlayer(id);
                        if (onlinePlayer != null) {
                            players.add(onlinePlayer);
                        }
                    } else {
                        log.info("Island " + name + " has invalid member-section: " + uuid);
                    }
                } else {
                    log.info("Island " + name + " has invalid member-section: " + uuid);
                }
            }
        }
        return players;
    }

    @Override
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

    @Override
    public String getSchematicName() {
        return config.getString("general.schematicName", Settings.island_schematicName);
    }

    public void setSchematicName(String schematicName) {
        config.set("general.schematicName", schematicName);
        dirty = true;
    }

    @Override
    public double getScoreMultiplier() {
        return config.getDouble("general.scoreMultiply", 1d);
    }

    public void setScoreMultiplier(Double d) {
        config.set("general.scoreMultiply", d);
        save();
    }

    @Override
    public double getScoreOffset() {
        return config.getDouble("general.scoreOffset", 0d);
    }

    public void setScoreOffset(Double d) {
        config.set("general.scoreOffset", d);
        save();
    }

    public int getHopperCount() {
        return config.getInt("blocks.hopperCount", 0);
    }

    public void setHopperCount(int i) {
        config.set("blocks.hopperCount", i);
        save();
    }
}
