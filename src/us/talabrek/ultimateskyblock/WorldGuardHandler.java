package us.talabrek.ultimateskyblock;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardHandler {
	public static void addPlayerToOldRegion(final String owner, final String player) {
		if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(owner + "Island")) {
			final DefaultDomain owners = getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(owner + "Island")
					.getOwners();
			owners.addPlayer(player);
			getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(owner + "Island").setOwners(owners);
		}
	}

	public static BlockVector getProtectionVectorLeft(final Location island) {
		return new BlockVector(island.getX() + Settings.island_protectionRange / 2, 255.0D, island.getZ() + Settings.island_protectionRange
				/ 2);
	}

	public static BlockVector getProtectionVectorRight(final Location island) {
		return new BlockVector(island.getX() - Settings.island_protectionRange / 2, 0.0D, island.getZ() - Settings.island_protectionRange
				/ 2);
	}

	public static WorldGuardPlugin getWorldGuard() {
		final Plugin plugin = uSkyBlock.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");

		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) { return null; }

		return (WorldGuardPlugin) plugin;
	}

	public static void islandLock(final CommandSender sender, final String player) {
		try {
			if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(player + "Island")) {
				getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(player + "Island")
						.setFlag(DefaultFlag.ENTRY, DefaultFlag.ENTRY.parseInput(getWorldGuard(), sender, "deny"));
				sender.sendMessage(ChatColor.YELLOW + "Your island is now locked. Only your party members may enter.");
				getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).save();
			} else {
				sender.sendMessage(ChatColor.RED + "You must be the party leader to lock your island!");
			}
		} catch (final Exception ex) {
			System.out.println("uSkyblock " + "ERROR: Failed to lock " + player + "'s Island (" + sender.getName() + ")");
			ex.printStackTrace();
		}
	}

	public static void islandUnlock(final CommandSender sender, final String player) {
		try {
			if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(player + "Island")) {
				getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(player + "Island")
						.setFlag(DefaultFlag.ENTRY, DefaultFlag.ENTRY.parseInput(getWorldGuard(), sender, "allow"));
				sender.sendMessage(ChatColor.YELLOW
						+ "Your island is unlocked and anyone may enter, however only you and your party members may build or remove blocks.");
				getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).save();
			} else {
				sender.sendMessage(ChatColor.RED + "You must be the party leader to unlock your island!");
			}
		} catch (final Exception ex) {
			System.out.println("uSkyblock " + "ERROR: Failed to unlock " + player + "'s Island (" + sender.getName() + ")");
			ex.printStackTrace();
		}
	}

	public static void protectAllIslands(final CommandSender sender) {
		String player = "";
		int checkislands = 0;
		try {
			if (Settings.island_protectWithWorldGuard) {
				final Player[] players = Bukkit.getServer().getOnlinePlayers();
				ProtectedRegion region;
				for (final Player playerx : players) {
					player = playerx.getName();
					if (uSkyBlock.getInstance().readPlayerFile(player).getIslandLocation() != null
							&& !getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(player + "Island")) {
						region = null;
						final DefaultDomain owners = new DefaultDomain();
						region = new ProtectedCuboidRegion(player + "Island", getProtectionVectorLeft(uSkyBlock.getInstance()
								.readPlayerFile(player).getIslandLocation()), getProtectionVectorRight(uSkyBlock.getInstance()
								.readPlayerFile(player).getIslandLocation()));
						owners.addPlayer(player);
						if (uSkyBlock.getInstance().hasParty(player)) {
							final PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(player);
							final List<?> members = pi.getMembers();
							if (!members.isEmpty()) {
								final Iterator<?> memlist = members.iterator();
								while (memlist.hasNext()) {
									owners.addPlayer((String) memlist.next());
								}
							}
						}
						region.setOwners(owners);
						region.setParent(getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion("__Global__"));
						region.setPriority(100);
						region.setFlag(
								DefaultFlag.GREET_MESSAGE,
								DefaultFlag.GREET_MESSAGE.parseInput(getWorldGuard(), sender, "You are entering a protected island area. ("
										+ player + ")"));
						region.setFlag(DefaultFlag.FAREWELL_MESSAGE, DefaultFlag.FAREWELL_MESSAGE.parseInput(getWorldGuard(), sender,
								"You are leaving a protected island area. (" + player + ")"));
						region.setFlag(DefaultFlag.PVP, DefaultFlag.PVP.parseInput(getWorldGuard(), sender, Settings.island_allowPvP));
						region.setFlag(DefaultFlag.CHEST_ACCESS, DefaultFlag.CHEST_ACCESS.parseInput(getWorldGuard(), sender, "deny"));
						region.setFlag(DefaultFlag.USE, DefaultFlag.USE.parseInput(getWorldGuard(), sender, "deny"));
						final ApplicableRegionSet set = getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld())
								.getApplicableRegions(uSkyBlock.getInstance().readPlayerFile(player).getIslandLocation());
						if (set.size() > 0) {
							for (final ProtectedRegion regions : set) {
								if (!regions.getId().equalsIgnoreCase("__global__")) {
									getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).removeRegion(regions.getId());
								}
							}
						}
						getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).addRegion(region);
						System.out
								.println("uSkyblock " + "New protected region created for " + player + "'s Island by " + sender.getName());
						checkislands++;
					}
				}
				final OfflinePlayer[] players2 = Bukkit.getServer().getOfflinePlayers();
				for (final OfflinePlayer playerx : players2) {
					player = playerx.getName();
					if (uSkyBlock.getInstance().readPlayerFile(player) == null) {
						System.out.println("uSkyblock " + player + " does not have an island file!");
					} else if (uSkyBlock.getInstance().readPlayerFile(player).getIslandLocation() != null
							&& !getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(player + "Island")) {
						region = null;
						final DefaultDomain owners = new DefaultDomain();
						region = new ProtectedCuboidRegion(player + "Island", getProtectionVectorLeft(uSkyBlock.getInstance()
								.readPlayerFile(player).getIslandLocation()), getProtectionVectorRight(uSkyBlock.getInstance()
								.readPlayerFile(player).getIslandLocation()));
						owners.addPlayer(player);
						if (uSkyBlock.getInstance().hasParty(player)) {
							final PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(player);
							final List<String> members = pi.getMembers();
							if (!members.isEmpty()) {
								final Iterator<String> memlist = members.iterator();
								while (memlist.hasNext()) {
									owners.addPlayer(memlist.next());
								}
							}
						}
						region.setOwners(owners);
						region.setParent(getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion("__Global__"));
						region.setFlag(
								DefaultFlag.GREET_MESSAGE,
								DefaultFlag.GREET_MESSAGE.parseInput(getWorldGuard(), sender, "You are entering a protected island area. ("
										+ player + ")"));
						region.setFlag(DefaultFlag.FAREWELL_MESSAGE, DefaultFlag.FAREWELL_MESSAGE.parseInput(getWorldGuard(), sender,
								"You are leaving a protected island area. (" + player + ")"));
						region.setFlag(DefaultFlag.PVP, DefaultFlag.PVP.parseInput(getWorldGuard(), sender, Settings.island_allowPvP));
						region.setFlag(DefaultFlag.CHEST_ACCESS, DefaultFlag.CHEST_ACCESS.parseInput(getWorldGuard(), sender, "deny"));
						region.setFlag(DefaultFlag.USE, DefaultFlag.USE.parseInput(getWorldGuard(), sender, "deny"));
						final ApplicableRegionSet set = getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld())
								.getApplicableRegions(uSkyBlock.getInstance().readPlayerFile(player).getIslandLocation());
						if (set.size() > 0) {
							for (final ProtectedRegion regions : set) {
								if (!regions.getId().equalsIgnoreCase("__global__")) {
									getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).removeRegion(regions.getId());
								}
							}
						}
						getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).addRegion(region);
						System.out
								.println("uSkyblock " + "New protected region created for " + player + "'s Island by " + sender.getName());
						checkislands++;
					}
				}
				System.out.println("uSkyblock " + "Protected " + checkislands + " islands.");
				getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).save();
			}
		} catch (final Exception ex) {
			System.out.println("uSkyblock " + "ERROR: Failed to protect " + player + "'s Island (" + sender.getName() + ")");
			ex.printStackTrace();
		}
	}

	public static void protectIsland(final CommandSender sender, final String player) {
		try {
			if (Settings.island_protectWithWorldGuard) {
				if (uSkyBlock.getInstance().readPlayerFile(player).getIslandLocation() != null
						&& !getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(player + "Island")) {
					ProtectedRegion region = null;
					final DefaultDomain owners = new DefaultDomain();
					region = new ProtectedCuboidRegion(player + "Island", getProtectionVectorLeft(uSkyBlock.getInstance()
							.readPlayerFile(player).getIslandLocation()), getProtectionVectorRight(uSkyBlock.getInstance()
							.readPlayerFile(player).getIslandLocation()));
					owners.addPlayer(player);
					region.setOwners(owners);
					region.setParent(getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion("__Global__"));
					region.setPriority(100);
					region.setFlag(
							DefaultFlag.GREET_MESSAGE,
							DefaultFlag.GREET_MESSAGE.parseInput(getWorldGuard(), sender, "You are entering a protected island area. ("
									+ player + ")"));
					region.setFlag(
							DefaultFlag.FAREWELL_MESSAGE,
							DefaultFlag.FAREWELL_MESSAGE.parseInput(getWorldGuard(), sender, "You are leaving a protected island area. ("
									+ player + ")"));
					region.setFlag(DefaultFlag.PVP, DefaultFlag.PVP.parseInput(getWorldGuard(), sender, Settings.island_allowPvP));
					region.setFlag(DefaultFlag.CHEST_ACCESS, DefaultFlag.CHEST_ACCESS.parseInput(getWorldGuard(), sender, "deny"));
					region.setFlag(DefaultFlag.USE, DefaultFlag.USE.parseInput(getWorldGuard(), sender, "deny"));
					final ApplicableRegionSet set = getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getApplicableRegions(
							uSkyBlock.getInstance().readPlayerFile(player).getIslandLocation());
					if (set.size() > 0) {
						for (final ProtectedRegion regions : set) {
							if (!regions.getId().equalsIgnoreCase("__global__")) {
								getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).removeRegion(regions.getId());
							}
						}
					}
					getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).addRegion(region);
					System.out.println("uSkyblock " + "New protected region created for " + player + "'s Island by " + sender.getName());
					getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).save();
				} else {
					sender.sendMessage("Player doesn't have an island or it's already protected!");
				}
			}
		} catch (final Exception ex) {
			System.out.println("uSkyblock " + "ERROR: Failed to protect " + player + "'s Island (" + sender.getName() + ")");
			ex.printStackTrace();
		}
	}

	public static void removePlayerFromRegion(final String owner, final String player) {
		if (getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(owner + "Island")) {
			final DefaultDomain owners = getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(owner + "Island")
					.getOwners();
			owners.removePlayer(player);
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
			region2 = new ProtectedCuboidRegion(player + "Island", getWorldGuard().getRegionManager(Bukkit.getWorld("skyworld"))
					.getRegion(owner + "Island").getMinimumPoint(), getWorldGuard()
					.getRegionManager(Bukkit.getWorld(Settings.general_worldName)).getRegion(owner + "Island").getMaximumPoint());
			region2.setOwners(getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion(owner + "Island").getOwners());
			region2.setParent(getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).getRegion("__Global__"));
			region2.setFlag(
					DefaultFlag.GREET_MESSAGE,
					DefaultFlag.GREET_MESSAGE.parseInput(getWorldGuard(), sender, "You are entering a protected island area. (" + player
							+ ")"));
			region2.setFlag(
					DefaultFlag.FAREWELL_MESSAGE,
					DefaultFlag.FAREWELL_MESSAGE.parseInput(getWorldGuard(), sender, "You are leaving a protected island area. (" + player
							+ ")"));
			region2.setFlag(DefaultFlag.PVP, DefaultFlag.PVP.parseInput(getWorldGuard(), sender, "deny"));
			getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).removeRegion(owner + "Island");
			getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).addRegion(region2);
		} catch (final Exception e) {
			System.out.println("Error transferring WorldGuard Protected Region from (" + owner + ") to (" + player + ")");
		}
	}
}