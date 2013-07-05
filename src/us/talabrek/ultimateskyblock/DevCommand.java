package us.talabrek.ultimateskyblock;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DevCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		if (!(sender instanceof Player)) { return false; }
		final Player player = (Player) sender;
		if (split.length == 0) {
			if (VaultHandler.checkPerk(player.getName(), "usb.mod.protect", player.getWorld())
					|| VaultHandler.checkPerk(player.getName(), "usb.mod.protectall", player.getWorld())
					|| VaultHandler.checkPerk(player.getName(), "usb.mod.topten", player.getWorld())
					|| VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld())
					|| VaultHandler.checkPerk(player.getName(), "usb.admin.delete", player.getWorld())
					|| VaultHandler.checkPerk(player.getName(), "usb.admin.remove", player.getWorld())
					|| VaultHandler.checkPerk(player.getName(), "usb.admin.register", player.getWorld()) || player.isOp()) {
				player.sendMessage("[dev usage]");
				if (VaultHandler.checkPerk(player.getName(), "usb.mod.protect", player.getWorld()) || player.isOp())
					player.sendMessage(ChatColor.YELLOW + "/dev protect <player>:" + ChatColor.WHITE + " add protection to an island.");
				if (VaultHandler.checkPerk(player.getName(), "usb.admin.reload", player.getWorld()) || player.isOp())
					player.sendMessage(ChatColor.YELLOW + "/dev reload:" + ChatColor.WHITE + " reload configuration from file.");
				if (VaultHandler.checkPerk(player.getName(), "usb.mod.protectall", player.getWorld()) || player.isOp())
					player.sendMessage(ChatColor.YELLOW + "/dev protectall:" + ChatColor.WHITE
							+ " add island protection to unprotected islands.");
				if (VaultHandler.checkPerk(player.getName(), "usb.mod.topten", player.getWorld()) || player.isOp())
					player.sendMessage(ChatColor.YELLOW + "/dev topten:" + ChatColor.WHITE + " manually update the top 10 list");
				if (VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld()) || player.isOp())
					player.sendMessage(ChatColor.YELLOW + "/dev orphancount:" + ChatColor.WHITE + " unused island locations count");
				if (VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld()) || player.isOp())
					player.sendMessage(ChatColor.YELLOW + "/dev clearorphan:" + ChatColor.WHITE + " remove any unused island locations.");
				if (VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld()) || player.isOp())
					player.sendMessage(ChatColor.YELLOW + "/dev saveorphan:" + ChatColor.WHITE
							+ " save the list of old (empty) island locations.");
				if (VaultHandler.checkPerk(player.getName(), "usb.admin.delete", player.getWorld()) || player.isOp())
					player.sendMessage(ChatColor.YELLOW + "/dev delete <player>:" + ChatColor.WHITE + " delete an island (removes blocks).");
				if (VaultHandler.checkPerk(player.getName(), "usb.admin.remove", player.getWorld()) || player.isOp())
					player.sendMessage(ChatColor.YELLOW + "/dev remove <player>:" + ChatColor.WHITE + " remove a player from an island.");
				if (VaultHandler.checkPerk(player.getName(), "usb.admin.register", player.getWorld()) || player.isOp())
					player.sendMessage(ChatColor.YELLOW + "/dev register <player>:" + ChatColor.WHITE
							+ " set a player's island to your location");
				if (VaultHandler.checkPerk(player.getName(), "usb.mod.challenges", player.getWorld()) || player.isOp())
					player.sendMessage(ChatColor.YELLOW + "/dev completechallenge <challengename> <player>:" + ChatColor.WHITE
							+ " marks a challenge as complete");
				if (VaultHandler.checkPerk(player.getName(), "usb.mod.challenges", player.getWorld()) || player.isOp())
					player.sendMessage(ChatColor.YELLOW + "/dev resetchallenge <challengename> <player>:" + ChatColor.WHITE
							+ " marks a challenge as incomplete");
				if (VaultHandler.checkPerk(player.getName(), "usb.mod.challenges", player.getWorld()) || player.isOp())
					player.sendMessage(ChatColor.YELLOW + "/dev resetallchallenges <challengename>:" + ChatColor.WHITE
							+ " resets all of the player's challenges");
				if (VaultHandler.checkPerk(player.getName(), "usb.admin.purge", player.getWorld()) || player.isOp())
					player.sendMessage(ChatColor.YELLOW + "/dev purge [TimeInDays]:" + ChatColor.WHITE
							+ " delete inactive islands older than [TimeInDays].");
				if (VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld()) || player.isOp())
					player.sendMessage(ChatColor.YELLOW + "/dev buildpartylist:" + ChatColor.WHITE
							+ " build a new party list (use this if parties are broken).");
				if (VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld()) || player.isOp())
					player.sendMessage(ChatColor.YELLOW + "/dev info <player>:" + ChatColor.WHITE
							+ " check the party information for the given player.");
			} else {
				player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
			}
		} else if (split.length == 1) {
			if (split[0].equals("clearorphan")
					&& (VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld()) || player.isOp())) {
				player.sendMessage(ChatColor.YELLOW + "Clearing all old (empty) island locations.");
				uSkyBlock.getInstance().clearOrphanedIsland();
			} else if (split[0].equals("protectall")
					&& (VaultHandler.checkPerk(player.getName(), "usb.mod.protectall", player.getWorld()) || player.isOp())) {
				player.sendMessage(ChatColor.YELLOW + "Protecting all unprotected player Islands.");
				if (Settings.island_protectWithWorldGuard) {
					player.sendMessage(ChatColor.YELLOW + "Protecting all unprotected player Islands.");
					WorldGuardHandler.protectAllIslands(sender);
				} else {
					player.sendMessage(ChatColor.RED + "You must enable WorldGuard protection in the config.yml to use this!");
				}
			} else if (split[0].equals("buildpartylist")
					&& (VaultHandler.checkPerk(player.getName(), "usb.mod.protectall", player.getWorld()) || player.isOp())) {
				player.sendMessage(ChatColor.YELLOW + "Building party lists..");
				buildPartyList();
			} else if (split[0].equals("orphancount")
					&& (VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld()) || player.isOp())) {
				player.sendMessage(ChatColor.YELLOW + "" + uSkyBlock.getInstance().orphanCount()
						+ " old island locations will be used before new ones.");
			} else if (split[0].equals("reload")
					&& (VaultHandler.checkPerk(player.getName(), "usb.admin.reload", player.getWorld()) || player.isOp())) {
				uSkyBlock.getInstance().reloadConfig();
				uSkyBlock.getInstance().loadPluginConfig();
				player.sendMessage(ChatColor.YELLOW + "Configuration reloaded from file.");
			} else if (split[0].equals("saveorphan")
					&& (VaultHandler.checkPerk(player.getName(), "usb.mod.orphan", player.getWorld()) || player.isOp())) {
				player.sendMessage(ChatColor.YELLOW + "Saving the orphan list.");
				uSkyBlock.getInstance().saveOrphans();
			} else if (split[0].equals("topten")
					&& (VaultHandler.checkPerk(player.getName(), "usb.mod.topten", player.getWorld()) || player.isOp())) {
				player.sendMessage(ChatColor.YELLOW + "Generating the Top Ten list");
				uSkyBlock.getInstance().updateTopTen(uSkyBlock.getInstance().generateTopTen());
				player.sendMessage(ChatColor.YELLOW + "Finished generation of the Top Ten list");
			} else if (split[0].equals("purge")
					&& (VaultHandler.checkPerk(player.getName(), "usb.admin.purge", player.getWorld()) || player.isOp())) {
				if (uSkyBlock.getInstance().isPurgeActive()) {
					player.sendMessage(ChatColor.RED + "A purge is already running, please wait for it to finish!");
					return true;
				}
				player.sendMessage(ChatColor.YELLOW + "Usage: /dev purge [TimeInDays]");
				return true;
			}
		} else if (split.length == 2) {
			if (split[0].equals("purge")
					&& (VaultHandler.checkPerk(player.getName(), "usb.admin.purge", player.getWorld()) || player.isOp())) {
				if (uSkyBlock.getInstance().isPurgeActive()) {
					player.sendMessage(ChatColor.RED + "A purge is already running, please wait for it to finish!");
					return true;
				}
				uSkyBlock.getInstance().activatePurge();
				final int time = Integer.parseInt(split[1]) * 24;
				player.sendMessage(ChatColor.YELLOW + "Marking all islands inactive for more than " + split[1] + " days.");
				uSkyBlock.getInstance().getServer().getScheduler().runTaskAsynchronously(uSkyBlock.getInstance(), new Runnable() {
					public void run() {
						final File directoryPlayers = new File(uSkyBlock.getInstance().getDataFolder() + File.separator + "players");

						long offlineTime = 0L;

						for (final File child : directoryPlayers.listFiles()) {
							if (Bukkit.getOfflinePlayer(child.getName()) != null && Bukkit.getPlayer(child.getName()) == null) {
								final OfflinePlayer oplayer = Bukkit.getOfflinePlayer(child.getName());
								offlineTime = oplayer.getLastPlayed();
								offlineTime = (System.currentTimeMillis() - offlineTime) / 3600000L;
								if (offlineTime > time && uSkyBlock.getInstance().hasIsland(oplayer.getName())) {
									final PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(oplayer.getName());
									if (pi != null) {
										if (!pi.getHasParty()) {
											if (pi.getIslandLevel() < 10) {
												if (child.getName() != null)
													uSkyBlock.getInstance().addToRemoveList(child.getName());
											}
										}
									}
								}
							}
						}
						System.out.println("uSkyblock "+"Removing " + uSkyBlock.getInstance().getRemoveList().size() + " inactive islands.");
						uSkyBlock.getInstance().getServer().getScheduler()
								.scheduleSyncRepeatingTask(uSkyBlock.getInstance(), new Runnable() {
									public void run() {
										if (uSkyBlock.getInstance().getRemoveList().size() > 0 && uSkyBlock.getInstance().isPurgeActive()) {
											uSkyBlock.getInstance().deletePlayerIsland(uSkyBlock.getInstance().getRemoveList().get(0));
											System.out.println("uSkyblock "+"[uSkyBlock] Purge: Removing "
													+ uSkyBlock.getInstance().getRemoveList().get(0) + "'s island");
											uSkyBlock.getInstance().deleteFromRemoveList();
										}

										if (uSkyBlock.getInstance().getRemoveList().size() == 0 && uSkyBlock.getInstance().isPurgeActive()) {
											uSkyBlock.getInstance().deactivatePurge();
											System.out.println("uSkyblock "+"[uSkyBlock] Finished purging marked inactive islands.");
										}
									}
								}, 0L, 20L);
					}
				});
			} else if (split[0].equals("goto")
					&& (VaultHandler.checkPerk(player.getName(), "usb.mod.goto", player.getWorld()) || player.isOp())) {
				PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[1]);
				if (pi.getHasParty() && !pi.getPartyLeader().equalsIgnoreCase(split[1]))
					pi = uSkyBlock.getInstance().readPlayerFile(pi.getPartyLeader());
				if (pi == null) {
					player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
				} else {
					if (pi.getHomeLocation() != null) {
						player.sendMessage(ChatColor.GREEN + "Teleporting to " + split[1] + "'s island.");
						player.teleport(pi.getIslandLocation());
						return true;
					}
					if (pi.getIslandLocation() != null) {
						player.sendMessage(ChatColor.GREEN + "Teleporting to " + split[1] + "'s island.");
						player.teleport(pi.getIslandLocation());
						return true;
					}
					player.sendMessage("Error: That player does not have an island!");
				}
			} else if (split[0].equals("remove")
					&& (VaultHandler.checkPerk(player.getName(), "usb.admin.remove", player.getWorld()) || player.isOp())) {
				final PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[1]);
				if (pi == null) {
					player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
				} else {
					if (pi.getIslandLocation() != null) {
						player.sendMessage(ChatColor.YELLOW + "Removing " + split[1] + "'s island.");
						uSkyBlock.getInstance().devDeletePlayerIsland(split[1]);
						return true;
					}
					player.sendMessage("Error: That player does not have an island!");
				}
			} else if (split[0].equals("delete")
					&& (VaultHandler.checkPerk(player.getName(), "usb.admin.delete", player.getWorld()) || player.isOp())) {
				final PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[1]);
				if (pi == null) {
					player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
				} else {
					if (pi.getIslandLocation() != null) {
						player.sendMessage(ChatColor.YELLOW + "Removing " + split[1] + "'s island.");
						uSkyBlock.getInstance().deletePlayerIsland(split[1]);
						return true;
					}
					player.sendMessage("Error: That player does not have an island!");
				}
			} else if (split[0].equals("register")
					&& (VaultHandler.checkPerk(player.getName(), "usb.admin.register", player.getWorld()) || player.isOp())) {
				final PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[1]);
				if (pi == null) {
					player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
				} else {
					if (pi.getHasIsland())
						uSkyBlock.getInstance().devDeletePlayerIsland(split[1]);
					if (uSkyBlock.getInstance().devSetPlayerIsland(sender, player.getLocation(), split[1])) {
						player.sendMessage(ChatColor.GREEN + "Set " + split[1] + "'s island to the bedrock nearest you.");
					} else
						player.sendMessage(ChatColor.RED + "Bedrock not found: unable to set the island!");
				}
			} else if (split[0].equals("info")
					&& (VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld()) || player.isOp())) {
				final PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[1]);
				if (pi == null) {
					player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
				} else if (pi.getHasParty()) {
					final PlayerInfo piL = uSkyBlock.getInstance().readPlayerFile(pi.getPartyLeader());
					final List<String> pList = piL.getMembers();
					if (pList.contains(split[1])) {
						if (split[1].equalsIgnoreCase(pi.getPartyLeader())) {
							pList.remove(split[1]);
						} else {
							pList.remove(pi.getPartyLeader());
						}
					}
					player.sendMessage(ChatColor.GREEN + pi.getPartyLeader() + " " + ChatColor.WHITE + pList.toString());
					player.sendMessage(ChatColor.YELLOW + "Island Location:" + ChatColor.WHITE + " ("
							+ pi.getPartyIslandLocation().getBlockX() + "," + pi.getPartyIslandLocation().getBlockY() + ","
							+ pi.getPartyIslandLocation().getBlockZ() + ")");
				} else {
					player.sendMessage(ChatColor.YELLOW + "That player is not a member of an island party.");
					if (pi.getHasIsland()) {
						player.sendMessage(ChatColor.YELLOW + "Island Location:" + ChatColor.WHITE + " ("
								+ pi.getIslandLocation().getBlockX() + "," + pi.getIslandLocation().getBlockY() + ","
								+ pi.getIslandLocation().getBlockZ() + ")");
					}
					if (pi.getPartyLeader() != null) {
						player.sendMessage(ChatColor.RED + "Party leader: " + pi.getPartyLeader() + " should be null!");
					}
					if (pi.getMembers() != null) {
						player.sendMessage(ChatColor.RED + "Player has party members, but shouldn't!");
					}
				}
			} else if (split[0].equals("resetallchallenges")
					&& (VaultHandler.checkPerk(player.getName(), "usb.mod.challenges", player.getWorld()) || player.isOp())) {
				if (!uSkyBlock.getInstance().getActivePlayers().containsKey(split[1])) {
					final PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[1]);
					if (pi == null) {
						player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
						return true;
					}
					pi.resetAllChallenges();
					uSkyBlock.getInstance().writePlayerFile(split[1], pi);
					player.sendMessage(ChatColor.YELLOW + split[1] + " has had all challenges reset.");
				} else {
					uSkyBlock.getInstance().getActivePlayers().get(split[1]).resetAllChallenges();
					player.sendMessage(ChatColor.YELLOW + split[1] + " has had all challenges reset.");
				}
			}
		} else if (split.length == 3) {
			if (split[0].equals("completechallenge")
					&& (VaultHandler.checkPerk(player.getName(), "usb.mod.challenges", player.getWorld()) || player.isOp())) {
				if (!uSkyBlock.getInstance().getActivePlayers().containsKey(split[2])) {
					final PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[2]);
					if (pi == null) {
						player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
						return true;
					}
					if (pi.checkChallenge(split[1].toLowerCase()) || !pi.challengeExists(split[1].toLowerCase())) {
						player.sendMessage(ChatColor.RED + "Challenge doesn't exist or is already completed");
						return true;
					}
					pi.completeChallenge(split[1].toLowerCase());
					uSkyBlock.getInstance().writePlayerFile(split[2], pi);
					player.sendMessage(ChatColor.YELLOW + "challange: " + split[1].toLowerCase() + " has been completed for " + split[2]);
				} else {
					if (uSkyBlock.getInstance().getActivePlayers().get(split[2]).checkChallenge(split[1].toLowerCase())
							|| !uSkyBlock.getInstance().getActivePlayers().get(split[2]).challengeExists(split[1].toLowerCase())) {
						player.sendMessage(ChatColor.RED + "Challenge doesn't exist or is already completed");
						return true;
					}
					uSkyBlock.getInstance().getActivePlayers().get(split[2]).completeChallenge(split[1].toLowerCase());
					player.sendMessage(ChatColor.YELLOW + "challange: " + split[1].toLowerCase() + " has been completed for " + split[2]);
				}
			} else if (split[0].equals("resetchallenge")
					&& (VaultHandler.checkPerk(player.getName(), "usb.mod.challenges", player.getWorld()) || player.isOp())) {
				if (!uSkyBlock.getInstance().getActivePlayers().containsKey(split[2])) {
					final PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[2]);
					if (pi == null) {
						player.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
						return true;
					}
					if (!pi.checkChallenge(split[1].toLowerCase()) || !pi.challengeExists(split[1].toLowerCase())) {
						player.sendMessage(ChatColor.RED + "Challenge doesn't exist or isn't yet completed");
						return true;
					}
					pi.resetChallenge(split[1].toLowerCase());
					uSkyBlock.getInstance().writePlayerFile(split[2], pi);
					player.sendMessage(ChatColor.YELLOW + "challange: " + split[1].toLowerCase() + " has been reset for " + split[2]);
				} else {
					if (!uSkyBlock.getInstance().getActivePlayers().get(split[2]).checkChallenge(split[1].toLowerCase())
							|| !uSkyBlock.getInstance().getActivePlayers().get(split[2]).challengeExists(split[1].toLowerCase())) {
						player.sendMessage(ChatColor.RED + "Challenge doesn't exist or isn't yet completed");
						return true;
					}
					uSkyBlock.getInstance().getActivePlayers().get(split[2]).resetChallenge(split[1].toLowerCase());
					player.sendMessage(ChatColor.YELLOW + "challange: " + split[1].toLowerCase() + " has been completed for " + split[2]);
				}
			}
		}
		return true;
	}

	public void buildPartyList() {
		final File folder = uSkyBlock.getInstance().directoryPlayers;
		final File[] listOfFiles = folder.listFiles();

		System.out.println("uSkyblock "+ChatColor.YELLOW + "[uSkyBlock] Building a new party list...");
		for (int i = 0; i < listOfFiles.length; i++) {
			PlayerInfo pi;
			if ((pi = uSkyBlock.getInstance().readPlayerFile(listOfFiles[i].getName())) != null) {
				if (pi.getHasParty()) {
					PlayerInfo piL;
					if (!pi.getPartyLeader().equalsIgnoreCase(listOfFiles[i].getName()))
						piL = uSkyBlock.getInstance().readPlayerFile(pi.getPartyLeader());
					else {
						piL = pi;
					}
					piL.getHasParty();

					if (!piL.getMembers().contains(listOfFiles[i].getName())) {
						piL.addMember(listOfFiles[i].getName());
					}

					uSkyBlock.getInstance().writePlayerFile(pi.getPartyLeader(), piL);
				}
			}
		}
		System.out.println("uSkyblock "+ChatColor.YELLOW + "[uSkyBlock] Party list completed.");
	}
}