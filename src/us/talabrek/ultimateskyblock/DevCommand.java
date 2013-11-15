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
	public void buildPartyList() {
		final File folder = uSkyBlock.getInstance().directoryPlayers;
		final File[] listOfFiles = folder.listFiles();

		System.out.println("uSkyblock " + ChatColor.YELLOW + "[uSkyBlock] Building a new party list...");
		for (int i = 0; i < listOfFiles.length; i++) {
			PlayerInfo pi;
			if ((pi = uSkyBlock.getInstance().readPlayerFile(listOfFiles[i].getName())) != null) {
				if (pi.getHasParty()) {
					PlayerInfo piL;
					if (!pi.getPartyLeader().equalsIgnoreCase(listOfFiles[i].getName())) {
						piL = uSkyBlock.getInstance().readPlayerFile(pi.getPartyLeader());
					} else {
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
		System.out.println("uSkyblock " + ChatColor.YELLOW + "[uSkyBlock] Party list completed.");
	}
	
	private boolean hasPerm(CommandSender sender, String perm)
	{
		if(sender instanceof Player)
			return VaultHandler.checkPerk(sender.getName(), perm, ((Player)sender).getWorld());
		
		return sender.hasPermission(perm);
	}
	
	private void displayHelp(CommandSender sender)
	{
		if (hasPerm(sender, "usb.mod.protect") || hasPerm(sender, "usb.mod.protectall")
				|| hasPerm(sender, "usb.mod.topten")
				|| hasPerm(sender, "usb.mod.orphan")
				|| hasPerm(sender, "usb.admin.delete")
				|| hasPerm(sender, "usb.admin.remove")
				|| hasPerm(sender, "usb.admin.register")) {
			sender.sendMessage("[dev usage]");
			if (hasPerm(sender, "usb.mod.protect"))
				sender.sendMessage(ChatColor.YELLOW + "/dev protect <player>:" + ChatColor.WHITE + " add protection to an island.");
			if (hasPerm(sender, "usb.admin.reload"))
				sender.sendMessage(ChatColor.YELLOW + "/dev reload:" + ChatColor.WHITE + " reload configuration from file.");
			if (hasPerm(sender, "usb.mod.protectall"))
				sender.sendMessage(ChatColor.YELLOW + "/dev protectall:" + ChatColor.WHITE + " add island protection to unprotected islands.");
			if (hasPerm(sender, "usb.mod.topten"))
				sender.sendMessage(ChatColor.YELLOW + "/dev topten:" + ChatColor.WHITE + " manually update the top 10 list");
			if (hasPerm(sender, "usb.mod.orphan"))
				sender.sendMessage(ChatColor.YELLOW + "/dev orphancount:" + ChatColor.WHITE + " unused island locations count");
			if (hasPerm(sender, "usb.mod.orphan"))
				sender.sendMessage(ChatColor.YELLOW + "/dev clearorphan:" + ChatColor.WHITE + " remove any unused island locations.");
			if (hasPerm(sender, "usb.mod.orphan"))
				sender.sendMessage(ChatColor.YELLOW + "/dev saveorphan:" + ChatColor.WHITE + " save the list of old (empty) island locations.");
			if (hasPerm(sender, "usb.admin.delete"))
				sender.sendMessage(ChatColor.YELLOW + "/dev delete <player>:" + ChatColor.WHITE + " delete an island (removes blocks).");
			if (hasPerm(sender, "usb.admin.remove"))
				sender.sendMessage(ChatColor.YELLOW + "/dev remove <player>:" + ChatColor.WHITE + " remove a player from an island.");
			if (hasPerm(sender, "usb.admin.register"))
				sender.sendMessage(ChatColor.YELLOW + "/dev register <player>:" + ChatColor.WHITE + " set a player's island to your location");
			if (hasPerm(sender, "usb.mod.challenges"))
				sender.sendMessage(ChatColor.YELLOW + "/dev completechallenge <challengename> <player>:" + ChatColor.WHITE + " marks a challenge as complete");
			if (hasPerm(sender, "usb.mod.challenges"))
				sender.sendMessage(ChatColor.YELLOW + "/dev resetchallenge <challengename> <player>:" + ChatColor.WHITE + " marks a challenge as incomplete");
			if (hasPerm(sender, "usb.mod.challenges"))
				sender.sendMessage(ChatColor.YELLOW + "/dev resetallchallenges <playername>:" + ChatColor.WHITE + " resets all of the player's challenges");
			if (hasPerm(sender, "usb.mod.challenges"))
				sender.sendMessage(ChatColor.YELLOW + "/dev viewchallenges <playername>: " + ChatColor.WHITE + " views the completed challenges for that player");
			if (hasPerm(sender, "usb.admin.purge"))
				sender.sendMessage(ChatColor.YELLOW + "/dev purge [TimeInDays]:" + ChatColor.WHITE + " delete inactive islands older than [TimeInDays].");
			if (hasPerm(sender, "usb.mod.party"))
				sender.sendMessage(ChatColor.YELLOW + "/dev buildpartylist:" + ChatColor.WHITE + " build a new party list (use this if parties are broken).");
			if (hasPerm(sender, "usb.mod.party"))
				sender.sendMessage(ChatColor.YELLOW + "/dev info <player>:" + ChatColor.WHITE + " check the party information for the given sender.");
		} 
		else
			sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
	}
	
	private void displayChallenges(CommandSender sender, OfflinePlayer player)
	{
		sender.sendMessage(ChatColor.GOLD + player.getName() + "'s Challanges:");

		sender.sendMessage(ChatColor.GOLD + Settings.challenges_ranks[0] + ": " + uSkyBlock.getInstance().getChallengesFromRank(player, Settings.challenges_ranks[0]));
		for (int i = 1; i < Settings.challenges_ranks.length; i++) 
		{
			int rankComplete = uSkyBlock.getInstance().checkRankCompletion(player, Settings.challenges_ranks[i - 1]);
			if (rankComplete <= 0)
				sender.sendMessage(ChatColor.GOLD + Settings.challenges_ranks[i] + ": " + uSkyBlock.getInstance().getChallengesFromRank(player, Settings.challenges_ranks[i]));
//			else
//				sender.sendMessage(ChatColor.GOLD + Settings.challenges_ranks[i] + ChatColor.GRAY + ": Complete " + rankComplete + " more " + Settings.challenges_ranks[i - 1] + " challenges to unlock this rank!");
		}
	}

	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] split) 
	{
		if (split.length == 0)
			displayHelp(sender);
		else if (split.length == 1) 
		{
			if (split[0].equals("clearorphan") && (hasPerm(sender, "usb.mod.orphan"))) 
			{
				sender.sendMessage(ChatColor.YELLOW + "Clearing all old (empty) island locations.");
				uSkyBlock.getInstance().clearOrphanedIsland();
			} 
			else if (split[0].equals("protectall") && (hasPerm(sender, "usb.mod.protectall"))) 
			{
				sender.sendMessage(ChatColor.YELLOW + "Protecting all unprotected player Islands.");
				if (Settings.island_protectWithWorldGuard) 
				{
					sender.sendMessage(ChatColor.YELLOW + "Protecting all unprotected player Islands.");
					WorldGuardHandler.protectAllIslands(sender);
				} 
				else
					sender.sendMessage(ChatColor.RED + "You must enable WorldGuard protection in the config.yml to use this!");
			} 
			else if (split[0].equals("buildpartylist") && (hasPerm(sender, "usb.mod.protectall"))) 
			{
				sender.sendMessage(ChatColor.YELLOW + "Building party lists..");
				buildPartyList();
			} 
			else if (split[0].equals("orphancount") && (hasPerm(sender, "usb.mod.orphan"))) 
			{
				sender.sendMessage(ChatColor.YELLOW + "" + uSkyBlock.getInstance().orphanCount()
						+ " old island locations will be used before new ones.");
			} 
			else if (split[0].equals("reload") && (hasPerm(sender, "usb.admin.reload"))) 
			{
				uSkyBlock.getInstance().reloadConfig();
				uSkyBlock.getInstance().loadPluginConfig();
				sender.sendMessage(ChatColor.YELLOW + "Configuration reloaded from file.");
			} 
			else if (split[0].equals("saveorphan") && (hasPerm(sender, "usb.mod.orphan"))) 
			{
				sender.sendMessage(ChatColor.YELLOW + "Saving the orphan list.");
				uSkyBlock.getInstance().saveOrphans();
			} 
			else if (split[0].equals("topten") && (hasPerm(sender, "usb.mod.topten"))) 
			{
				sender.sendMessage(ChatColor.YELLOW + "Generating the Top Ten list");
				uSkyBlock.getInstance().updateTopTen(uSkyBlock.getInstance().generateTopTen());
				sender.sendMessage(ChatColor.YELLOW + "Finished generation of the Top Ten list");
			} 
			else if (split[0].equals("purge") && (hasPerm(sender, "usb.admin.purge"))) 
			{
				if (uSkyBlock.getInstance().isPurgeActive()) 
				{
					sender.sendMessage(ChatColor.RED + "A purge is already running, please wait for it to finish!");
					return true;
				}
				sender.sendMessage(ChatColor.YELLOW + "Usage: /dev purge [TimeInDays]");
				return true;
			}
		} 
		else if (split.length == 2) 
		{
			if (split[0].equals("purge") && (hasPerm(sender, "usb.admin.purge"))) 
			{
				if (uSkyBlock.getInstance().isPurgeActive()) 
				{
					sender.sendMessage(ChatColor.RED + "A purge is already running, please wait for it to finish!");
					return true;
				}
				uSkyBlock.getInstance().activatePurge();
				final int time = Integer.parseInt(split[1]) * 24;
				sender.sendMessage(ChatColor.YELLOW + "Marking all islands inactive for more than " + split[1] + " days.");
				uSkyBlock.getInstance().getServer().getScheduler().runTaskAsynchronously(uSkyBlock.getInstance(), new Runnable() 
				{
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
												if (child.getName() != null) {
													uSkyBlock.getInstance().addToRemoveList(child.getName());
												}
											}
										}
									}
								}
							}
						}
						System.out.println("uSkyblock " + "Removing " + uSkyBlock.getInstance().getRemoveList().size()
								+ " inactive islands.");
						uSkyBlock.getInstance().getServer().getScheduler()
								.scheduleSyncRepeatingTask(uSkyBlock.getInstance(), new Runnable() {
									public void run() {
										if (uSkyBlock.getInstance().getRemoveList().size() > 0 && uSkyBlock.getInstance().isPurgeActive()) {
											uSkyBlock.getInstance().deletePlayerIsland(uSkyBlock.getInstance().getRemoveList().get(0));
											System.out.println("uSkyblock " + "[uSkyBlock] Purge: Removing "
													+ uSkyBlock.getInstance().getRemoveList().get(0) + "'s island");
											uSkyBlock.getInstance().deleteFromRemoveList();
										}

										if (uSkyBlock.getInstance().getRemoveList().size() == 0 && uSkyBlock.getInstance().isPurgeActive()) {
											uSkyBlock.getInstance().deactivatePurge();
											System.out.println("uSkyblock " + "[uSkyBlock] Finished purging marked inactive islands.");
										}
									}
								}, 0L, 20L);
					}
				});
			} 
			else if (split[0].equals("goto") && (hasPerm(sender, "usb.mod.goto"))) 
			{
				if(!(sender instanceof Player))
				{
					sender.sendMessage(ChatColor.RED + "You must be a player to use this.");
					return true;
				}
				
				Player player = (Player)sender;
				PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[1]);
				
				if(pi == null)
				{
					sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
					return true;
				}
				
				if (pi.getHasParty() && !pi.getPartyLeader().equalsIgnoreCase(split[1])) {
					pi = uSkyBlock.getInstance().readPlayerFile(pi.getPartyLeader());
				}
				if (pi == null) {
					sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
				} else {
					if (pi.getHomeLocation() != null) {
						sender.sendMessage(ChatColor.GREEN + "Teleporting to " + split[1] + "'s island.");
						player.teleport(pi.getIslandLocation());
						return true;
					}
					if (pi.getIslandLocation() != null) {
						sender.sendMessage(ChatColor.GREEN + "Teleporting to " + split[1] + "'s island.");
						player.teleport(pi.getIslandLocation());
						return true;
					}
					sender.sendMessage("Error: That player does not have an island!");
				}
			} 
			else if (split[0].equals("remove") && (hasPerm(sender, "usb.admin.remove"))) 
			{
				final PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[1]);
				if (pi == null)
					sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
				else 
				{
					if (pi.getIslandLocation() != null) 
					{
						sender.sendMessage(ChatColor.YELLOW + "Removing " + split[1] + "'s island.");
						uSkyBlock.getInstance().devDeletePlayerIsland(split[1]);
						return true;
					}
					sender.sendMessage("Error: That player does not have an island!");
				}
			} 
			else if (split[0].equals("delete") && (hasPerm(sender, "usb.admin.delete"))) 
			{
				final PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[1]);
				if (pi == null)
					sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
				else 
				{
					if (pi.getIslandLocation() != null) 
					{
						sender.sendMessage(ChatColor.YELLOW + "Removing " + split[1] + "'s island.");
						uSkyBlock.getInstance().deletePlayerIsland(split[1]);
						return true;
					}
					sender.sendMessage("Error: That player does not have an island!");
				}
			} 
			else if (split[0].equals("register") && (hasPerm(sender, "usb.admin.register"))) 
			{
				if(!(sender instanceof Player))
				{
					sender.sendMessage(ChatColor.RED + "You must be a player to use this.");
					return true;
				}
				
				Player player = (Player)sender;
				
				final PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[1]);
				if (pi == null)
					sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
				else 
				{
					if (pi.getHasIsland())
						uSkyBlock.getInstance().devDeletePlayerIsland(split[1]);
					
					if (uSkyBlock.getInstance().devSetPlayerIsland(sender, player.getLocation(), split[1]))
						sender.sendMessage(ChatColor.GREEN + "Set " + split[1] + "'s island to the bedrock nearest you.");
					else
						sender.sendMessage(ChatColor.RED + "Bedrock not found: unable to set the island!");
				}
			} 
			else if (split[0].equals("info") && (hasPerm(sender, "usb.mod.party"))) 
			{
				final PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[1]);
				if (pi == null)
					sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
				else if (pi.getHasParty()) 
				{
					final PlayerInfo piL = uSkyBlock.getInstance().readPlayerFile(pi.getPartyLeader());
					final List<String> pList = piL.getMembers();
					if (pList.contains(split[1])) 
					{
						if (split[1].equalsIgnoreCase(pi.getPartyLeader()))
							pList.remove(split[1]);
						else
							pList.remove(pi.getPartyLeader());
						
					}
					sender.sendMessage(ChatColor.GREEN + pi.getPartyLeader() + " " + ChatColor.WHITE + pList.toString());
					sender.sendMessage(ChatColor.YELLOW + "Island Location:" + ChatColor.WHITE + " ("
							+ pi.getPartyIslandLocation().getBlockX() + "," + pi.getPartyIslandLocation().getBlockY() + ","
							+ pi.getPartyIslandLocation().getBlockZ() + ")");
				} 
				else 
				{
					sender.sendMessage(ChatColor.YELLOW + "That player is not a member of an island party.");
					if (pi.getHasIsland()) 
					{
						sender.sendMessage(ChatColor.YELLOW + "Island Location:" + ChatColor.WHITE + " ("
								+ pi.getIslandLocation().getBlockX() + "," + pi.getIslandLocation().getBlockY() + ","
								+ pi.getIslandLocation().getBlockZ() + ")");
					}
					if (pi.getPartyLeader() != null)
						sender.sendMessage(ChatColor.RED + "Party leader: " + pi.getPartyLeader() + " should be null!");
					
					if (pi.getMembers() != null)
						sender.sendMessage(ChatColor.RED + "Player has party members, but shouldn't!");
					
				}
			} 
			else if (split[0].equals("resetallchallenges") && (hasPerm(sender, "usb.mod.challenges"))) 
			{
				PlayerInfo pi = uSkyBlock.getInstance().getPlayer(split[1]);
				if(pi == null)
				{
					sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
					return true;
				}
				pi.resetAllChallenges();
				sender.sendMessage(ChatColor.YELLOW + split[1] + " has had all challenges reset.");
			}
			else if(split[0].equals("viewchallenges") && hasPerm(sender, "usb.mod.challenges"))
			{
				OfflinePlayer player = Bukkit.getOfflinePlayer(split[1]);
				if(!player.hasPlayedBefore())
				{
					player = Bukkit.getPlayer(split[1]);
					if(player == null)
					{
						sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
						return true;
					}
				}
				
				displayChallenges(sender, player);
			}
		} 
		else if (split.length == 3) 
		{
			if (split[0].equals("completechallenge") && (hasPerm(sender, "usb.mod.challenges"))) 
			{
				if (!uSkyBlock.getInstance().isActivePlayer(split[2])) 
				{
					final PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[2]);
					if (pi == null) 
					{
						sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
						return true;
					}
					if (pi.checkChallenge(split[1].toLowerCase()) || !pi.challengeExists(split[1].toLowerCase())) 
					{
						sender.sendMessage(ChatColor.RED + "Challenge doesn't exist or is already completed");
						return true;
					}
					pi.completeChallenge(split[1].toLowerCase());
					uSkyBlock.getInstance().writePlayerFile(split[2], pi);
					sender.sendMessage(ChatColor.YELLOW + "challange: " + split[1].toLowerCase() + " has been completed for " + split[2]);
				} 
				else 
				{
					if (uSkyBlock.getInstance().getPlayer(split[2]).checkChallenge(split[1].toLowerCase())
							|| !uSkyBlock.getInstance().getPlayer(split[2]).challengeExists(split[1].toLowerCase())) 
					{
						sender.sendMessage(ChatColor.RED + "Challenge doesn't exist or is already completed");
						return true;
					}
					uSkyBlock.getInstance().getPlayer(split[2]).completeChallenge(split[1].toLowerCase());
					sender.sendMessage(ChatColor.YELLOW + "challange: " + split[1].toLowerCase() + " has been completed for " + split[2]);
				}
			} 
			else if (split[0].equals("resetchallenge") && (hasPerm(sender, "usb.mod.challenges"))) 
			{
				if (!uSkyBlock.getInstance().isActivePlayer(split[2])) 
				{
					final PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(split[2]);
					if (pi == null) 
					{
						sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
						return true;
					}
					if (!pi.checkChallenge(split[1].toLowerCase()) || !pi.challengeExists(split[1].toLowerCase())) 
					{
						sender.sendMessage(ChatColor.RED + "Challenge doesn't exist or isn't yet completed");
						return true;
					}
					pi.resetChallenge(split[1].toLowerCase());
					uSkyBlock.getInstance().writePlayerFile(split[2], pi);
					sender.sendMessage(ChatColor.YELLOW + "challange: " + split[1].toLowerCase() + " has been reset for " + split[2]);
				} 
				else 
				{
					if (!uSkyBlock.getInstance().getPlayer(split[2]).checkChallenge(split[1].toLowerCase())
							|| !uSkyBlock.getInstance().getPlayer(split[2]).challengeExists(split[1].toLowerCase())) 
					{
						sender.sendMessage(ChatColor.RED + "Challenge doesn't exist or isn't yet completed");
						return true;
					}
					uSkyBlock.getInstance().getPlayer(split[2]).resetChallenge(split[1].toLowerCase());
					sender.sendMessage(ChatColor.YELLOW + "challange: " + split[1].toLowerCase() + " has been completed for " + split[2]);
				}
			}
		}
		return true;
	}
}