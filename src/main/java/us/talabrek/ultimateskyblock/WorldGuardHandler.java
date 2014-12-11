package us.talabrek.ultimateskyblock;

import com.sk89q.worldguard.bukkit.*;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.*;
import org.bukkit.entity.*;
import com.sk89q.worldguard.domains.*;
import org.bukkit.command.*;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.regions.*;
import com.sk89q.worldguard.protection.*;

import java.util.logging.Level;

import com.sk89q.worldedit.*;
import org.bukkit.*;

public class WorldGuardHandler {
    private static final int VERSION = 1;

    public static WorldGuardPlugin getWorldGuard() {
        final Plugin plugin = uSkyBlock.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null;
        }
        return (WorldGuardPlugin) plugin;
    }

    public static boolean protectIsland(final Player sender, final String player, final PlayerInfo pi) {
        try {
            WorldGuardPlugin worldGuard = getWorldGuard();
            RegionManager regionManager = worldGuard.getRegionManager(uSkyBlock.getSkyBlockWorld());
            String regionName = player + "Island";
            if (pi.getIslandLocation() != null && noOrOldRegion(regionManager, regionName, pi)) {
                ProtectedCuboidRegion region = new ProtectedCuboidRegion(player + "Island",
                        getProtectionVectorLeft(pi.getIslandLocation()),
                        getProtectionVectorRight(pi.getIslandLocation()));
                final DefaultDomain owners = new DefaultDomain();
                owners.addPlayer(player);
                region.setOwners(owners);
                region.setPriority(100);
                region.setFlag(DefaultFlag.GREET_MESSAGE, DefaultFlag.GREET_MESSAGE.parseInput(worldGuard, sender, "\u00a7d** You are entering a protected island area. (" + player + ")"));
                region.setFlag(DefaultFlag.FAREWELL_MESSAGE, DefaultFlag.FAREWELL_MESSAGE.parseInput(worldGuard, sender, "\u00a7d** You are leaving a protected island area. (" + player + ")"));
                if (uSkyBlock.getInstance().getConfig().getBoolean("options.island.allowPvP")) {
                    region.setFlag(DefaultFlag.PVP, StateFlag.State.ALLOW);
                } else {
                    region.setFlag(DefaultFlag.PVP, StateFlag.State.DENY);
                }
                region.setFlag(DefaultFlag.DESTROY_VEHICLE, StateFlag.State.DENY);
                region.setFlag(DefaultFlag.ENTITY_ITEM_FRAME_DESTROY, StateFlag.State.DENY);
                region.setFlag(DefaultFlag.ENTITY_PAINTING_DESTROY, StateFlag.State.DENY);

                final ApplicableRegionSet set = regionManager.getApplicableRegions(pi.getIslandLocation());
                if (set.size() > 0) {
                    for (ProtectedRegion regions : set) {
                        if (!regions.getId().equalsIgnoreCase("__global__")) {
                            regionManager.removeRegion(regions.getId());
                        }
                    }
                }
                regionManager.addRegion(region);
                System.out.print("New protected region created for " + player + "'s Island by " + sender.getName());
                regionManager.save();
                uSkyBlock.getInstance().getIslandConfig(pi).set("general.regionVersion", VERSION);
                return true;
            }
        } catch (Exception ex) {
            System.out.print("ERROR: Failed to protect " + player + "'s Island (" + sender.getName() + ")");
            ex.printStackTrace();
        }
        return false;
    }

    private static boolean noOrOldRegion(RegionManager regionManager, String regionId, PlayerInfo playerInfo) {
        if (!regionManager.hasRegion(regionId)) {
            return true;
        }
        return uSkyBlock.getInstance().getIslandConfig(playerInfo).getInt("general.regionVersion", 0) < VERSION;
    }

    public static void islandLock(final CommandSender sender, final String player) {
        try {
            if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(player + "Island")) {
                getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(player + "Island").setFlag(DefaultFlag.ENTRY, DefaultFlag.ENTRY.parseInput(getWorldGuard(), sender, "deny"));
                sender.sendMessage(ChatColor.YELLOW + "Your island is now locked. Only your party members may enter.");
                getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).save();
            } else {
                sender.sendMessage(ChatColor.RED + "You must be the party leader to lock your island!");
            }
        } catch (Exception ex) {
            System.out.print("ERROR: Failed to lock " + player + "'s Island (" + sender.getName() + ")");
            ex.printStackTrace();
        }
    }

    public static void islandUnlock(final CommandSender sender, final String player) {
        try {
            if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(player + "Island")) {
                getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(player + "Island").setFlag(DefaultFlag.ENTRY, DefaultFlag.ENTRY.parseInput(getWorldGuard(), sender, "allow"));
                sender.sendMessage(ChatColor.YELLOW + "Your island is unlocked and anyone may enter, however only you and your party members may build or remove blocks.");
                getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).save();
            } else {
                sender.sendMessage(ChatColor.RED + "You must be the party leader to unlock your island!");
            }
        } catch (Exception ex) {
            System.out.print("ERROR: Failed to unlock " + player + "'s Island (" + sender.getName() + ")");
            ex.printStackTrace();
        }
    }

    public static BlockVector getProtectionVectorLeft(final Location island) {
        return new BlockVector(island.getX() + Settings.island_protectionRange / 2, 255.0, island.getZ() + Settings.island_protectionRange / 2);
    }

    public static BlockVector getProtectionVectorRight(final Location island) {
        return new BlockVector(island.getX() - Settings.island_protectionRange / 2, 0.0, island.getZ() - Settings.island_protectionRange / 2);
    }

    public static void removePlayerFromRegion(final String owner, final String player) {
        if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(owner + "Island")) {
            final DefaultDomain owners = getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(owner + "Island").getOwners();
            owners.removePlayer(player);
            getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(owner + "Island").setOwners(owners);
        }
    }

    public static void addPlayerToOldRegion(final String owner, final String player) {
        if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(owner + "Island")) {
            final DefaultDomain owners = getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(owner + "Island").getOwners();
            owners.addPlayer(player);
            getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(owner + "Island").setOwners(owners);
        }
    }

    public static void resetPlayerRegion(final String owner) {
        if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(owner + "Island")) {
            final DefaultDomain owners = new DefaultDomain();
            owners.addPlayer(owner);
            getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(owner + "Island").setOwners(owners);
        }
    }

    public static void transferRegion(final String owner, final String player, final CommandSender sender) {
        try {
            ProtectedRegion region2 = null;
            region2 = new ProtectedCuboidRegion(player + "Island", getWorldGuard().getRegionManager(Bukkit.getWorld("skyworld")).getRegion(owner + "Island").getMinimumPoint(), getWorldGuard().getRegionManager(Bukkit.getWorld(Settings.general_worldName)).getRegion(owner + "Island").getMaximumPoint());
            region2.setOwners(getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(owner + "Island").getOwners());
            region2.setParent(getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion("__Global__"));
            region2.setFlag(DefaultFlag.GREET_MESSAGE, DefaultFlag.GREET_MESSAGE.parseInput(getWorldGuard(), sender, "\u00a7d** You are entering a protected island area. (" + player + ")"));
            region2.setFlag(DefaultFlag.FAREWELL_MESSAGE, DefaultFlag.FAREWELL_MESSAGE.parseInput(getWorldGuard(), sender, "\u00a7d** You are leaving a protected island area. (" + player + ")"));
            region2.setFlag(DefaultFlag.PVP, DefaultFlag.PVP.parseInput(getWorldGuard(), sender, "deny"));
            region2.setFlag(DefaultFlag.DESTROY_VEHICLE, DefaultFlag.DESTROY_VEHICLE.parseInput(getWorldGuard(), sender, "deny"));
            region2.setFlag(DefaultFlag.ENTITY_ITEM_FRAME_DESTROY, DefaultFlag.ENTITY_ITEM_FRAME_DESTROY.parseInput(getWorldGuard(), sender, "deny"));
            region2.setFlag(DefaultFlag.ENTITY_PAINTING_DESTROY, DefaultFlag.ENTITY_PAINTING_DESTROY.parseInput(getWorldGuard(), sender, "deny"));
            getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).removeRegion(owner + "Island");
            getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).addRegion(region2);
        } catch (Exception e) {
            uSkyBlock.log(Level.INFO, "Error transferring WorldGuard Protected Region from (" + owner + ") to (" + player + ")");
        }
    }
}
