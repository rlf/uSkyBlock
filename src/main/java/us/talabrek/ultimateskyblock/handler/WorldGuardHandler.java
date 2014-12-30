package us.talabrek.ultimateskyblock.handler;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.logging.Level;

public class WorldGuardHandler {
    private static final int VERSION = 3;

    public static WorldGuardPlugin getWorldGuard() {
        final Plugin plugin = uSkyBlock.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null;
        }
        return (WorldGuardPlugin) plugin;
    }

    public static boolean protectIsland(final CommandSender sender, final PlayerInfo pi) {
        uSkyBlock plugin = uSkyBlock.getInstance();
        try {
            WorldGuardPlugin worldGuard = getWorldGuard();
            RegionManager regionManager = worldGuard.getRegionManager(uSkyBlock.getSkyBlockWorld());
            IslandInfo islandConfig = plugin.getIslandInfo(pi);
            String regionName = islandConfig.getName() + "island";
            if (pi.getIslandLocation() != null && noOrOldRegion(regionManager, regionName, islandConfig)) {
                ProtectedCuboidRegion region = setRegionFlags(sender, islandConfig);
                final ApplicableRegionSet set = regionManager.getApplicableRegions(pi.getIslandLocation());
                if (set.size() > 0) {
                    for (ProtectedRegion regions : set) {
                        if (!regions.getId().equalsIgnoreCase("__global__")) {
                            regionManager.removeRegion(regions.getId());
                        }
                    }
                }
                regionManager.addRegion(region);
                plugin.log(Level.INFO, "New protected region created for " + pi.getPlayerName() + "'s Island by " + sender.getName());
                regionManager.save();
                islandConfig.setRegionVersion(VERSION);
                return true;
            }
        } catch (Exception ex) {
            plugin.log(Level.SEVERE, "ERROR: Failed to protect " + pi.getPlayerName() + "'s Island (" + sender.getName() + ")", ex);
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
            regionManager.save();
        } catch (StorageException|InvalidFlagFormat e) {
            uSkyBlock.getInstance().log(Level.SEVERE, "ERROR: Failed to update region for " + islandInfo.getName(), e);
        }

    }
    private static ProtectedCuboidRegion setRegionFlags(CommandSender sender, IslandInfo islandConfig) throws InvalidFlagFormat {
        String regionName = islandConfig.getName() + "island";
        uSkyBlock plugin = uSkyBlock.getInstance();
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
        region.setFlag(DefaultFlag.GREET_MESSAGE, DefaultFlag.GREET_MESSAGE.parseInput(getWorldGuard(), sender, "\u00a7d** You are entering a protected island area. (" + islandConfig.getLeader() + ")"));
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, DefaultFlag.FAREWELL_MESSAGE.parseInput(getWorldGuard(), sender, "\u00a7d** You are leaving a protected island area. (" + islandConfig.getLeader() + ")"));
        if (plugin.getConfig().getBoolean("options.island.allowPvP")) {
            region.setFlag(DefaultFlag.PVP, StateFlag.State.ALLOW);
        } else {
            region.setFlag(DefaultFlag.PVP, StateFlag.State.DENY);
        }
        if (islandConfig.isLocked()) {
            region.setFlag(DefaultFlag.ENTRY, StateFlag.State.DENY);
        } else {
            region.setFlag(DefaultFlag.ENTRY, StateFlag.State.ALLOW);
        }
        region.setFlag(DefaultFlag.DESTROY_VEHICLE, StateFlag.State.DENY);
        region.setFlag(DefaultFlag.ENTITY_ITEM_FRAME_DESTROY, StateFlag.State.DENY);
        region.setFlag(DefaultFlag.ENTITY_PAINTING_DESTROY, StateFlag.State.DENY);
        return region;
    }

    private static boolean noOrOldRegion(RegionManager regionManager, String regionId, IslandInfo island) {
        if (!regionManager.hasRegion(regionId)) {
            return true;
        }
        return island.getRegionVersion() < VERSION;
    }

    public static void islandLock(final CommandSender sender, final String islandName) {
        try {
            if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(islandName + "island")) {
                getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(islandName + "island").setFlag(DefaultFlag.ENTRY, DefaultFlag.ENTRY.parseInput(getWorldGuard(), sender, "deny"));
                sender.sendMessage("\u00a7eYour island is now locked. Only your party members may enter.");
                getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).save();
            } else {
                sender.sendMessage("\u00a74You must be the party leader to lock your island!");
            }
        } catch (Exception ex) {
            uSkyBlock.getInstance().log(Level.SEVERE, "ERROR: Failed to lock " + islandName + "'s Island (" + sender.getName() + ")", ex);
        }
    }

    public static void islandUnlock(final CommandSender sender, final String islandName) {
        try {
            if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(islandName + "island")) {
                getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(islandName + "island").setFlag(DefaultFlag.ENTRY, DefaultFlag.ENTRY.parseInput(getWorldGuard(), sender, "allow"));
                sender.sendMessage("\u00a7eYour island is unlocked and anyone may enter, however only you and your party members may build or remove blocks.");
                getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).save();
            } else {
                sender.sendMessage("\u00a74You must be the party leader to unlock your island!");
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
        if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(islandName + "island")) {
            final DefaultDomain owners = getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(islandName + "island").getOwners();
            owners.removePlayer(player);
            getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(islandName + "island").setOwners(owners);
        }
    }

    public static void addPlayerToOldRegion(final String islandName, final String player) {
        if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(islandName + "island")) {
            final DefaultDomain owners = getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(islandName + "island").getOwners();
            owners.addPlayer(player);
            getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(islandName + "island").setOwners(owners);
        }
    }

    public static String getRegionAt(Location location) {
        WorldGuardPlugin worldGuard = getWorldGuard();
        RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
        ApplicableRegionSet applicableRegions = regionManager.getApplicableRegions(location);
        for (ProtectedRegion region : applicableRegions) {
            String id = region.getId().toLowerCase();
            if (!id.equalsIgnoreCase("__global__") && id.endsWith("island")) {
                return id.substring(0, id.length()-6);
            }
        }
        return null;
    }

    public static void removeIslandRegion(String islandName) {
        RegionManager regionManager = getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld());
        regionManager.removeRegion(islandName + "island");
    }
}
