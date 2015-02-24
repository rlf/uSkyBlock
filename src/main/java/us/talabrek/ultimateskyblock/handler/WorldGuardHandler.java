package us.talabrek.ultimateskyblock.handler;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.VersionUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class WorldGuardHandler {
    private static final String CN = WorldGuardHandler.class.getName();
    private static final Logger log = Logger.getLogger(CN);
    private static final int VERSION = 7;
    private static BukkitTask saveTask;

    public static WorldGuardPlugin getWorldGuard() {
        final Plugin plugin = uSkyBlock.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null;
        }
        return (WorldGuardPlugin) plugin;
    }

    public static boolean protectIsland(final CommandSender sender, final PlayerInfo pi) {
        log.entering(CN, "protectIsland", new Object[]{sender, pi});
        try {
            uSkyBlock plugin = uSkyBlock.getInstance();
            IslandInfo islandConfig = plugin.getIslandInfo(pi);
            if (islandConfig == null) {
                return false;
            }
            if (islandConfig.getLeader().isEmpty()) {
                islandConfig.setupPartyLeader(pi.getPlayerName());
                updateRegion(sender, islandConfig);
                return true;
            } else {
                return protectIsland(plugin, sender, islandConfig);
            }
        } finally {
            log.exiting(CN, "protectIsland");
        }
    }

    public static boolean protectIsland(uSkyBlock plugin, CommandSender sender, IslandInfo islandConfig) {
        try {
            WorldGuardPlugin worldGuard = getWorldGuard();
            RegionManager regionManager = worldGuard.getRegionManager(uSkyBlock.getSkyBlockWorld());
            String regionName = islandConfig.getName() + "island";
            if (islandConfig != null && noOrOldRegion(regionManager, regionName, islandConfig)) {
                ProtectedCuboidRegion region = setRegionFlags(sender, islandConfig);
                final Iterable<ProtectedRegion> set = regionManager.getApplicableRegions(islandConfig.getIslandLocation());
                for (ProtectedRegion regions : set) {
                    if (!(regions instanceof GlobalProtectedRegion)) {
                        regionManager.removeRegion(regions.getId());
                    }
                }
                regionManager.addRegion(region);
                plugin.log(Level.INFO, "New protected region created for " + islandConfig.getLeader() + "'s Island by " + sender.getName());
                save(regionManager);
                islandConfig.setRegionVersion(VERSION);
                return true;
            }
        } catch (Exception ex) {
            String name = islandConfig != null ? islandConfig.getLeader() : "Unknown";
            plugin.log(Level.SEVERE, "ERROR: Failed to protect " + name + "'s Island (" + sender.getName() + ")", ex);
        }
        return false;
    }

    public static void updateRegion(CommandSender sender, IslandInfo islandInfo) {
        try {
            ProtectedCuboidRegion region = setRegionFlags(sender, islandInfo);
            RegionManager regionManager = getWorldGuard().getRegionManager(uSkyBlock.getInstance().getWorld());
            regionManager.removeRegion(islandInfo.getName() + "island");
            regionManager.removeRegion(islandInfo.getLeader() + "island");
            regionManager.addRegion(region);
            save(regionManager);
        } catch (Exception e) {
            uSkyBlock.getInstance().log(Level.SEVERE, "ERROR: Failed to update region for " + islandInfo.getName(), e);
        }

    }

    private static ProtectedCuboidRegion setRegionFlags(CommandSender sender, IslandInfo islandConfig) throws InvalidFlagFormat {
        String regionName = islandConfig.getName() + "island";
        Location islandLocation = islandConfig.getIslandLocation();
        ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionName,
                getProtectionVectorLeft(islandLocation),
                getProtectionVectorRight(islandLocation));
        final DefaultDomain owners = new DefaultDomain();
        for (String member : islandConfig.getMembers()) {
            owners.addPlayer(member);
        }
        region.setOwners(owners);
        region.setPriority(100);
        region.setFlag(DefaultFlag.GREET_MESSAGE,
                DefaultFlag.GREET_MESSAGE.parseInput(getWorldGuard(), sender, "\u00a7d** You are entering \u00a7b" + islandConfig.getLeader() + "'s \u00a7disland."));
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE,
                DefaultFlag.FAREWELL_MESSAGE.parseInput(getWorldGuard(), sender, "\u00a7d** You are leaving \u00a7b" + islandConfig.getLeader() + "'s \u00a7disland."));
        setVersionSpecificFlags(region);
        if (Settings.island_allowPvP) {
            region.setFlag(DefaultFlag.PVP, StateFlag.State.ALLOW);
        } else {
            region.setFlag(DefaultFlag.PVP, StateFlag.State.DENY);
        }
        if (islandConfig.isLocked()) {
            region.setFlag(DefaultFlag.ENTRY, StateFlag.State.DENY);
        } else {
            region.setFlag(DefaultFlag.ENTRY, StateFlag.State.ALLOW);
        }
        return region;
    }

    private static void setVersionSpecificFlags(ProtectedCuboidRegion region) {
        WorldGuardPlugin worldGuard = getWorldGuard();
        if (worldGuard != null && worldGuard.isEnabled() && worldGuard.getDescription() != null) {
            VersionUtil.Version wgVersion = VersionUtil.getVersion(worldGuard.getDescription().getVersion());
            if (wgVersion.isGTE("6.0")) {
                // Default values sort of bring us there... niiiiice
            } else {
                // 5.9 or below
                region.setFlag(DefaultFlag.ENTITY_ITEM_FRAME_DESTROY, StateFlag.State.DENY);
                region.setFlag(DefaultFlag.ENTITY_PAINTING_DESTROY, StateFlag.State.DENY);
                region.setFlag(DefaultFlag.CHEST_ACCESS, StateFlag.State.DENY);
                region.setFlag(DefaultFlag.USE, StateFlag.State.DENY);
                region.setFlag(DefaultFlag.DESTROY_VEHICLE, StateFlag.State.DENY);
            }
        }
    }

    private static boolean noOrOldRegion(RegionManager regionManager, String regionId, IslandInfo island) {
        if (!regionManager.hasRegion(regionId)) {
            return true;
        }
        if (regionManager.getRegion(regionId).getOwners().size() == 0) {
            return true;
        }
        return island.getRegionVersion() < VERSION;
    }

    public static void islandLock(final CommandSender sender, final String islandName) {
        try {
            RegionManager regionManager = getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld());
            if (regionManager.hasRegion(islandName + "island")) {
                ProtectedRegion region = regionManager.getRegion(islandName + "island");
                region.setFlag(DefaultFlag.ENTRY, StateFlag.State.DENY);
                sender.sendMessage(tr("\u00a7eYour island is now locked. Only your party members may enter."));
                save(regionManager);
            } else {
                sender.sendMessage(tr("\u00a74You must be the party leader to lock your island!"));
            }
        } catch (Exception ex) {
            uSkyBlock.getInstance().log(Level.SEVERE, "ERROR: Failed to lock " + islandName + "'s Island (" + sender.getName() + ")", ex);
        }
    }

    private static void save(final RegionManager regionManager) {
        if (saveTask != null) {
            saveTask.cancel();
            saveTask = null;
        }
        saveTask = Bukkit.getScheduler().runTaskLaterAsynchronously(uSkyBlock.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    // Shouldn't be needed in WG 6 - seems to be in WG 5.
                    regionManager.save();
                } catch (Exception e) {
                    log.log(Level.WARNING, "Unable to save regions", e);
                }
            }
        }, 100); // Save in 5 secs.
    }

    public static void islandUnlock(final CommandSender sender, final String islandName) {
        try {
            RegionManager regionManager = getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld());
            if (regionManager.hasRegion(islandName + "island")) {
                ProtectedRegion region = regionManager.getRegion(islandName + "island");
                region.setFlag(DefaultFlag.ENTRY, StateFlag.State.ALLOW);
                sender.sendMessage(tr("\u00a7eYour island is unlocked and anyone may enter, however only you and your party members may build or remove blocks."));
                save(regionManager);
            } else {
                sender.sendMessage(tr("\u00a74You must be the party leader to unlock your island!"));
            }
        } catch (Exception ex) {
            uSkyBlock.getInstance().log(Level.SEVERE, "ERROR: Failed to unlock " + islandName + "'s Island (" + sender.getName() + ")", ex);
        }
    }

    public static BlockVector getProtectionVectorLeft(final Location island) {
        return new BlockVector(island.getX() + Settings.island_protectionRange / 2, 255.0, island.getZ() + Settings.island_protectionRange / 2);
    }

    public static BlockVector getProtectionVectorRight(final Location island) {
        return new BlockVector(island.getX() - Settings.island_protectionRange / 2, 0.0, island.getZ() - Settings.island_protectionRange / 2);
    }

    public static void removePlayerFromRegion(final String islandName, final String player) {
        RegionManager regionManager = getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld());
        try {
            if (regionManager.hasRegion(islandName + "island")) {
                ProtectedRegion region = regionManager.getRegion(islandName + "island");
                final DefaultDomain owners = region.getOwners();
                owners.removePlayer(player);
                if (owners.size() == 0) {
                    region.setFlag(DefaultFlag.GREET_MESSAGE, DefaultFlag.GREET_MESSAGE.parseInput(getWorldGuard(), Bukkit.getConsoleSender(), "\u00a74** You are entering a protected - but abandoned - island area."));
                    region.setFlag(DefaultFlag.FAREWELL_MESSAGE, DefaultFlag.FAREWELL_MESSAGE.parseInput(getWorldGuard(), Bukkit.getConsoleSender(), "\u00a74** You are leaving an abandoned island."));
                }
                region.setOwners(owners);
                regionManager.addRegion(region);
                save(regionManager);
            }
        } catch (Exception e) {
            uSkyBlock.getInstance().log(Level.WARNING, "Error saving island region after removal of " + player);
        }
    }

    public static void addPlayerToOldRegion(final String islandName, final String player) {
        if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(islandName + "island")) {
            final DefaultDomain owners = getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(islandName + "island").getOwners();
            owners.addPlayer(player);
            getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(islandName + "island").setOwners(owners);
        }
    }

    public static String getIslandNameAt(Location location) {
        WorldGuardPlugin worldGuard = getWorldGuard();
        RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
        Iterable<ProtectedRegion> applicableRegions = regionManager.getApplicableRegions(location);
        for (ProtectedRegion region : applicableRegions) {
            String id = region.getId().toLowerCase();
            if (!id.equalsIgnoreCase("__global__") && id.endsWith("island")) {
                return id.substring(0, id.length() - 6);
            }
        }
        return null;
    }

    public static ProtectedRegion getIslandRegionAt(Location location) {
        WorldGuardPlugin worldGuard = getWorldGuard();
        RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
        Iterable<ProtectedRegion> applicableRegions = regionManager.getApplicableRegions(location);
        for (ProtectedRegion region : applicableRegions) {
            String id = region.getId().toLowerCase();
            if (!id.equalsIgnoreCase("__global__") && id.endsWith("island")) {
                return region;
            }
        }
        return null;
    }

    public static void removeIslandRegion(String islandName) {
        RegionManager regionManager = getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld());
        regionManager.removeRegion(islandName + "island");
    }

    public static void setupGlobal(World world) {
        RegionManager regionManager = getWorldGuard().getRegionManager(world);
        if (regionManager != null) {
            ProtectedRegion global = regionManager.getRegion("__global__");
            if (global == null) {
                global = new GlobalProtectedRegion("__global__");
            }
            global.setFlag(DefaultFlag.BUILD, StateFlag.State.DENY);
            regionManager.addRegion(global);
            save(regionManager);
        }
    }

    private static Set<ProtectedRegion> getRegions(ApplicableRegionSet set) {
        Set<ProtectedRegion> regions = new HashSet<>();
        for (ProtectedRegion region : set) {
            regions.add(region);
        }
        return regions;
    }

    public static Set<ProtectedRegion> getIntersectingRegions(Location islandLocation) {
        log.entering(CN, "getIntersectingRegions", islandLocation);
        RegionManager regionManager = getWorldGuard().getRegionManager(islandLocation.getWorld());
        ApplicableRegionSet applicableRegions = regionManager.getApplicableRegions(getIslandRegion(islandLocation));
        Set<ProtectedRegion> regions = getRegions(applicableRegions);
        for (Iterator<ProtectedRegion> iterator = regions.iterator(); iterator.hasNext(); ) {
            if (iterator.next() instanceof GlobalProtectedRegion) {
                iterator.remove();
            }
        }
        log.exiting(CN, "getIntersectingRegions");
        return regions;
    }

    public static boolean isIslandIntersectingSpawn(Location islandLocation) {
        log.entering(CN, "isIslandIntersectingSpawn", islandLocation);
        try {
            int r = Settings.general_spawnSize;
            if (r == 0) {
                return false;
            }
            ProtectedRegion spawn = new ProtectedCuboidRegion("spawn", new BlockVector(-r, 0, -r), new BlockVector(r, 255, r));
            ProtectedCuboidRegion islandRegion = getIslandRegion(islandLocation);
            return !islandRegion.getIntersectingRegions(Collections.singletonList(spawn)).isEmpty();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Unable to locate intersecting regions", e);
            return false;
        } finally {
            log.exiting(CN, "isIslandIntersectingSpawn");
        }
    }

    private static ProtectedCuboidRegion getIslandRegion(Location islandLocation) {
        int r = Settings.island_radius;
        Vector islandCenter = new Vector(islandLocation.getBlockX(), 0, islandLocation.getBlockZ());
        return new ProtectedCuboidRegion(String.format("%d,%dIsland", islandCenter.getBlockX(), islandLocation.getBlockZ()),
                new BlockVector(islandCenter.subtract(r, 0, r)),
                new BlockVector(islandCenter.add(r, 255, r)));
    }
}
