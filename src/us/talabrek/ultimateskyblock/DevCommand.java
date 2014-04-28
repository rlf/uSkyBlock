package us.talabrek.ultimateskyblock;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.talabrek.ultimateskyblock.async.IslandProtector;
import us.talabrek.ultimateskyblock.async.PartyListBuilder;
import us.talabrek.ultimateskyblock.async.Purger;
import us.talabrek.ultimateskyblock.async.TopGenerator;

public class DevCommand implements CommandExecutor {
	public void buildPartyList() 
	{
		Bukkit.getScheduler().runTaskAsynchronously(uSkyBlock.getInstance(), new PartyListBuilder());
	}
	
	private void displayHelp(CommandSender sender)
	{
		if (VaultHandler.hasPerm(sender, "usb.mod.protect") || VaultHandler.hasPerm(sender, "usb.mod.protectall")
				|| VaultHandler.hasPerm(sender, "usb.mod.topten")
				|| VaultHandler.hasPerm(sender, "usb.mod.orphan")
				|| VaultHandler.hasPerm(sender, "usb.admin.delete")
				|| VaultHandler.hasPerm(sender, "usb.admin.remove")
				|| VaultHandler.hasPerm(sender, "usb.admin.register")) {
			sender.sendMessage("[dev usage]");
			if (VaultHandler.hasPerm(sender, "usb.mod.protect"))
				sender.sendMessage(ChatColor.YELLOW + "/dev protect <player>:" + ChatColor.WHITE + " add protection to an island.");
			if (VaultHandler.hasPerm(sender, "usb.admin.reload"))
				sender.sendMessage(ChatColor.YELLOW + "/dev reload:" + ChatColor.WHITE + " reload configuration from file.");
			if (VaultHandler.hasPerm(sender, "usb.mod.protectall"))
				sender.sendMessage(ChatColor.YELLOW + "/dev protectall:" + ChatColor.WHITE + " add island protection to unprotected islands.");
			if (VaultHandler.hasPerm(sender, "usb.mod.topten"))
				sender.sendMessage(ChatColor.YELLOW + "/dev topten:" + ChatColor.WHITE + " manually update the top 10 list");
			if (VaultHandler.hasPerm(sender, "usb.mod.orphan"))
				sender.sendMessage(ChatColor.YELLOW + "/dev orphancount:" + ChatColor.WHITE + " unused island locations count");
			if (VaultHandler.hasPerm(sender, "usb.mod.orphan"))
				sender.sendMessage(ChatColor.YELLOW + "/dev clearorphan:" + ChatColor.WHITE + " remove any unused island locations.");
			if (VaultHandler.hasPerm(sender, "usb.mod.orphan"))
				sender.sendMessage(ChatColor.YELLOW + "/dev saveorphan:" + ChatColor.WHITE + " save the list of old (empty) island locations.");
			if (VaultHandler.hasPerm(sender, "usb.admin.delete"))
				sender.sendMessage(ChatColor.YELLOW + "/dev delete <player>:" + ChatColor.WHITE + " delete an island (removes blocks).");
			if (VaultHandler.hasPerm(sender, "usb.admin.remove"))
				sender.sendMessage(ChatColor.YELLOW + "/dev remove <player>:" + ChatColor.WHITE + " remove a player from an island.");
			if (VaultHandler.hasPerm(sender, "usb.admin.register"))
				sender.sendMessage(ChatColor.YELLOW + "/dev register <player>:" + ChatColor.WHITE + " set a player's island to your location");
			if (VaultHandler.hasPerm(sender, "usb.mod.challenges"))
				sender.sendMessage(ChatColor.YELLOW + "/dev completechallenge <challengename> <player>:" + ChatColor.WHITE + " marks a challenge as complete");
			if (VaultHandler.hasPerm(sender, "usb.mod.challenges"))
				sender.sendMessage(ChatColor.YELLOW + "/dev resetchallenge <challengename> <player>:" + ChatColor.WHITE + " marks a challenge as incomplete");
			if (VaultHandler.hasPerm(sender, "usb.mod.challenges"))
				sender.sendMessage(ChatColor.YELLOW + "/dev resetallchallenges <playername>:" + ChatColor.WHITE + " resets all of the player's challenges");
			if (VaultHandler.hasPerm(sender, "usb.mod.challenges"))
				sender.sendMessage(ChatColor.YELLOW + "/dev viewchallenges <playername>: " + ChatColor.WHITE + " views the completed challenges for that player");
			if (VaultHandler.hasPerm(sender, "usb.admin.purge"))
				sender.sendMessage(ChatColor.YELLOW + "/dev purge <datediff>:" + ChatColor.WHITE + " delete inactive islands older than <datediff>. The format is a standard date diff");
			if (VaultHandler.hasPerm(sender, "usb.mod.party"))
				sender.sendMessage(ChatColor.YELLOW + "/dev buildpartylist:" + ChatColor.WHITE + " build a new party list (use this if parties are broken).");
			if (VaultHandler.hasPerm(sender, "usb.mod.party"))
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
			int rankComplete = uSkyBlock.getInstance().checkRankCompletion(player.getUniqueId(), Settings.challenges_ranks[i - 1]);
			if (rankComplete <= 0)
				sender.sendMessage(ChatColor.GOLD + Settings.challenges_ranks[i] + ": " + uSkyBlock.getInstance().getChallengesFromRank(player, Settings.challenges_ranks[i]));
		}
	}
	
	private boolean onPlayerCommand(CommandSender sender, String[] split)
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
		
		UUIDPlayerInfo pi = uSkyBlock.getInstance().getPlayer(player.getUniqueId());
		
		if(pi == null)
		{
			sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
			return true;
		}
		
		if (split[0].equals("goto")) 
		{
			if(!VaultHandler.hasPerm(sender, "usb.mod.goto"))
			{
				sender.sendMessage(ChatColor.RED + "You do not have permission to use that.");
				return true;
			}
			
			if(!(sender instanceof Player))
			{
				sender.sendMessage(ChatColor.RED + "You must be a player to use this.");
				return true;
			}
			
			UUIDPlayerInfo info = uSkyBlock.getInstance().getPlayer(player.getUniqueId());
			if(info != null)
			{
				final Location target = info.getTeleportLocation();
				if(target != null)
				{
					sender.sendMessage(ChatColor.GOLD + "WARNING: No safe location found. Unsafe teleport in 5 seconds.");
					final Player sendingPlayer = (Player)sender;
					Bukkit.getScheduler().scheduleSyncDelayedTask(uSkyBlock.getInstance(), new Runnable()
					{
						@Override
						public void run()
						{
							if(!sendingPlayer.teleport(target))
								sendingPlayer.sendMessage(ChatColor.RED + "Error: Teleport was cancelled.");
						}
					}, 100L);
					
					return true;
				}
			}
			sender.sendMessage(ChatColor.RED + "Error: That player does not have an island!");
		} 
		else if (split[0].equals("remove")) 
		{
			if(!VaultHandler.hasPerm(sender, "usb.admin.remove"))
			{
				sender.sendMessage(ChatColor.RED + "You do not have permission to use that.");
				return true;
			}
			
			if (pi.getIslandLocation() != null) 
			{
				sender.sendMessage(ChatColor.YELLOW + "Removing " + player.getName() + "'s island.");
				uSkyBlock.getInstance().devDeletePlayerIsland(player.getUniqueId());
			}
			else
				sender.sendMessage("Error: That player does not have an island!");
		} 
		else if (split[0].equals("delete")) 
		{
			if(!VaultHandler.hasPerm(sender, "usb.admin.delete"))
			{
				sender.sendMessage(ChatColor.RED + "You do not have permission to use that.");
				return true;
			}
			
			if (pi.getIslandLocation() != null) 
			{
				sender.sendMessage(ChatColor.YELLOW + "Removing " + player.getName() + "'s island.");
				uSkyBlock.getInstance().removeIsland(pi);
			}
			else
				sender.sendMessage("Error: That player does not have an island!");
		} 
		else if (split[0].equals("register")) 
		{
			if(!VaultHandler.hasPerm(sender, "usb.admin.register"))
			{
				sender.sendMessage(ChatColor.RED + "You do not have permission to use that.");
				return true;
			}
			
			if(!(sender instanceof Player))
			{
				sender.sendMessage(ChatColor.RED + "You must be a player to use this.");
				return true;
			}
			
			if (pi.getHasIsland())
				uSkyBlock.getInstance().devDeletePlayerIsland(player.getUniqueId());
			
			if (uSkyBlock.getInstance().devSetPlayerIsland(sender, ((Player)sender).getLocation(), player.getUniqueId()))
				sender.sendMessage(ChatColor.GREEN + "Set " + player.getName() + "'s island to the bedrock nearest you.");
			else
				sender.sendMessage(ChatColor.RED + "Bedrock not found: unable to set the island!");
		} 
		else if (split[0].equals("info")) 
		{
			if(!VaultHandler.hasPerm(sender, "usb.mod.party"))
			{
				sender.sendMessage(ChatColor.RED + "You do not have permission to use that.");
				return true;
			}
			
			if (pi.getHasParty()) 
			{
				final UUIDPlayerInfo piL = uSkyBlock.getInstance().getPlayer(pi.getPartyLeader());
				final List<UUID> pList = piL.getMembers();
				if (pList.contains(player.getUniqueId()))
				{
					if (player.getUniqueId().equals(pi.getPartyLeader()))
						pList.remove(player.getUniqueId());
					else
						pList.remove(pi.getPartyLeader());
					
				}

				final List<String> pListStrings = new ArrayList<String>();
				for (UUID uuid : pList) {
                    System.out.println("Party members: " + uuid.toString());
					pListStrings.add(Bukkit.getOfflinePlayer(uuid).getName());
				}

				sender.sendMessage(ChatColor.GREEN + Bukkit.getOfflinePlayer(pi.getPartyLeader()).getName()
						+ " " + ChatColor.WHITE + pListStrings.toString());
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
					sender.sendMessage(ChatColor.RED + "Party leader: " + Bukkit.getOfflinePlayer(pi.getPartyLeader()).getName() + " should be null!");
				
				if (!pi.getMembers().isEmpty())
					sender.sendMessage(ChatColor.RED + "Player has party members, but shouldn't!");
				
			}
		} 
		else if (split[0].equals("resetallchallenges")) 
		{
			if(!VaultHandler.hasPerm(sender, "usb.mod.challenges"))
			{
				sender.sendMessage(ChatColor.RED + "You do not have permission to use that.");
				return true;
			}
			
			pi.resetAllChallenges();
			sender.sendMessage(ChatColor.YELLOW + player.getName() + " has had all challenges reset.");
		}
		else if(split[0].equals("viewchallenges"))
		{
			if(!VaultHandler.hasPerm(sender, "usb.mod.challenges"))
			{
				sender.sendMessage(ChatColor.RED + "You do not have permission to use that.");
				return true;
			}
			
			displayChallenges(sender, player);
		}
		else if(split[0].equals("protect"))
		{
			if(!VaultHandler.hasPerm(sender, "usb.mod.protect"))
			{
				sender.sendMessage(ChatColor.RED + "You do not have permission to use that.");
				return true;
			}
			
			if (Settings.island_protectWithWorldGuard) 
			{
				try
				{
					WorldGuardHandler.protectIsland(pi);
					sender.sendMessage(ChatColor.GREEN + "Protected " + player.getName() + "'s island.");
				}
				catch(IllegalArgumentException e)
				{
					sender.sendMessage(ChatColor.RED + e.getMessage());
				}
				catch(IllegalStateException e)
				{
					e.printStackTrace();
					sender.sendMessage(ChatColor.RED + "Unable to comply. A problem with WorldGuard occurred.");
				}
			} 
			else
				sender.sendMessage(ChatColor.RED + "You must enable WorldGuard protection in the config.yml to use this!");
		}
		else
		{
			sender.sendMessage(ChatColor.RED + "Unknown command " + ChatColor.YELLOW + "/dev " + split[0]);
			return true;
		}
		return true;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) 
	{
		if (split.length == 0)
			displayHelp(sender);
		else if (split.length == 1) 
		{
			if (split[0].equals("clearorphan")) 
			{
				if(!VaultHandler.hasPerm(sender, "usb.mod.orphan"))
				{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that.");
					return true;
				}
				
				sender.sendMessage(ChatColor.YELLOW + "Clearing all old (empty) island locations.");
				uSkyBlock.getInstance().clearOrphanedIsland();
			} 
			else if (split[0].equals("protectall")) 
			{
				if(!VaultHandler.hasPerm(sender, "usb.mod.protectall"))
				{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that.");
					return true;
				}
				
				if (Settings.island_protectWithWorldGuard) 
				{
					sender.sendMessage(ChatColor.YELLOW + "Protecting all unprotected player Islands.");
					Bukkit.getScheduler().runTaskAsynchronously(uSkyBlock.getInstance(), new IslandProtector());
				} 
				else
					sender.sendMessage(ChatColor.RED + "You must enable WorldGuard protection in the config.yml to use this!");
			} 
			else if (split[0].equals("buildpartylist")) 
			{
				if(!VaultHandler.hasPerm(sender, "usb.mod.party"))
				{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that.");
					return true;
				}
				
				sender.sendMessage(ChatColor.YELLOW + "Building party lists..");
				buildPartyList();
			} 
			else if (split[0].equals("orphancount")) 
			{
				if(!VaultHandler.hasPerm(sender, "usb.mod.orphan"))
				{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that.");
					return true;
				}
				
				sender.sendMessage(ChatColor.YELLOW + "" + uSkyBlock.getInstance().orphanCount()
						+ " old island locations will be used before new ones.");
			} 
			else if (split[0].equals("reload")) 
			{
				if(!VaultHandler.hasPerm(sender, "usb.admin.reload"))
				{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that.");
					return true;
				}
				
				uSkyBlock.getInstance().reloadConfig();
				uSkyBlock.getInstance().loadPluginConfig();
				sender.sendMessage(ChatColor.YELLOW + "Configuration reloaded from file.");
			} 
			else if (split[0].equals("saveorphan")) 
			{
				if(!VaultHandler.hasPerm(sender, "usb.mod.orphan"))
				{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that.");
					return true;
				}
				
				sender.sendMessage(ChatColor.YELLOW + "Saving the orphan list.");
				uSkyBlock.getInstance().saveOrphans();
			} 
			else if (split[0].equals("topten")) 
			{
				if(!VaultHandler.hasPerm(sender, "usb.mod.topten"))
				{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that.");
					return true;
				}
				
				sender.sendMessage(ChatColor.YELLOW + "Updating the Top Ten list");
				Bukkit.getScheduler().runTaskAsynchronously(uSkyBlock.getInstance(), new TopGenerator());
			} 
			else
			{
				sender.sendMessage(ChatColor.RED + "Unknown command " + ChatColor.YELLOW + "/dev " + split[0]);
				return true;
			}
		} 
		else if (split.length == 2) 
		{
			if (split[0].equals("purge")) 
			{
				if(!VaultHandler.hasPerm(sender, "usb.admin.purge"))
				{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that.");
					return true;
				}
				
				long time = Misc.parseDateDiff(split[1]);
				
				if(time == 0)
				{
					sender.sendMessage(ChatColor.RED + "Unknown date diff format: " + split[1]);
					return true;
				}
				else if(time < 0)
				{
					sender.sendMessage(ChatColor.RED + "Cannot use a negative date diff here (that would be in the future!).");
					return true;
				}
				
				sender.sendMessage(ChatColor.YELLOW + "Purging islands for players that have not been on for " + Misc.dateDiffToString(time, false) + ".");
				Bukkit.getScheduler().runTaskAsynchronously(uSkyBlock.getInstance(), new Purger(time, false));
			} 
			else
				return onPlayerCommand(sender, split);
		} 
		else if (split.length == 3) 
		{
			OfflinePlayer player = Bukkit.getOfflinePlayer(split[2]);
			if(!player.hasPlayedBefore())
			{
				player = Bukkit.getPlayer(split[2]);
				if(player == null)
				{
					sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
					return true;
				}
			}
			
			UUIDPlayerInfo pi = uSkyBlock.getInstance().getPlayer(player.getUniqueId());
			
			if(pi == null)
			{
				sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
				return true;
			}
			
			if (split[0].equals("completechallenge")) 
			{
				if(!VaultHandler.hasPerm(sender, "usb.mod.challenges"))
				{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that.");
					return true;
				}
				
				if (pi.checkChallenge(split[1].toLowerCase()) || !pi.challengeExists(split[1].toLowerCase())) 
				{
					sender.sendMessage(ChatColor.RED + "Challenge doesn't exist or is already completed");
					return true;
				}
				
				pi.completeChallenge(split[1].toLowerCase());
				sender.sendMessage(ChatColor.YELLOW + "challange: " + split[1].toLowerCase() + " has been completed for " + player.getName());
			} 
			else if (split[0].equals("resetchallenge")) 
			{
				if(!VaultHandler.hasPerm(sender, "usb.mod.challenges"))
				{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that.");
					return true;
				}
				
				if (!pi.checkChallenge(split[1].toLowerCase()) || !pi.challengeExists(split[1].toLowerCase())) 
				{
					sender.sendMessage(ChatColor.RED + "Challenge doesn't exist or isn't yet completed");
					return true;
				}
				
				pi.resetChallenge(split[1].toLowerCase());
				sender.sendMessage(ChatColor.YELLOW + "challange: " + split[1].toLowerCase() + " has been completed for " + player.getName());
			}
			else
			{
				sender.sendMessage(ChatColor.RED + "Unknown command " + ChatColor.YELLOW + "/dev " + split[0]);
				return true;
			}
		}
		return true;
	}
}