package us.talabrek.ultimateskyblock.island;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.api.IslandLevel;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.task.RenamePlayerTask;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.FileUtil;
import us.talabrek.ultimateskyblock.util.LocationUtil;
import us.talabrek.ultimateskyblock.util.TimeUtil;
import us.talabrek.ultimateskyblock.uuid.PlayerNameChangedEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import static org.bukkit.Material.AIR;
import static org.bukkit.Material.BEDROCK;

/**
 * Responsible for island creation, locating locations, purging, clearing etc.
 */
public class IslandLogic {
    private final uSkyBlock plugin;
    private final File directoryIslands;

    private final Map<String, IslandInfo> islands = new ConcurrentHashMap<>();

    private volatile long lastGenerate = 0;
    private final List<IslandLevel> ranks = new ArrayList<>();

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

    public void clearIsland(Location loc, Runnable afterDeletion) {
        World skyBlockWorld = plugin.getWorld();
        ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(loc);
        if (region != null) {
            WorldEditHandler.clearIsland(skyBlockWorld, region, afterDeletion);
        }
    }

    public boolean clearFlatland(final CommandSender sender, final Location loc, int delay) {
        if (loc == null) {
            return false;
        }
        final World w = loc.getWorld();
        final int px = loc.getBlockX();
        final int pz = loc.getBlockZ();
        final int py = 0;
        final int range = Math.max(Settings.island_protectionRange, Settings.island_distance) + 1;
        final int radius = range/2;
        // 5 sampling points...
        if (w.getBlockAt(px, py, pz).getType() == BEDROCK
                || w.getBlockAt(px+radius, py, pz+radius).getType() == BEDROCK
                || w.getBlockAt(px+radius, py, pz-radius).getType() == BEDROCK
                || w.getBlockAt(px-radius, py, pz+radius).getType() == BEDROCK
                || w.getBlockAt(px-radius, py, pz-radius).getType() == BEDROCK)
        {
            sender.sendMessage(String.format("\u00a74Flatland detected under your island!\u00a7e Clearing it in %s, stay clear.", TimeUtil.ticksAsString(delay)));
            final AtomicInteger sharedY = new AtomicInteger(0);
            final Runnable clearYLayer = new Runnable() {
                long tStart;
                long timeUsed = 0;
                @Override
                public void run() {
                    long t = System.currentTimeMillis();
                    int y = sharedY.getAndIncrement();
                    if (y  <= 3) {
                        if (y == 0) {
                            tStart = t;
                        }
                        for (int dx = 1; dx <= range; dx++) {
                            for (int dz = 1; dz <= range; dz++) {
                                Block b = w.getBlockAt(px + (dx % 2 == 0 ? dx/2 : -dx/2),
                                        y, pz + (dz % 2 == 0 ? dz/2 : -dz/2));
                                if (b.getType() != AIR) {
                                    b.setType(AIR);
                                }
                            }
                        }
                        long diffTicks = (System.currentTimeMillis() - t)/50;
                        timeUsed += diffTicks;
                        plugin.getServer().getScheduler().runTaskLater(plugin, this, diffTicks);
                    } else {
                        plugin.log(Level.INFO, String.format("Flatland cleared at %s in %s (%s)", LocationUtil.asString(loc), TimeUtil.millisAsString(System.currentTimeMillis() - tStart), TimeUtil.millisAsString(timeUsed)));
                        sender.sendMessage("\u00a7eFlatland was cleared under your island. Take care.");
                    }
                }
            };
            plugin.getServer().getScheduler().runTaskLater(plugin, clearYLayer, delay);
            return true;
        }
        return false;
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
        int playerrank = 0;
        sender.sendMessage("\u00a7eDisplaying the top 10 islands:");
        synchronized (ranks) {
            if (ranks == null || ranks.isEmpty()) {
                sender.sendMessage("\u00a74Top ten list is empty! (perhaps the generation failed?)");
            }
            int place = 1;
            for (final IslandLevel level : ranks.subList(0, Math.min(ranks.size(), 10))) {
                sender.sendMessage(String.format("\u00a7a#%2d \u00a77(%5.2f): %s \u00a77%s", place, level.getScore(),
                        level.getLeaderName(), level.getMembers()));
                if (level.hasMember(sender.getName())) {
                    playerrank = place;
                }
                place++;
            }
        }
        if (playerrank > 0) {
            sender.sendMessage("\u00a7eYour rank is: " + ChatColor.WHITE + playerrank);
        }
    }

    public void showTopTen(final CommandSender sender) {
        long t = System.currentTimeMillis();
        if (t > (lastGenerate + (Settings.island_topTenTimeout*60000)) || sender.hasPermission("usb.admin.topten")) {
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

    public List<IslandLevel> getRanks(int offset, int length) {
        synchronized (ranks) {
            int size = ranks.size();
            if (size <= offset) {
                return Collections.emptyList();
            }
            return ranks.subList(offset, Math.min(size-offset, length));
        }
    }

    private void generateTopTen() {
        List<IslandLevel> topTen = new ArrayList<>();
        final File folder = directoryIslands;
        final File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            FileConfiguration islandConfig = readIslandConfig(file);
            double level = islandConfig != null ? islandConfig.getDouble("general.level", 0) : 0;
            if (islandConfig != null && level > 0) {
                String partyLeader = islandConfig.getString("party.leader");
                PlayerInfo pi = plugin.getPlayerInfo(partyLeader);
                String partyLeaderName = partyLeader;
                if (pi != null) {
                    partyLeaderName = pi.getDisplayName();
                }
                String toStr = "";
                ConfigurationSection members = islandConfig.getConfigurationSection("party.members");
                if (members != null) {
                    Set<String> membersStr = members.getKeys(false);
                    if (membersStr != null) {
                        membersStr.remove(partyLeader);
                        toStr = Arrays.toString(membersStr.toArray(new String[membersStr.size()]));
                        toStr = toStr.substring(1, toStr.length()-1);
                    }
                }
                topTen.add(new IslandLevel(islandConfig.getName(), partyLeaderName, !toStr.isEmpty() ? "(" + toStr + ")" : "", level));
            }
        }
        Collections.sort(topTen);
        synchronized (ranks) {
            lastGenerate = System.currentTimeMillis();
            ranks.clear();
            ranks.addAll(topTen);
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

    public void renamePlayer(PlayerInfo playerInfo, Runnable completion, PlayerNameChangedEvent... changes) {
        String[] files = directoryIslands.list(FileUtil.createYmlFilenameFilter());
        RenamePlayerTask task = new RenamePlayerTask(playerInfo.locationForParty(), files, this, changes);
        plugin.getAsyncExecutor().execute(plugin, task, completion, 0.8f, 20);
    }

    public void renamePlayer(String islandName, PlayerNameChangedEvent... changes) {
        IslandInfo islandInfo = getIslandInfo(islandName);
        if (islandInfo != null) {
            for (PlayerNameChangedEvent e : changes) {
                islandInfo.renamePlayer(e.getPlayer(), e.getOldName());
            }
            if (!islandInfo.hasOnlineMembers()) {
                removeIslandFromMemory(islandInfo.getName());
            }
        }
    }
}
