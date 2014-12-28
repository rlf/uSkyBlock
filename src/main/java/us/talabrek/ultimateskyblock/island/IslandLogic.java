package us.talabrek.ultimateskyblock.island;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Responsible for island creation, locating locations, purging, clearing etc.
 */
public class IslandLogic {
    private final uSkyBlock plugin;
    private final File directoryIslands;

    private final Map<String, IslandInfo> islands = new ConcurrentHashMap<>();

    private volatile long lastGenerate = 0;
    private final LinkedHashMap<String, Double> top = new LinkedHashMap<>();

    public IslandLogic(uSkyBlock plugin, File directoryIslands) {
        this.plugin = plugin;
        this.directoryIslands = directoryIslands;
    }

    public IslandInfo getIslandInfo(String islandName) {
        if (!islands.containsKey(islandName)) {
            islands.put(islandName, new IslandInfo(islandName));
        }
        return islands.get(islandName);
    }

    public IslandInfo getIslandInfo(PlayerInfo playerInfo) {
        if (playerInfo.getHasIsland()) {
            return getIslandInfo(playerInfo.locationForParty());
        }
        return null;
    }

    public void loadIslandChunks(Location l, int radius) {
        World world = l.getWorld();
        final int px = l.getBlockX();
        final int pz = l.getBlockZ();
        for (int x = -radius-16; x <= radius+16; x += 16) {
            for (int z = -radius-16; z <= radius+16; z += 16) {
                world.loadChunk((px + x) / 16, (pz + z) / 16, true);
            }
        }
    }

    public void clearIsland(Location loc) {
        World skyBlockWorld = plugin.getWorld();
        ApplicableRegionSet applicableRegions = WorldGuardHandler.getWorldGuard().getRegionManager(skyBlockWorld).getApplicableRegions(loc);
        for (ProtectedRegion region : applicableRegions) {
            if (!region.getId().equalsIgnoreCase("__global__")) {
                WorldEditHandler.clearIsland(skyBlockWorld, region);
            }
        }
    }

    public void reloadIsland(Location location) {
        reloadIsland(location2Name(location));
    }

    private String location2Name(Location location) {
        return location != null ? ("" + location.getBlockX() + "," + location.getBlockZ()) : "null";
    }

    public void reloadIsland(String location) {
        getIslandInfo(location).reload();
    }

    private void displayTopTen(final CommandSender sender) {
        int i = 1;
        int playerrank = 0;
        sender.sendMessage(ChatColor.YELLOW + "Displaying the top 10 islands:");
        if (top == null || top.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Top ten list is empty! (perhaps the generation failed?)");
        }
        for (final String playerName : top.keySet()) {
            if (i <= 10) {
                sender.sendMessage(String.format(ChatColor.GREEN + "#%2d: %s - Island level %5.2f", i, playerName, top.get(playerName)));
            }
            if (playerName != null && playerName.equalsIgnoreCase(sender.getName())) {
                playerrank = i;
            }
            ++i;
        }
        if (playerrank > 0) {
            sender.sendMessage(ChatColor.YELLOW + "Your rank is: " + ChatColor.WHITE + playerrank);
        }
    }

    public void showTopTen(final CommandSender sender) {
        long t = System.currentTimeMillis();
        if (t > (lastGenerate + (Settings.island_topTenTimeout*60000))) {
            lastGenerate = t;
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    generateTopTen();
                    displayTopTen(sender);
                }
            });
        } else {
            displayTopTen(sender);
        }
    }
    private void generateTopTen() {
        final HashMap<String, Double> tempMap = new HashMap<>();
        final File folder = directoryIslands;
        final File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            FileConfiguration islandConfig = readIslandConfig(file);
            if (islandConfig != null && islandConfig.getInt("general.level") > 0) {
                String partyLeader = islandConfig.getString("party.leader");
                PlayerInfo pi = plugin.getPlayerInfo(partyLeader);
                if (pi != null) {
                    tempMap.put(pi.getDisplayName(), islandConfig.getDouble("general.level"));
                } else {
                    tempMap.put(partyLeader, islandConfig.getDouble("general.level"));
                }
            }
        }
        TreeMap<String, Double> sortedMap = new TreeMap<>(new TopTenComparator(tempMap));
        sortedMap.putAll(tempMap);
        synchronized (top) {
            lastGenerate = System.currentTimeMillis();
            top.clear();
            top.putAll(sortedMap);
        }
    }

    private FileConfiguration readIslandConfig(File file) {
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            uSkyBlock.log(Level.WARNING, "Error reading island " + file.getName(), e);
            return null;
        }
        return config;
    }


    public synchronized IslandInfo createIsland(String location, String player) {
        IslandInfo info = getIslandInfo(location);
        info.clearIslandConfig(player);
        return info;
    }

    public synchronized void deleteIslandConfig(final String location) {
        File file = new File(this.directoryIslands, location + ".yml");
        file.delete();
        islands.remove(location);
    }

    public void removeIslandFromMemory(String islandName) {
        getIslandInfo(islandName).save();
        islands.remove(islandName);
    }
}
