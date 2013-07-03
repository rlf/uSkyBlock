package us.talabrek.ultimateskyblock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class IslandCommand implements CommandExecutor {
	public Location Islandlocation;
	private List<String> tempParty;
	private String tempLeader;
	private String tempTargetPlayer;
	/*   29 */public boolean allowInfo = true;
	/*   30 */private HashMap<String, String> inviteList = new HashMap<String, String>();
	String tPlayer;

	public IslandCommand() {
		/*   34 */this.inviteList.put("NoInvited", "NoInviter");
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		/*   38 */if (!(sender instanceof Player)) {
			/*   39 */return false;
		}
		/*   41 */Player player = (Player) sender;

		/*   44 */if (!VaultHandler.checkPerk(player.getName(), "usb.island.create", player.getWorld())) {
			/*   45 */player.sendMessage(ChatColor.RED + "You don't have permission to use that command!");
			/*   46 */return true;
		}

		/*   49 */PlayerInfo pi = (PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(player.getName());
		/*   50 */if (pi == null) {
			/*   52 */player.sendMessage(ChatColor.RED + "Error: Couldn't read your player data!");
			/*   53 */return true;
		}

		/*   56 */if (uSkyBlock.getInstance().hasParty(player.getName())) {
			/*   59 */this.tempLeader = pi.getPartyLeader();
			/*   60 */this.tempParty = pi.getMembers();
		}

		/*   63 */if ((pi.getIslandLocation() != null) || (pi.getHasParty())) {
			/*   64 */if (split.length == 0) {
				/*   65 */if ((pi.getHomeLocation() != null) || (pi.getHasParty())) {
					/*   66 */uSkyBlock.getInstance().homeTeleport(player);
				} else {
					/*   69 */((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(player.getName())).setHomeLocation(pi
							.getIslandLocation());
				}

				/*   72 */return true;
				/*   73 */}
			if (split.length == 1) {
				/*   75 */if ((split[0].equals("restart")) || (split[0].equals("reset"))) {
					/*   77 */if (pi.getHasParty()) {
						/*   79 */if (!pi.getPartyLeader().equalsIgnoreCase(player.getName()))
							/*   80 */player
									.sendMessage(ChatColor.RED
											+ "Only the owner may restart this island. Leave this island in order to start your own (/island leave).");
						else
							/*   82 */player
									.sendMessage(ChatColor.YELLOW
											+ "You must remove all players from your island before you can restart it (/island kick <player>). See a list of players currently part of your island using /island party.");
						/*   83 */return true;
					}
					/*   85 */if ((!uSkyBlock.getInstance().onRestartCooldown(player)) || (Settings.general_cooldownRestart == 0)) {
						/*   88 */uSkyBlock.getInstance().deletePlayerIsland(player.getName());
						/*   89 */uSkyBlock.getInstance().setRestartCooldown(player);
						/*   90 */return createIsland(sender);
					}

					/*   93 */player.sendMessage(ChatColor.YELLOW + "You can restart your island in "
							+ uSkyBlock.getInstance().getRestartCooldownTime(player) / 1000L + " seconds.");
					/*   94 */return true;
				}
				/*   96 */if (((split[0].equals("sethome")) || (split[0].equals("tpset")))
						&& (VaultHandler.checkPerk(player.getName(), "usb.island.sethome", player.getWorld()))) {
					/*   97 */uSkyBlock.getInstance().homeSet(player);
					/*   98 */return true;
					/*   99 */}
				if (split[0].equals("lock")) {
					/*  100 */if ((Settings.island_allowIslandLock)
							&& (VaultHandler.checkPerk(player.getName(), "usb.lock", player.getWorld())))
						/*  101 */WorldGuardHandler.islandLock(sender, player.getName());
					else
						/*  103 */player.sendMessage(ChatColor.RED + "You don't have access to this command!");
					/*  104 */return true;
					/*  105 */}
				if (split[0].equals("unlock")) {
					/*  106 */if ((Settings.island_allowIslandLock)
							&& (VaultHandler.checkPerk(player.getName(), "usb.lock", player.getWorld())))
						/*  107 */WorldGuardHandler.islandUnlock(sender, player.getName());
					else
						/*  109 */player.sendMessage(ChatColor.RED + "You don't have access to this command!");
					/*  110 */return true;
					/*  111 */}
				if (split[0].equals("help")) {
					/*  112 */player.sendMessage(ChatColor.GREEN + "[SkyBlock command usage]");

					/*  114 */player.sendMessage(ChatColor.YELLOW + "/island :" + ChatColor.WHITE
							+ " start your island, or teleport back to one you have.");
					/*  115 */player.sendMessage(ChatColor.YELLOW + "/island restart :" + ChatColor.WHITE
							+ " delete your island and start a new one.");
					/*  116 */player.sendMessage(ChatColor.YELLOW + "/island sethome :" + ChatColor.WHITE
							+ " set your island teleport point.");
					/*  117 */if (Settings.island_useIslandLevel) {
						/*  119 */player.sendMessage(ChatColor.YELLOW + "/island level :" + ChatColor.WHITE + " check your island level");
						/*  120 */player.sendMessage(ChatColor.YELLOW + "/island level <player> :" + ChatColor.WHITE
								+ " check another player's island level.");
					}
					/*  122 */if (VaultHandler.checkPerk(player.getName(), "usb.party.create", player.getWorld())) {
						/*  124 */player.sendMessage(ChatColor.YELLOW + "/island party :" + ChatColor.WHITE
								+ " view your party information.");
						/*  125 */player.sendMessage(ChatColor.YELLOW + "/island invite <player>:" + ChatColor.WHITE
								+ " invite a player to join your island.");
						/*  126 */player.sendMessage(ChatColor.YELLOW + "/island leave :" + ChatColor.WHITE
								+ " leave another player's island.");
					}
					/*  128 */if (VaultHandler.checkPerk(player.getName(), "usb.party.kick", player.getWorld())) {
						/*  130 */player.sendMessage(ChatColor.YELLOW + "/island kick <player>:" + ChatColor.WHITE
								+ " remove a player from your island.");
					}
					/*  132 */if (VaultHandler.checkPerk(player.getName(), "usb.party.join", player.getWorld())) {
						/*  134 */player.sendMessage(ChatColor.YELLOW + "/island [accept/reject]:" + ChatColor.WHITE
								+ " accept/reject an invitation.");
					}
					/*  136 */if (VaultHandler.checkPerk(player.getName(), "usb.party.makeleader", player.getWorld())) {
						/*  138 */player.sendMessage(ChatColor.YELLOW + "/island makeleader <player>:" + ChatColor.WHITE
								+ " transfer the island to <player>.");
					}
					/*  140 */player.sendMessage(ChatColor.YELLOW + "/island top :" + ChatColor.WHITE + " see the top ranked islands.");
					/*  141 */if (Settings.island_allowIslandLock) {
						/*  143 */if (!VaultHandler.checkPerk(player.getName(), "usb.lock", player.getWorld())) {
							/*  145 */player.sendMessage(ChatColor.DARK_GRAY + "/island lock :" + ChatColor.GRAY
									+ " non-group members can't enter your island.");
							/*  146 */player.sendMessage(ChatColor.DARK_GRAY + "/island unlock :" + ChatColor.GRAY
									+ " allow anyone to enter your island.");
						} else {
							/*  149 */player.sendMessage(ChatColor.YELLOW + "/island lock :" + ChatColor.WHITE
									+ " non-group members can't enter your island.");
							/*  150 */player.sendMessage(ChatColor.YELLOW + "/island unlock :" + ChatColor.WHITE
									+ " allow anyone to enter your island.");
						}

					}

					/*  155 */if (Bukkit.getServer().getServerId().equalsIgnoreCase("UltimateSkyblock")) {
						/*  157 */player.sendMessage(ChatColor.YELLOW + "/dungeon :" + ChatColor.WHITE + " to warp to the dungeon world.");
						/*  158 */player.sendMessage(ChatColor.YELLOW + "/fun :" + ChatColor.WHITE
								+ " to warp to the Mini-Game/Fun world.");
						/*  159 */player.sendMessage(ChatColor.YELLOW + "/pvp :" + ChatColor.WHITE + " join a pvp match.");
						/*  160 */player.sendMessage(ChatColor.YELLOW + "/spleef :" + ChatColor.WHITE + " join spleef match.");
						/*  161 */player.sendMessage(ChatColor.YELLOW + "/hub :" + ChatColor.WHITE + " warp to the world hub Sanconia.");
					}
					/*  163 */return true;
					/*  164 */}
				if ((split[0].equals("top")) && (VaultHandler.checkPerk(player.getName(), "usb.island.topten", player.getWorld()))) {
					/*  165 */uSkyBlock.getInstance().displayTopTen(player);
					/*  166 */return true;
					/*  167 */}
				if (((split[0].equals("info")) || (split[0].equals("level")))
						&& (VaultHandler.checkPerk(player.getName(), "usb.island.info", player.getWorld()))
						&& (Settings.island_useIslandLevel)) {
					/*  169 */if (uSkyBlock.getInstance().playerIsOnIsland(player)) {
						/*  171 */if ((!uSkyBlock.getInstance().onInfoCooldown(player)) || (Settings.general_cooldownInfo == 0)) {
							/*  173 */uSkyBlock.getInstance().setInfoCooldown(player);
							/*  174 */if ((!pi.getHasParty()) && (!pi.getHasIsland())) {
								/*  176 */player.sendMessage(ChatColor.RED + "You do not have an island!");
							}
							/*  178 */else
								getIslandLevel(player, player.getName());
							/*  179 */return true;
						}

						/*  182 */player.sendMessage(ChatColor.YELLOW + "You can use that command again in "
								+ uSkyBlock.getInstance().getInfoCooldownTime(player) / 1000L + " seconds.");
						/*  183 */return true;
					}

					/*  187 */player.sendMessage(ChatColor.YELLOW + "You must be on your island to use this command.");
					/*  188 */return true;
				}
				/*  190 */if ((split[0].equals("invite"))
						&& (VaultHandler.checkPerk(player.getName(), "usb.party.create", player.getWorld()))) {
					/*  192 */player.sendMessage(ChatColor.YELLOW + "Use" + ChatColor.WHITE + " /island invite <playername>"
							+ ChatColor.YELLOW + " to invite a player to your island.");
					/*  193 */if (uSkyBlock.getInstance().hasParty(player.getName())) {
						/*  195 */if (this.tempLeader.equalsIgnoreCase(player.getName())) {
							/*  197 */if (VaultHandler.checkPerk(player.getName(), "usb.extra.partysize", player.getWorld())) {
								/*  199 */if (this.tempParty.size() < Settings.general_maxPartySize * 2) {
									/*  201 */player.sendMessage(ChatColor.GREEN + "You can invite "
											+ (Settings.general_maxPartySize * 2 - this.tempParty.size()) + " more players.");
								}
								/*  203 */else
									player.sendMessage(ChatColor.RED + "You can't invite any more players.");
								/*  204 */return true;
							}

							/*  207 */if (this.tempParty.size() < Settings.general_maxPartySize) {
								/*  209 */player.sendMessage(ChatColor.GREEN + "You can invite "
										+ (Settings.general_maxPartySize - this.tempParty.size()) + " more players.");
							}
							/*  211 */else
								player.sendMessage(ChatColor.RED + "You can't invite any more players.");
							/*  212 */return true;
						}

						/*  216 */player.sendMessage(ChatColor.RED + "Only the island's owner can invite!");
						/*  217 */return true;
					}

					/*  220 */return true;
					/*  221 */}
				if ((split[0].equals("accept")) && (VaultHandler.checkPerk(player.getName(), "usb.party.join", player.getWorld()))) {
					/*  224 */if ((uSkyBlock.getInstance().onInfoCooldown(player)) && (Settings.general_cooldownInfo > 0)) {
						/*  226 */player.sendMessage(ChatColor.YELLOW + "You can't join an island for another "
								+ uSkyBlock.getInstance().getRestartCooldownTime(player) / 1000L + " seconds.");
						/*  227 */return true;
					}
					/*  229 */if ((!uSkyBlock.getInstance().hasParty(player.getName())) && (this.inviteList.containsKey(player.getName()))) {
						/*  231 */if (!uSkyBlock.getInstance().hasParty((String) this.inviteList.get(player.getName()))) {
							/*  233 */if (pi.getHasIsland()) {
								/*  235 */uSkyBlock.getInstance().deletePlayerIsland(player.getName());
							}

							/*  239 */addPlayertoParty(player.getName(), (String) this.inviteList.get(player.getName()));
							/*  240 */addPlayertoParty((String) this.inviteList.get(player.getName()),
									(String) this.inviteList.get(player.getName()));
							/*  241 */player.sendMessage(ChatColor.GREEN
									+ "You have joined an island! Use /island party to see the other members.");
							/*  242 */if (Bukkit.getPlayer((String) this.inviteList.get(player.getName())) != null) {
								/*  244 */Bukkit.getPlayer((String) this.inviteList.get(player.getName())).sendMessage(
										ChatColor.GREEN + player.getName() + " has joined your island!");
							}
						} else {
							/*  249 */if (pi.getHasIsland()) {
								/*  251 */uSkyBlock.getInstance().deletePlayerIsland(player.getName());
								/*  252 */}
							player.sendMessage(ChatColor.GREEN + "You have joined an island! Use /island party to see the other members.");
							/*  253 */addPlayertoParty(player.getName(), (String) this.inviteList.get(player.getName()));
							/*  254 */if (Bukkit.getPlayer((String) this.inviteList.get(player.getName())) != null) {
								/*  256 */Bukkit.getPlayer((String) this.inviteList.get(player.getName())).sendMessage(
										ChatColor.GREEN + player.getName() + " has joined your island!");
							} else {
								/*  260 */player.sendMessage(ChatColor.RED + "You couldn't join the island, maybe it's full.");

								/*  262 */return true;
							}
						}
						/*  265 */uSkyBlock.getInstance().setRestartCooldown(player);

						/*  267 */uSkyBlock.getInstance().homeTeleport(player);

						/*  269 */player.getInventory().clear();
						/*  270 */player.getEquipment().clear();

						/*  274 */if ((Settings.island_protectWithWorldGuard)
								&& (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard"))) {
							/*  276 */if (WorldGuardHandler.getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld())
									.hasRegion((String) this.inviteList.get(player.getName()) + "Island")) {
								/*  278 */WorldGuardHandler.addPlayerToOldRegion((String) this.inviteList.get(player.getName()),
										player.getName());
							}
						}
						/*  281 */this.inviteList.remove(player.getName());
						/*  282 */return true;
					}

					/*  285 */player.sendMessage(ChatColor.RED + "You can't use that command right now.");
					/*  286 */return true;
				}

				/*  289 */if (split[0].equals("reject")) {
					/*  291 */if (this.inviteList.containsKey(player.getName())) {
						/*  293 */player.sendMessage(ChatColor.YELLOW + "You have rejected the invitation to join an island.");
						/*  294 */if (Bukkit.getPlayer((String) this.inviteList.get(player.getName())) != null) {
							/*  296 */Bukkit.getPlayer((String) this.inviteList.get(player.getName())).sendMessage(
									ChatColor.RED + player.getName() + " has rejected your island invite!");
							/*  297 */}
						this.inviteList.remove(player.getName());
					} else {
						/*  299 */player.sendMessage(ChatColor.RED + "You haven't been invited.");
						/*  300 */}
					return true;
				}

				/*  305 */if (split[0].equalsIgnoreCase("partypurge")) {
					/*  307 */if (VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld())) {
						/*  309 */player.sendMessage(ChatColor.RED + "This command no longer functions!");
					}
					/*  311 */else
						player.sendMessage(ChatColor.RED + "You can't access that command!");
					/*  312 */return true;
					/*  313 */}
				if (split[0].equalsIgnoreCase("partyclean")) {
					/*  315 */if (VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld())) {
						/*  317 */player.sendMessage(ChatColor.RED + "This command no longer functions!");
					}
					/*  319 */else
						player.sendMessage(ChatColor.RED + "You can't access that command!");
					/*  320 */return true;
					/*  321 */}
				if (split[0].equalsIgnoreCase("purgeinvites")) {
					/*  323 */if (VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld())) {
						/*  325 */player.sendMessage(ChatColor.RED + "Deleting all invites!");
						/*  326 */invitePurge();
					} else {
						/*  328 */player.sendMessage(ChatColor.RED + "You can't access that command!");
						/*  329 */}
					return true;
					/*  330 */}
				if (split[0].equalsIgnoreCase("partylist")) {
					/*  332 */if (VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld())) {
						/*  334 */player.sendMessage(ChatColor.RED + "This command is currently not active.");
					} else
						/*  337 */player.sendMessage(ChatColor.RED + "You can't access that command!");
					/*  338 */return true;
					/*  339 */}
				if (split[0].equalsIgnoreCase("invitelist")) {
					/*  341 */if (VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld())) {
						/*  343 */player.sendMessage(ChatColor.RED + "Checking Invites.");
						/*  344 */inviteDebug(player);
					} else {
						/*  346 */player.sendMessage(ChatColor.RED + "You can't access that command!");
						/*  347 */}
					return true;
				}

				/*  351 */if ((split[0].equals("leave"))
						&& (VaultHandler.checkPerk(player.getName(), "usb.party.join", player.getWorld()))) {
					/*  353 */if (player.getWorld().getName().equalsIgnoreCase(uSkyBlock.getSkyBlockWorld().getName())) {
						/*  355 */if (uSkyBlock.getInstance().hasParty(player.getName())) {
							/*  357 */if (((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(player.getName())).getPartyLeader()
									.equalsIgnoreCase(player.getName())) {
								/*  389 */player
										.sendMessage(ChatColor.YELLOW + "You are the leader, use /island remove <player> instead.");
								/*  390 */return true;
							}

							/*  395 */player.getInventory().clear();
							/*  396 */player.getEquipment().clear();
							/*  397 */if (Settings.extras_sendToSpawn)
								/*  398 */player.performCommand("spawn");
							else {
								/*  400 */player.teleport(uSkyBlock.getSkyBlockWorld().getSpawnLocation());
							}

							/*  403 */removePlayerFromParty(player.getName(), this.tempLeader);

							/*  405 */player.sendMessage(ChatColor.YELLOW + "You have left the island and returned to the player spawn.");
							/*  406 */if (Bukkit.getPlayer(this.tempLeader) != null)
								/*  407 */Bukkit.getPlayer(this.tempLeader).sendMessage(
										ChatColor.RED + player.getName() + " has left your island!");
							/*  408 */this.tempParty.remove(player.getName());
							/*  409 */if (this.tempParty.size() < 2) {
								/*  411 */removePlayerFromParty(this.tempLeader, this.tempLeader);
							}

							/*  415 */if ((Settings.island_protectWithWorldGuard)
									&& (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard"))) {
								/*  417 */WorldGuardHandler.removePlayerFromRegion(this.tempLeader, player.getName());
							}
						} else {
							/*  422 */player
									.sendMessage(ChatColor.RED
											+ "You can't leave your island if you are the only person. Try using /island restart if you want a new one!");
							/*  423 */return true;
						}
					} else {
						/*  427 */player.sendMessage(ChatColor.RED + "You must be in the skyblock world to leave your party!");
					}
					/*  429 */return true;
					/*  430 */}
				if (split[0].equals("party")) {
					/*  432 */if (VaultHandler.checkPerk(player.getName(), "usb.party.create", player.getWorld()))
						/*  433 */player.sendMessage(ChatColor.WHITE + "/island invite <playername>" + ChatColor.YELLOW
								+ " to invite a player to join your island.");
					/*  434 */if (uSkyBlock.getInstance().hasParty(player.getName())) {
						/*  436 */player.sendMessage(ChatColor.WHITE + "/island leave" + ChatColor.YELLOW
								+ " leave your current island and return to spawn");
						/*  437 */if (this.tempLeader.equalsIgnoreCase(sender.getName())) {
							/*  439 */if (VaultHandler.checkPerk(player.getName(), "usb.party.kick", player.getWorld()))
								/*  440 */player.sendMessage(ChatColor.WHITE + "/island remove <playername>" + ChatColor.YELLOW
										+ " remove <playername> from your island");
							/*  441 */if (VaultHandler.checkPerk(player.getName(), "usb.party.makeleader", player.getWorld()))
								/*  442 */player.sendMessage(ChatColor.WHITE + "/island makeleader <playername>" + ChatColor.YELLOW
										+ " give ownership of the island to <playername>");
							/*  443 */if (VaultHandler.checkPerk(player.getName(), "usb.extra.partysize", player.getWorld())) {
								/*  445 */if (this.tempParty.size() < Settings.general_maxPartySize * 2) {
									/*  447 */player.sendMessage(ChatColor.GREEN + "You can invite "
											+ (Settings.general_maxPartySize * 2 - this.tempParty.size()) + " more players.");
								}
								/*  449 */else
									player.sendMessage(ChatColor.RED + "You can't invite any more players.");

							}
							/*  452 */else if (this.tempParty.size() < Settings.general_maxPartySize) {
								/*  454 */player.sendMessage(ChatColor.GREEN + "You can invite "
										+ (Settings.general_maxPartySize - this.tempParty.size()) + " more players.");
							}
							/*  456 */else
								player.sendMessage(ChatColor.RED + "You can't invite any more players.");

						}

						/*  460 */player.sendMessage(ChatColor.YELLOW + "Listing your island members:");
						/*  461 */PlayerInfo tPi = uSkyBlock.getInstance().readPlayerFile(this.tempLeader);
						/*  462 */player.sendMessage(ChatColor.WHITE + tPi.getMembers().toString());
						/*  463 */} else if (this.inviteList.containsKey(player.getName())) {
						/*  465 */player.sendMessage(ChatColor.YELLOW + (String) this.inviteList.get(player.getName())
								+ " has invited you to join their island.");
						/*  466 */player.sendMessage(ChatColor.WHITE + "/island [accept/reject]" + ChatColor.YELLOW
								+ " to accept or reject the invite.");
					}
					/*  468 */return true;
				}
				/*  470 */} else if (split.length == 2) {
				/*  471 */if (((split[0].equals("info")) || (split[0].equals("level")))
						&& (VaultHandler.checkPerk(player.getName(), "usb.island.info", player.getWorld()))
						&& (Settings.island_useIslandLevel)) {
					/*  473 */if ((!uSkyBlock.getInstance().onInfoCooldown(player)) || (Settings.general_cooldownInfo == 0)) {
						/*  475 */uSkyBlock.getInstance().setInfoCooldown(player);
						/*  476 */if ((!pi.getHasParty()) && (!pi.getHasIsland())) {
							/*  478 */player.sendMessage(ChatColor.RED + "You do not have an island!");
						}
						/*  480 */else
							getIslandLevel(player, split[1]);
						/*  481 */return true;
					}

					/*  484 */player.sendMessage(ChatColor.YELLOW + "You can use that command again in "
							+ uSkyBlock.getInstance().getInfoCooldownTime(player) / 1000L + " seconds.");
					/*  485 */return true;
				}
				/*  487 */if ((split[0].equalsIgnoreCase("invite"))
						&& (VaultHandler.checkPerk(player.getName(), "usb.party.create", player.getWorld()))) {
					/*  494 */if (Bukkit.getPlayer(split[1]) == null) {
						/*  496 */player.sendMessage(ChatColor.RED + "That player is offline or doesn't exist.");
						/*  497 */return true;
					}
					/*  499 */if (!Bukkit.getPlayer(split[1]).isOnline()) {
						/*  501 */player.sendMessage(ChatColor.RED + "That player is offline or doesn't exist.");
						/*  502 */return true;
					}
					/*  504 */if (!uSkyBlock.getInstance().hasIsland(player.getName())) {
						/*  506 */player.sendMessage(ChatColor.RED + "You must have an island in order to invite people to it!");
						/*  507 */return true;
					}
					/*  509 */if (player.getName().equalsIgnoreCase(Bukkit.getPlayer(split[1]).getName())) {
						/*  511 */player.sendMessage(ChatColor.RED + "You can't invite yourself!");
						/*  512 */return true;
					}
					/*  514 */if (uSkyBlock.getInstance().hasParty(player.getName())) {
						/*  516 */if (this.tempLeader.equalsIgnoreCase(player.getName())) {
							/*  518 */if (!uSkyBlock.getInstance().hasParty(Bukkit.getPlayer(split[1]).getName())) {
								/*  520 */if (VaultHandler.checkPerk(player.getName(), "usb.extra.partysize", player.getWorld())) {
									/*  522 */if (this.tempParty.size() < Settings.general_maxPartySize * 2) {
										/*  524 */if (this.inviteList.containsValue(player.getName())) {
											/*  526 */this.inviteList.remove(getKeyByValue(this.inviteList, player.getName()));
											/*  527 */player.sendMessage(ChatColor.YELLOW + "Removing your previous invite.");
										}
										/*  529 */this.inviteList.put(Bukkit.getPlayer(split[1]).getName(), player.getName());
										/*  530 */player.sendMessage(ChatColor.GREEN + "Invite sent to "
												+ Bukkit.getPlayer(split[1]).getName());

										/*  532 */Bukkit.getPlayer(split[1]).sendMessage(
												player.getName() + " has invited you to join their island!");
										/*  533 */Bukkit.getPlayer(split[1]).sendMessage(
												ChatColor.WHITE + "/island [accept/reject]" + ChatColor.YELLOW
														+ " to accept or reject the invite.");
										/*  534 */Bukkit.getPlayer(split[1]).sendMessage(
												ChatColor.RED + "WARNING: You will lose your current island if you accept!");
									} else {
										/*  536 */player.sendMessage(ChatColor.RED + "Your island is full, you can't invite anyone else.");
									}
								}
								/*  539 */else if (this.tempParty.size() < Settings.general_maxPartySize) {
									/*  541 */if (this.inviteList.containsValue(player.getName())) {
										/*  543 */this.inviteList.remove(getKeyByValue(this.inviteList, player.getName()));
										/*  544 */player.sendMessage(ChatColor.YELLOW + "Removing your previous invite.");
									}
									/*  546 */this.inviteList.put(Bukkit.getPlayer(split[1]).getName(), player.getName());
									/*  547 */player.sendMessage(ChatColor.GREEN + "Invite sent to "
											+ Bukkit.getPlayer(split[1]).getName());

									/*  549 */Bukkit.getPlayer(split[1]).sendMessage(
											player.getName() + " has invited you to join their island!");
									/*  550 */Bukkit.getPlayer(split[1]).sendMessage(
											ChatColor.WHITE + "/island [accept/reject]" + ChatColor.YELLOW
													+ " to accept or reject the invite.");
									/*  551 */Bukkit.getPlayer(split[1]).sendMessage(
											ChatColor.RED + "WARNING: You will lose your current island if you accept!");
								} else {
									/*  553 */player.sendMessage(ChatColor.RED + "Your island is full, you can't invite anyone else.");
								}
							}
							/*  556 */else
								player.sendMessage(ChatColor.RED + "That player is already with a group on an island.");
						} else
							/*  558 */player.sendMessage(ChatColor.RED + "Only the island's owner may invite new players.");
					} else {
						if (!uSkyBlock.getInstance().hasParty(player.getName())) {
							/*  561 */if (!uSkyBlock.getInstance().hasParty(Bukkit.getPlayer(split[1]).getName())) {
								/*  563 */if (this.inviteList.containsValue(player.getName())) {
									/*  565 */this.inviteList.remove(getKeyByValue(this.inviteList, player.getName()));
									/*  566 */player.sendMessage(ChatColor.YELLOW + "Removing your previous invite.");
								}
								/*  568 */this.inviteList.put(Bukkit.getPlayer(split[1]).getName(), player.getName());

								/*  571 */player.sendMessage(ChatColor.GREEN + "Invite sent to " + Bukkit.getPlayer(split[1]).getName());
								/*  572 */Bukkit.getPlayer(split[1]).sendMessage(
										player.getName() + " has invited you to join their island!");
								/*  573 */Bukkit.getPlayer(split[1])
										.sendMessage(
												ChatColor.WHITE + "/island [accept/reject]" + ChatColor.YELLOW
														+ " to accept or reject the invite.");
								/*  574 */Bukkit.getPlayer(split[1]).sendMessage(
										ChatColor.RED + "WARNING: You will lose your current island if you accept!");
							} else {
								/*  576 */player.sendMessage(ChatColor.RED + "That player is already with a group on an island.");
								/*  577 */}
							return true;
						}

						/*  580 */player.sendMessage(ChatColor.RED + "Only the island's owner may invite new players!");
						/*  581 */return true;
					}
					/*  583 */return true;
					/*  584 */}
				if (((split[0].equalsIgnoreCase("remove")) || (split[0].equalsIgnoreCase("kick")))
						&& (VaultHandler.checkPerk(player.getName(), "usb.party.kick", player.getWorld()))) {
					/*  587 */if ((Bukkit.getPlayer(split[1]) == null) && (Bukkit.getOfflinePlayer(split[1]) == null)) {
						/*  589 */player.sendMessage(ChatColor.RED + "That player doesn't exist.");
						/*  590 */return true;
					}
					/*  592 */if (Bukkit.getPlayer(split[1]) == null) {
						/*  594 */this.tempTargetPlayer = Bukkit.getOfflinePlayer(split[1]).getName();
					} else {
						/*  597 */this.tempTargetPlayer = Bukkit.getPlayer(split[1]).getName();
					}
					/*  599 */if (this.tempParty.contains(split[1])) {
						/*  601 */this.tempTargetPlayer = split[1];
					}
					/*  603 */if (uSkyBlock.getInstance().hasParty(player.getName())) {
						/*  605 */if (this.tempLeader.equalsIgnoreCase(player.getName())) {
							/*  607 */if (this.tempParty.contains(this.tempTargetPlayer)) {
								/*  609 */if (player.getName().equalsIgnoreCase(this.tempTargetPlayer)) {
									/*  611 */player
											.sendMessage(ChatColor.RED + "Use /island leave to remove all people from your island");
									/*  612 */return true;
								}
								/*  614 */if (Bukkit.getPlayer(split[1]) != null) {
									/*  616 */if (Bukkit.getPlayer(split[1]).getWorld().getName()
											.equalsIgnoreCase(uSkyBlock.getSkyBlockWorld().getName())) {
										/*  618 */Bukkit.getPlayer(split[1]).getInventory().clear();
										/*  619 */Bukkit.getPlayer(split[1]).getEquipment().clear();
										/*  620 */Bukkit.getPlayer(split[1]).sendMessage(
												ChatColor.RED + player.getName() + " has removed you from their island!");
									}
									/*  622 */if (Settings.extras_sendToSpawn)
										/*  623 */Bukkit.getPlayer(split[1]).performCommand("spawn");
									else {
										/*  625 */Bukkit.getPlayer(split[1]).teleport(uSkyBlock.getSkyBlockWorld().getSpawnLocation());
									}

								}

								/*  630 */if (Bukkit.getPlayer(this.tempLeader) != null)
									/*  631 */Bukkit.getPlayer(this.tempLeader).sendMessage(
											ChatColor.RED + this.tempTargetPlayer + " has been removed from the island.");
								/*  632 */removePlayerFromParty(this.tempTargetPlayer, this.tempLeader);
								/*  633 */this.tempParty.remove(this.tempTargetPlayer);
								/*  634 */if (this.tempParty.size() < 2) {
									/*  636 */removePlayerFromParty(player.getName(), this.tempLeader);
								}

								/*  642 */if ((Settings.island_protectWithWorldGuard)
										&& (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard"))) {
									/*  644 */WorldGuardHandler.removePlayerFromRegion(player.getName(), this.tempTargetPlayer);
								}
							} else {
								/*  648 */System.out.println("Player " + player.getName() + " failed to remove " + this.tempTargetPlayer);
								/*  649 */player.sendMessage(ChatColor.RED + "That player is not part of your island group!");
							}
						}
						/*  652 */else
							player.sendMessage(ChatColor.RED + "Only the island's owner may remove people from the island!");
					} else
						/*  654 */player.sendMessage(ChatColor.RED + "No one else is on your island, are you seeing things?");
					/*  655 */return true;
					/*  656 */}
				if ((split[0].equalsIgnoreCase("makeleader"))
						&& (VaultHandler.checkPerk(player.getName(), "usb.party.makeleader", player.getWorld()))) {
					/*  658 */if (Bukkit.getPlayer(split[1]) == null) {
						/*  660 */player.sendMessage(ChatColor.RED + "That player must be online to transfer the island.");
						/*  661 */return true;
					}

					/*  664 */if ((!uSkyBlock.getInstance().getActivePlayers().containsKey(player.getName()))
							|| (!uSkyBlock.getInstance().getActivePlayers().containsKey(Bukkit.getPlayer(split[1]).getName()))) {
						/*  666 */player.sendMessage(ChatColor.RED + "Both players must be online to transfer an island.");
						/*  667 */return true;
					}

					/*  670 */if (!((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(player.getName())).getHasParty()) {
						/*  672 */player.sendMessage(ChatColor.RED + "You must be in a party to transfer your island.");
						/*  673 */return true;
					}

					/*  676 */if (((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(player.getName())).getMembers().size() > 2) {
						/*  678 */player.sendMessage(ChatColor.RED
								+ "Remove all players from your party other than the player you are transferring to.");
						/*  679 */System.out.println(player.getName() + " tried to transfer his island, but his party has too many people!");
						/*  680 */return true;
					}

					/*  683 */this.tempTargetPlayer = Bukkit.getPlayer(split[1]).getName();

					/*  685 */if (this.tempParty.contains(split[1])) {
						/*  687 */this.tempTargetPlayer = split[1];
					}
					/*  689 */if (uSkyBlock.getInstance().hasParty(player.getName())) {
						/*  691 */if (this.tempLeader.equalsIgnoreCase(player.getName())) {
							/*  693 */if (this.tempParty.contains(this.tempTargetPlayer)) {
								/*  695 */if (Bukkit.getPlayer(split[1]) != null)
									/*  696 */Bukkit.getPlayer(split[1]).sendMessage(
											ChatColor.GREEN + "You are now the owner of your island.");
								/*  697 */player.sendMessage(ChatColor.GREEN + Bukkit.getPlayer(split[1]).getName()
										+ " is now the owner of your island!");
								/*  698 */removePlayerFromParty(this.tempTargetPlayer, this.tempLeader);
								/*  699 */removePlayerFromParty(player.getName(), this.tempLeader);
								/*  700 */addPlayertoParty(player.getName(), this.tempTargetPlayer);
								/*  701 */addPlayertoParty(this.tempTargetPlayer, this.tempTargetPlayer);

								/*  714 */uSkyBlock.getInstance().transferIsland(player.getName(), this.tempTargetPlayer);

								/*  719 */if ((Settings.island_protectWithWorldGuard)
										&& (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard"))) {
									/*  721 */WorldGuardHandler.transferRegion(player.getName(), this.tempTargetPlayer, sender);
								}
								/*  723 */return true;
							}
							/*  725 */player.sendMessage(ChatColor.RED + "That player is not part of your island group!");
						} else {
							/*  727 */player.sendMessage(ChatColor.RED + "This isn't your island, so you can't give it away!");
						}
					} else
						player.sendMessage(ChatColor.RED + "Could not change leaders.");
					/*  730 */return true;
					/*  731 */}
				if (split[0].equalsIgnoreCase("checkparty")) {
					/*  732 */if (VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld())) {
						/*  734 */player.sendMessage(ChatColor.YELLOW + "Checking Party of " + split[1]);
						/*  735 */PlayerInfo pip = uSkyBlock.getInstance().readPlayerFile(split[1]);
						/*  736 */if (pip == null) {
							/*  738 */player.sendMessage(ChatColor.YELLOW + "That player doesn't exist!");
						}
						/*  741 */else if (pip.getHasParty()) {
							/*  743 */if (pip.getPartyLeader().equalsIgnoreCase(split[1])) {
								/*  744 */player.sendMessage(pip.getMembers().toString());
							} else {
								/*  747 */PlayerInfo pip2 = uSkyBlock.getInstance().readPlayerFile(pip.getPartyLeader());
								/*  748 */player.sendMessage(pip2.getMembers().toString());
							}
						} else
							/*  752 */player.sendMessage(ChatColor.RED + "That player is not in an island party!");
					} else {
						/*  756 */player.sendMessage(ChatColor.RED + "You can't access that command!");
						/*  757 */}
					return true;
				}
			}
		} else {
			/*  761 */player.sendMessage(ChatColor.GREEN + "Creating a new island for you.");
			/*  762 */return createIsland(sender);
		}
		/*  764 */return false;
	}

	private boolean createIsland(CommandSender sender) {
		/*  781 */Player player = (Player) sender;
		/*  782 */Location last = uSkyBlock.getInstance().getLastIsland();
		/*  783 */last.setY(Settings.island_height);
		try {
			do {
				/*  793 */uSkyBlock.getInstance().removeNextOrphan();

				/*  791 */if (!uSkyBlock.getInstance().hasOrphanedIsland())
					break;
			} while (uSkyBlock.getInstance().islandAtLocation(uSkyBlock.getInstance().checkOrphan()));

			/*  811 */while ((uSkyBlock.getInstance().hasOrphanedIsland())
					&& (!uSkyBlock.getInstance().checkOrphan().getWorld().getName().equalsIgnoreCase(Settings.general_worldName))) {
				/*  813 */uSkyBlock.getInstance().removeNextOrphan();
			}
			Location next;
			/*  817 */if ((uSkyBlock.getInstance().hasOrphanedIsland())
					&& (!uSkyBlock.getInstance().islandAtLocation(uSkyBlock.getInstance().checkOrphan()))) {
				/*  818 */next = uSkyBlock.getInstance().getOrphanedIsland();
				/*  819 */uSkyBlock.getInstance().saveOrphans();
				/*  820 */uSkyBlock.getInstance().updateOrphans();
			} else {
				/*  824 */next = nextIslandLocation(last);
				/*  825 */uSkyBlock.getInstance().setLastIsland(next);

				/*  827 */while (uSkyBlock.getInstance().islandAtLocation(next)) {
					/*  830 */next = nextIslandLocation(next);
				}

				/*  833 */uSkyBlock.getInstance().setLastIsland(next);
			}
			/*  835 */boolean hasIslandNow = false;

			/*  837 */if ((uSkyBlock.getInstance().getSchemFile().length > 0)
					&& (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldEdit"))) {
				/*  839 */String cSchem = "";
				/*  840 */for (int i = 0; i < uSkyBlock.getInstance().getSchemFile().length; i++) {
					/*  842 */if (!hasIslandNow) {
						/*  844 */if (uSkyBlock.getInstance().getSchemFile()[i].getName().lastIndexOf('.') > 0) {
							/*  846 */cSchem = uSkyBlock.getInstance().getSchemFile()[i].getName().substring(0,
									uSkyBlock.getInstance().getSchemFile()[i].getName().lastIndexOf('.'));
						}
						/*  848 */else
							cSchem = uSkyBlock.getInstance().getSchemFile()[i].getName();

						/*  850 */if (VaultHandler.checkPerk(player.getName(), "usb.schematic." + cSchem, uSkyBlock.getSkyBlockWorld())) {
							/*  852 */if (WorldEditHandler.loadIslandSchematic(uSkyBlock.getSkyBlockWorld(), uSkyBlock.getInstance()
									.getSchemFile()[i], next)) {
								/*  854 */setChest(next, player);
								/*  855 */hasIslandNow = true;
							}
						}
					}
				}
				/*  860 */if (!hasIslandNow) {
					/*  862 */for (int i = 0; i < uSkyBlock.getInstance().getSchemFile().length; i++) {
						/*  864 */if (uSkyBlock.getInstance().getSchemFile()[i].getName().lastIndexOf('.') > 0) {
							/*  866 */cSchem = uSkyBlock.getInstance().getSchemFile()[i].getName().substring(0,
									uSkyBlock.getInstance().getSchemFile()[i].getName().lastIndexOf('.'));
						}
						/*  868 */else
							cSchem = uSkyBlock.getInstance().getSchemFile()[i].getName();
						/*  869 */if (cSchem.equalsIgnoreCase(Settings.island_schematicName)) {
							/*  871 */if (WorldEditHandler.loadIslandSchematic(uSkyBlock.getSkyBlockWorld(), uSkyBlock.getInstance()
									.getSchemFile()[i], next)) {
								/*  873 */setChest(next, player);
								/*  874 */hasIslandNow = true;
							}
						}
					}
				}
			}
			/*  880 */if (!hasIslandNow) {
				/*  882 */if (!Settings.island_useOldIslands)
					/*  883 */generateIslandBlocks(next.getBlockX(), next.getBlockZ(), player, uSkyBlock.getSkyBlockWorld());
				else {
					/*  885 */oldGenerateIslandBlocks(next.getBlockX(), next.getBlockZ(), player, uSkyBlock.getSkyBlockWorld());
				}
			}
			/*  888 */setNewPlayerIsland(player, next);

			/*  890 */player.getInventory().clear();
			/*  891 */player.getEquipment().clear();
			/*  892 */Iterator ents = player.getNearbyEntities(50.0D, 250.0D, 50.0D).iterator();
			/*  893 */while (ents.hasNext()) {
				/*  895 */Entity tempent = (Entity) ents.next();
				/*  896 */if (!(tempent instanceof Player)) {
					/*  901 */tempent.remove();
				}
			}
			/*  903 */if ((Settings.island_protectWithWorldGuard) && (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")))
				/*  904 */WorldGuardHandler.protectIsland(sender, sender.getName());
		} catch (Exception ex) {
			/*  907 */player.sendMessage("Could not create your Island. Pleace contact a server moderator.");
			/*  908 */ex.printStackTrace();
			/*  909 */return false;
		}
		/*  911 */return true;
	}

	public void generateIslandBlocks(int x, int z, Player player, World world) {
		/*  916 */int y = Settings.island_height;
		/*  917 */Block blockToChange = world.getBlockAt(x, y, z);
		/*  918 */blockToChange.setTypeId(7);
		/*  919 */islandLayer1(x, z, player, world);
		/*  920 */islandLayer2(x, z, player, world);
		/*  921 */islandLayer3(x, z, player, world);
		/*  922 */islandLayer4(x, z, player, world);
		/*  923 */islandExtras(x, z, player, world);
	}

	public void oldGenerateIslandBlocks(int x, int z, Player player, World world) {
		/*  927 */int y = Settings.island_height;

		/*  929 */for (int x_operate = x; x_operate < x + 3; x_operate++) {
			/*  930 */for (int y_operate = y; y_operate < y + 3; y_operate++) {
				/*  931 */for (int z_operate = z; z_operate < z + 6; z_operate++) {
					/*  932 */Block blockToChange = world.getBlockAt(x_operate, y_operate, z_operate);
					/*  933 */blockToChange.setTypeId(2);
				}
			}
		}

		/*  938 */for (int x_operate = x + 3; x_operate < x + 6; x_operate++) {
			/*  939 */for (int y_operate = y; y_operate < y + 3; y_operate++) {
				/*  940 */for (int z_operate = z + 3; z_operate < z + 6; z_operate++) {
					/*  941 */Block blockToChange = world.getBlockAt(x_operate, y_operate, z_operate);
					/*  942 */blockToChange.setTypeId(2);
				}
			}

		}

		/*  948 */for (int x_operate = x + 3; x_operate < x + 7; x_operate++) {
			/*  949 */for (int y_operate = y + 7; y_operate < y + 10; y_operate++) {
				/*  950 */for (int z_operate = z + 3; z_operate < z + 7; z_operate++) {
					/*  951 */Block blockToChange = world.getBlockAt(x_operate, y_operate, z_operate);
					/*  952 */blockToChange.setTypeId(18);
				}
			}

		}

		/*  958 */for (int y_operate = y + 3; y_operate < y + 9; y_operate++) {
			/*  959 */Block blockToChange = world.getBlockAt(x + 5, y_operate, z + 5);
			/*  960 */blockToChange.setTypeId(17);
		}

		/*  965 */Block blockToChange = world.getBlockAt(x + 1, y + 3, z + 1);
		/*  966 */blockToChange.setTypeId(54);
		/*  967 */Chest chest = (Chest) blockToChange.getState();
		/*  968 */Inventory inventory = chest.getInventory();
		/*  969 */inventory.clear();
		/*  970 */inventory.setContents(Settings.island_chestItems);
		/*  971 */if (Settings.island_addExtraItems) {
			/*  973 */for (int i = 0; i < Settings.island_extraPermissions.length; i++) {
				/*  975 */if (VaultHandler.checkPerk(player.getName(), "usb." + Settings.island_extraPermissions[i], player.getWorld())) {
					/*  977 */String[] chestItemString = uSkyBlock.getInstance().getConfig()
							.getString("options.island.extraPermissions." + Settings.island_extraPermissions[i]).split(" ");
					/*  978 */ItemStack[] tempChest = new ItemStack[chestItemString.length];
					/*  979 */String[] amountdata = new String[2];
					/*  980 */for (int j = 0; j < chestItemString.length; j++) {
						/*  982 */amountdata = chestItemString[j].split(":");
						/*  983 */tempChest[j] = new ItemStack(Integer.parseInt(amountdata[0]), Integer.parseInt(amountdata[1]));
						/*  984 */inventory.addItem(new ItemStack[] { tempChest[j] });
					}
				}
			}
		}

		/*  990 */blockToChange = world.getBlockAt(x, y, z);
		/*  991 */blockToChange.setTypeId(7);

		/*  994 */blockToChange = world.getBlockAt(x + 2, y + 1, z + 1);
		/*  995 */blockToChange.setTypeId(12);
		/*  996 */blockToChange = world.getBlockAt(x + 2, y + 1, z + 2);
		/*  997 */blockToChange.setTypeId(12);
		/*  998 */blockToChange = world.getBlockAt(x + 2, y + 1, z + 3);
		/*  999 */blockToChange.setTypeId(12);
	}

	private Location nextIslandLocation(Location lastIsland) {
		/* 1006 */int x = (int) lastIsland.getX();
		/* 1007 */int z = (int) lastIsland.getZ();
		/* 1008 */Location nextPos = lastIsland;
		/* 1009 */if (x < z) {
			/* 1011 */if (-1 * x < z) {
				/* 1013 */nextPos.setX(nextPos.getX() + Settings.island_distance);
				/* 1014 */return nextPos;
			}
			/* 1016 */nextPos.setZ(nextPos.getZ() + Settings.island_distance);
			/* 1017 */return nextPos;
		}
		/* 1019 */if (x > z) {
			/* 1021 */if (-1 * x >= z) {
				/* 1023 */nextPos.setX(nextPos.getX() - Settings.island_distance);
				/* 1024 */return nextPos;
			}
			/* 1026 */nextPos.setZ(nextPos.getZ() - Settings.island_distance);
			/* 1027 */return nextPos;
		}
		/* 1029 */if (x <= 0) {
			/* 1031 */nextPos.setZ(nextPos.getZ() + Settings.island_distance);
			/* 1032 */return nextPos;
		}
		/* 1034 */nextPos.setZ(nextPos.getZ() - Settings.island_distance);
		/* 1035 */return nextPos;
	}

	private void islandLayer1(int x, int z, Player player, World world) {
		/* 1040 */int y = Settings.island_height;
		/* 1041 */y = Settings.island_height + 4;
		/* 1042 */for (int x_operate = x - 3; x_operate <= x + 3; x_operate++) {
			/* 1044 */for (int z_operate = z - 3; z_operate <= z + 3; z_operate++) {
				/* 1046 */Block blockToChange = world.getBlockAt(x_operate, y, z_operate);
				/* 1047 */blockToChange.setTypeId(2);
			}
		}
		/* 1050 */Block blockToChange = world.getBlockAt(x - 3, y, z + 3);
		/* 1051 */blockToChange.setTypeId(0);
		/* 1052 */blockToChange = world.getBlockAt(x - 3, y, z - 3);
		/* 1053 */blockToChange.setTypeId(0);
		/* 1054 */blockToChange = world.getBlockAt(x + 3, y, z - 3);
		/* 1055 */blockToChange.setTypeId(0);
		/* 1056 */blockToChange = world.getBlockAt(x + 3, y, z + 3);
		/* 1057 */blockToChange.setTypeId(0);
	}

	private void islandLayer2(int x, int z, Player player, World world) {
		/* 1061 */int y = Settings.island_height;
		/* 1062 */y = Settings.island_height + 3;
		/* 1063 */for (int x_operate = x - 2; x_operate <= x + 2; x_operate++) {
			/* 1065 */for (int z_operate = z - 2; z_operate <= z + 2; z_operate++) {
				/* 1067 */Block blockToChange = world.getBlockAt(x_operate, y, z_operate);
				/* 1068 */blockToChange.setTypeId(3);
			}
		}
		/* 1071 */Block blockToChange = world.getBlockAt(x - 3, y, z);
		/* 1072 */blockToChange.setTypeId(3);
		/* 1073 */blockToChange = world.getBlockAt(x + 3, y, z);
		/* 1074 */blockToChange.setTypeId(3);
		/* 1075 */blockToChange = world.getBlockAt(x, y, z - 3);
		/* 1076 */blockToChange.setTypeId(3);
		/* 1077 */blockToChange = world.getBlockAt(x, y, z + 3);
		/* 1078 */blockToChange.setTypeId(3);
		/* 1079 */blockToChange = world.getBlockAt(x, y, z);
		/* 1080 */blockToChange.setTypeId(12);
	}

	private void islandLayer3(int x, int z, Player player, World world) {
		/* 1085 */int y = Settings.island_height;
		/* 1086 */y = Settings.island_height + 2;
		/* 1087 */for (int x_operate = x - 1; x_operate <= x + 1; x_operate++) {
			/* 1089 */for (int z_operate = z - 1; z_operate <= z + 1; z_operate++) {
				/* 1091 */Block blockToChange = world.getBlockAt(x_operate, y, z_operate);
				/* 1092 */blockToChange.setTypeId(3);
			}
		}
		/* 1095 */Block blockToChange = world.getBlockAt(x - 2, y, z);
		/* 1096 */blockToChange.setTypeId(3);
		/* 1097 */blockToChange = world.getBlockAt(x + 2, y, z);
		/* 1098 */blockToChange.setTypeId(3);
		/* 1099 */blockToChange = world.getBlockAt(x, y, z - 2);
		/* 1100 */blockToChange.setTypeId(3);
		/* 1101 */blockToChange = world.getBlockAt(x, y, z + 2);
		/* 1102 */blockToChange.setTypeId(3);
		/* 1103 */blockToChange = world.getBlockAt(x, y, z);
		/* 1104 */blockToChange.setTypeId(12);
	}

	private void islandLayer4(int x, int z, Player player, World world) {
		/* 1109 */int y = Settings.island_height;
		/* 1110 */y = Settings.island_height + 1;
		/* 1111 */Block blockToChange = world.getBlockAt(x - 1, y, z);
		/* 1112 */blockToChange.setTypeId(3);
		/* 1113 */blockToChange = world.getBlockAt(x + 1, y, z);
		/* 1114 */blockToChange.setTypeId(3);
		/* 1115 */blockToChange = world.getBlockAt(x, y, z - 1);
		/* 1116 */blockToChange.setTypeId(3);
		/* 1117 */blockToChange = world.getBlockAt(x, y, z + 1);
		/* 1118 */blockToChange.setTypeId(3);
		/* 1119 */blockToChange = world.getBlockAt(x, y, z);
		/* 1120 */blockToChange.setTypeId(12);
	}

	private void islandExtras(int x, int z, Player player, World world) {
		/* 1125 */int y = Settings.island_height;

		/* 1127 */Block blockToChange = world.getBlockAt(x, y + 5, z);
		/* 1128 */blockToChange.setTypeId(17);
		/* 1129 */blockToChange = world.getBlockAt(x, y + 6, z);
		/* 1130 */blockToChange.setTypeId(17);
		/* 1131 */blockToChange = world.getBlockAt(x, y + 7, z);
		/* 1132 */blockToChange.setTypeId(17);
		/* 1133 */y = Settings.island_height + 8;
		/* 1134 */for (int x_operate = x - 2; x_operate <= x + 2; x_operate++) {
			/* 1136 */for (int z_operate = z - 2; z_operate <= z + 2; z_operate++) {
				/* 1138 */blockToChange = world.getBlockAt(x_operate, y, z_operate);
				/* 1139 */blockToChange.setTypeId(18);
			}
		}
		/* 1142 */blockToChange = world.getBlockAt(x + 2, y, z + 2);
		/* 1143 */blockToChange.setTypeId(0);
		/* 1144 */blockToChange = world.getBlockAt(x + 2, y, z - 2);
		/* 1145 */blockToChange.setTypeId(0);
		/* 1146 */blockToChange = world.getBlockAt(x - 2, y, z + 2);
		/* 1147 */blockToChange.setTypeId(0);
		/* 1148 */blockToChange = world.getBlockAt(x - 2, y, z - 2);
		/* 1149 */blockToChange.setTypeId(0);
		/* 1150 */blockToChange = world.getBlockAt(x, y, z);
		/* 1151 */blockToChange.setTypeId(17);
		/* 1152 */y = Settings.island_height + 9;
		/* 1153 */for (int x_operate = x - 1; x_operate <= x + 1; x_operate++) {
			/* 1155 */for (int z_operate = z - 1; z_operate <= z + 1; z_operate++) {
				/* 1157 */blockToChange = world.getBlockAt(x_operate, y, z_operate);
				/* 1158 */blockToChange.setTypeId(18);
			}
		}
		/* 1161 */blockToChange = world.getBlockAt(x - 2, y, z);
		/* 1162 */blockToChange.setTypeId(18);
		/* 1163 */blockToChange = world.getBlockAt(x + 2, y, z);
		/* 1164 */blockToChange.setTypeId(18);
		/* 1165 */blockToChange = world.getBlockAt(x, y, z - 2);
		/* 1166 */blockToChange.setTypeId(18);
		/* 1167 */blockToChange = world.getBlockAt(x, y, z + 2);
		/* 1168 */blockToChange.setTypeId(18);
		/* 1169 */blockToChange = world.getBlockAt(x, y, z);
		/* 1170 */blockToChange.setTypeId(17);
		/* 1171 */y = Settings.island_height + 10;
		/* 1172 */blockToChange = world.getBlockAt(x - 1, y, z);
		/* 1173 */blockToChange.setTypeId(18);
		/* 1174 */blockToChange = world.getBlockAt(x + 1, y, z);
		/* 1175 */blockToChange.setTypeId(18);
		/* 1176 */blockToChange = world.getBlockAt(x, y, z - 1);
		/* 1177 */blockToChange.setTypeId(18);
		/* 1178 */blockToChange = world.getBlockAt(x, y, z + 1);
		/* 1179 */blockToChange.setTypeId(18);
		/* 1180 */blockToChange = world.getBlockAt(x, y, z);
		/* 1181 */blockToChange.setTypeId(17);
		/* 1182 */blockToChange = world.getBlockAt(x, y + 1, z);
		/* 1183 */blockToChange.setTypeId(18);

		/* 1185 */blockToChange = world.getBlockAt(x, Settings.island_height + 5, z + 1);
		/* 1186 */blockToChange.setTypeId(54);
		/* 1187 */Chest chest = (Chest) blockToChange.getState();
		/* 1188 */Inventory inventory = chest.getInventory();
		/* 1189 */inventory.clear();
		/* 1190 */inventory.setContents(Settings.island_chestItems);
		/* 1191 */if (Settings.island_addExtraItems) {
			/* 1193 */for (int i = 0; i < Settings.island_extraPermissions.length; i++) {
				/* 1195 */if (VaultHandler.checkPerk(player.getName(), "usb." + Settings.island_extraPermissions[i], player.getWorld())) {
					/* 1197 */String[] chestItemString = uSkyBlock.getInstance().getConfig()
							.getString("options.island.extraPermissions." + Settings.island_extraPermissions[i]).split(" ");
					/* 1198 */ItemStack[] tempChest = new ItemStack[chestItemString.length];
					/* 1199 */String[] amountdata = new String[2];
					/* 1200 */for (int j = 0; j < chestItemString.length; j++) {
						/* 1202 */amountdata = chestItemString[j].split(":");
						/* 1203 */tempChest[j] = new ItemStack(Integer.parseInt(amountdata[0]), Integer.parseInt(amountdata[1]));
						/* 1204 */inventory.addItem(new ItemStack[] { tempChest[j] });
					}
				}
			}
		}
	}

	private void setNewPlayerIsland(Player player, Location loc) {
		/* 1213 */((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(player.getName())).setHasIsland(true);
		/* 1214 */((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(player.getName())).setIslandLocation(loc);

		/* 1225 */player.teleport(getChestSpawnLoc(loc, player));
		/* 1226 */uSkyBlock.getInstance().homeSet(player);
		/* 1227 */uSkyBlock.getInstance().writePlayerFile(player.getName(),
				(PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(player.getName()));
	}

	private void inviteDebug(Player player) {
		/* 1270 */player.sendMessage(this.inviteList.toString());
	}

	private void invitePurge() {
		/* 1275 */this.inviteList.clear();
		/* 1276 */this.inviteList.put("NoInviter", "NoInvited");
	}

	public boolean addPlayertoParty(String playername, String partyleader) {
		/* 1298 */if (!uSkyBlock.getInstance().getActivePlayers().containsKey(playername)) {
			/* 1300 */System.out.println("Failed to add player to party! (" + playername + ")");
			/* 1301 */return false;
		}
		/* 1303 */if (!uSkyBlock.getInstance().getActivePlayers().containsKey(partyleader)) {
			/* 1305 */System.out.println("Failed to add player to party! (" + playername + ")");
			/* 1306 */return false;
		}
		/* 1308 */System.out.println("Adding player: " + playername + " to party with leader: " + partyleader);
		/* 1309 */((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(playername)).setJoinParty(partyleader,
				((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(partyleader)).getIslandLocation());
		/* 1310 */if (!playername.equalsIgnoreCase(partyleader)) {
			/* 1312 */if (((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(partyleader)).getHomeLocation() != null) {
				/* 1313 */((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(playername))
						.setHomeLocation(((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(partyleader)).getHomeLocation());
			} else {
				/* 1316 */((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(playername))
						.setHomeLocation(((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(partyleader)).getIslandLocation());
			}

			/* 1319 */if (!((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(partyleader)).getMembers().contains(playername)) {
				/* 1321 */((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(partyleader)).addMember(playername);
			}
			/* 1323 */if (!((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(partyleader)).getMembers().contains(partyleader)) {
				/* 1325 */((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(partyleader)).addMember(partyleader);
			}
		}
		/* 1328 */uSkyBlock.getInstance().writePlayerFile(playername,
				(PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(playername));

		/* 1330 */if (!((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(playername)).getPartyLeader().equalsIgnoreCase(
				partyleader)) {
			/* 1332 */System.out.println("Error adding player to a new party!");
			/* 1333 */return false;
		}
		/* 1335 */return true;
	}

	public void removePlayerFromParty(String playername, String partyleader) {
		/* 1342 */if (uSkyBlock.getInstance().getActivePlayers().containsKey(playername)) {
			/* 1344 */if (!((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(playername)).getPartyLeader().equalsIgnoreCase(
					playername))
				/* 1345 */((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(playername)).setHomeLocation(null);
			/* 1346 */((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(playername)).setLeaveParty();
			/* 1347 */uSkyBlock.getInstance().writePlayerFile(playername,
					(PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(playername));
		} else {
			/* 1350 */PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(playername);
			/* 1351 */if (!pi.getPartyLeader().equalsIgnoreCase(playername))
				/* 1352 */pi.setHomeLocation(null);
			/* 1353 */pi.setLeaveParty();
			/* 1354 */uSkyBlock.getInstance().writePlayerFile(playername, pi);
		}
		/* 1356 */if (uSkyBlock.getInstance().getActivePlayers().containsKey(partyleader)) {
			/* 1358 */if (((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(partyleader)).getMembers().contains(playername)) {
				/* 1360 */((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(partyleader)).removeMember(playername);
			}
			/* 1362 */uSkyBlock.getInstance().writePlayerFile(partyleader,
					(PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(partyleader));
		} else {
			/* 1365 */PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(partyleader);
			/* 1366 */if (pi.getMembers().contains(playername))
				/* 1367 */pi.removeMember(playername);
			/* 1368 */uSkyBlock.getInstance().writePlayerFile(partyleader, pi);
		}
	}

	@SuppressWarnings("unchecked")
	public <T, E> T getKeyByValue(Map<T, E> map, E value) {
		/* 1373 */for (Map.Entry entry : map.entrySet()) {
			/* 1374 */if (value.equals(entry.getValue())) {
				/* 1375 */return (T) entry.getKey();
			}
		}
		/* 1378 */return null;
	}

	public boolean getIslandLevel(Player player, String islandPlayer) {
		/* 1383 */if (this.allowInfo) {
			/* 1385 */System.out.println("Preparing to calculate island level");
			/* 1386 */this.allowInfo = false;
			/* 1387 */final String playerx = player.getName();
			/* 1388 */final String islandPlayerx = islandPlayer;
			/* 1389 */if ((!uSkyBlock.getInstance().hasIsland(islandPlayer)) && (!uSkyBlock.getInstance().hasParty(islandPlayer))) {
				/* 1391 */player.sendMessage(ChatColor.RED + "That player is invalid or does not have an island!");
				/* 1392 */this.allowInfo = true;
				/* 1393 */return false;
			}
			/* 1395 */uSkyBlock.getInstance().getServer().getScheduler().runTaskAsynchronously(uSkyBlock.getInstance(), new Runnable() {
				public void run() {
					/* 1399 */System.out.println("Calculating island level in async thread");
					try {
						/* 1401 */String player = playerx;
						/* 1402 */String islandPlayer = islandPlayerx;
						Location l;
						/* 1409 */if (((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(player)).getHasParty()) {
							/* 1411 */l = ((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(player)).getPartyIslandLocation();
						} else
							/* 1414 */l = ((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(player)).getIslandLocation();
						/* 1415 */int blockcount = 0;
						/* 1416 */if (player.equalsIgnoreCase(islandPlayer)) {
							/* 1418 */int cobblecount = 0;
							/* 1419 */int px = l.getBlockX();
							/* 1420 */int py = l.getBlockY();
							/* 1421 */int pz = l.getBlockZ();
							/* 1422 */for (int x = -50; x <= 50; x++) {
								/* 1423 */for (int y = Settings.island_height * -1; y <= 255 - Settings.island_height; y++) {
									/* 1424 */for (int z = -50; z <= 50; z++) {
										/* 1426 */Block b = new Location(l.getWorld(), px + x, py + y, pz + z).getBlock();
										/* 1427 */if (b.getTypeId() == 57)
											/* 1428 */blockcount += 300;
										/* 1429 */if ((b.getTypeId() == 41) || (b.getTypeId() == 116) || (b.getTypeId() == 122))
											/* 1430 */blockcount += 150;
										/* 1431 */if ((b.getTypeId() == 49) || (b.getTypeId() == 42))
											/* 1432 */blockcount += 10;
										/* 1433 */if ((b.getTypeId() == 47) || (b.getTypeId() == 84))
											/* 1434 */blockcount += 5;
										/* 1435 */if ((b.getTypeId() == 79) || (b.getTypeId() == 82) || (b.getTypeId() == 112)
												|| (b.getTypeId() == 2) || (b.getTypeId() == 110))
											/* 1436 */blockcount += 3;
										/* 1437 */if ((b.getTypeId() == 98) || (b.getTypeId() == 45) || (b.getTypeId() == 35)
												|| (b.getTypeId() == 24) ||
												/* 1438 */(b.getTypeId() == 121) || (b.getTypeId() == 108) || (b.getTypeId() == 109)
												|| (b.getTypeId() == 43) ||
												/* 1439 */(b.getTypeId() == 20))
											/* 1440 */blockcount += 2;
										/* 1441 */if (((b.getTypeId() != 0) && (b.getTypeId() != 8) && (b.getTypeId() != 9)
												&& (b.getTypeId() != 10) && (b.getTypeId() != 11) && (b.getTypeId() != 4))
												|| ((b.getTypeId() == 4) && (cobblecount < 10000))) {
											/* 1443 */blockcount++;
											/* 1444 */if (b.getTypeId() == 4) {
												/* 1446 */cobblecount++;
											}
										}
									}
								}
							}
						}

						/* 1454 */if (player.equalsIgnoreCase(islandPlayer)) {
							/* 1456 */((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(player))
									.setIslandLevel(blockcount / 100);
						}
					} catch (Exception e) {
						/* 1459 */System.out.println("Error while calculating Island Level: " + e);
						/* 1460 */IslandCommand.this.allowInfo = true;
					}
					/* 1462 */System.out.println("Finished async info thread");

					/* 1465 */uSkyBlock.getInstance().getServer().getScheduler()
							.scheduleSyncDelayedTask(uSkyBlock.getInstance(), new Runnable() {
								public void run() {
									/* 1470 */IslandCommand.this.allowInfo = true;
									/* 1471 */System.out.println("Back to sync thread for info");
									/* 1472 */if (Bukkit.getPlayer(playerx) != null) {
										/* 1474 */Bukkit.getPlayer(playerx).sendMessage(
												ChatColor.YELLOW + "Information about " + islandPlayerx + "'s Island:");
										/* 1475 */if (playerx.equalsIgnoreCase(islandPlayerx)) {
											/* 1477 */Bukkit.getPlayer(playerx).sendMessage(
													ChatColor.GREEN
															+ "Island level is "
															+ ChatColor.WHITE
															+ ((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(playerx))
																	.getIslandLevel());
										} else {
											/* 1480 */PlayerInfo pi = uSkyBlock.getInstance().readPlayerFile(islandPlayerx);
											/* 1481 */if (pi != null)
												/* 1482 */Bukkit.getPlayer(playerx).sendMessage(
														ChatColor.GREEN + "Island level is " + ChatColor.WHITE + pi.getIslandLevel());
											else
												/* 1484 */Bukkit.getPlayer(playerx).sendMessage(ChatColor.RED + "Error: Invalid Player");
										}
									}
									/* 1487 */System.out.println("Finished with sync thread for info");
								}
							}, 0L);
				}
			});
		} else {
			/* 1494 */player.sendMessage(ChatColor.RED + "Can't use that command right now! Try again in a few seconds.");
			/* 1495 */System.out.println(player.getName() + " tried to use /island info but someone else used it first!");
			/* 1496 */return false;
		}
		/* 1498 */return true;
	}

	public void setChest(Location loc, Player player) {
		/* 1503 */for (int x = -15; x <= 15; x++)
			/* 1504 */for (int y = -15; y <= 15; y++)
				/* 1505 */for (int z = -15; z <= 15; z++)
					/* 1506 */if (uSkyBlock.getSkyBlockWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z)
							.getTypeId() == 54) {
						/* 1508 */Block blockToChange = uSkyBlock.getSkyBlockWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y,
								loc.getBlockZ() + z);
						/* 1509 */Chest chest = (Chest) blockToChange.getState();
						/* 1510 */Inventory inventory = chest.getInventory();
						/* 1511 */inventory.clear();
						/* 1512 */inventory.setContents(Settings.island_chestItems);
						/* 1513 */if (Settings.island_addExtraItems) {
							/* 1515 */for (int i = 0; i < Settings.island_extraPermissions.length; i++) {
								/* 1517 */if (VaultHandler.checkPerk(player.getName(), "usb." + Settings.island_extraPermissions[i],
										player.getWorld())) {
									/* 1519 */String[] chestItemString = uSkyBlock.getInstance().getConfig()
											.getString("options.island.extraPermissions." + Settings.island_extraPermissions[i]).split(" ");
									/* 1520 */ItemStack[] tempChest = new ItemStack[chestItemString.length];
									/* 1521 */String[] amountdata = new String[2];
									/* 1522 */for (int j = 0; j < chestItemString.length; j++) {
										/* 1524 */amountdata = chestItemString[j].split(":");
										/* 1525 */tempChest[j] = new ItemStack(Integer.parseInt(amountdata[0]),
												Integer.parseInt(amountdata[1]));
										/* 1526 */inventory.addItem(new ItemStack[] { tempChest[j] });
									}
								}
							}
						}
					}
	}

	public Location getChestSpawnLoc(Location loc, Player player) {
		/* 1540 */for (int x = -15; x <= 15; x++) {
			/* 1541 */for (int y = -15; y <= 15; y++) {
				/* 1542 */for (int z = -15; z <= 15; z++) {
					/* 1543 */if (uSkyBlock.getSkyBlockWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z)
							.getTypeId() == 54) {
						/* 1545 */if ((uSkyBlock.getSkyBlockWorld()
								.getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + (z + 1)).getTypeId() == 0)
								&&
								/* 1546 */(uSkyBlock.getSkyBlockWorld()
										.getBlockAt(loc.getBlockX() + x, loc.getBlockY() + (y - 1), loc.getBlockZ() + (z + 1)).getTypeId() != 0))
							/* 1547 */return new Location(uSkyBlock.getSkyBlockWorld(), loc.getBlockX() + x, loc.getBlockY() + (y + 1),
									loc.getBlockZ() + (z + 1));
						/* 1548 */if ((uSkyBlock.getSkyBlockWorld()
								.getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + (z - 1)).getTypeId() == 0)
								&&
								/* 1549 */(uSkyBlock.getSkyBlockWorld()
										.getBlockAt(loc.getBlockX() + x, loc.getBlockY() + (y - 1), loc.getBlockZ() + (z - 1)).getTypeId() != 0))
							/* 1550 */return new Location(uSkyBlock.getSkyBlockWorld(), loc.getBlockX() + x, loc.getBlockY() + (y + 1),
									loc.getBlockZ() + (z + 1));
						/* 1551 */if ((uSkyBlock.getSkyBlockWorld()
								.getBlockAt(loc.getBlockX() + (x + 1), loc.getBlockY() + y, loc.getBlockZ() + z).getTypeId() == 0)
								&&
								/* 1552 */(uSkyBlock.getSkyBlockWorld()
										.getBlockAt(loc.getBlockX() + (x + 1), loc.getBlockY() + (y - 1), loc.getBlockZ() + z).getTypeId() != 0))
							/* 1553 */return new Location(uSkyBlock.getSkyBlockWorld(), loc.getBlockX() + x, loc.getBlockY() + (y + 1),
									loc.getBlockZ() + (z + 1));
						/* 1554 */if ((uSkyBlock.getSkyBlockWorld()
								.getBlockAt(loc.getBlockX() + (x - 1), loc.getBlockY() + y, loc.getBlockZ() + z).getTypeId() == 0)
								&&
								/* 1555 */(uSkyBlock.getSkyBlockWorld()
										.getBlockAt(loc.getBlockX() + (x - 1), loc.getBlockY() + (y - 1), loc.getBlockZ() + z).getTypeId() != 0))
							/* 1556 */return new Location(uSkyBlock.getSkyBlockWorld(), loc.getBlockX() + x, loc.getBlockY() + (y + 1),
									loc.getBlockZ() + (z + 1));
						/* 1557 */return loc;
					}
				}
			}
		}
		/* 1562 */return loc;
	}
}