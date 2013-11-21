package us.talabrek.ultimateskyblock;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.HashBiMap;

import us.talabrek.ultimateskyblock.async.IslandBuilder;

public class IslandCommand implements CommandExecutor {
	public boolean allowInfo = true;
	private final HashBiMap<String, String> inviteList = HashBiMap.create();
	public Location Islandlocation;
	private String tempLeader;
	private List<String> tempParty;
	private String tempTargetPlayer;
	String tPlayer;

	public IslandCommand() {
		inviteList.put("NoInvited", "NoInviter");
	}

	private boolean addPlayertoParty(final String playername, final String partyleader) {
		if (!uSkyBlock.getInstance().isActivePlayer(playername)) {
			System.out.println("uSkyblock " + "Failed to add player to party! (" + playername + ")");
			return false;
		}
		if (!uSkyBlock.getInstance().isActivePlayer(partyleader)) {
			System.out.println("uSkyblock " + "Failed to add player to party! (" + playername + ")");
			return false;
		}
		System.out.println("uSkyblock " + "Adding player: " + playername + " to party with leader: " + partyleader);
		uSkyBlock.getInstance().getPlayer(playername).setJoinParty(partyleader, uSkyBlock.getInstance().getPlayer(partyleader).getIslandLocation());
		if (!playername.equalsIgnoreCase(partyleader)) {
			if (uSkyBlock.getInstance().getPlayer(partyleader).getHomeLocation() != null) {
				uSkyBlock.getInstance().getPlayer(playername).setHomeLocation(uSkyBlock.getInstance().getPlayer(partyleader).getHomeLocation());
			} else {
				uSkyBlock.getInstance().getPlayer(playername).setHomeLocation(uSkyBlock.getInstance().getPlayer(partyleader).getIslandLocation());
			}

			if (!uSkyBlock.getInstance().getPlayer(partyleader).getMembers().contains(playername)) {
				uSkyBlock.getInstance().getPlayer(partyleader).addMember(playername);
			}
			if (!uSkyBlock.getInstance().getPlayer(partyleader).getMembers().contains(partyleader)) {
				uSkyBlock.getInstance().getPlayer(partyleader).addMember(partyleader);
			}
		}
		uSkyBlock.getInstance().writePlayerFile(playername, uSkyBlock.getInstance().getPlayer(playername));

		if (!uSkyBlock.getInstance().getPlayer(playername).getPartyLeader().equalsIgnoreCase(partyleader)) {
			System.out.println("uSkyblock " + "Error adding player to a new party!");
			return false;
		}
		return true;
	}

	

	

	

	private int calculateIslandLevel(Location l) 
	{
		if (l == null)
			return 0;

		int cobbleCount = 0;
		int blockCount = 0;
		int px = l.getBlockX();
		int py = l.getBlockY();
		int pz = l.getBlockZ();
		for (int x = -50; x <= 50; ++x) 
		{
			for (int y = 0; y <= 255; ++y) 
			{
				for (int z = -50; z <= 50; ++z) 
				{
					final Block b = new Location(l.getWorld(), px + x, py + y, pz + z).getBlock();
					switch(b.getType())
					{
					case DIAMOND_BLOCK:
					case EMERALD_BLOCK:
					case BEACON:
						blockCount += 300;
						break;
					case GOLD_BLOCK:
					case ENCHANTMENT_TABLE:
					case DRAGON_EGG:
						blockCount += 150;
						break;
					case OBSIDIAN:
					case IRON_BLOCK:
					case REDSTONE_BLOCK:
						blockCount += 10;
						break;
					case BOOKSHELF:
					case JUKEBOX:
					case HARD_CLAY:
					case STAINED_CLAY:
						blockCount += 5;
						break;
					case ICE:
					case CLAY:
					case NETHER_BRICK:
					case GRASS:
					case MYCEL:
					case GLOWSTONE:
					case NETHER_BRICK_STAIRS:
					case QUARTZ_BLOCK:
					case QUARTZ_STAIRS:
						blockCount += 3;
						break;
					case SMOOTH_BRICK:
					case BRICK:
					case WOOL:
					case SANDSTONE:
					case ENDER_STONE:
					case BRICK_STAIRS:
					case SMOOTH_STAIRS:
					case DOUBLE_STEP:
					case GLASS:
						blockCount += 2;
						break;
					case COBBLESTONE:
						if(cobbleCount < 10000)
						{
							++cobbleCount;
							++blockCount;
						}
						break;
						
					// 0 pointers
					case WATER:
					case STATIONARY_WATER:
					case LAVA:
					case STATIONARY_LAVA:
						break;
						
					default:
						++blockCount;
						break;
					}
				}
			}
		}

		return blockCount / 100;
	}

	private boolean getIslandLevel(final Player player, final String islandPlayer) {
		if (allowInfo) {
			System.out.println("uSkyblock " + "Preparing to calculate island level");
			allowInfo = false;
			if (!uSkyBlock.getInstance().hasIsland(islandPlayer) && !uSkyBlock.getInstance().hasParty(islandPlayer)) {
				player.sendMessage(ChatColor.RED + "That player is invalid or does not have an island!");
				allowInfo = true;
				return false;
			}
			try {

				Location l;
				PlayerInfo playerInfo = uSkyBlock.getInstance().getPlayer(player.getName());
				if (playerInfo.getHasParty()) {
					l = playerInfo.getPartyIslandLocation();
				} else {
					l = playerInfo.getIslandLocation();
				}

				if (player.getName().equalsIgnoreCase(islandPlayer)) {
					playerInfo.setIslandLevel(calculateIslandLevel(l));
				}

			} catch (final Exception e) {
				System.out.println("uSkyblock " + "Error while calculating Island Level: " + e);
				e.printStackTrace();
				allowInfo = true;
			}

			allowInfo = true;

			if (player != null) {
				player.sendMessage(ChatColor.YELLOW + "Information about " + islandPlayer + "'s Island:");
				if (player.getName().equalsIgnoreCase(islandPlayer)) {
					player.sendMessage(ChatColor.GREEN + "Island level is " + ChatColor.WHITE + uSkyBlock.getInstance().getPlayer(player.getName()).getIslandLevel());
				} else {
					final PlayerInfo pi = uSkyBlock.getInstance().getPlayer(islandPlayer);
					if (pi != null) {
						if (pi.getIslandLevel() == 0) {
							player.sendMessage(ChatColor.RED + "Recalculating, please wait");

							Location l;

							if (pi.getHasParty()) {
								l = pi.getPartyIslandLocation();
							} else {
								l = pi.getIslandLocation();
							}

							if (l == null) {
								player.sendMessage(ChatColor.RED + "Error: Invalid Player");
								return true;
							}

							pi.setIslandLevel(calculateIslandLevel(l));

							Bukkit.getScheduler().runTaskAsynchronously(uSkyBlock.getInstance(), new Runnable() {

								@Override
								public void run() {
									uSkyBlock.getInstance().writePlayerFile(pi.getPlayerName(), pi);
								}

							});
						}
						player.sendMessage(ChatColor.GREEN + "Island level is " + ChatColor.WHITE + pi.getIslandLevel());
					} else {
						player.sendMessage(ChatColor.RED + "Error: Invalid Player");
					}
				}
			}
		} else {
			player.sendMessage(ChatColor.RED + "Can't use that command right now! Try again in a few seconds.");
			System.out.println("uSkyblock " + player.getName() + " tried to use /island info but someone else used it first!");
			return false;
		}
		return true;
	}

	private void inviteDebug(final Player player) {
		player.sendMessage(inviteList.toString());
	}

	private void invitePurge() {
		inviteList.clear();
		inviteList.put("NoInviter", "NoInvited");
	}

	

	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) 
	{
		if (!(sender instanceof Player)) 
			return false;

		if (!VaultHandler.hasPerm(sender, "usb.island.create")) 
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to use that command!");
			return true;
		}
		
		Player player = (Player) sender;
		PlayerInfo pi = uSkyBlock.getInstance().getPlayer(sender.getName());
		if (pi == null) 
		{
			player.sendMessage(ChatColor.RED + "Error: Couldn't read your player data!");
			return true;
		}

		if (uSkyBlock.getInstance().hasParty(player.getName())) 
		{
			tempLeader = pi.getPartyLeader();
			tempParty = pi.getMembers();
		}

		if (pi.getIslandLocation() != null || pi.getHasParty()) {
			if (split.length == 0)
			{
				if(uSkyBlock.getInstance().homeTeleport(player, player))
					player.sendMessage(ChatColor.GREEN + "Teleporting you to your island. (/island help for more info)");
				else if(pi.getHasIsland() || pi.getHasParty())
					player.sendMessage(ChatColor.RED + "There is no safe location to put you. Please contact a moderator or admin.");

				return true;
			}
			if (split.length == 1) {
				if (split[0].equals("restart") || split[0].equals("reset")) {
					if (pi.getHasParty()) {
						if (!pi.getPartyLeader().equalsIgnoreCase(player.getName())) {
							player.sendMessage(ChatColor.RED + "Only the owner may restart this island. Leave this island in order to start your own (/island leave).");
						} else {
							player.sendMessage(ChatColor.YELLOW + "You must remove all players from your island before you can restart it (/island kick <player>). See a list of players currently part of your island using /island party.");
						}
						return true;
					}
					if (!uSkyBlock.getInstance().onRestartCooldown(player) || Settings.general_cooldownRestart == 0) 
					{
						player.sendMessage(ChatColor.GREEN + "Creating a new island for you.");
						// TODO: THis is causing issues, the island is removed after because this is a delayed task
						uSkyBlock.getInstance().restartIsland(pi);
						uSkyBlock.getInstance().setRestartCooldown(player);
						return true;
					}

					player.sendMessage(ChatColor.YELLOW + "You can restart your island in " + uSkyBlock.getInstance().getRestartCooldownTime(player) / 1000L + " seconds.");
					return true;
				}
				if ((split[0].equals("sethome") || split[0].equals("tpset")) && VaultHandler.checkPerk(player.getName(), "usb.island.sethome", player.getWorld())) {
					uSkyBlock.getInstance().homeSet(player);
					return true;
				}
				if (split[0].equals("lock")) {
					if (Settings.island_allowIslandLock && VaultHandler.checkPerk(player.getName(), "usb.lock", player.getWorld())) {
						WorldGuardHandler.islandLock(sender, player.getName());
					} else {
						player.sendMessage(ChatColor.RED + "You don't have access to this command!");
					}
					return true;
				}
				if (split[0].equals("unlock")) {
					if (Settings.island_allowIslandLock && VaultHandler.checkPerk(player.getName(), "usb.lock", player.getWorld())) {
						WorldGuardHandler.islandUnlock(sender, player.getName());
					} else {
						player.sendMessage(ChatColor.RED + "You don't have access to this command!");
					}
					return true;
				}
				if (split[0].equals("help")) {
					player.sendMessage(ChatColor.GREEN + "[SkyBlock command usage]");

					player.sendMessage(ChatColor.YELLOW + "/island :" + ChatColor.WHITE + " start your island, or teleport back to one you have.");
					player.sendMessage(ChatColor.YELLOW + "/island restart :" + ChatColor.WHITE + " delete your island and start a new one.");
					player.sendMessage(ChatColor.YELLOW + "/island sethome :" + ChatColor.WHITE + " set your island teleport point.");
					if (Settings.island_useIslandLevel) {
						player.sendMessage(ChatColor.YELLOW + "/island level :" + ChatColor.WHITE + " check your island level");
						player.sendMessage(ChatColor.YELLOW + "/island level <player> :" + ChatColor.WHITE + " check another player's island level.");
					}
					if (VaultHandler.checkPerk(player.getName(), "usb.party.create", player.getWorld())) {
						player.sendMessage(ChatColor.YELLOW + "/island party :" + ChatColor.WHITE + " view your party information.");
						player.sendMessage(ChatColor.YELLOW + "/island invite <player>:" + ChatColor.WHITE + " invite a player to join your island.");
						player.sendMessage(ChatColor.YELLOW + "/island leave :" + ChatColor.WHITE + " leave another player's island.");
					}
					if (VaultHandler.checkPerk(player.getName(), "usb.party.kick", player.getWorld())) {
						player.sendMessage(ChatColor.YELLOW + "/island kick <player>:" + ChatColor.WHITE + " remove a player from your island.");
					}
					if (VaultHandler.checkPerk(player.getName(), "usb.party.join", player.getWorld())) {
						player.sendMessage(ChatColor.YELLOW + "/island [accept/reject]:" + ChatColor.WHITE + " accept/reject an invitation.");
					}
					if (VaultHandler.checkPerk(player.getName(), "usb.party.makeleader", player.getWorld())) {
						player.sendMessage(ChatColor.YELLOW + "/island makeleader <player>:" + ChatColor.WHITE + " transfer the island to <player>.");
					}
					player.sendMessage(ChatColor.YELLOW + "/island top :" + ChatColor.WHITE + " see the top ranked islands.");
					if (Settings.island_allowIslandLock) {
						if (!VaultHandler.checkPerk(player.getName(), "usb.lock", player.getWorld())) {
							player.sendMessage(ChatColor.DARK_GRAY + "/island lock :" + ChatColor.GRAY + " non-group members can't enter your island.");
							player.sendMessage(ChatColor.DARK_GRAY + "/island unlock :" + ChatColor.GRAY + " allow anyone to enter your island.");
						} else {
							player.sendMessage(ChatColor.YELLOW + "/island lock :" + ChatColor.WHITE + " non-group members can't enter your island.");
							player.sendMessage(ChatColor.YELLOW + "/island unlock :" + ChatColor.WHITE + " allow anyone to enter your island.");
						}

					}

					return true;
				}
				if (split[0].equals("top") && VaultHandler.checkPerk(player.getName(), "usb.island.topten", player.getWorld())) {
					uSkyBlock.getInstance().displayTopTen(player);
					return true;
				}
				if ((split[0].equals("info") || split[0].equals("level")) && VaultHandler.checkPerk(player.getName(), "usb.island.info", player.getWorld()) && Settings.island_useIslandLevel) {
					if (uSkyBlock.getInstance().playerIsOnIsland(player)) {
						if (true) {// !uSkyBlock.getInstance().onInfoCooldown(player)
									// || Settings.general_cooldownInfo == 0) {
							// uSkyBlock.getInstance().setInfoCooldown(player);
							if (!pi.getHasParty() && !pi.getHasIsland()) {
								player.sendMessage(ChatColor.RED + "You do not have an island!");
							} else {
								getIslandLevel(player, player.getName());
							}
							return true;
						}

						// player.sendMessage(ChatColor.YELLOW +
						// "You can use that command again in "
						// + uSkyBlock.getInstance().getInfoCooldownTime(player)
						// / 1000L + " seconds.");
						// return true;
					}

					player.sendMessage(ChatColor.YELLOW + "You must be on your island to use this command.");
					return true;
				}
				if (split[0].equals("invite") && VaultHandler.checkPerk(player.getName(), "usb.party.create", player.getWorld())) {
					player.sendMessage(ChatColor.YELLOW + "Use" + ChatColor.WHITE + " /island invite <playername>" + ChatColor.YELLOW + " to invite a player to your island.");
					if (uSkyBlock.getInstance().hasParty(player.getName())) {
						if (tempLeader.equalsIgnoreCase(player.getName())) {
							if (VaultHandler.checkPerk(player.getName(), "usb.extra.partysize", player.getWorld())) {
								if (tempParty.size() < Settings.general_maxPartySize * 2) {
									player.sendMessage(ChatColor.GREEN + "You can invite " + (Settings.general_maxPartySize * 2 - tempParty.size()) + " more players.");
								} else {
									player.sendMessage(ChatColor.RED + "You can't invite any more players.");
								}
								return true;
							}

							if (tempParty.size() < Settings.general_maxPartySize) {
								player.sendMessage(ChatColor.GREEN + "You can invite " + (Settings.general_maxPartySize - tempParty.size()) + " more players.");
							} else {
								player.sendMessage(ChatColor.RED + "You can't invite any more players.");
							}
							return true;
						}

						player.sendMessage(ChatColor.RED + "Only the island's owner can invite!");
						return true;
					}

					return true;
				}
				if (split[0].equals("accept") && VaultHandler.checkPerk(player.getName(), "usb.party.join", player.getWorld())) {
					if (uSkyBlock.getInstance().onInfoCooldown(player) && Settings.general_cooldownInfo > 0) {
						player.sendMessage(ChatColor.YELLOW + "You can't join an island for another " + uSkyBlock.getInstance().getRestartCooldownTime(player) / 1000L + " seconds.");
						return true;
					}
					if (!uSkyBlock.getInstance().hasParty(player.getName()) && inviteList.containsKey(player.getName())) {
						if (!uSkyBlock.getInstance().hasParty(inviteList.get(player.getName()))) {
							if (pi.getHasIsland()) {
								uSkyBlock.getInstance().removeIsland(pi);
							}

							addPlayertoParty(player.getName(), inviteList.get(player.getName()));
							addPlayertoParty(inviteList.get(player.getName()), inviteList.get(player.getName()));
							player.sendMessage(ChatColor.GREEN + "You have joined an island! Use /island party to see the other members.");
							if (Bukkit.getPlayer(inviteList.get(player.getName())) != null) {
								Bukkit.getPlayer(inviteList.get(player.getName())).sendMessage(ChatColor.GREEN + player.getName() + " has joined your island!");
							}
						} else {
							if (pi.getHasIsland()) {
								uSkyBlock.getInstance().removeIsland(pi);
							}
							player.sendMessage(ChatColor.GREEN + "You have joined an island! Use /island party to see the other members.");
							addPlayertoParty(player.getName(), inviteList.get(player.getName()));
							if (Bukkit.getPlayer(inviteList.get(player.getName())) != null) {
								Bukkit.getPlayer(inviteList.get(player.getName())).sendMessage(ChatColor.GREEN + player.getName() + " has joined your island!");
							} else {
								player.sendMessage(ChatColor.RED + "You couldn't join the island, maybe it's full.");

								return true;
							}
						}
						uSkyBlock.getInstance().setRestartCooldown(player);

						uSkyBlock.getInstance().homeTeleport(player, player);

						player.getInventory().clear();
						player.getEquipment().clear();

						if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
							if (WorldGuardHandler.getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(inviteList.get(player.getName()) + "Island")) {
								WorldGuardHandler.addPlayerToOldRegion(inviteList.get(player.getName()), player.getName());
							}
						}
						inviteList.remove(player.getName());
						return true;
					}

					player.sendMessage(ChatColor.RED + "You can't use that command right now.");
					return true;
				}

				if (split[0].equals("reject")) {
					if (inviteList.containsKey(player.getName())) {
						player.sendMessage(ChatColor.YELLOW + "You have rejected the invitation to join an island.");
						if (Bukkit.getPlayer(inviteList.get(player.getName())) != null) {
							Bukkit.getPlayer(inviteList.get(player.getName())).sendMessage(ChatColor.RED + player.getName() + " has rejected your island invite!");
						}
						inviteList.remove(player.getName());
					} else {
						player.sendMessage(ChatColor.RED + "You haven't been invited.");
					}
					return true;
				}

				if (split[0].equalsIgnoreCase("partypurge")) {
					if (VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld())) {
						player.sendMessage(ChatColor.RED + "This command no longer functions!");
					} else {
						player.sendMessage(ChatColor.RED + "You can't access that command!");
					}
					return true;
				}
				if (split[0].equalsIgnoreCase("partyclean")) {
					if (VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld())) {
						player.sendMessage(ChatColor.RED + "This command no longer functions!");
					} else {
						player.sendMessage(ChatColor.RED + "You can't access that command!");
					}
					return true;
				}
				if (split[0].equalsIgnoreCase("purgeinvites")) {
					if (VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld())) {
						player.sendMessage(ChatColor.RED + "Deleting all invites!");
						invitePurge();
					} else {
						player.sendMessage(ChatColor.RED + "You can't access that command!");
					}
					return true;
				}
				if (split[0].equalsIgnoreCase("partylist")) {
					if (VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld())) {
						player.sendMessage(ChatColor.RED + "This command is currently not active.");
					} else {
						player.sendMessage(ChatColor.RED + "You can't access that command!");
					}
					return true;
				}
				if (split[0].equalsIgnoreCase("invitelist")) {
					if (VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld())) {
						player.sendMessage(ChatColor.RED + "Checking Invites.");
						inviteDebug(player);
					} else {
						player.sendMessage(ChatColor.RED + "You can't access that command!");
					}
					return true;
				}

				if (split[0].equals("leave") && VaultHandler.checkPerk(player.getName(), "usb.party.join", player.getWorld())) {
					if (player.getWorld().getName().equalsIgnoreCase(uSkyBlock.getSkyBlockWorld().getName())) {
						if (uSkyBlock.getInstance().hasParty(player.getName())) {
							if (pi.getPartyLeader().equalsIgnoreCase(player.getName())) {
								player.sendMessage(ChatColor.YELLOW + "You are the leader, use /island remove <player> instead.");
								return true;
							}

							player.getInventory().clear();
							player.getEquipment().clear();
							if (Settings.extras_sendToSpawn) {
								player.performCommand("spawn");
							} else {
								player.teleport(uSkyBlock.getSkyBlockWorld().getSpawnLocation());
							}

							removePlayerFromParty(player.getName(), tempLeader);

							player.sendMessage(ChatColor.YELLOW + "You have left the island and returned to the player spawn.");
							if (Bukkit.getPlayer(tempLeader) != null) {
								Bukkit.getPlayer(tempLeader).sendMessage(ChatColor.RED + player.getName() + " has left your island!");
							}
							tempParty.remove(player.getName());
							if (tempParty.size() < 2) {
								removePlayerFromParty(tempLeader, tempLeader);
							}

							if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
								WorldGuardHandler.removePlayerFromRegion(tempLeader, player.getName());
							}
						} else {
							player.sendMessage(ChatColor.RED + "You can't leave your island if you are the only person. Try using /island restart if you want a new one!");
							return true;
						}
					} else {
						player.sendMessage(ChatColor.RED + "You must be in the skyblock world to leave your party!");
					}
					return true;
				}
				if (split[0].equals("party")) {
					if (VaultHandler.checkPerk(player.getName(), "usb.party.create", player.getWorld())) {
						player.sendMessage(ChatColor.WHITE + "/island invite <playername>" + ChatColor.YELLOW + " to invite a player to join your island.");
					}
					if (uSkyBlock.getInstance().hasParty(player.getName())) {
						player.sendMessage(ChatColor.WHITE + "/island leave" + ChatColor.YELLOW + " leave your current island and return to spawn");
						if (tempLeader.equalsIgnoreCase(sender.getName())) {
							if (VaultHandler.checkPerk(player.getName(), "usb.party.kick", player.getWorld())) {
								player.sendMessage(ChatColor.WHITE + "/island remove <playername>" + ChatColor.YELLOW + " remove <playername> from your island");
							}
							if (VaultHandler.checkPerk(player.getName(), "usb.party.makeleader", player.getWorld())) {
								player.sendMessage(ChatColor.WHITE + "/island makeleader <playername>" + ChatColor.YELLOW + " give ownership of the island to <playername>");
							}
							if (VaultHandler.checkPerk(player.getName(), "usb.extra.partysize", player.getWorld())) {
								if (tempParty.size() < Settings.general_maxPartySize * 2) {
									player.sendMessage(ChatColor.GREEN + "You can invite " + (Settings.general_maxPartySize * 2 - tempParty.size()) + " more players.");
								} else {
									player.sendMessage(ChatColor.RED + "You can't invite any more players.");
								}

							} else if (tempParty.size() < Settings.general_maxPartySize) {
								player.sendMessage(ChatColor.GREEN + "You can invite " + (Settings.general_maxPartySize - tempParty.size()) + " more players.");
							} else {
								player.sendMessage(ChatColor.RED + "You can't invite any more players.");
							}

						}

						player.sendMessage(ChatColor.YELLOW + "Listing your island members:");
						final PlayerInfo tPi = uSkyBlock.getInstance().getPlayer(tempLeader);
						player.sendMessage(ChatColor.WHITE + tPi.getMembers().toString());
					} else if (inviteList.containsKey(player.getName())) {
						player.sendMessage(ChatColor.YELLOW + inviteList.get(player.getName()) + " has invited you to join their island.");
						player.sendMessage(ChatColor.WHITE + "/island [accept/reject]" + ChatColor.YELLOW + " to accept or reject the invite.");
					}
					return true;
				}
			} else if (split.length == 2) {
				if ((split[0].equals("info") || split[0].equals("level")) && VaultHandler.checkPerk(player.getName(), "usb.island.info", player.getWorld()) && Settings.island_useIslandLevel) {
					if (true) {// !uSkyBlock.getInstance().onInfoCooldown(player)
								// || Settings.general_cooldownInfo == 0) {
						// uSkyBlock.getInstance().setInfoCooldown(player);
						if (!pi.getHasParty() && !pi.getHasIsland()) {
							player.sendMessage(ChatColor.RED + "You do not have an island!");
						} else {
							getIslandLevel(player, split[1]);
						}
						return true;
					}

					// player.sendMessage(ChatColor.YELLOW +
					// "You can use that command again in "
					// + uSkyBlock.getInstance().getInfoCooldownTime(player) /
					// 1000L + " seconds.");
					// return true;
				}
				if (split[0].equalsIgnoreCase("invite") && VaultHandler.checkPerk(player.getName(), "usb.party.create", player.getWorld())) {
					if (Bukkit.getPlayer(split[1]) == null) {
						player.sendMessage(ChatColor.RED + "That player is offline or doesn't exist.");
						return true;
					}
					if (!Bukkit.getPlayer(split[1]).isOnline()) {
						player.sendMessage(ChatColor.RED + "That player is offline or doesn't exist.");
						return true;
					}
					if (!uSkyBlock.getInstance().hasIsland(player.getName())) {
						player.sendMessage(ChatColor.RED + "You must have an island in order to invite people to it!");
						return true;
					}
					if (player.getName().equalsIgnoreCase(Bukkit.getPlayer(split[1]).getName())) {
						player.sendMessage(ChatColor.RED + "You can't invite yourself!");
						return true;
					}
					if (uSkyBlock.getInstance().hasParty(player.getName())) {
						if (tempLeader.equalsIgnoreCase(player.getName())) {
							if (!uSkyBlock.getInstance().hasParty(Bukkit.getPlayer(split[1]).getName())) {
								if (VaultHandler.checkPerk(player.getName(), "usb.extra.partysize", player.getWorld())) {
									if (tempParty.size() < Settings.general_maxPartySize * 2) {
										if (inviteList.containsValue(player.getName())) {
											inviteList.inverse().remove(player.getName());
											player.sendMessage(ChatColor.YELLOW + "Removing your previous invite.");
										}
										inviteList.put(Bukkit.getPlayer(split[1]).getName(), player.getName());
										player.sendMessage(ChatColor.GREEN + "Invite sent to " + Bukkit.getPlayer(split[1]).getName());

										Bukkit.getPlayer(split[1]).sendMessage(player.getName() + " has invited you to join their island!");
										Bukkit.getPlayer(split[1]).sendMessage(ChatColor.WHITE + "/island [accept/reject]" + ChatColor.YELLOW + " to accept or reject the invite.");
										Bukkit.getPlayer(split[1]).sendMessage(ChatColor.RED + "WARNING: You will lose your current island if you accept!");
									} else {
										player.sendMessage(ChatColor.RED + "Your island is full, you can't invite anyone else.");
									}
								} else if (tempParty.size() < Settings.general_maxPartySize) {
									if (inviteList.containsValue(player.getName())) {
										inviteList.inverse().remove(player.getName());
										player.sendMessage(ChatColor.YELLOW + "Removing your previous invite.");
									}
									inviteList.put(Bukkit.getPlayer(split[1]).getName(), player.getName());
									player.sendMessage(ChatColor.GREEN + "Invite sent to " + Bukkit.getPlayer(split[1]).getName());

									Bukkit.getPlayer(split[1]).sendMessage(player.getName() + " has invited you to join their island!");
									Bukkit.getPlayer(split[1]).sendMessage(ChatColor.WHITE + "/island [accept/reject]" + ChatColor.YELLOW + " to accept or reject the invite.");
									Bukkit.getPlayer(split[1]).sendMessage(ChatColor.RED + "WARNING: You will lose your current island if you accept!");
								} else {
									player.sendMessage(ChatColor.RED + "Your island is full, you can't invite anyone else.");
								}
							} else {
								player.sendMessage(ChatColor.RED + "That player is already with a group on an island.");
							}
						} else {
							player.sendMessage(ChatColor.RED + "Only the island's owner may invite new players.");
						}
					} else {
						if (!uSkyBlock.getInstance().hasParty(player.getName())) {
							if (!uSkyBlock.getInstance().hasParty(Bukkit.getPlayer(split[1]).getName())) {
								if (inviteList.containsValue(player.getName())) {
									inviteList.inverse().remove(player.getName());
									player.sendMessage(ChatColor.YELLOW + "Removing your previous invite.");
								}
								inviteList.put(Bukkit.getPlayer(split[1]).getName(), player.getName());

								player.sendMessage(ChatColor.GREEN + "Invite sent to " + Bukkit.getPlayer(split[1]).getName());
								Bukkit.getPlayer(split[1]).sendMessage(player.getName() + " has invited you to join their island!");
								Bukkit.getPlayer(split[1]).sendMessage(ChatColor.WHITE + "/island [accept/reject]" + ChatColor.YELLOW + " to accept or reject the invite.");
								Bukkit.getPlayer(split[1]).sendMessage(ChatColor.RED + "WARNING: You will lose your current island if you accept!");
							} else {
								player.sendMessage(ChatColor.RED + "That player is already with a group on an island.");
							}
							return true;
						}

						player.sendMessage(ChatColor.RED + "Only the island's owner may invite new players!");
						return true;
					}
					return true;
				}
				if ((split[0].equalsIgnoreCase("remove") || split[0].equalsIgnoreCase("kick")) && VaultHandler.checkPerk(player.getName(), "usb.party.kick", player.getWorld())) {
					if (Bukkit.getPlayer(split[1]) == null && Bukkit.getOfflinePlayer(split[1]) == null) {
						player.sendMessage(ChatColor.RED + "That player doesn't exist.");
						return true;
					}
					if (Bukkit.getPlayer(split[1]) == null) {
						tempTargetPlayer = Bukkit.getOfflinePlayer(split[1]).getName();
					} else {
						tempTargetPlayer = Bukkit.getPlayer(split[1]).getName();
					}
					if (tempParty.contains(split[1])) {
						tempTargetPlayer = split[1];
					}
					if (uSkyBlock.getInstance().hasParty(player.getName())) {
						if (tempLeader.equalsIgnoreCase(player.getName())) {
							if (tempParty.contains(tempTargetPlayer)) {
								if (player.getName().equalsIgnoreCase(tempTargetPlayer)) {
									player.sendMessage(ChatColor.RED + "Use /island leave to remove all people from your island");
									return true;
								}
								if (Bukkit.getPlayer(split[1]) != null) {
									if (Bukkit.getPlayer(split[1]).getWorld().getName().equalsIgnoreCase(uSkyBlock.getSkyBlockWorld().getName())) {
										Bukkit.getPlayer(split[1]).getInventory().clear();
										Bukkit.getPlayer(split[1]).getEquipment().clear();
										Bukkit.getPlayer(split[1]).sendMessage(ChatColor.RED + player.getName() + " has removed you from their island!");
									}
									if (Settings.extras_sendToSpawn) {
										Bukkit.getPlayer(split[1]).performCommand("spawn");
									} else {
										Bukkit.getPlayer(split[1]).teleport(uSkyBlock.getSkyBlockWorld().getSpawnLocation());
									}

								}

								if (Bukkit.getPlayer(tempLeader) != null) {
									Bukkit.getPlayer(tempLeader).sendMessage(ChatColor.RED + tempTargetPlayer + " has been removed from the island.");
								}
								removePlayerFromParty(tempTargetPlayer, tempLeader);
								tempParty.remove(tempTargetPlayer);
								if (tempParty.size() < 2) {
									removePlayerFromParty(player.getName(), tempLeader);
								}

								if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
									WorldGuardHandler.removePlayerFromRegion(player.getName(), tempTargetPlayer);
								}
							} else {
								System.out.println("uSkyblock " + "Player " + player.getName() + " failed to remove " + tempTargetPlayer);
								player.sendMessage(ChatColor.RED + "That player is not part of your island group!");
							}
						} else {
							player.sendMessage(ChatColor.RED + "Only the island's owner may remove people from the island!");
						}
					} else {
						player.sendMessage(ChatColor.RED + "No one else is on your island, are you seeing things?");
					}
					return true;
				}
				if (split[0].equalsIgnoreCase("makeleader") && VaultHandler.checkPerk(player.getName(), "usb.party.makeleader", player.getWorld())) {
					if (Bukkit.getPlayer(split[1]) == null) {
						player.sendMessage(ChatColor.RED + "That player must be online to transfer the island.");
						return true;
					}

					if (!uSkyBlock.getInstance().isActivePlayer(player.getName()) || !uSkyBlock.getInstance().isActivePlayer(Bukkit.getPlayer(split[1]).getName())) {
						player.sendMessage(ChatColor.RED + "Both players must be online to transfer an island.");
						return true;
					}

					if (!pi.getHasParty()) {
						player.sendMessage(ChatColor.RED + "You must be in a party to transfer your island.");
						return true;
					}

					if (pi.getMembers().size() > 2) {
						player.sendMessage(ChatColor.RED + "Remove all players from your party other than the player you are transferring to.");
						System.out.println("uSkyblock " + player.getName() + " tried to transfer his island, but his party has too many people!");
						return true;
					}

					tempTargetPlayer = Bukkit.getPlayer(split[1]).getName();

					if (tempParty.contains(split[1])) {
						tempTargetPlayer = split[1];
					}
					if (uSkyBlock.getInstance().hasParty(player.getName())) {
						if (tempLeader.equalsIgnoreCase(player.getName())) {
							if (tempParty.contains(tempTargetPlayer)) {
								if (Bukkit.getPlayer(split[1]) != null) {
									Bukkit.getPlayer(split[1]).sendMessage(ChatColor.GREEN + "You are now the owner of your island.");
								}
								player.sendMessage(ChatColor.GREEN + Bukkit.getPlayer(split[1]).getName() + " is now the owner of your island!");
								removePlayerFromParty(tempTargetPlayer, tempLeader);
								removePlayerFromParty(player.getName(), tempLeader);
								addPlayertoParty(player.getName(), tempTargetPlayer);
								addPlayertoParty(tempTargetPlayer, tempTargetPlayer);

								uSkyBlock.getInstance().transferIsland(player.getName(), tempTargetPlayer);

								if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
									WorldGuardHandler.transferRegion(player.getName(), tempTargetPlayer, sender);
								}
								return true;
							}
							player.sendMessage(ChatColor.RED + "That player is not part of your island group!");
						} else {
							player.sendMessage(ChatColor.RED + "This isn't your island, so you can't give it away!");
						}
					} else {
						player.sendMessage(ChatColor.RED + "Could not change leaders.");
					}
					return true;
				}
				if (split[0].equalsIgnoreCase("checkparty")) {
					if (VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld())) {
						player.sendMessage(ChatColor.YELLOW + "Checking Party of " + split[1]);
						final PlayerInfo pip = uSkyBlock.getInstance().getPlayer(split[1]);
						if (pip == null) {
							player.sendMessage(ChatColor.YELLOW + "That player doesn't exist!");
						} else if (pip.getHasParty()) {
							if (pip.getPartyLeader().equalsIgnoreCase(split[1])) {
								player.sendMessage(pip.getMembers().toString());
							} else {
								final PlayerInfo pip2 = uSkyBlock.getInstance().getPlayer(pip.getPartyLeader());
								player.sendMessage(pip2.getMembers().toString());
							}
						} else {
							player.sendMessage(ChatColor.RED + "That player is not in an island party!");
						}
					} else {
						player.sendMessage(ChatColor.RED + "You can't access that command!");
					}
					return true;
				}
			}
		} 
		else 
		{
			player.sendMessage(ChatColor.GREEN + "Creating a new island for you.");
			Bukkit.getScheduler().runTask(uSkyBlock.getInstance(), new IslandBuilder(player));
			return true;
		}
		return false;
	}

	public void removePlayerFromParty(final String playername, final String partyleader) {
		if (uSkyBlock.getInstance().isActivePlayer(playername)) {
			PlayerInfo pi = uSkyBlock.getInstance().getPlayer(playername);
			if (pi != null && pi.getPartyLeader() != null) {
				if (!pi.getPartyLeader().equalsIgnoreCase(playername)) {
					uSkyBlock.getInstance().getPlayer(playername).setHomeLocation(null);
				}
			}
			uSkyBlock.getInstance().getPlayer(playername).setLeaveParty();
			uSkyBlock.getInstance().writePlayerFile(playername, uSkyBlock.getInstance().getPlayer(playername));
		} else {
			final PlayerInfo pi = uSkyBlock.getInstance().getPlayer(playername);
			if (!pi.getPartyLeader().equalsIgnoreCase(playername)) {
				pi.setHomeLocation(null);
			}
			pi.setLeaveParty();
			uSkyBlock.getInstance().writePlayerFile(playername, pi);
		}
		if (uSkyBlock.getInstance().isActivePlayer(partyleader)) {
			if (uSkyBlock.getInstance().getPlayer(partyleader).getMembers().contains(playername)) {
				uSkyBlock.getInstance().getPlayer(partyleader).removeMember(playername);
			}
			uSkyBlock.getInstance().writePlayerFile(partyleader, uSkyBlock.getInstance().getPlayer(partyleader));
		} else {
			final PlayerInfo pi = uSkyBlock.getInstance().getPlayer(partyleader);
			if (pi.getMembers().contains(playername)) {
				pi.removeMember(playername);
			}
			uSkyBlock.getInstance().writePlayerFile(partyleader, pi);
		}
	}

	

	
}