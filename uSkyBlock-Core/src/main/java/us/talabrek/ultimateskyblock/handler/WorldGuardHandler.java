package us.talabrek.ultimateskyblock.handler;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.lockfuglsang.minecraft.po.I18nUtil;
import dk.lockfuglsang.minecraft.util.VersionUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public class WorldGuardHandler {
    private static final String CN = WorldGuardHandler.class.getName();
    private static final Logger log = Logger.getLogger(CN);
    private static final String VERSION = "14";

    public static WorldGuardPlatform getWorldGuard() {
        final Plugin plugin = uSkyBlock.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null;
        }
        return WorldGuard.getInstance().getPlatform();
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
                updateRegion(islandConfig);
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
            RegionManager regionManager = getRegionManager(plugin.getWorld());
            String regionName = islandConfig.getName() + "island";
            if (islandConfig != null && noOrOldRegion(regionManager, regionName, islandConfig)) {
                updateRegion(islandConfig);
                islandConfig.setRegionVersion(getVersion());
                return true;
            }
        } catch (Exception ex) {
            String name = islandConfig != null ? islandConfig.getLeader() : "Unknown";
            LogUtil.log(Level.SEVERE, "ERROR: Failed to protect " + name + "'s Island (" + sender.getName() + ")", ex);
        }
        return false;
    }

    private static String getVersion() {
        return VERSION + " " + I18nUtil.getLocale();
    }

    public static void updateRegion(IslandInfo islandInfo) {
        try {
            ProtectedCuboidRegion region = setRegionFlags(islandInfo);
            RegionManager regionManager = getRegionManager(uSkyBlock.getInstance().getWorld());
            regionManager.removeRegion(islandInfo.getName() + "island");
            regionManager.removeRegion(islandInfo.getLeader() + "island");
            regionManager.addRegion(region);
            String netherName = islandInfo.getName() + "nether";
            region = setRegionFlags(islandInfo, netherName);
            World netherWorld = uSkyBlock.getInstance().getSkyBlockNetherWorld();
            if (netherWorld != null) {
                regionManager = getRegionManager(netherWorld);
                regionManager.removeRegion(netherName);
                regionManager.addRegion(region);
            }
            islandInfo.setRegionVersion(getVersion());
        } catch (Exception e) {
            LogUtil.log(Level.SEVERE, "ERROR: Failed to update region for " + islandInfo.getName(), e);
        }
    }

    private static RegionManager getRegionManager(World world) {
        return getWorldGuard().getRegionContainer().get(new BukkitWorld(world));
    }

    private static ProtectedCuboidRegion setRegionFlags(IslandInfo islandConfig) throws InvalidFlagFormat {
        String regionName = islandConfig.getName() + "island";
        return setRegionFlags(islandConfig, regionName);
    }

    private static ProtectedCuboidRegion setRegionFlags(IslandInfo islandConfig, String regionName) throws InvalidFlagFormat {
        Location islandLocation = islandConfig.getIslandLocation();
        BlockVector minPoint = getProtectionVectorRight(islandLocation);
        BlockVector maxPoint = getProtectionVectorLeft(islandLocation);
        if (regionName != null && regionName.endsWith("nether")) {
            minPoint = new BlockVector(minPoint.setY(6));
            maxPoint = new BlockVector(maxPoint.setY(119));
        }
        ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionName, minPoint, maxPoint);
        final DefaultDomain owners = new DefaultDomain();
        DefaultDomain members = new DefaultDomain();
        for (UUID member : islandConfig.getMemberUUIDs()) {
            owners.addPlayer(member);
        }
        for (UUID trust : islandConfig.getTrusteeUUIDs()) {
            members.addPlayer(trust);
        }
        region.setOwners(owners);
        region.setMembers(members);
        region.setPriority(100);
        if (uSkyBlock.getInstance().getConfig().getBoolean("worldguard.entry-message", true)) {
            if (owners.size() == 0) {
                region.setFlag(Flags.GREET_MESSAGE, tr("\u00a74** You are entering a protected - but abandoned - island area."));
            } else {
                region.setFlag(Flags.GREET_MESSAGE, tr("\u00a7d** You are entering \u00a7b{0}''s \u00a7disland.", islandConfig.getLeader()));
            }
        } else {
            region.setFlag(Flags.GREET_MESSAGE, null);
        }
        if (uSkyBlock.getInstance().getConfig().getBoolean("worldguard.exit-message", true)) {
            if (owners.size() == 0) {
                region.setFlag(Flags.FAREWELL_MESSAGE, tr("\u00a74** You are leaving an abandoned island."));
            } else {
                region.setFlag(Flags.FAREWELL_MESSAGE, tr("\u00a7d** You are leaving \u00a7b{0}''s \u00a7disland.", islandConfig.getLeader()));
            }
        } else {
            region.setFlag(Flags.FAREWELL_MESSAGE, null);
        }
        setVersionSpecificFlags(region);
        region.setFlag(Flags.PVP, null);
        boolean isLocked = islandConfig.isLocked();
        updateLockStatus(region, isLocked);
        return region;
    }

    private static void updateLockStatus(ProtectedRegion region, boolean isLocked) {
        if (isLocked) {
            region.setFlag(Flags.ENTRY, StateFlag.State.DENY);
        } else {
            region.setFlag(Flags.ENTRY, null);
        }
    }

    private static void setVersionSpecificFlags(ProtectedCuboidRegion region) {
        Plugin worldGuard = uSkyBlock.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuard != null && worldGuard.isEnabled() && worldGuard.getDescription() != null) {
            VersionUtil.Version wgVersion = VersionUtil.getVersion(worldGuard.getDescription().getVersion());
            if (wgVersion.isGTE("6.0")) {
                // Default values sort of bring us there... niiiiice
            } else {
                // 5.9 or below
                region.setFlag(Flags.ENTITY_ITEM_FRAME_DESTROY, StateFlag.State.DENY);
                region.setFlag(Flags.ENTITY_PAINTING_DESTROY, StateFlag.State.DENY);
                region.setFlag(Flags.CHEST_ACCESS, StateFlag.State.DENY);
                region.setFlag(Flags.USE, StateFlag.State.DENY);
                region.setFlag(Flags.DESTROY_VEHICLE, StateFlag.State.DENY);
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
        return !island.getRegionVersion().equals(getVersion());
    }

    public static void islandLock(final CommandSender sender, final String islandName) {
        try {
            RegionManager regionManager = getRegionManager(uSkyBlock.getSkyBlockWorld());
            if (regionManager.hasRegion(islandName + "island")) {
                ProtectedRegion region = regionManager.getRegion(islandName + "island");
                updateLockStatus(region, true);
                sender.sendMessage(tr("\u00a7eYour island is now locked. Only your party members may enter."));
            } else {
                sender.sendMessage(tr("\u00a74You must be the party leader to lock your island!"));
            }
        } catch (Exception ex) {
            LogUtil.log(Level.SEVERE, "ERROR: Failed to lock " + islandName + "'s Island (" + sender.getName() + ")", ex);
        }
    }

    public static void islandUnlock(final CommandSender sender, final String islandName) {
        try {
            RegionManager regionManager = getRegionManager(uSkyBlock.getSkyBlockWorld());
            if (regionManager.hasRegion(islandName + "island")) {
                ProtectedRegion region = regionManager.getRegion(islandName + "island");
                updateLockStatus(region, false);
                sender.sendMessage(tr("\u00a7eYour island is unlocked and anyone may enter, however only you and your party members may build or remove blocks."));
            } else {
                sender.sendMessage(tr("\u00a74You must be the party leader to unlock your island!"));
            }
        } catch (Exception ex) {
            LogUtil.log(Level.SEVERE, "ERROR: Failed to unlock " + islandName + "'s Island (" + sender.getName() + ")", ex);
        }
    }

    public static BlockVector getProtectionVectorLeft(final Location island) {
        return new BlockVector(island.getX() + Settings.island_radius - 1, 255.0, island.getZ() + Settings.island_radius - 1);
    }

    public static BlockVector getProtectionVectorRight(final Location island) {
        return new BlockVector(island.getX() - Settings.island_radius, 0.0, island.getZ() - Settings.island_radius);
    }

    public static String getIslandNameAt(Location location) {
        RegionManager regionManager = getRegionManager(location.getWorld());
        Iterable<ProtectedRegion> applicableRegions = regionManager.getApplicableRegions(toVector(location));
        for (ProtectedRegion region : applicableRegions) {
            String id = region.getId().toLowerCase();
            if (!id.equalsIgnoreCase("__global__") && (id.endsWith("island") || id.endsWith("nether"))) {
                return id.substring(0, id.length() - 6);
            }
        }
        return null;
    }

    public static ProtectedRegion getIslandRegionAt(Location location) {
        RegionManager regionManager = getRegionManager(location.getWorld());
        if (regionManager == null) {
            return null;
        }
        Iterable<ProtectedRegion> applicableRegions = regionManager.getApplicableRegions(toVector(location));
        for (ProtectedRegion region : applicableRegions) {
            String id = region.getId().toLowerCase();
            if (!id.equalsIgnoreCase("__global__") && (id.endsWith("island") || id.endsWith("nether"))) {
                return region;
            }
        }
        return null;
    }

    private static Vector toVector(Location location) {
        return new Vector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static ProtectedRegion getNetherRegionAt(Location location) {
        if (!Settings.nether_enabled || location == null) {
            return null;
        }
        RegionManager regionManager = getRegionManager(location.getWorld());
        if (regionManager == null) {
            return null;
        }
        Iterable<ProtectedRegion> applicableRegions = regionManager.getApplicableRegions(toVector(location));
        for (ProtectedRegion region : applicableRegions) {
            String id = region.getId().toLowerCase();
            if (!id.equalsIgnoreCase("__global__") && id.endsWith("nether")) {
                return region;
            }
        }
        return null;
    }

    public static void removeIslandRegion(String islandName) {
        RegionManager regionManager = getRegionManager(uSkyBlock.getSkyBlockWorld());
        regionManager.removeRegion(islandName + "island");
        regionManager.removeRegion(islandName + "nether");
    }

    public static void setupGlobal(World world) {
        RegionManager regionManager = getRegionManager(world);
        if (regionManager != null) {
            ProtectedRegion global = regionManager.getRegion("__global__");
            if (global == null) {
                global = new GlobalProtectedRegion("__global__");
            }
            global.setFlag(Flags.BUILD, StateFlag.State.DENY);
            if (Settings.island_allowPvP) {
                global.setFlag(Flags.PVP, StateFlag.State.ALLOW);
            } else {
                global.setFlag(Flags.PVP, StateFlag.State.DENY);
            }
            regionManager.addRegion(global);
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
        RegionManager regionManager = getRegionManager(islandLocation.getWorld());
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

    public static ProtectedCuboidRegion getIslandRegion(Location islandLocation) {
        int r = Settings.island_radius;
        Vector islandCenter = new Vector(islandLocation.getBlockX(), 0, islandLocation.getBlockZ());
        return new ProtectedCuboidRegion(
                String.format("%d,%disland", islandCenter.getBlockX(), islandLocation.getBlockZ()),
                getProtectionVectorLeft(islandLocation),
                getProtectionVectorRight(islandLocation));
    }

    public static List<Player> getPlayersInRegion(World world, ProtectedRegion region) {
        // Note: This might be heavy - for large servers...
        List<Player> players = new ArrayList<>();
        if (region == null) {
            return players;
        }
        for (Player player : world.getPlayers()) {
            if (player != null && player.isOnline()) {
                Location p = player.getLocation();
                if (region.contains(p.getBlockX(), p.getBlockY(), p.getBlockZ())) {
                    players.add(player);
                }
            }
        }
        return players;
    }

    public static List<LivingEntity> getCreaturesInRegion(World world, ProtectedRegion region) {
        List<LivingEntity> livingEntities = world.getLivingEntities();
        List<LivingEntity> creatures = new ArrayList<>();
        for (LivingEntity e : livingEntities) {
            if (region.contains(asVector(e.getLocation()))) {
                creatures.add(e);
            }
        }
        return creatures;
    }

    private static Vector asVector(Location location) {
        if (location == null) {
            return new Vector(0, 0, 0);
        }
        return toVector(location);
    }
}
