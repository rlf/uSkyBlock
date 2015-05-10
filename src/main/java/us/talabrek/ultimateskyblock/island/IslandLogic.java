package us.talabrek.ultimateskyblock.island;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.api.IslandLevel;
import us.talabrek.ultimateskyblock.api.IslandRank;
import us.talabrek.ultimateskyblock.api.event.uSkyBlockEvent;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.handler.task.WorldEditClearTask;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.FileUtil;
import us.talabrek.ultimateskyblock.util.LocationUtil;
import us.talabrek.ultimateskyblock.util.PlayerUtil;
import us.talabrek.ultimateskyblock.util.TimeUtil;
import us.talabrek.ultimateskyblock.uuid.PlayerNameChangedEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bukkit.Material.BEDROCK;
import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * Responsible for island creation, locating locations, purging, clearing etc.
 */
public class IslandLogic {
    private static final Logger log = Logger.getLogger(IslandLogic.class.getName());
    private final uSkyBlock plugin;
    private final File directoryIslands;

    private final Map<String, IslandInfo> islands = new ConcurrentHashMap<>();
    private final boolean showMembers;
    private final boolean flatlandFix;

    private volatile long lastGenerate = 0;
    private final List<IslandLevel> ranks = new ArrayList<>();

    public IslandLogic(uSkyBlock plugin, File directoryIslands) {
        this.plugin = plugin;
        this.directoryIslands = directoryIslands;
        showMembers = plugin.getConfig().getBoolean("options.island.topTenShowMembers", true);
        flatlandFix = plugin.getConfig().getBoolean("options.island.fixFlatland", false);
    }

    public synchronized IslandInfo getIslandInfo(String islandName) {
        if (islandName == null || islandName.isEmpty()) {
            return null;
        }
        if (!islands.containsKey(islandName)) {
            islands.put(islandName, new IslandInfo(islandName));
        }
        return islands.get(islandName);
    }

    public IslandInfo getIslandInfo(PlayerInfo playerInfo) {
        if (playerInfo != null && playerInfo.getHasIsland()) {
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
        log.log(Level.FINE, "clearing island at {0}", loc);
        World skyBlockWorld = plugin.getWorld();
        ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(loc);
        if (region != null) {
            for (Player player : WorldEditHandler.getPlayersInRegion(plugin.getWorld(), region)) {
                if (player != null && player.isOnline() && !player.isFlying()) {
                    player.sendMessage(tr("\u00a7cThe island you are on is being deleted! Sending you to spawn."));
                    plugin.spawnTeleport(player);
                }
            }
            WorldEditHandler.clearIsland(skyBlockWorld, region, afterDeletion);
        } else {
            log.log(Level.WARNING, "Trying to delete an island - with no WG region! ({0})", LocationUtil.asString(loc));
            afterDeletion.run();
        }
    }

    public boolean clearFlatland(final CommandSender sender, final Location loc, final int delay) {
        if (loc == null) {
            return false;
        }
        if (delay > 0 && !flatlandFix) {
            return false; // Skip
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
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
                    sender.sendMessage(String.format("\u00a7c-----------------------------------\n\u00a7cFlatland detected under your island!\n\u00a7e Clearing it in %s, stay clear.\n\u00a7c-----------------------------------\n", TimeUtil.ticksAsString(delay)));
                    new WorldEditClearTask(plugin, sender, new CuboidRegion(new Vector(px-radius, 0, pz-radius),
                            new Vector(px+radius, 4, pz+radius)),
                            "\u00a7eFlatland was cleared under your island (%s). Take care.").runTaskLater(plugin, delay);
                }
            }
        };
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
        return false;
    }

    public void displayTopTen(final CommandSender sender, int page) {
        synchronized (ranks) {
            int maxpage = (( ranks.size()-1) / 10) + 1;
            if (page > maxpage) {
                page = maxpage;
            }
            if (page < 1) {
                page = 1;
            }
            sender.sendMessage(tr("\u00a7eWALL OF FAME (page {0} of {1}):", page, maxpage));
            if (ranks == null || ranks.isEmpty()) {
                sender.sendMessage(tr("\u00a74Top ten list is empty! (perhaps the generation failed?)"));
            }
            int place = 1;
            String playerName = sender instanceof Player ? ((Player)sender).getDisplayName() : sender.getName();
            PlayerInfo playerInfo = plugin.getPlayerInfo(playerName);
            IslandRank rank = null;
            if (playerInfo != null && playerInfo.getHasIsland()) {
                rank = getRank(playerInfo.locationForParty());
            }
            int offset = (page-1) * 10;
            place += offset;
            for (final IslandLevel level : ranks.subList(offset, Math.min(ranks.size(), 10*page))) {
                String members = "";
                if (showMembers && !level.getMembers().isEmpty()) {
                    members = Arrays.toString(level.getMembers().toArray(new String[level.getMembers().size()]));
                }
                sender.sendMessage(String.format(tr("\u00a7a#%2d \u00a77(%5.2f): \u00a7e%s \u00a77%s"), place, level.getScore(),
                        level.getLeaderName(), members));
                place++;
            }
            if (rank != null) {
                sender.sendMessage(tr("\u00a7eYour rank is: \u00a7f{0}", rank.getRank()));
            }
        }

    }

    public void showTopTen(final CommandSender sender, final int page) {
        long t = System.currentTimeMillis();
        if (t > (lastGenerate + (Settings.island_topTenTimeout*60000)) || sender.hasPermission("usb.admin.topten")) {
            lastGenerate = t;
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    generateTopTen(sender);
                    displayTopTen(sender, page);
                }
            });
        } else {
            displayTopTen(sender, page);
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

    public void generateTopTen(final CommandSender sender) {
        List<IslandLevel> topTen = new ArrayList<>();
        final File folder = directoryIslands;
        final String[] listOfFiles = folder.list(FileUtil.createIslandFilenameFilter());
        for (String file : listOfFiles) {
            String islandName = FileUtil.getBasename(file);
            try {
                boolean wasLoaded = islands.containsKey(islandName);
                IslandInfo islandInfo = getIslandInfo(islandName);
                double level = islandInfo != null ? islandInfo.getLevel() : 0;
                if (islandInfo != null && level > 10) {
                    IslandLevel islandLevel = createIslandLevel(islandInfo, level);
                    topTen.add(islandLevel);
                }
                if (!wasLoaded) {
                    islands.remove(islandName);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error during rank generation", e);
            }
        }
        Collections.sort(topTen);
        synchronized (ranks) {
            lastGenerate = System.currentTimeMillis();
            ranks.clear();
            ranks.addAll(topTen);
        }
        plugin.fireChangeEvent(sender, uSkyBlockEvent.Cause.RANK_UPDATED);
    }

    private IslandLevel createIslandLevel(IslandInfo islandInfo, double level) {
        String partyLeader = islandInfo.getLeader();
        String partyLeaderName = PlayerUtil.getPlayerDisplayName(partyLeader);
        List<String> memberList = new ArrayList<>(islandInfo.getMembers());
        memberList.remove(partyLeader);
        List<String> names = new ArrayList<>();
        for (String name : memberList) {
            String displayName = PlayerUtil.getPlayerDisplayName(name);
            if (displayName != null) {
                names.add(displayName);
            }
        }
        return new IslandLevel(islandInfo.getName(), partyLeaderName, names, level);
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

    public synchronized void removeIslandFromMemory(String islandName) {
        islands.remove(islandName);
    }

    public void renamePlayer(PlayerInfo playerInfo, Runnable completion, PlayerNameChangedEvent change) {
        List<String> islands = new ArrayList<>();
        islands.add(playerInfo.locationForParty());
        islands.addAll(playerInfo.getBannedFrom());
        for (String islandName : islands) {
            renamePlayer(islandName, change);
        }
        if (completion != null) {
            completion.run();
        }
    }

    public void renamePlayer(String islandName, PlayerNameChangedEvent e) {
        IslandInfo islandInfo = getIslandInfo(islandName);
        if (islandInfo != null) {
            islandInfo.renamePlayer(e.getPlayer(), e.getOldName());
            if (!islandInfo.hasOnlineMembers()) {
                removeIslandFromMemory(islandInfo.getName());
            }
        }
    }

    public void updateRank(IslandInfo islandInfo, IslandScore score) {
        synchronized (ranks) {
            IslandLevel islandLevel = createIslandLevel(islandInfo, score.getScore());
            ranks.remove(islandLevel);
            ranks.add(islandLevel);
            Collections.sort(ranks);
        }
    }

    public boolean hasIsland(Location loc) {
        return loc == null || new File(directoryIslands, LocationUtil.getIslandName(loc) + ".yml").exists();
    }

    public IslandRank getRank(String islandName) {
        if (ranks != null) {
            ArrayList<IslandLevel> rankList = new ArrayList<>(ranks);
            for (int i = 0; i < rankList.size(); i++) {
                IslandLevel level = rankList.get(i);
                if (level.getIslandName().equalsIgnoreCase(islandName)) {
                    return new IslandRank(level, i+1);
                }
            }
        }
        return null;
    }
}
