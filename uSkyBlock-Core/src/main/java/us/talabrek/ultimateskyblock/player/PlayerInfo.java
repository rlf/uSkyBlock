package us.talabrek.ultimateskyblock.player;

import com.google.common.base.Preconditions;
import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.challenge.ChallengeCompletion;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;
import us.talabrek.ultimateskyblock.util.LogUtil;
import us.talabrek.ultimateskyblock.util.UUIDUtil;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerInfo implements Serializable, us.talabrek.ultimateskyblock.api.PlayerInfo {
    private static final String CN = PlayerInfo.class.getName();
    private static final Logger log = Logger.getLogger(CN);
    private static final long serialVersionUID = 1L;
    private static final int YML_VERSION = 1;
    private String playerName;
    private String displayName;
    private UUID uuid;

    private Location islandLocation;

    private Location homeLocation;

    private FileConfiguration playerData;
    private File playerConfigFile;

    private boolean islandGenerating = false;
    private boolean dirty = false;

    public PlayerInfo(String currentPlayerName, UUID playerUUID) {
        this.uuid = playerUUID;
        this.playerName = currentPlayerName;
        this.playerConfigFile = new File(uSkyBlock.getInstance().directoryPlayers, this.playerName + ".yml");
        if (this.playerConfigFile.exists() || !uSkyBlock.getInstance().getPlayerNameChangeManager().hasNameChanged(playerUUID, currentPlayerName)) {
            loadPlayer();
        }
    }
    
    public void startNewIsland(final Location l) {
        this.setIslandLocation(l);
        this.homeLocation = null;
    }

    public void removeFromIsland() {
        this.setIslandLocation(null);
        this.homeLocation = null;
        islandGenerating = false;
    }

    @Override
    public boolean getHasIsland() {
        return getIslandLocation() != null;
    }

    public String locationForParty() {
        return LocationUtil.getIslandName(this.islandLocation);
    }

    @Override
    public Player getPlayer() {
        if (uuid != null) {
            return Bukkit.getPlayer(uuid);
        }
        return Bukkit.getPlayer(this.playerName);
    }

    @Override
    public String getPlayerName() {
        return this.playerName;
    }

    public void setIslandLocation(final Location l) {
        this.islandLocation = l != null ? l.clone() : null;
    }

    @Override
    public Location getIslandLocation() {
        return islandLocation != null && islandLocation.getBlockY() != 0 ? islandLocation.clone() : null;
    }

    @Override
    public Location getIslandNetherLocation() {
        Location l = getIslandLocation();
        World nether = uSkyBlock.getInstance().getSkyBlockNetherWorld();
        if (nether == null) {
            return null;
        }
        if (l != null) {
            l.setWorld(nether);
            l.setY(l.getY()/2);
        }
        return l;
    }

    public void setHomeLocation(final Location l) {
        this.homeLocation = l != null ? l.clone() : null;
    }

    @Override
    public Location getHomeLocation() {
        return homeLocation != null ? homeLocation.clone() : null;
    }

    @Override
    public String getDisplayName() {
        return displayName != null ? displayName : playerName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setJoinParty(final Location l) {
        this.islandLocation = l != null ? l.clone() : null;
        // TODO: 09/09/2015 - R4zorax: Use the leaders home instead
        this.homeLocation = l != null ? l.clone() : null;
    }

    public void completeChallenge(final String challenge, boolean silent) {
        uSkyBlock.getInstance().getChallengeLogic().completeChallenge(this, challenge);
        if (silent) {
            return;
        }
        IslandInfo island = getIslandInfo();
        if (island != null) {
            island.sendMessageToOnlineMembers(I18nUtil.tr("\u00a79{0}\u00a7f has completed the \u00a79{1}\u00a7f challenge!", getPlayerName(), challenge));
        }
    }

    public void resetChallenge(final String challenge) {
        uSkyBlock.getInstance().getChallengeLogic().resetChallenge(this, challenge);
    }

    public int checkChallenge(final String challenge) {
        return uSkyBlock.getInstance().getChallengeLogic().checkChallenge(this, challenge);
    }

    public ChallengeCompletion getChallenge(final String challenge) {
        return uSkyBlock.getInstance().getChallengeLogic().getChallenge(this, challenge);
    }

    public void resetAllChallenges() {
        uSkyBlock.getInstance().getChallengeLogic().resetAllChallenges(this);
    }

    private void setupPlayer() {
        FileConfiguration playerConfig = getConfig();
        ConfigurationSection pSection = playerConfig.createSection("player");
        pSection.set("islandX", 0);
        pSection.set("islandY", 0);
        pSection.set("islandZ", 0);
        pSection.set("homeX", 0);
        pSection.set("homeY", 0);
        pSection.set("homeZ", 0);
        pSection.set("homeYaw", 0);
        pSection.set("homePitch", 0);
    }
    
    private PlayerInfo loadPlayer() {
        Player onlinePlayer = getPlayer();
        try {
            log.entering(CN, "loadPlayer:" + this.playerName);
            FileConfiguration playerConfig = getConfig();
            if (!playerConfig.contains("player.islandY") || playerConfig.getInt("player.islandY", 0) == 0) {
                this.islandLocation = null;
                this.homeLocation = null;
                createPlayerConfig();
                return this;
            }
            try {
                this.displayName = playerConfig.getString("player.displayName", playerName);
                this.uuid = UUIDUtil.fromString(playerConfig.getString("player.uuid", null));
                this.islandLocation = new Location(uSkyBlock.getSkyBlockWorld(),
                        playerConfig.getInt("player.islandX"), playerConfig.getInt("player.islandY"), playerConfig.getInt("player.islandZ"));
                this.homeLocation = new Location(uSkyBlock.getSkyBlockWorld(),
                        playerConfig.getInt("player.homeX") + 0.5, playerConfig.getInt("player.homeY") + 0.2, playerConfig.getInt("player.homeZ") + 0.5,
                        (float) playerConfig.getDouble("player.homeYaw", 0.0),
                        (float) playerConfig.getDouble("player.homePitch", 0.0));

                log.exiting(CN, "loadPlayer");
                return this;
            } catch (Exception e) {
                LogUtil.log(Level.INFO, "Returning null while loading, not good!");
                return null;
            }
        } finally {
            if (onlinePlayer != null && onlinePlayer.isOnline()) {
                updatePlayerInfo(onlinePlayer);
            }
        }
    }
    
    // TODO: 09/12/2014 - R4zorax: All this should be made UUID
    private void reloadPlayerConfig() {
        playerConfigFile = new File(uSkyBlock.getInstance().directoryPlayers, playerName + ".yml");
        playerData = YamlConfiguration.loadConfiguration(playerConfigFile);
    }

    private void createPlayerConfig() {
        LogUtil.log(Level.FINER, "Creating new player config!");
        setupPlayer();
    }

    public FileConfiguration getConfig() {
        if (playerData == null) {
            reloadPlayerConfig();
        }
        return playerData;
    }

    public void save() {
        dirty = true;
        if (!playerConfigFile.exists()) {
            saveToFile();
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void saveToFile() {
        log.fine("Saving player-info for " + playerName + " to file");
        // TODO: 11/05/2015 - R4zorax: Instead of saving directly, schedule it for later...
        log.entering(CN, "save", playerName);
        if (playerData == null) {
            LogUtil.log(Level.INFO, "Can't save player data!");
            return;
        }
        FileConfiguration playerConfig = playerData;
        playerConfig.set("version", YML_VERSION);
        playerConfig.set("player.hasIsland", null); // Remove it (deprecated)
        playerConfig.set("player.displayName", displayName);
        playerConfig.set("player.uuid", UUIDUtil.asString(uuid));
        Location location = this.getIslandLocation();
        if (location != null) {
            playerConfig.set("player.islandX", location.getBlockX());
            playerConfig.set("player.islandY", location.getBlockY());
            playerConfig.set("player.islandZ", location.getBlockZ());
        } else {
            playerConfig.set("player.islandX", 0);
            playerConfig.set("player.islandY", 0);
            playerConfig.set("player.islandZ", 0);
        }
        Location home = this.getHomeLocation();
        if (home != null) {
            playerConfig.set("player.homeX", home.getBlockX());
            playerConfig.set("player.homeY", home.getBlockY());
            playerConfig.set("player.homeZ", home.getBlockZ());
            playerConfig.set("player.homeYaw", home.getYaw());
            playerConfig.set("player.homePitch", home.getPitch());
        } else {
            playerConfig.set("player.homeX", 0);
            playerConfig.set("player.homeY", 0);
            playerConfig.set("player.homeZ", 0);
            playerConfig.set("player.homeYaw", 0);
            playerConfig.set("player.homePitch", 0);
        }
        playerConfigFile = new File(uSkyBlock.getInstance().directoryPlayers, playerName + ".yml");
        try {
            playerConfig.save(playerConfigFile);
            LogUtil.log(Level.FINEST, "Player data saved!");
        } catch (IOException ex) {
            uSkyBlock.getInstance().getLogger().log(Level.SEVERE, "Could not save config to " + playerConfigFile, ex);
        }
        log.exiting(CN, "save");
        dirty = false;
    }

    @Override
    public Collection<us.talabrek.ultimateskyblock.api.ChallengeCompletion> getChallenges() {
        Collection<us.talabrek.ultimateskyblock.api.ChallengeCompletion> copy = new ArrayList<>();
        copy.addAll(uSkyBlock.getInstance().getChallengeLogic().getChallenges(this));
        return copy;
    }

    @Override
    public String toString() {
        // TODO: 01/06/2015 - R4zorax: use i18n.tr
        String str = "\u00a7bPlayer Info:\n";
        str += ChatColor.GRAY + "  - name: " + ChatColor.DARK_AQUA + getPlayerName() + "\n";
        str += ChatColor.GRAY + "  - nick: " + ChatColor.DARK_AQUA + getDisplayName() + "\n";
        str += ChatColor.GRAY + "  - hasIsland: " + ChatColor.DARK_AQUA +  getHasIsland() + "\n";
        str += ChatColor.GRAY + "  - home: " + ChatColor.DARK_AQUA +  LocationUtil.asString(getHomeLocation()) + "\n";
        str += ChatColor.GRAY + "  - island: " + ChatColor.DARK_AQUA + LocationUtil.asString(getIslandLocation()) + "\n";
        str += ChatColor.GRAY + "  - banned from: " + ChatColor.DARK_AQUA + getBannedFrom() + "\n";
        str += ChatColor.GRAY + "  - trusted on: " + ChatColor.DARK_AQUA + playerData.getStringList("trustedOn") + "\n";
        // TODO: 28/12/2014 - R4zorax: Some info about challenges?
        return str;
    }

    public void updatePlayerInfo(Player player) {
        if (!player.getDisplayName().equals(displayName) || !player.getUniqueId().equals(uuid)) {
            setDisplayName(player.getDisplayName());
            uuid = player.getUniqueId();
            save();
        }
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    public synchronized void renameFrom(String oldName) {
        Preconditions.checkState(!Bukkit.isPrimaryThread(), "This method cannot run in the main server thread!");

        // Delete the new file if for some reason it already exists.
        if (this.playerConfigFile.exists()) {
            this.playerConfigFile.delete();
        }
        File oldPlayerFile = new File(this.playerConfigFile.getParent(), oldName + ".yml");
        if (oldPlayerFile.exists()) {
            // Copy the old file to the new file.
            try {
                Files.move(oldPlayerFile.toPath(), this.playerConfigFile.toPath());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        // Delete the old player file if it still exists.
        if (oldPlayerFile.exists()) {
            oldPlayerFile.delete();
        }

        // Reload the config.
        reloadPlayerConfig();

        loadPlayer();
    }
    
    public void banFromIsland(String name) {
        List<String> bannedFrom = playerData.getStringList("bannedFrom");
        if (bannedFrom != null && !bannedFrom.contains(name)) {
            bannedFrom.add(name);
            playerData.set("bannedFrom", bannedFrom);
            save();
        }
    }

    public void unbanFromIsland(String name) {
        List<String> bannedFrom = playerData.getStringList("bannedFrom");
        if (bannedFrom != null && bannedFrom.contains(name)) {
            bannedFrom.remove(name);
            playerData.set("bannedFrom", bannedFrom);
            save();
        }
    }

    @Override
    public List<String> getBannedFrom() {
        return playerData.getStringList("bannedFrom");
    }

    public long getLastSaved() {
        return playerConfigFile.lastModified();
    }

    public void addTrust(String name) {
        List<String> trustedOn = playerData.getStringList("trustedOn");
        if (!trustedOn.contains(name)) {
            trustedOn.add(name);
            playerData.set("trustedOn", trustedOn);
        }
        save();
    }

    public void removeTrust(String name) {
        List<String> trustedOn = playerData.getStringList("trustedOn");
        trustedOn.remove(name);
        playerData.set("trustedOn", trustedOn);
        save();
    }

    @Override
    public List<String> getTrustedOn() {
        return playerData.getStringList("trustedOn");
    }

    public boolean isIslandGenerating() {
        return this.islandGenerating;
    }

    public void setIslandGenerating(boolean value) {
        this.islandGenerating = value;
    }

    @Override
    public IslandInfo getIslandInfo() {
        if (getHasIsland() && locationForParty() != null) {
            return uSkyBlock.getInstance().getIslandInfo(this);
        }
        return null;
    }

    public void setClearInventoryOnNextEntry(boolean b) {
        playerData.set("clearInventoryOnNextEntry", b ? b : null);
        save();
    }

    public boolean isClearInventoryOnNextEntry() {
        return playerData != null && playerData.getBoolean("clearInventoryOnNextEntry", false);
    }
}
