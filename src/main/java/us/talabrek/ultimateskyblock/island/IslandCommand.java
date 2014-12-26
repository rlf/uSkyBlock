package us.talabrek.ultimateskyblock.island;

import org.bukkit.command.*;
import org.bukkit.entity.*;

import java.util.*;
import java.util.logging.Level;

import org.bukkit.*;
import us.talabrek.ultimateskyblock.*;
import us.talabrek.ultimateskyblock.player.PlayerInfo;

public class IslandCommand implements CommandExecutor {

    public boolean allowInfo; // Note: this is SHARED among ALL players... is this intended?
    private HashMap<String, String> inviteList;

    public IslandCommand() {
        super();
        this.allowInfo = true;
        inviteList = new HashMap<>();
        inviteList.put("NoInvited", "NoInviter");
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] split) {
        uSkyBlock sky = uSkyBlock.getInstance();
        if (!sky.isRequirementsMet(sender, false)) {
            sender.sendMessage(ChatColor.RED + "/is is currently disabled, contact a server-administrator");
            return true;
        }
        if (!(sender instanceof Player)) {
            return handleConsoleCommand(sender, command, label, split);
        }
        final Player player = (Player) sender;
        final PlayerInfo pi = sky.getPlayerInfo(player);
        if (pi == null) {
            player.sendMessage(ChatColor.RED + "Error: Couldn't read your player data!");
            return true;
        }
        String iName = "";
        IslandInfo island = null;
        if (pi.getHasIsland()) {
            iName = pi.locationForParty();
            island = sky.getIslandInfo(iName);
            island.updatePartyNumber(player);
        }
        boolean hasIsland = island != null;
        if (split.length == 0) {
            player.openInventory(sky.getMenu().displayIslandGUI(player));
            return true;
        }
        if (split.length == 1) {
            if (requireIsland(split[0]) && !hasIsland) {
                player.sendMessage(ChatColor.RED + "No island!" +
                        ChatColor.YELLOW + " You do not currently have an island, use " +
                        ChatColor.AQUA + "/is create" + ChatColor.YELLOW + " to get one");
            }
            if ((split[0].equals("restart") || split[0].equals("reset")) && hasIsland) {
                if (island.getPartySize() > 1) {
                    if (!island.isLeader(player)) {
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
                if ((split[0].equals("sethome") || split[0].equals("tpset")) && pi.getHasIsland() && VaultHandler.checkPerk(player.getName(), "usb.island.sethome", player.getWorld())) {
                    sky.homeSet(player);
                    return true;
                }
                if ((split[0].equals("log") || split[0].equals("l")) && pi.getHasIsland() && VaultHandler.checkPerk(player.getName(), "usb.island.create", player.getWorld())) {
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
                    }else if (pi.getHasIsland()){
                        if (island.isLeader(player)) {
                            player.sendMessage(ChatColor.RED + "Island found!" +
                                    ChatColor.YELLOW + " You already have an island. If you want a fresh island, type" +
                                    ChatColor.AQUA + " /is restart" + ChatColor.YELLOW + " to get one");
                        } else {
                            player.sendMessage(ChatColor.RED + "Island found!" +
                                    ChatColor.YELLOW + " You are already a member of an island. To start your own, first" +
                                    ChatColor.AQUA + " /is leave");
                        }
                    }
                    return true;
                } else {
                    if ((split[0].equals("home") || split[0].equals("h")) && pi.getHasIsland() && VaultHandler.checkPerk(player.getName(), "usb.island.sethome", player.getWorld())) {
                        if (pi.getHomeLocation() == null) {
                            sky.getPlayerInfo(player).setHomeLocation(pi.getIslandLocation());
                        }
                        sky.homeTeleport(player);
                        return true;
                    }
                    if ((split[0].equals("setwarp") || split[0].equals("warpset")) && pi.getHasIsland() && VaultHandler.checkPerk(player.getName(), "usb.extra.addwarp", player.getWorld())) {
                        if (island.hasPerm(player, "canChangeWarp")) {
                            sky.sendMessageToIslandGroup(iName, String.valueOf(player.getName()) + " changed the island warp location.");
                            sky.warpSet(player);
                        } else {
                            player.sendMessage("\u00a7cYou do not have permission to set your island's warp point!");
                        }
                        return true;
                    }
                    if (split[0].equals("warp") || split[0].equals("w")) {
                        if (VaultHandler.checkPerk(player.getName(), "usb.extra.addwarp", player.getWorld())) {
                            if (island.hasWarp()) {
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
                    if ((split[0].equals("togglewarp") || split[0].equals("tw")) && pi.getHasIsland()) {
                        if (VaultHandler.checkPerk(player.getName(), "usb.extra.addwarp", player.getWorld())) {
                            if (island.hasPerm(player, "canToggleWarp")) {
                                if (!island.hasWarp()) {
                                    if (island.isLocked()) {
                                        player.sendMessage(ChatColor.RED + "Your island is locked. You must unlock it before enabling your warp.");
                                        return true;
                                    }
                                    sky.sendMessageToIslandGroup(iName, String.valueOf(player.getName()) + " activated the island warp.");
                                    island.setWarpActive(true);
                                } else {
                                    sky.sendMessageToIslandGroup(iName, String.valueOf(player.getName()) + " deactivated the island warp.");
                                    island.setWarpActive(false);
                                }
                            } else {
                                player.sendMessage("\u00a7cYou do not have permission to enable/disable your island's warp!");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You do not have permission to create a warp on your island!");
                        }
                        return true;
                    }
                    if ((split[0].equals("ban") || split[0].equals("banned") || split[0].equals("banlist") || split[0].equals("b")) && pi.getHasIsland()) {
                        if (VaultHandler.checkPerk(player.getName(), "usb.island.ban", player.getWorld())) {
                            player.sendMessage(ChatColor.YELLOW + "The following players are banned from warping to your island:");
                            player.sendMessage(ChatColor.RED + this.getBanList(player));
                            player.sendMessage(ChatColor.YELLOW + "To ban/unban from your island, use /island ban <player>");
                        } else {
                            player.sendMessage(ChatColor.RED + "You do not have permission to ban players from your island!");
                        }
                        return true;
                    }
                    if (split[0].equals("lock") && pi.getHasIsland()) {
                        if (Settings.island_allowIslandLock && VaultHandler.checkPerk(player.getName(), "usb.lock", player.getWorld())) {
                            if (island.hasPerm(player, "canToggleLock")) {
                                island.lock(player);
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to lock your island!");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You don't have access to this command!");
                        }
                        return true;
                    }
                    if (split[0].equals("unlock") && pi.getHasIsland()) {
                        if (Settings.island_allowIslandLock && VaultHandler.checkPerk(player.getName(), "usb.lock", player.getWorld())) {
                            if (island.hasPerm(player, "canToggleLock")) {
                                island.unlock(player);
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
                        sky.getIslandLogic().showTopTen(player);
                        return true;
                    }
                    if ((split[0].equals("biome") || split[0].equals("b")) && pi.getHasIsland()) {
                        player.openInventory(sky.getMenu().displayBiomeGUI(player)); // Weird, that we show the UI
                        if (!island.hasPerm(player, "canChangeBiome")) {
                            player.sendMessage("\u00a7cYou do not have permission to change the biome of your current island.");
                        }
                        return true;
                    }
                    if ((split[0].equals("info") || split[0].equals("level")) && pi.getHasIsland() && VaultHandler.checkPerk(player.getName(), "usb.island.info", player.getWorld()) && Settings.island_useIslandLevel) {
                        if (!sky.playerIsOnIsland(player)) {
                            player.sendMessage(ChatColor.YELLOW + "You must be on your island to use this command.");
                            return true;
                        }
                        if (!sky.onInfoCooldown(player) || Settings.general_cooldownInfo == 0) {
                            sky.setInfoCooldown(player);
                            if (!island.isParty() && !pi.getHasIsland()) {
                                player.sendMessage(ChatColor.RED + "You do not have an island!");
                            } else {
                                getIslandLevel(player, player.getName(), split[0]);
                            }
                            return true;
                        }
                        player.sendMessage(ChatColor.YELLOW + "You can use that command again in " + sky.getInfoCooldownTime(player) / 1000L + " seconds.");
                        return true;
                    } else if (split[0].equals("invite") && pi.getHasIsland() && VaultHandler.checkPerk(player.getName(), "usb.party.create", player.getWorld())) {
                        player.sendMessage(ChatColor.YELLOW + "Use" + ChatColor.WHITE + " /island invite <playername>" + ChatColor.YELLOW + " to invite a player to your island.");
                        if (!island.isParty()) {
                            return true;
                        }
                        if (!island.isLeader(player) || !island.hasPerm(player, "canInviteOthers")) {
                            player.sendMessage(ChatColor.RED + "Only the island's owner can invite!");
                            return true;
                        }
                        int diff = island.getMaxPartySize() - island.getPartySize();
                        if (diff > 0) {
                            player.sendMessage(ChatColor.GREEN + "You can invite " + diff + " more players.");
                        } else {
                            player.sendMessage(ChatColor.RED + "You can't invite any more players.");
                        }
                        return true;
                    } else if (split[0].equals("accept") && VaultHandler.checkPerk(player.getName(), "usb.party.join", player.getWorld())) {
                        if (sky.onRestartCooldown(player) && Settings.general_cooldownRestart > 0) {
                            player.sendMessage(ChatColor.YELLOW + "You can't join an island for another " + sky.getRestartCooldownTime(player) / 1000L + " seconds.");
                            return true;
                        }
                        if ((island != null && island.isParty()) || !inviteList.containsKey(player.getName())) {
                            player.sendMessage(ChatColor.RED + "You can't use that command right now. Leave your current party first.");
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
                            island = sky.getIslandInfo(inviteList.get(player.getName()));
                            WorldGuardHandler.addPlayerToOldRegion(island.getLeader(), player.getName());
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
                        if (split[0].equals("leave") && pi.getHasIsland() && VaultHandler.checkPerk(player.getName(), "usb.party.join", player.getWorld())) {
                            if (player.getWorld().getName().equalsIgnoreCase(uSkyBlock.getSkyBlockWorld().getName())) {
                                if (!island.isParty()) {
                                    player.sendMessage(ChatColor.RED + "You can't leave your island if you are the only person. Try using /island restart if you want a new one!");
                                    return true;
                                }
                                if (island.isLeader(player)) {
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
                                WorldGuardHandler.removePlayerFromRegion(island.getLeader(), player.getName());
                                removePlayerFromParty(player.getName(), island.getLeader(), pi.locationForParty());
                                player.sendMessage(ChatColor.YELLOW + "You have left the island and returned to the player spawn.");
                                if (Bukkit.getPlayer(island.getLeader()) != null) {
                                    Bukkit.getPlayer(island.getLeader()).sendMessage(ChatColor.RED + player.getName() + " has left your island!");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You must be in the skyblock world to leave your party!");
                            }
                            return true;
                        }
                        if (split[0].equals("party") && pi.getHasIsland()) {
                            if (VaultHandler.checkPerk(player.getName(), "usb.party.create", player.getWorld())) {
                                player.openInventory(sky.getMenu().displayPartyGUI(player));
                            }
                            player.sendMessage(ChatColor.YELLOW + "Listing your island members:");
                            String total = "\u00a7a<" + island.getLeader() + "> ";
                            for (final String member : island.getMembers()) {
                                if (!member.equalsIgnoreCase(island.getLeader())) {
                                    total = "\u00a7e[" + member + "]";
                                }
                            }
                            player.sendMessage(total);
                            return true;
                        }
                    }
                }
            }
            player.sendMessage(ChatColor.RED + "Invalid SkyBlock command. Please refer to" + ChatColor.AQUA + " /is help");
            return true;
        } else if (split.length == 2) {
            if ((split[0].equals("info") || split[0].equals("level")) && pi.getHasIsland() && VaultHandler.checkPerk(player.getName(), "usb.island.info", player.getWorld()) && Settings.island_useIslandLevel) {
                if (!sky.onInfoCooldown(player) || Settings.general_cooldownInfo == 0) {
                    sky.setInfoCooldown(player);
                    if (!island.isParty() && !pi.getHasIsland()) {
                        player.sendMessage(ChatColor.RED + "You do not have an island!");
                    } else {
                        this.getIslandLevel(player, split[1], split[0]);
                    }
                    return true;
                }
                player.sendMessage(ChatColor.YELLOW + "You can use that command again in " + sky.getInfoCooldownTime(player) / 1000L + " seconds.");
                return true;
            } else {
                Player otherPlayer = Bukkit.getPlayer(split[1]); // Might not be used, might be
                if (split[0].equals("warp") || split[0].equals("w")) {
                    if (VaultHandler.checkPerk(player.getName(), "usb.island.warp", player.getWorld())) {
                        PlayerInfo wPi;
                        if (otherPlayer != null) {
                            wPi = sky.getPlayerInfo(otherPlayer);
                        } else {
                            wPi  = sky.getPlayerInfo(split[1]);
                        }
                        if (wPi == null || !wPi.getHasIsland()) {
                            player.sendMessage(ChatColor.RED + "That player does not exist!");
                            return true;
                        }
                        island = sky.getIslandInfo(wPi);
                        if (!island.hasWarp()) {
                            player.sendMessage(ChatColor.RED + "That player does not have an active warp.");
                            return true;
                        }
                        if (!island.isBanned(player)) {
                            sky.warpTeleport(player, wPi);
                        } else {
                            player.sendMessage(ChatColor.RED + "That player has forbidden you from warping to their island.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have permission to warp to other islands!");
                    }
                    return true;
                }
                if (split[0].equals("ban") && island != null) {
                    if (VaultHandler.checkPerk(player.getName(), "usb.island.ban", player.getWorld())) {
                        if (island.getMembers().contains(split[1])) {
                            sender.sendMessage(ChatColor.RED + "You can't ban members. Remove them first!");
                            return true;
                        }
                        if (!island.hasPerm(player.getName(), "canKickOthers")) {
                            sender.sendMessage(ChatColor.RED + "You do not have permission to kick/ban players.");
                            return true;
                        }
                        if (!island.isBanned(split[1])) {
                            island.banPlayer(split[1]);
                            player.sendMessage(ChatColor.YELLOW + "You have banned " + ChatColor.RED + split[1] + ChatColor.YELLOW + " from warping to your island.");
                        } else {
                            island.unbanPlayer(split[1]);
                            player.sendMessage(ChatColor.YELLOW + "You have unbanned " + ChatColor.GREEN + split[1] + ChatColor.YELLOW + " from warping to your island.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have permission to ban players from this island!");
                    }
                    return true;
                }
                if ((split[0].equals("biome") || split[0].equals("b")) && pi.getHasIsland()) {
                    if (island.hasPerm(player, "canChangeBiome")) {
                        if (sky.onBiomeCooldown(player) && Settings.general_biomeChange != 0) {
                            player.sendMessage(ChatColor.YELLOW + "You can change your biome again in " + sky.getBiomeCooldownTime(player) / 1000L / 60L + " minutes.");
                            return true;
                        }
                        if (sky.playerIsOnIsland(player)) {
                            if (sky.changePlayerBiome(player, split[1])) {
                                player.sendMessage(ChatColor.GREEN + "You have changed your island's biome to " + split[1].toUpperCase());
                                player.sendMessage(ChatColor.GREEN + "You may need to go to spawn, or relog, to see the changes.");
                                sky.sendMessageToIslandGroup(iName, String.valueOf(player.getName()) + " changed the island biome to " + split[1].toUpperCase());
                                sky.setBiomeCooldown(player);
                            } else {
                                player.sendMessage(ChatColor.GREEN + "Unknown biome name, changing your biome to OCEAN");
                                player.sendMessage(ChatColor.GREEN + "You may need to go to spawn, or relog, to see the changes.");
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
                if (split[0].equalsIgnoreCase("invite") && pi.getHasIsland() && VaultHandler.checkPerk(player.getName(), "usb.party.create", player.getWorld())) {
                    if (!island.hasPerm(player, "canInviteOthers")) {
                        player.sendMessage(ChatColor.RED + "You do not have permission to invite others to this island!");
                        return true;
                    }
                    if (otherPlayer == null) {
                        player.sendMessage(ChatColor.RED + "That player is offline or doesn't exist.");
                        return true;
                    }
                    if (!otherPlayer.isOnline()) {
                        player.sendMessage(ChatColor.RED + "That player is offline or doesn't exist.");
                        return true;
                    }
                    if (!sky.hasIsland(player.getName())) {
                        player.sendMessage(ChatColor.RED + "You must have an island in order to invite people to it!");
                        return true;
                    }
                    if (player.getName().equalsIgnoreCase(otherPlayer.getName())) {
                        player.sendMessage(ChatColor.RED + "You can't invite yourself!");
                        return true;
                    }
                    if (island.isLeader(otherPlayer)) {
                        player.sendMessage(ChatColor.RED + "That player is the leader of your island!");
                        return true;
                    }
                    if (sky.getIslandInfo(sky.getPlayerInfo(split[1])) != null && sky.getIslandInfo(sky.getPlayerInfo(split[1])).isParty()) {
                        player.sendMessage(ChatColor.RED + "That player is already with a group on an island.");
                        return true;
                    }
                    if (island.getPartySize() >= island.getMaxPartySize()) {
                        player.sendMessage(ChatColor.RED + "Your island is full, you can't invite anyone else.");
                    }
                    if (inviteList.containsValue(player.getName())) {
                        inviteList.remove(this.getKeyByValue(this.inviteList, player.getName()));
                        player.sendMessage(ChatColor.YELLOW + "Removing your previous invite.");
                    }
                    inviteList.put(otherPlayer.getName(), player.getName());
                    player.sendMessage(ChatColor.GREEN + "Invite sent to " + otherPlayer.getName());
                    otherPlayer.sendMessage(String.valueOf(player.getName()) + " has invited you to join their island!");
                    otherPlayer.sendMessage(ChatColor.WHITE + "/island [accept/reject]" + ChatColor.YELLOW + " to accept or reject the invite.");
                    otherPlayer.sendMessage(ChatColor.RED + "WARNING: You will lose your current island if you accept!");
                    sky.sendMessageToIslandGroup(iName, String.valueOf(player.getName()) + " invited " + otherPlayer.getName() + " to the island group.");
                    return true;
                }
                if ((split[0].equalsIgnoreCase("remove") || split[0].equalsIgnoreCase("kick")) && VaultHandler.checkPerk(player.getName(), "usb.party.kick", player.getWorld())) {
                    if (island == null || !island.hasPerm(player, "canKickOthers")) {
                        player.sendMessage(ChatColor.RED + "You do not have permission to kick others from this island!");
                        return true;
                    }
                    if (otherPlayer == null && Bukkit.getOfflinePlayer(split[1]) == null) {
                        player.sendMessage(ChatColor.RED + "That player doesn't exist.");
                        return true;
                    }
                    String tempTargetPlayer = null;
                    Player tempPlayer = null;
                    if (otherPlayer == null) {
                        tempTargetPlayer = Bukkit.getOfflinePlayer(split[1]).getName();
                    } else {
                        tempTargetPlayer = otherPlayer.getName();
                    }
                    if (island.getMembers().contains(split[1])) {
                        tempTargetPlayer = split[1];
                    }
                    if (island.isParty()) {
                        if (!island.isLeader(tempTargetPlayer)) {
                            if (island.getMembers().contains(tempTargetPlayer)) {
                                if (player.getName().equalsIgnoreCase(tempTargetPlayer)) {
                                    player.sendMessage(ChatColor.RED + "Stop kickin' yourself!");
                                    return true;
                                }
                                if (otherPlayer != null) {
                                    sky.clearPlayerInventory(otherPlayer);
                                    otherPlayer.sendMessage(ChatColor.RED + player.getName() + " has removed you from their island!");
                                    if (Settings.extras_sendToSpawn) {
                                        otherPlayer.performCommand("spawn");
                                    } else {
                                        otherPlayer.teleport(uSkyBlock.getSkyBlockWorld().getSpawnLocation());
                                    }
                                }
                                if (Bukkit.getPlayer(island.getLeader()) != null) {
                                    Bukkit.getPlayer(island.getLeader()).sendMessage(ChatColor.RED + tempTargetPlayer + " has been removed from the island.");
                                }
                                this.removePlayerFromParty(tempTargetPlayer, island.getLeader(), pi.locationForParty());
                                uSkyBlock.log(Level.INFO, "Removing from " + island.getLeader() + "'s Island");
                                WorldGuardHandler.removePlayerFromRegion(island.getLeader(), tempTargetPlayer);
                            } else {
                                System.out.print("Player " + player.getName() + " failed to remove " + tempTargetPlayer);
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
            player.sendMessage(ChatColor.RED + "Invalid SkyBlock command. Please refer to" + ChatColor.AQUA + " /is help");
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Invalid SkyBlock command. Please refer to" + ChatColor.AQUA + " /is help");
            return true;
        }
    }

    private boolean requireIsland(String s) {
        return !(Arrays.asList("create", "top", "help", "join").contains(s.toLowerCase()));
    }

    private boolean handleConsoleCommand(CommandSender sender, Command command, String label, String[] split) {
        if (split.length == 1) {
            if (split[0].equalsIgnoreCase("top")) {
                uSkyBlock.getInstance().getIslandLogic().showTopTen(sender);
                return true;
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
            sky.getIslandInfo(leaderInfo).setupPartyMember(player.getName());
        }
        playerInfo.save();
        sky.sendMessageToIslandGroup(leaderInfo.locationForParty(), player.getName() + " has joined your island group.");
        return true;
    }

    public void removePlayerFromParty(final String playername, final String partyleader, final String location) {
        uSkyBlock sky = uSkyBlock.getInstance();
        PlayerInfo playerInfo = sky.getPlayerInfo(playername);
        sky.getIslandInfo(playerInfo).removeMember(playername);
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
        PlayerInfo info = uSkyBlock.getInstance().getPlayerInfo(islandPlayer);
        if (!info.getHasIsland() && !uSkyBlock.getInstance().getIslandInfo(info).isParty()) {
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
                        uSkyBlock.getInstance().getIslandInfo(playerInfo).setLevel(score.getScore());
                        playerInfo.save();
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
                                    player.sendMessage(score.getState().getColor() + String.format("%05.2f  %d %s",
                                            score.getScore(), score.getCount(),
                                            VaultHandler.getItemName(score.getBlock())));
                                }
                                player.sendMessage(String.format(ChatColor.GREEN + "Island level is %5.2f", shared[0].getScore()));
                            } else {
                                player.sendMessage(String.format(ChatColor.GREEN + "Island level is %5.2f", uSkyBlock.getInstance().getIslandInfo(playerInfo).getLevel()));
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
