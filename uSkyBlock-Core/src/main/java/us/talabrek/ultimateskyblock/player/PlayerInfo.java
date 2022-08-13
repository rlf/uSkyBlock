package us.talabrek.ultimateskyblock.player;

import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.talabrek.ultimateskyblock.challenge.Challenge;
import us.talabrek.ultimateskyblock.challenge.ChallengeCompletion;
import us.talabrek.ultimateskyblock.hook.permissions.PermissionsHook;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;
import us.talabrek.ultimateskyblock.util.LogUtil;
import us.talabrek.ultimateskyblock.util.UUIDUtil;
import us.talabrek.ultimateskyblock.uuid.PlayerDB;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public class PlayerInfo implements Serializable, us.talabrek.ultimateskyblock.api.PlayerInfo {
    private static final String CN = PlayerInfo.class.getName();
    private static final Logger log = Logger.getLogger(CN);
    private static final long serialVersionUID = 1L;
    private static final int YML_VERSION = 1;
    private final uSkyBlock plugin;
    private String playerName;
    private String displayName;
    private UUID uuid;

    private Location islandLocation;

    private Location homeLocation;

    private volatile FileConfiguration playerData;
    private File playerConfigFile;

    private boolean islandGenerating = false;
    private boolean dirty = false;

    public PlayerInfo(String currentPlayerName, UUID playerUUID, uSkyBlock plugin) {
        this.plugin = plugin;
        this.uuid = playerUUID;
        this.playerName = currentPlayerName;
        // Prefer UUID over Name
        playerConfigFile = new File(uSkyBlock.getInstance().directoryPlayers, UUIDUtil.asString(playerUUID) + ".yml");
        File nameFile = new File(uSkyBlock.getInstance().directoryPlayers, playerName + ".yml");
        if (!playerConfigFile.exists() && nameFile.exists() && !currentPlayerName.equals(PlayerDB.UNKNOWN_PLAYER_NAME)) {
            nameFile.renameTo(playerConfigFile);
        }
        playerData = new YmlConfiguration();
        if (playerConfigFile.exists()) {
            FileUtil.readConfig(playerData, playerConfigFile);
        }
        loadPlayer();
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
        Player player = null;
        if (uuid != null) {
            player = uSkyBlock.getInstance().getPlayerDB().getPlayer(uuid);
        }
        if (player == null && playerName != null) {
            player = uSkyBlock.getInstance().getPlayerDB().getPlayer(playerName);
        }
        return player;
    }

    public OfflinePlayer getOfflinePlayer() {
        if (uuid != null) {
            return uSkyBlock.getInstance().getPlayerDB().getOfflinePlayer(uuid);
        }
        return null;
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
        World nether = uSkyBlock.getInstance().getWorldManager().getNetherWorld();
        if (nether == null) {
            return null;
        }
        if (l != null) {
            l.setWorld(nether);
            l.setY(l.getY() / 2);
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

    public void completeChallenge(Challenge challenge, boolean silent) {
        uSkyBlock.getInstance().getChallengeLogic().completeChallenge(this, challenge.getName());
        if (silent) {
            return;
        }
        IslandInfo island = getIslandInfo();
        if (island != null) {
            island.sendMessageToOnlineMembers(tr("\u00a79{0}\u00a7f has completed the \u00a79{1}\u00a7f challenge!",
                getPlayerName(), challenge.getDisplayName()));
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
        FileConfiguration playerConfig = playerData;
        ConfigurationSection pSection = playerConfig.createSection("player");
        pSection.set("islandX", 0);
        pSection.set("islandY", 0);
        pSection.set("islandZ", 0);
        pSection.set("homeX", 0);
        pSection.set("homeY", 0);
        pSection.set("homeZ", 0);
        pSection.set("homeYaw", 0);
        pSection.set("homePitch", 0);
        pSection.set("perms", null);
    }

    private PlayerInfo loadPlayer() {
        if (!playerData.contains("player.islandY") || playerData.getInt("player.islandY", 0) == 0) {
            this.islandLocation = null;
            this.homeLocation = null;
            createPlayerConfig();
            return this;
        }
        try {
            this.displayName = playerData.getString("player.displayName", playerName);
            this.uuid = UUIDUtil.fromString(playerData.getString("player.uuid", null));
            this.islandLocation = new Location(uSkyBlock.getInstance().getWorldManager().getWorld(),
                    playerData.getInt("player.islandX"), playerData.getInt("player.islandY"), playerData.getInt("player.islandZ"));
            this.homeLocation = new Location(uSkyBlock.getInstance().getWorldManager().getWorld(),
                    playerData.getInt("player.homeX") + 0.5, playerData.getInt("player.homeY") + 0.2, playerData.getInt("player.homeZ") + 0.5,
                    (float) playerData.getDouble("player.homeYaw", 0.0),
                    (float) playerData.getDouble("player.homePitch", 0.0));

            log.exiting(CN, "loadPlayer");
            return this;
        } catch (Exception e) {
            LogUtil.log(Level.INFO, "Returning null while loading, not good!");
            return null;
        }
    }

    private void createPlayerConfig() {
        LogUtil.log(Level.FINER, "Creating new player config!");
        setupPlayer();
    }

    public FileConfiguration getConfig() {
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
            LogUtil.log(Level.INFO, "Can't save player data! (" + playerName + ", " + uuid + ", " + playerConfigFile + ")");
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
        str += ChatColor.GRAY + "  - hasIsland: " + ChatColor.DARK_AQUA + getHasIsland() + "\n";
        str += ChatColor.GRAY + "  - home: " + ChatColor.DARK_AQUA + LocationUtil.asString(getHomeLocation()) + "\n";
        str += ChatColor.GRAY + "  - island: " + ChatColor.DARK_AQUA + LocationUtil.asString(getIslandLocation()) + "\n";
        str += ChatColor.GRAY + "  - banned from: " + ChatColor.DARK_AQUA + getBannedFrom() + "\n";
        str += ChatColor.GRAY + "  - trusted on: " + ChatColor.DARK_AQUA + playerData.getStringList("trustedOn") + "\n";
        // TODO: 28/12/2014 - R4zorax: Some info about challenges?
        return str;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
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
        return playerData.getBoolean("clearInventoryOnNextEntry", false);
    }

    public void onTeleport(final Player player) {
        if (isClearInventoryOnNextEntry()) {
            uSkyBlock.getInstance().sync(() -> uSkyBlock.getInstance().clearPlayerInventory(player), 50);
        }
        List<String> pending = playerData.getStringList("pending-commands");
        if (!pending.isEmpty()) {
            uSkyBlock.getInstance().execCommands(player, pending);
            playerData.set("pending-commands", null);
            save();
        }
        List<String> pendingPermissions = playerData.getStringList("pending-permissions");
        if (!pendingPermissions.isEmpty()) {
            if (addPermissions(pendingPermissions)) {
                playerData.set("pending-permissions", null);
            }
            save();
        }
    }

    public boolean execCommands(@Nullable List<String> commands) {
        if (commands == null || commands.isEmpty()) {
            return true;
        }
        Player player = getPlayer();
        if (player != null && player.isOnline()) {
            uSkyBlock.getInstance().execCommands(player, commands);
            return true;
        } else {
            List<String> pending = playerData.getStringList("pending-commands");
            pending.addAll(commands);
            playerData.set("pending-commands", pending);
            save();
            return false;
        }
    }

    public boolean addPermissions(@Nullable List<String> perms) {
        if (perms == null || perms.isEmpty()) {
            return true;
        }

        Player target = getPlayer();
        Optional<PermissionsHook> hook = plugin.getHookManager().getPermissionsHook();

        if (target != null && target.isOnline() && hook.isPresent()) {
            List<String> permList = playerData.getStringList("player.perms");
            PermissionsHook pHook = hook.get();

            for (String perm : perms) {
                if (!pHook.hasPermission(target, perm)) {
                    permList.add(perm);
                    pHook.addPermission(target, perm);
                }
            }
            playerData.set("player.perms", permList);
            save();
            return true;
        } else {
            List<String> pending = playerData.getStringList("pending-permissions");
            pending.addAll(perms);
            playerData.set("pending-permissions", pending);
            save();
            return false;
        }
    }

    public void clearPerms(@NotNull Player target) {
        Validate.notNull(target, "Target cannot be null!");

        final List<String> perms = playerData.getStringList("player.perms");
        if (!perms.isEmpty()) {
            plugin.getHookManager().getPermissionsHook().ifPresent((hook) -> {
                for (String perm : perms) {
                    hook.removePermission(target, perm);
                }
            });
            playerData.set("player.perms", null);
            playerData.set("pending-permissions", null);
            save();
        }
    }
}
