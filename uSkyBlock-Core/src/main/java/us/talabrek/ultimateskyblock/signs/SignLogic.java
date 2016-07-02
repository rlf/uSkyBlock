package us.talabrek.ultimateskyblock.signs;

import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.talabrek.ultimateskyblock.challenge.Challenge;
import us.talabrek.ultimateskyblock.challenge.ChallengeCompletion;
import us.talabrek.ultimateskyblock.challenge.ChallengeLogic;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.ItemStackUtil;
import us.talabrek.ultimateskyblock.util.LocationUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;
import static us.talabrek.ultimateskyblock.util.FormatUtil.wordWrap;

/**
 * Responsible for keeping track of signs.
 */
public class SignLogic {
    private static final Logger log = Logger.getLogger(SignLogic.class.getName());
    public static final int SIGN_LINE_WIDTH = 11; // Actually more like 15, but we break after.
    private final YmlConfiguration config;
    private final File configFile;
    private final uSkyBlock plugin;
    private final ChallengeLogic challengeLogic;

    public SignLogic(uSkyBlock plugin) {
        this.plugin = plugin;
        configFile = new File(plugin.getDataFolder(), "signs.yml");
        config = FileUtil.getYmlConfiguration("signs.yml");
        challengeLogic = plugin.getChallengeLogic();
    }

    public void addSign(Sign block, String[] lines, Chest chest) {
        Location loc = block.getLocation();
        ConfigurationSection signs = config.getConfigurationSection("signs");
        if (signs == null) {
            signs = config.createSection("signs");
        }
        String signLocation = LocationUtil.asKey(loc);
        ConfigurationSection signSection = signs.createSection(signLocation);
        signSection.set("location", LocationUtil.asString(loc));
        signSection.set("challenge", lines[1]);
        String chestLocation = LocationUtil.asString(chest.getLocation());
        signSection.set("chest", chestLocation);
        ConfigurationSection chests = config.getConfigurationSection("chests");
        if (chests == null) {
            chests = config.createSection("chests");
        }
        String chestPath = LocationUtil.asKey(chest.getLocation());
        List<String> signList = chests.getStringList(chestPath);
        if (!signList.contains(signLocation)) {
            signList.add(signLocation);
        }
        chests.set(chestPath, signList);
        saveAsync();
        updateSigns(chest.getLocation());
    }

    public void removeSign(final Location loc) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                removeSignAsync(loc);
            }
        });
    }

    private void removeSignAsync(Location loc) {
        String signKey = LocationUtil.asKey(loc);
        String chestLoc = config.getString("signs." + signKey + ".chest", null);
        if (chestLoc != null) {
            String chestKey = LocationUtil.asKey(LocationUtil.fromString(chestLoc));
            List<String> signList = config.getStringList("chests." + chestKey);
            signList.remove(signKey);
            if (signList.isEmpty()) {
                config.set("chests." + chestKey, null);
            }
        }
        config.set("signs." + signKey, null);
        save();
    }

    public void removeChest(final Location loc) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                removeChestAsync(loc);
            }
        });
    }

    private void removeChestAsync(Location loc) {
        String chestKey = LocationUtil.asKey(loc);
        List<String> signList = config.getStringList("chests." + chestKey);
        for (String signKey : signList) {
            config.set("signs." + signKey, null);
        }
        config.set("chests." + chestKey, null);
        save();
    }

    public void updateSigns(final Location... containerLocations) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                for (Location loc : containerLocations) {
                    updateSignAsync(loc);
                }
            }
        });
    }

    private void updateSignAsync(final Location loc) {
        if (loc == null || !plugin.isSkyAssociatedWorld(loc.getWorld())) {
            return;
        }
        String locString = LocationUtil.asKey(loc);
        List<String> signList = config.getStringList("chests." + locString);
        if (signList.isEmpty()) {
            return;
        }
        String islandName = WorldGuardHandler.getIslandNameAt(loc);
        if (islandName == null) {
            return;
        }
        for (String signLoc : signList) {
            String challengeName = config.getString("signs." + signLoc + ".challenge", null);
            if (challengeName == null) {
                continue;
            }
            final Challenge challenge = challengeLogic.getChallenge(challengeName);
            if (challenge == null || challenge.getType() != Challenge.Type.PLAYER) {
                continue;
            }
            final List<ItemStack> requiredItems = new ArrayList<>();
            boolean isChallengeAvailable = false;
            if (challengeLogic.isIslandSharing()) {
                final ChallengeCompletion completion = challengeLogic.getIslandCompletion(islandName, challengeName);
                if (completion != null) {
                    requiredItems.addAll(challenge.getRequiredItems(completion.getTimesCompletedInCooldown()));
                }
                IslandInfo islandInfo = plugin.getIslandInfo(islandName);
                if (islandInfo != null && islandInfo.getLeaderUniqueId() != null) {
                    PlayerInfo playerInfo = plugin.getPlayerInfo(islandInfo.getLeaderUniqueId());
                    if (playerInfo != null) {
                        isChallengeAvailable = challenge.getRank().isAvailable(playerInfo);
                    }
                }
            }
            String signLocString = config.getString("signs." + signLoc + ".location", null);
            final Location signLocation = LocationUtil.fromString(signLocString);
            final boolean challengeLocked = !isChallengeAvailable;
            // Back to sync
            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    updateSignFromChestSync(loc, signLocation, challenge, requiredItems, challengeLocked);
                }
            });
        }
    }

    private void updateSignFromChestSync(Location chestLoc, Location signLoc, Challenge challenge, List<ItemStack> requiredItems, boolean challengeLocked) {
        Block chestBlock = chestLoc.getBlock();
        Block signBlock = signLoc != null ? signLoc.getBlock() : null;
        if (chestBlock != null && signBlock != null
                && chestBlock.getType() == Material.CHEST && chestBlock.getState() instanceof Chest
                && signBlock.getType() == Material.WALL_SIGN && signBlock.getState() instanceof Sign
                ) {
            Sign sign = (Sign) signBlock.getState();
            Chest chest = (Chest) chestBlock.getState();
            int missing = -1;
            if (!requiredItems.isEmpty() && !challengeLocked) {
                missing = 0;
                for (ItemStack required : requiredItems) {
                    if (!chest.getInventory().containsAtLeast(required, required.getAmount())) {
                        missing += required.getAmount() - plugin.getChallengeLogic().getCountOf(chest.getInventory(), required);
                    }
                }
            }
            String format = "\u00a72\u00a7l";
            if (missing > 0) {
                format = "\u00a74\u00a7l";
            }
            List<String> lines = wordWrap(challenge.getDisplayName(), SIGN_LINE_WIDTH, SIGN_LINE_WIDTH);
            if (challengeLocked) {
                lines.add(tr("\u00a74\u00a7lLocked Challenge"));
            } else {
                lines.addAll(wordWrap(challenge.getDescription(), SIGN_LINE_WIDTH, SIGN_LINE_WIDTH));
            }
            for (int i = 0; i < 3; i++) {
                if (i < lines.size()) {
                    sign.setLine(i, lines.get(i));
                } else {
                    sign.setLine(i, "");
                }
            }
            if (missing > 0) {
                sign.setLine(3, format + missing);
            } else if (missing == 0) {
                sign.setLine(3, format + tr("READY"));
            } else if (lines.size() > 3) {
                sign.setLine(3, lines.get(3));
            } else {
                sign.setLine(3, "");
            }
            if (!sign.update()) {
                log.info("Unable to update sign at " + LocationUtil.asString(signLoc));
            }
        }
    }

    private void saveAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                save();
            }
        });
    }

    private void save() {
        synchronized (configFile) {
            try {
                config.save(configFile);
            } catch (IOException e) {
                log.info("Unable to save to " + configFile);
            }
        }
    }

    public void signClicked(final Player player, final Location location) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                tryCompleteAsync(player, location);
            }
        });
    }

    private void tryCompleteAsync(final Player player, Location location) {
        String signLoc = LocationUtil.asKey(location);
        String challengeName = config.getString("signs." + signLoc + ".challenge", null);
        if (challengeName != null) {
            String islandName = WorldGuardHandler.getIslandNameAt(location);
            String chestLocString = config.getString("signs." + signLoc + ".chest", null);
            final Location chestLoc = LocationUtil.fromString(chestLocString);
            if (islandName != null && chestLoc != null) {
                final Challenge challenge = challengeLogic.getChallenge(challengeName);
                if (challenge == null || challenge.getType() != Challenge.Type.PLAYER) {
                    return;
                }
                PlayerInfo playerInfo = plugin.getPlayerInfo(player);
                if (playerInfo == null) {
                    return;
                }
                if (!challenge.getRank().isAvailable(playerInfo)) {
                    player.sendMessage(tr("\u00a74The {0} challenge is not available yet!", challenge.getDisplayName()));
                    return;
                }
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        tryComplete(player, chestLoc, challenge);
                    }
                });
            }
        }
    }

    private void tryComplete(Player player, Location chestLoc, Challenge challenge) {
        BlockState state = chestLoc.getBlock().getState();
        if (!(state instanceof Chest)) {
            return;
        }
        PlayerInfo playerInfo = plugin.getPlayerInfo(player);
        if (playerInfo == null || !playerInfo.getHasIsland()) {
            return;
        }
        ChallengeCompletion completion = challengeLogic.getChallenge(playerInfo, challenge.getName());
        List<ItemStack> requiredItems = challenge.getRequiredItems(completion.getTimesCompletedInCooldown());
        Chest chest = (Chest) state;
        int missing = 0;
        for (ItemStack required : requiredItems) {
            int diff = 0;
            if (!player.getInventory().containsAtLeast(required, required.getAmount())) {
                diff = required.getAmount() - plugin.getChallengeLogic().getCountOf(player.getInventory(), required);
            }
            if (diff > 0 && !chest.getInventory().containsAtLeast(required, diff)) {
                diff -= plugin.getChallengeLogic().getCountOf(chest.getInventory(), required);
            } else {
                diff = 0;
            }
            missing += diff;
        }
        if (missing == 0) {
            ItemStack[] items = requiredItems.toArray(new ItemStack[requiredItems.size()]);
            ItemStack[] copy = ItemStackUtil.clone(requiredItems).toArray(new ItemStack[requiredItems.size()]);
            HashMap<Integer, ItemStack> missingItems = player.getInventory().removeItem(items);
            missingItems = chest.getInventory().removeItem(missingItems.values().toArray(new ItemStack[missingItems.size()]));
            if (!missingItems.isEmpty()) {
                // This effectively means, we just donated some items to the player (exploit!!)
                log.warning("Not all items removed from chest and player: " + missingItems.values());
            }
            HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(copy);
            if (leftOvers.isEmpty()) {
                plugin.getChallengeLogic().completeChallenge(player, challenge.getName());
            } else {
                chest.getInventory().addItem(leftOvers.values().toArray(new ItemStack[leftOvers.size()]));
                player.sendMessage(tr("\u00a7cWARNING:\u00a7e Could not transfer all the required items to your inventory!"));
            }
            updateSigns(chest.getLocation());
        } else {
            player.sendMessage(tr("\u00a7cNot enough items in chest to complete challenge!"));
        }
    }

}
