package us.talabrek.ultimateskyblock.island;

import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;

import java.util.*;
import java.util.logging.Level;

import org.bukkit.*;
import us.talabrek.ultimateskyblock.*;
import us.talabrek.ultimateskyblock.player.PlayerInfo;

public class IslandCommand implements CommandExecutor {
    private List<String> banList;
    private String tempTargetPlayer;
    public boolean allowInfo;
    Set<String> memberList;
    private HashMap<String, String> inviteList;

    public IslandCommand() {
        super();
        this.allowInfo = true;
        this.memberList = null;
        (this.inviteList = new HashMap<>()).put("NoInvited", "NoInviter");
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] split) {
        if (!(sender instanceof Player)) {
            return handleConsoleCommand(sender, command, label, split);
        }
        final Player player = (Player) sender;
        uSkyBlock sky = uSkyBlock.getInstance();
        final PlayerInfo pi = sky.getPlayerInfo(player);
        if (pi == null) {
            player.sendMessage(ChatColor.RED + "Error: Couldn't read your player data!");
            return true;
        }
        String iName = "";
        FileConfiguration islandConfig = null;
        if (pi.getHasIsland() && pi.getIslandLocation() != null) {
            iName = pi.locationForParty();
            islandConfig = sky.getIslandConfig(pi);
        }
        if (split.length == 0) {
            sky.updatePartyNumber(player);
            player.openInventory(sky.getMenu().displayIslandGUI(player));
            return true;
        }
        if (split.length == 1) {
            if ((split[0].equals("restart") || split[0].equals("reset")) && pi.getIslandLocation() != null) {
                if (sky.getIslandConfig(iName).getInt("party.currentSize") > 1) {
                    if (!sky.getIslandConfig(iName).getString("party.leader").equalsIgnoreCase(player.getName())) {
                        player.sendMessage(ChatColor.RED + "Only the owner may restart this island. Leave this island in order to start your own (/island leave).");
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "You must remove all players from your island before you can restart it (/island kick <player>). See a list of players currently part of your island using /island party.");
                    }
                    return true;
                }
                if (!sky.onRestartCooldown(player) || Settings.general_cooldownRestart == 0) {
                    return sky.restartPlayerIsland(player, pi.getIslandLocation());
                }
                player.sendMessage(ChatColor.YELLOW + "You can restart your island in " + sky.getRestartCooldownTime(player) / 1000L + " seconds.");
                return true;
            } else {
                if ((split[0].equals("sethome") || split[0].equals("tpset")) && pi.getIslandLocation() != null && VaultHandler.checkPerk(player.getName(), "usb.island.sethome", player.getWorld())) {
                    sky.homeSet(player);
                    return true;
                }
                if ((split[0].equals("log") || split[0].equals("l")) && pi.getIslandLocation() != null && VaultHandler.checkPerk(player.getName(), "usb.island.create", player.getWorld())) {
                    player.openInventory(sky.getMenu().displayLogGUI(player));
                    return true;
                }
                if ((split[0].equals("create") || split[0].equals("c")) && VaultHandler.checkPerk(player.getName(), "usb.island.create", player.getWorld())) {
                    if (pi.getIslandLocation() == null) {
                        sky.createIsland(sender, pi);
                        return true;
                    }
                    if (pi.getIslandLocation().getBlockX() == 0 && pi.getIslandLocation().getBlockY() == 0 && pi.getIslandLocation().getBlockZ() == 0) {
                        sky.createIsland(sender, pi);
                        return true;
                    }
                    return true;
                } else {
                    if ((split[0].equals("home") || split[0].equals("h")) && pi.getIslandLocation() != null && VaultHandler.checkPerk(player.getName(), "usb.island.sethome", player.getWorld())) {
                        if (pi.getHomeLocation() == null) {
                            sky.getPlayerInfo(player).setHomeLocation(pi.getIslandLocation());
                        }
                        sky.homeTeleport(player);
                        return true;
                    }
                    if ((split[0].equals("setwarp") || split[0].equals("warpset")) && pi.getIslandLocation() != null && VaultHandler.checkPerk(player.getName(), "usb.extra.addwarp", player.getWorld())) {
                        if (sky.getIslandConfig(iName).getBoolean("party.members." + player.getName() + ".canChangeWarp")) {
                            sky.sendMessageToIslandGroup(iName, String.valueOf(player.getName()) + " changed the island warp location.");
                            sky.warpSet(player);
                        } else {
                            player.sendMessage("\u00a7cYou do not have permission to set your island's warp point!");
                        }
                        return true;
                    }
                    if (split[0].equals("warp") || split[0].equals("w")) {
                        if (VaultHandler.checkPerk(player.getName(), "usb.extra.addwarp", player.getWorld())) {
                            if (sky.getIslandConfig(iName).getBoolean("general.warpActive")) {
                                player.sendMessage(ChatColor.GREEN + "Your incoming warp is active, players may warp to your island.");
                            } else {
                                player.sendMessage(ChatColor.RED + "Your incoming warp is inactive, players may not warp to your island.");
                            }
                            player.sendMessage(ChatColor.WHITE + "Set incoming warp to your current location using " + ChatColor.YELLOW + "/island setwarp");
                            player.sendMessage(ChatColor.WHITE + "Toggle your warp on/off using " + ChatColor.YELLOW + "/island togglewarp");
                        } else {
                            player.sendMessage(ChatColor.RED + "You do not have permission to create a warp on your island!");
                        }
                        if (VaultHandler.checkPerk(player.getName(), "usb.island.warp", player.getWorld())) {
                            player.sendMessage(ChatColor.WHITE + "Warp to another island using " + ChatColor.YELLOW + "/island warp <player>");
                        } else {
                            player.sendMessage(ChatColor.RED + "You do not have permission to warp to other islands!");
                        }
                        return true;
                    }
                    if ((split[0].equals("togglewarp") || split[0].equals("tw")) && pi.getIslandLocation() != null) {
                        if (VaultHandler.checkPerk(player.getName(), "usb.extra.addwarp", player.getWorld())) {
                            if (sky.getIslandConfig(iName).getBoolean("party.members." + player.getName() + ".canToggleWarp")) {
                                if (!sky.getIslandConfig(iName).getBoolean("general.warpActive")) {
                                    if (sky.getIslandConfig(iName).getBoolean("general.locked")) {
                                        player.sendMessage(ChatColor.RED + "Your island is locked. You must unlock it before enabling your warp.");
                                        return true;
                                    }
                                    sky.sendMessageToIslandGroup(iName, String.valueOf(player.getName()) + " activated the island warp.");
                                    sky.getIslandConfig(iName).set("general.warpActive", true);
                                } else {
                                    sky.sendMessageToIslandGroup(iName, String.valueOf(player.getName()) + " deactivated the island warp.");
                                    sky.getIslandConfig(iName).set("general.warpActive", false);
                                }
                            } else {
                                player.sendMessage("\u00a7cYou do not have permission to enable/disable your island's warp!");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You do not have permission to create a warp on your island!");
                        }
                        return true;
                    }
                    if ((split[0].equals("ban") || split[0].equals("banned") || split[0].equals("banlist") || split[0].equals("b")) && pi.getIslandLocation() != null) {
                        if (VaultHandler.checkPerk(player.getName(), "usb.island.ban", player.getWorld())) {
                            player.sendMessage(ChatColor.YELLOW + "The following players are banned from warping to your island:");
                            player.sendMessage(ChatColor.RED + this.getBanList(player));
                            player.sendMessage(ChatColor.YELLOW + "To ban/unban from your island, use /island ban <player>");
                        } else {
                            player.sendMessage(ChatColor.RED + "You do not have permission to ban players from your island!");
                        }
                        return true;
                    }
                    if (split[0].equals("lock") && pi.getIslandLocation() != null) {
                        if (Settings.island_allowIslandLock && VaultHandler.checkPerk(player.getName(), "usb.lock", player.getWorld())) {
                            if (sky.getIslandConfig(iName).getBoolean("party.members." + player.getName() + ".canToggleLock")) {
                                WorldGuardHandler.islandLock(sender, sky.getIslandConfig(iName).getString("party.leader"));
                                sky.getIslandConfig(iName).set("general.locked", true);
                                sky.sendMessageToIslandGroup(iName, String.valueOf(player.getName()) + " locked the island.");
                                if (sky.getIslandConfig(iName).getBoolean("general.warpActive")) {
                                    sky.getIslandConfig(iName).set("general.warpActive", false);
                                    player.sendMessage(ChatColor.RED + "Since your island is locked, your incoming warp has been deactivated.");
                                    sky.sendMessageToIslandGroup(iName, String.valueOf(player.getName()) + " deactivated the island warp.");
                                }
                                sky.saveIslandConfig(iName);
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to lock your island!");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You don't have access to this command!");
                        }
                        return true;
                    }
                    if (split[0].equals("unlock") && pi.getIslandLocation() != null) {
                        if (Settings.island_allowIslandLock && VaultHandler.checkPerk(player.getName(), "usb.lock", player.getWorld())) {
                            if (sky.getIslandConfig(iName).getBoolean("party.members." + player.getName() + ".canToggleLock")) {
                                WorldGuardHandler.islandUnlock(sender, sky.getIslandConfig(iName).getString("party.leader"));
                                sky.getIslandConfig(iName).set("general.locked", false);
                                sky.sendMessageToIslandGroup(iName, String.valueOf(player.getName()) + " unlocked the island.");
                                sky.saveIslandConfig(iName);
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to unlock your island!");
                            }
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
                        if (VaultHandler.checkPerk(player.getName(), "usb.island.warp", player.getWorld())) {
                            player.sendMessage(ChatColor.YELLOW + "/island warp <player> :" + ChatColor.WHITE + " warp to another player's island.");
                        }
                        if (VaultHandler.checkPerk(player.getName(), "usb.extra.addwarp", player.getWorld())) {
                            player.sendMessage(ChatColor.YELLOW + "/island setwarp :" + ChatColor.WHITE + " set your island's warp location.");
                            player.sendMessage(ChatColor.YELLOW + "/island togglewarp :" + ChatColor.WHITE + " enable/disable warping to your island.");
                        }
                        if (VaultHandler.checkPerk(player.getName(), "usb.island.ban", player.getWorld())) {
                            player.sendMessage(ChatColor.YELLOW + "/island ban <player> :" + ChatColor.WHITE + " ban/unban a player from your island.");
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
                        if (Bukkit.getServer().getServerId().equalsIgnoreCase("UltimateSkyblock")) {
                            player.sendMessage(ChatColor.YELLOW + "/dungeon :" + ChatColor.WHITE + " to warp to the dungeon world.");
                            player.sendMessage(ChatColor.YELLOW + "/fun :" + ChatColor.WHITE + " to warp to the Mini-Game/Fun world.");
                            player.sendMessage(ChatColor.YELLOW + "/pvp :" + ChatColor.WHITE + " join a pvp match.");
                            player.sendMessage(ChatColor.YELLOW + "/spleef :" + ChatColor.WHITE + " join spleef match.");
                            player.sendMessage(ChatColor.YELLOW + "/hub :" + ChatColor.WHITE + " warp to the world hub Sanconia.");
                        }
                        return true;
                    }
                    if (split[0].equals("top") && VaultHandler.checkPerk(player.getName(), "usb.island.topten", player.getWorld())) {
                        sky.displayTopTen(player);
                        return true;
                    }
                    if ((split[0].equals("biome") || split[0].equals("b")) && pi.getIslandLocation() != null) {
                        player.openInventory(sky.getMenu().displayBiomeGUI(player));
                        if (!sky.getIslandConfig(iName).getBoolean("party.members." + player.getName() + ".canChangeBiome")) {
                            player.sendMessage("\u00a7cYou do not have permission to change the biome of your current island.");
                        }
                        return true;
                    }
                    if ((split[0].equals("info") || split[0].equals("level")) && pi.getIslandLocation() != null && VaultHandler.checkPerk(player.getName(), "usb.island.info", player.getWorld()) && Settings.island_useIslandLevel) {
                        if (!sky.playerIsOnIsland(player)) {
                            player.sendMessage(ChatColor.YELLOW + "You must be on your island to use this command.");
                            return true;
                        }
                        if (!sky.onInfoCooldown(player) || Settings.general_cooldownInfo == 0) {
                            sky.setInfoCooldown(player);
                            if (!pi.getHasParty() && !pi.getHasIsland()) {
                                player.sendMessage(ChatColor.RED + "You do not have an island!");
                            } else {
                                getIslandLevel(player, player.getName(), split[0]);
                            }
                            return true;
                        }
                        player.sendMessage(ChatColor.YELLOW + "You can use that command again in " + sky.getInfoCooldownTime(player) / 1000L + " seconds.");
                        return true;
                    } else if (split[0].equals("invite") && pi.getIslandLocation() != null && VaultHandler.checkPerk(player.getName(), "usb.party.create", player.getWorld())) {
                        player.sendMessage(ChatColor.YELLOW + "Use" + ChatColor.WHITE + " /island invite <playername>" + ChatColor.YELLOW + " to invite a player to your island.");
                        if (!sky.hasParty(player.getName())) {
                            return true;
                        }
                        if (!islandConfig.getString("party.leader").equalsIgnoreCase(player.getName()) && !islandConfig.getBoolean("party.members." + player.getName() + ".canInviteOthers")) {
                            player.sendMessage(ChatColor.RED + "Only the island's owner can invite!");
                            return true;
                        }
                        if (VaultHandler.checkPerk(player.getName(), "usb.extra.partysize", player.getWorld())) {
                            if (islandConfig.getInt("party.currentSize") < Settings.general_maxPartySize * 2) {
                                player.sendMessage(ChatColor.GREEN + "You can invite " + (Settings.general_maxPartySize * 2 - islandConfig.getInt("party.currentSize")) + " more players.");
                            } else {
                                player.sendMessage(ChatColor.RED + "You can't invite any more players.");
                            }
                            return true;
                        }
                        if (islandConfig.getInt("party.currentSize") < Settings.general_maxPartySize) {
                            player.sendMessage(ChatColor.GREEN + "You can invite " + (Settings.general_maxPartySize - islandConfig.getInt("party.currentSize")) + " more players.");
                        } else {
                            player.sendMessage(ChatColor.RED + "You can't invite any more players.");
                        }
                        return true;
                    } else if (split[0].equals("accept") && VaultHandler.checkPerk(player.getName(), "usb.party.join", player.getWorld())) {
                        if (sky.onRestartCooldown(player) && Settings.general_cooldownRestart > 0) {
                            player.sendMessage(ChatColor.YELLOW + "You can't join an island for another " + sky.getRestartCooldownTime(player) / 1000L + " seconds.");
                            return true;
                        }
                        if (sky.hasParty(player.getName()) || !inviteList.containsKey(player.getName())) {
                            player.sendMessage(ChatColor.RED + "You can't use that command right now.");
                            return true;
                        }
                        if (pi.getHasIsland()) {
                            sky.deletePlayerIsland(player.getName());
                        }
                        player.sendMessage(ChatColor.GREEN + "You have joined an island! Use /island party to see the other members.");
                        addPlayertoParty(player, inviteList.get(player.getName()));
                        if (Bukkit.getPlayer(inviteList.get(player.getName())) != null) {
                            Bukkit.getPlayer(inviteList.get(player.getName())).sendMessage(ChatColor.GREEN + player.getName() + " has joined your island!");
                            sky.setRestartCooldown(player);
                            sky.homeTeleport(player);
                            sky.clearPlayerInventory(player);
                            islandConfig = sky.getIslandConfig(inviteList.get(player.getName()));
                            WorldGuardHandler.addPlayerToOldRegion(islandConfig.getString("party.leader"), player.getName());
                            inviteList.remove(player.getName());
                            return true;
                        }
                        player.sendMessage(ChatColor.RED + "You couldn't join the island, maybe it's full.");
                        return true;
                    } else {
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
                        if (split[0].equalsIgnoreCase("invitelist")) {
                            if (VaultHandler.checkPerk(player.getName(), "usb.mod.party", player.getWorld())) {
                                player.sendMessage(ChatColor.RED + "Checking Invites.");
                                this.inviteDebug(player);
                            } else {
                                player.sendMessage(ChatColor.RED + "You can't access that command!");
                            }
                            return true;
                        }
                        if (split[0].equals("leave") && pi.getIslandLocation() != null && VaultHandler.checkPerk(player.getName(), "usb.party.join", player.getWorld())) {
                            if (player.getWorld().getName().equalsIgnoreCase(uSkyBlock.getSkyBlockWorld().getName())) {
                                if (!sky.hasParty(player.getName())) {
                                    player.sendMessage(ChatColor.RED + "You can't leave your island if you are the only person. Try using /island restart if you want a new one!");
                                    return true;
                                }
                                if (sky.getIslandConfig(iName).getString("party.leader").equalsIgnoreCase(player.getName())) {
                                    player.sendMessage(ChatColor.YELLOW + "You own this island, use /island remove <player> instead.");
                                    return true;
                                }
                                player.getInventory().clear();
                                player.getEquipment().clear();
                                if (Settings.extras_sendToSpawn) {
                                    player.performCommand("spawn");
                                } else {
                                    player.teleport(uSkyBlock.getSkyBlockWorld().getSpawnLocation());
                                }
                                WorldGuardHandler.removePlayerFromRegion(islandConfig.getString("party.leader"), player.getName());
                                this.removePlayerFromParty(player.getName(), islandConfig.getString("party.leader"), pi.locationForParty());
                                player.sendMessage(ChatColor.YELLOW + "You have left the island and returned to the player spawn.");
                                if (Bukkit.getPlayer(islandConfig.getString("party.leader")) != null) {
                                    Bukkit.getPlayer(islandConfig.getString("party.leader")).sendMessage(ChatColor.RED + player.getName() + " has left your island!");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You must be in the skyblock world to leave your party!");
                            }
                            return true;
                        }
                        if (split[0].equals("party") && pi.getIslandLocation() != null) {
                            if (VaultHandler.checkPerk(player.getName(), "usb.party.create", player.getWorld())) {
                                player.openInventory(sky.getMenu().displayPartyGUI(player));
                            }
                            player.sendMessage(ChatColor.YELLOW + "Listing your island members:");
                            String total = "";
                            this.memberList = islandConfig.getConfigurationSection("party.members").getKeys(false);
                            total = 1 + "\u00a7a<" + islandConfig.getString("party.leader") + "> ";
                            for (final String temp : this.memberList) {
                                if (!temp.equalsIgnoreCase(islandConfig.getString("party.leader"))) {
                                    total = 1 + "\u00a7e[" + temp + "]";
                                }
                            }
                            player.sendMessage(total);
                            return true;
                        }
                    }
                }
            }
        } else if (split.length == 2) {
            if ((split[0].equals("info") || split[0].equals("level")) && pi.getIslandLocation() != null && VaultHandler.checkPerk(player.getName(), "usb.island.info", player.getWorld()) && Settings.island_useIslandLevel) {
                if (!sky.onInfoCooldown(player) || Settings.general_cooldownInfo == 0) {
                    sky.setInfoCooldown(player);
                    if (!pi.getHasParty() && !pi.getHasIsland()) {
                        player.sendMessage(ChatColor.RED + "You do not have an island!");
                    } else {
                        this.getIslandLevel(player, split[1], split[0]);
                    }
                    return true;
                }
                player.sendMessage(ChatColor.YELLOW + "You can use that command again in " + sky.getInfoCooldownTime(player) / 1000L + " seconds.");
                return true;
            } else {
                if (split[0].equals("warp") || split[0].equals("w")) {
                    if (VaultHandler.checkPerk(player.getName(), "usb.island.warp", player.getWorld())) {
                        PlayerInfo wPi = sky.getPlayerInfo(Bukkit.getPlayer(split[1]));
                        if (wPi == null || !wPi.getHasIsland()) {
                            player.sendMessage(ChatColor.RED + "That player does not exist!");
                            return true;
                        }
                        islandConfig = sky.getIslandConfig(wPi);
                        if (!islandConfig.getBoolean("general.warpActive")) {
                            player.sendMessage(ChatColor.RED + "That player does not have an active warp.");
                            return true;
                        }
                        if (!islandConfig.contains("banned.list." + player.getName())) {
                            sky.warpTeleport(player, wPi);
                        } else {
                            player.sendMessage(ChatColor.RED + "That player has forbidden you from warping to their island.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have permission to warp to other islands!");
                    }
                    return true;
                }
                if (split[0].equals("ban") && islandConfig != null) {
                    if (VaultHandler.checkPerk(player.getName(), "usb.island.ban", player.getWorld())) {
                        if (!islandConfig.contains("banned.list." + player.getName())) {
                            (this.banList = islandConfig.getStringList("banned.list")).add(split[1]);
                            islandConfig.set("banned.list", this.banList);
                            player.sendMessage(ChatColor.YELLOW + "You have banned " + ChatColor.RED + split[1] + ChatColor.YELLOW + " from warping to your island.");
                        } else {
                            (this.banList = islandConfig.getStringList("banned.list")).remove(split[1]);
                            islandConfig.set("banned.list", this.banList);
                            player.sendMessage(ChatColor.YELLOW + "You have unbanned " + ChatColor.GREEN + split[1] + ChatColor.YELLOW + " from warping to your island.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have permission to ban players from this island!");
                    }
                    return true;
                }
                if ((split[0].equals("biome") || split[0].equals("b")) && pi.getIslandLocation() != null) {
                    if (sky.getIslandConfig(iName).getBoolean("party.members." + player.getName() + ".canChangeBiome")) {
                        if (sky.onBiomeCooldown(player) && Settings.general_biomeChange != 0) {
                            player.sendMessage(ChatColor.YELLOW + "You can change your biome again in " + sky.getBiomeCooldownTime(player) / 1000L / 60L + " minutes.");
                            return true;
                        }
                        if (sky.playerIsOnIsland(player)) {
                            if (sky.changePlayerBiome(player, split[1])) {
                                player.sendMessage(ChatColor.GREEN + "You have changed your island's biome to " + split[1].toUpperCase());
                                player.sendMessage(ChatColor.GREEN + "You may need to relog to see the changes.");
                                sky.sendMessageToIslandGroup(iName, String.valueOf(player.getName()) + " changed the island biome to " + split[1].toUpperCase());
                                sky.setBiomeCooldown(player);
                            } else {
                                player.sendMessage(ChatColor.GREEN + "Unknown biome name, changing your biome to OCEAN");
                                player.sendMessage(ChatColor.GREEN + "You may need to relog to see the changes.");
                                sky.sendMessageToIslandGroup(iName, String.valueOf(player.getName()) + " changed the island biome to OCEAN");
                            }
                        } else {
                            player.sendMessage(ChatColor.YELLOW + "You must be on your island to change the biome!");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have permission to change the biome of this island!");
                    }
                    return true;
                }
                if (split[0].equalsIgnoreCase("invite") && pi.getIslandLocation() != null && VaultHandler.checkPerk(player.getName(), "usb.party.create", player.getWorld())) {
                    if (!sky.getIslandConfig(iName).getBoolean("party.members." + player.getName() + ".canInviteOthers")) {
                        player.sendMessage(ChatColor.RED + "You do not have permission to invite others to this island!");
                        return true;
                    }
                    if (Bukkit.getPlayer(split[1]) == null) {
                        player.sendMessage(ChatColor.RED + "That player is offline or doesn't exist.");
                        return true;
                    }
                    if (!Bukkit.getPlayer(split[1]).isOnline()) {
                        player.sendMessage(ChatColor.RED + "That player is offline or doesn't exist.");
                        return true;
                    }
                    if (!sky.hasIsland(player.getName())) {
                        player.sendMessage(ChatColor.RED + "You must have an island in order to invite people to it!");
                        return true;
                    }
                    if (player.getName().equalsIgnoreCase(Bukkit.getPlayer(split[1]).getName())) {
                        player.sendMessage(ChatColor.RED + "You can't invite yourself!");
                        return true;
                    }
                    if (sky.hasParty(player.getName())) {
                        if (!islandConfig.getString("party.leader").equalsIgnoreCase(Bukkit.getPlayer(split[1]).getName())) {
                            if (!sky.hasParty(Bukkit.getPlayer(split[1]).getName())) {
                                if (islandConfig.getInt("party.currentSize") < islandConfig.getInt("party.maxSize")) {
                                    if (inviteList.containsValue(player.getName())) {
                                        inviteList.remove(this.getKeyByValue(this.inviteList, player.getName()));
                                        player.sendMessage(ChatColor.YELLOW + "Removing your previous invite.");
                                    }
                                    inviteList.put(Bukkit.getPlayer(split[1]).getName(), player.getName());
                                    player.sendMessage(ChatColor.GREEN + "Invite sent to " + Bukkit.getPlayer(split[1]).getName());
                                    Bukkit.getPlayer(split[1]).sendMessage(String.valueOf(player.getName()) + " has invited you to join their island!");
                                    Bukkit.getPlayer(split[1]).sendMessage(ChatColor.WHITE + "/island [accept/reject]" + ChatColor.YELLOW + " to accept or reject the invite.");
                                    Bukkit.getPlayer(split[1]).sendMessage(ChatColor.RED + "WARNING: You will lose your current island if you accept!");
                                    sky.sendMessageToIslandGroup(iName, String.valueOf(player.getName()) + " invited " + Bukkit.getPlayer(split[1]).getName() + " to the island group.");
                                } else {
                                    player.sendMessage(ChatColor.RED + "Your island is full, you can't invite anyone else.");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "That player is already with a group on an island.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "That player is the leader of your island!");
                        }
                        return true;
                    }
                    if (!sky.hasParty(player.getName())) {
                        if (!sky.hasParty(Bukkit.getPlayer(split[1]).getName())) {
                            if (inviteList.containsValue(player.getName())) {
                                inviteList.remove(this.getKeyByValue(this.inviteList, player.getName()));
                                player.sendMessage(ChatColor.YELLOW + "Removing your previous invite.");
                            }
                            inviteList.put(Bukkit.getPlayer(split[1]).getName(), player.getName());
                            player.sendMessage(ChatColor.GREEN + "Invite sent to " + Bukkit.getPlayer(split[1]).getName());
                            Bukkit.getPlayer(split[1]).sendMessage(String.valueOf(player.getName()) + " has invited you to join their island!");
                            Bukkit.getPlayer(split[1]).sendMessage(ChatColor.WHITE + "/island [accept/reject]" + ChatColor.YELLOW + " to accept or reject the invite.");
                            Bukkit.getPlayer(split[1]).sendMessage(ChatColor.RED + "WARNING: You will lose your current island if you accept!");
                        } else {
                            player.sendMessage(ChatColor.RED + "That player is already with a group on an island.");
                        }
                        return true;
                    }
                    player.sendMessage(ChatColor.RED + "Only the island's owner may invite new players!");
                    return true;
                } else if ((split[0].equalsIgnoreCase("remove") || split[0].equalsIgnoreCase("kick")) && islandConfig != null && VaultHandler.checkPerk(player.getName(), "usb.party.kick", player.getWorld())) {
                    if (!sky.getIslandConfig(iName).getBoolean("party.members." + player.getName() + ".canKickOthers")) {
                        player.sendMessage(ChatColor.RED + "You do not have permission to kick others from this island!");
                        return true;
                    }
                    if (Bukkit.getPlayer(split[1]) == null && Bukkit.getOfflinePlayer(split[1]) == null) {
                        player.sendMessage(ChatColor.RED + "That player doesn't exist.");
                        return true;
                    }
                    if (Bukkit.getPlayer(split[1]) == null) {
                        this.tempTargetPlayer = Bukkit.getOfflinePlayer(split[1]).getName();
                    } else {
                        this.tempTargetPlayer = Bukkit.getPlayer(split[1]).getName();
                    }
                    if (islandConfig.contains("party.members." + split[1])) {
                        this.tempTargetPlayer = split[1];
                    }
                    if (sky.hasParty(player.getName())) {
                        if (!islandConfig.getString("party.leader").equalsIgnoreCase(this.tempTargetPlayer)) {
                            if (islandConfig.contains("party.members." + this.tempTargetPlayer)) {
                                if (player.getName().equalsIgnoreCase(this.tempTargetPlayer)) {
                                    player.sendMessage(ChatColor.RED + "Stop kickin' yourself!");
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
                                if (Bukkit.getPlayer(islandConfig.getString("party.leader")) != null) {
                                    Bukkit.getPlayer(islandConfig.getString("party.leader")).sendMessage(ChatColor.RED + this.tempTargetPlayer + " has been removed from the island.");
                                }
                                this.removePlayerFromParty(this.tempTargetPlayer, islandConfig.getString("party.leader"), pi.locationForParty());
                                uSkyBlock.log(Level.INFO, "Removing from " + islandConfig.getString("party.leader") + "'s Island");
                                WorldGuardHandler.removePlayerFromRegion(islandConfig.getString("party.leader"), this.tempTargetPlayer);
                            } else {
                                System.out.print("Player " + player.getName() + " failed to remove " + this.tempTargetPlayer);
                                player.sendMessage(ChatColor.RED + "That player is not part of your island group!");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You can't remove the leader from the Island!");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "No one else is on your island, are you seeing things?");
                    }
                    return true;
                }
            }
        }
        return true;
    }

    private boolean handleConsoleCommand(CommandSender sender, Command command, String label, String[] split) {
        if (split.length == 1) {
            if (split[0].equalsIgnoreCase("top")) {
                return uSkyBlock.getInstance().displayTopTen(sender);
            }
        }
        return false;
    }

    private void inviteDebug(final Player player) {
        player.sendMessage(inviteList.toString());
    }

    private void invitePurge() {
        inviteList.clear();
        inviteList.put("NoInviter", "NoInvited");
    }

    public boolean addPlayertoParty(final Player player, final String partyleader) {
        uSkyBlock sky = uSkyBlock.getInstance();
        PlayerInfo playerInfo = sky.getPlayerInfo(player);
        PlayerInfo leaderInfo = sky.getPlayerInfo(partyleader);
        playerInfo.setJoinParty(leaderInfo.getIslandLocation());
        if (playerInfo != leaderInfo) { // Caching is done in sky, this should be safe...
            if (leaderInfo.getHomeLocation() != null) {
                playerInfo.setHomeLocation(leaderInfo.getHomeLocation());
            } else {
                playerInfo.setHomeLocation(leaderInfo.getIslandLocation());
            }
            sky.setupPartyMember(leaderInfo.locationForParty(), player.getName());
        }
        playerInfo.save();
        sky.sendMessageToIslandGroup(leaderInfo.locationForParty(), 1 + " has joined your island group.");
        return true;
    }

    public void removePlayerFromParty(final String playername, final String partyleader, final String location) {
        uSkyBlock sky = uSkyBlock.getInstance();
        PlayerInfo playerInfo = sky.getPlayerInfo(playername);
        sky.getIslandConfig(location).set("party.members." + playername, null);
        sky.getIslandConfig(location).set("party.currentSize", sky.getIslandConfig(location).getInt("party.currentSize") - 1);
        sky.saveIslandConfig(location);
        sky.sendMessageToIslandGroup(location, 1 + " has been removed from the island group.");
        playerInfo.setHomeLocation(null);
        playerInfo.setLeaveParty();
        playerInfo.save();
    }

    public <T, E> T getKeyByValue(final Map<T, E> map, final E value) {
        for (final Map.Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public boolean getIslandLevel(final Player player, final String islandPlayer, final String cmd) {
        if (!this.allowInfo) {
            player.sendMessage(ChatColor.RED + "Can't use that command right now! Try again in a few seconds.");
            System.out.print(String.valueOf(player.getName()) + " tried to use /island info but someone else used it first!");
            return false;
        }
        this.allowInfo = false;
        if (!uSkyBlock.getInstance().hasIsland(islandPlayer) && !uSkyBlock.getInstance().hasParty(islandPlayer)) {
            player.sendMessage(ChatColor.RED + "That player is invalid or does not have an island!");
            this.allowInfo = true;
            return false;
        }
        final PlayerInfo playerInfo = islandPlayer.equals(player.getName()) ? uSkyBlock.getInstance().getPlayerInfo(player) : new PlayerInfo(islandPlayer);
        if (player.getName().equals(playerInfo.getPlayerName())) {
            uSkyBlock.getInstance().getIslandLogic().loadIslandChunks(playerInfo.getIslandLocation(), Settings.island_protectionRange/2);
        }
        uSkyBlock.getInstance().getServer().getScheduler().runTaskAsynchronously(uSkyBlock.getInstance(), new Runnable() {
            @Override
            public void run() {
                final IslandScore[] shared = new IslandScore[1];
                if (player.getName().equals(playerInfo.getPlayerName())) {
                    try {
                        IslandScore score = uSkyBlock.getInstance().getLevelLogic().calculateScore(playerInfo);
                        uSkyBlock.getInstance().getIslandConfig(playerInfo).set("general.level", score.getScore());
                        playerInfo.save();
                        uSkyBlock.getInstance().saveIslandConfig(playerInfo.locationForParty());
                        if (cmd.equalsIgnoreCase("info")) {
                            shared[0] = score;
                        }
                    } catch (Exception e) {
                        uSkyBlock.log(Level.SEVERE, "Error while calculating Island Level", e);
                    } finally {
                        IslandCommand.this.allowInfo = true;
                    }
                }
                uSkyBlock.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(uSkyBlock.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        IslandCommand.this.allowInfo = true;
                        if (player.isOnline()) {
                            player.sendMessage(ChatColor.YELLOW + "Information about " + islandPlayer + "'s Island:");
                            if (cmd.equalsIgnoreCase("info") && shared[0] != null) {
                                player.sendMessage("Score Count Block");
                                for (BlockScore score : shared[0].getTop(10)) {
                                    Material material = Material.getMaterial(score.getBlockId());
                                    player.sendMessage(String.format("%05.2f  %d %s",
                                            score.getScore(), score.getCount(), VaultHandler.getItemName(material)));
                                }
                                player.sendMessage(String.format(ChatColor.GREEN + "Island level is %5.2f", shared[0].getScore()));
                            } else {
                                player.sendMessage(String.format(ChatColor.GREEN + "Island level is %5.2f", uSkyBlock.getInstance().getIslandConfig(playerInfo).getDouble("general.level")));
                            }
                        }
                    }
                }, 0L);
            }
        });
        return true;
    }

    public String getBanList(final Player player) {
        return null;
    }
}
