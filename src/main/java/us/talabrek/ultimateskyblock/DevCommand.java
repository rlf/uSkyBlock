package us.talabrek.ultimateskyblock;

import org.bukkit.command.*;
import org.bukkit.entity.*;

import java.io.*;

import org.bukkit.*;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;

public class DevCommand implements CommandExecutor {
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] split) {
        final Player player;
        if (split.length == 0) {
            if (sender.hasPermission("usb.mod.protect") || sender.hasPermission("usb.mod.protectall") || sender.hasPermission("usb.mod.topten") || sender.hasPermission("usb.mod.orphan") || sender.hasPermission("usb.admin.delete") || sender.hasPermission("usb.admin.remove") || sender.hasPermission("usb.admin.register")) {
            	sender.sendMessage("[dev usage]");
                if (sender.hasPermission("usb.mod.protect")) {
                	sender.sendMessage(ChatColor.YELLOW + "/dev protect <player>:" + ChatColor.WHITE + " add protection to an island.");
                }
                if (sender.hasPermission("usb.admin.reload")) {
                	sender.sendMessage(ChatColor.YELLOW + "/dev reload:" + ChatColor.WHITE + " reload configuration from file.");
                }
                if (sender.hasPermission("usb.mod.protectall")) {
                    sender.sendMessage(ChatColor.YELLOW + "/dev protectall:" + ChatColor.WHITE + " add island protection to unprotected islands.");
                }
                if (sender.hasPermission("usb.mod.topten")) {
                    sender.sendMessage(ChatColor.YELLOW + "/dev topten:" + ChatColor.WHITE + " manually update the top 10 list");
                }
                if (sender.hasPermission("usb.mod.orphan")) {
                    sender.sendMessage(ChatColor.YELLOW + "/dev orphancount:" + ChatColor.WHITE + " unused island locations count");
                }
                if (sender.hasPermission("usb.mod.orphan")) {
                    sender.sendMessage(ChatColor.YELLOW + "/dev clearorphan:" + ChatColor.WHITE + " remove any unused island locations.");
                }
                if (sender.hasPermission("usb.mod.orphan")) {
                    sender.sendMessage(ChatColor.YELLOW + "/dev saveorphan:" + ChatColor.WHITE + " save the list of old (empty) island locations.");
                }
                if (sender.hasPermission("usb.admin.delete")) {
                    sender.sendMessage(ChatColor.YELLOW + "/dev delete <player>:" + ChatColor.WHITE + " delete an island (removes blocks).");
                }
                if (sender.hasPermission("usb.admin.remove")) {
                    sender.sendMessage(ChatColor.YELLOW + "/dev remove <player>:" + ChatColor.WHITE + " remove a player from an island.");
                }
                if (sender.hasPermission("usb.admin.register")) {
                    sender.sendMessage(ChatColor.YELLOW + "/dev register <player>:" + ChatColor.WHITE + " set a player's island to your location");
                }
                if (sender.hasPermission("usb.mod.challenges")) {
                    sender.sendMessage(ChatColor.YELLOW + "/dev completechallenge <challengename> <player>:" + ChatColor.WHITE + " marks a challenge as complete");
                }
                if (sender.hasPermission("usb.mod.challenges")) {
                    sender.sendMessage(ChatColor.YELLOW + "/dev resetchallenge <challengename> <player>:" + ChatColor.WHITE + " marks a challenge as incomplete");
                }
                if (sender.hasPermission("usb.mod.challenges")) {
                    sender.sendMessage(ChatColor.YELLOW + "/dev resetallchallenges <challengename>:" + ChatColor.WHITE + " resets all of the player's challenges");
                }
                if (sender.hasPermission("usb.admin.purge")) {
                    sender.sendMessage(ChatColor.YELLOW + "/dev purge [TimeInDays]:" + ChatColor.WHITE + " delete inactive islands older than [TimeInDays].");
                }
                if (sender.hasPermission("usb.mod.party")) {
                    sender.sendMessage(ChatColor.YELLOW + "/dev buildpartylist:" + ChatColor.WHITE + " build a new party list (use this if parties are broken).");
                }
                if (sender.hasPermission("usb.mod.party")) {
                    sender.sendMessage(ChatColor.YELLOW + "/dev info <player>:" + ChatColor.WHITE + " check the party information for the given player.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            }
        } else if (split.length == 1) {
            if (split[0].equals("clearorphan") && (sender.hasPermission("usb.mod.orphan"))) {
                sender.sendMessage(ChatColor.YELLOW + "Clearing all old (empty) island locations.");
                uSkyBlock.getInstance().clearOrphanedIsland();
            } else if (split[0].equals("buildislandlist") && (sender.hasPermission("usb.mod.protectall"))) {
                sender.sendMessage(ChatColor.YELLOW + "Building island list..");
                uSkyBlock.getInstance().buildIslandList();
                sender.sendMessage(ChatColor.YELLOW + "Finished building island list..");
            } else if (split[0].equals("orphancount") && (sender.hasPermission("usb.mod.orphan"))) {
                sender.sendMessage(new StringBuilder().append(ChatColor.YELLOW).append(uSkyBlock.getInstance().orphanCount()).append(" old island locations will be used before new ones.").toString());
            } else if (split[0].equals("reload") && (sender.hasPermission("usb.admin.reload"))) {
                uSkyBlock.getInstance().reloadConfig();
                Settings.loadPluginConfig(uSkyBlock.getInstance().getConfig());
                sender.sendMessage(ChatColor.YELLOW + "Configuration reloaded from file.");
            } else if (split[0].equals("saveorphan") && (sender.hasPermission("usb.mod.orphan"))) {
                sender.sendMessage(ChatColor.YELLOW + "Saving the orphan list.");
                uSkyBlock.getInstance().saveOrphans();
            } else if (split[0].equals("topten") && (sender.hasPermission("usb.mod.topten"))) {
                sender.sendMessage(ChatColor.YELLOW + "Generating the Top Ten list");
                uSkyBlock.getInstance().getIslandLogic().showTopTen(sender);
                sender.sendMessage(ChatColor.YELLOW + "Finished generation of the Top Ten list");
            } else if (split[0].equals("purge") && (sender.hasPermission("usb.admin.purge"))) {
                if (uSkyBlock.getInstance().isPurgeActive()) {
                    sender.sendMessage(ChatColor.RED + "A purge is already running, please wait for it to finish!");
                    return true;
                }
                sender.sendMessage(ChatColor.YELLOW + "Usage: /dev purge [TimeInDays]");
                return true;
            }
        } else if (split.length == 2) {
            if (split[0].equals("purge") && (sender.hasPermission("usb.admin.purge"))) {
                if (uSkyBlock.getInstance().isPurgeActive()) {
                    sender.sendMessage(ChatColor.RED + "A purge is already running, please wait for it to finish!");
                    return true;
                }
                uSkyBlock.getInstance().activatePurge();
                final int time = Integer.parseInt(split[1], 10) * 24;
                sender.sendMessage(ChatColor.YELLOW + "Marking all islands inactive for more than " + split[1] + " days.");
                uSkyBlock.getInstance().getServer().getScheduler().runTaskAsynchronously(uSkyBlock.getInstance(), new Runnable() {
                    @SuppressWarnings("deprecation")
					@Override
                    public void run() {
                        final File directoryPlayers = new File(uSkyBlock.getInstance().getDataFolder() + File.separator + "players");
                        long offlineTime = 0L;
                        File[] listFiles;
                        for (int length = (listFiles = directoryPlayers.listFiles()).length, i = 0; i < length; ++i) {
                            final File child = listFiles[i];
                            if (Bukkit.getOfflinePlayer(child.getName()) != null && Bukkit.getPlayer(child.getName()) == null) {
                                final OfflinePlayer oplayer = Bukkit.getOfflinePlayer(child.getName());
                                offlineTime = oplayer.getLastPlayed();
                                offlineTime = (System.currentTimeMillis() - offlineTime) / 3600000L;
                                if (offlineTime > time && uSkyBlock.getInstance().hasIsland(oplayer.getName())) {
                                    final PlayerInfo pi = new PlayerInfo(oplayer.getName());
                                    if (pi.getHasIsland()) {
                                        IslandInfo islandInfo = uSkyBlock.getInstance().getIslandInfo(pi);
                                        if (!islandInfo.isParty()) {
                                            if (islandInfo.getLevel() < 10 && child.getName() != null) {
                                                uSkyBlock.getInstance().addToRemoveList(child.getName());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        System.out.print("Removing " + uSkyBlock.getInstance().getRemoveList().size() + " inactive islands.");
                        uSkyBlock.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(uSkyBlock.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                if (uSkyBlock.getInstance().getRemoveList().size() > 0 && uSkyBlock.getInstance().isPurgeActive()) {
                                    uSkyBlock.getInstance().deletePlayerIsland(uSkyBlock.getInstance().getRemoveList().get(0));
                                    System.out.print("[uSkyBlock] Purge: Removing " + uSkyBlock.getInstance().getRemoveList().get(0) + "'s island");
                                    uSkyBlock.getInstance().deleteFromRemoveList();
                                }
                                if (uSkyBlock.getInstance().getRemoveList().size() == 0 && uSkyBlock.getInstance().isPurgeActive()) {
                                    uSkyBlock.getInstance().deactivatePurge();
                                    System.out.print("[uSkyBlock] Finished purging marked inactive islands.");
                                }
                            }
                        }, 0L, 20L);
                    }
                });
            } else if (split[0].equals("goto") && (sender.hasPermission("usb.mod.goto"))) {
                if (!(sender instanceof Player)) {
                    return false;
                }
                player = (Player) sender;
                final PlayerInfo pi = new PlayerInfo(split[1]);
                if (!pi.getHasIsland()) {
                    sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
                } else {
                    if (pi.getHomeLocation() != null) {
                        sender.sendMessage(ChatColor.GREEN + "Teleporting to " + split[1] + "'s island.");
                        player.teleport(pi.getHomeLocation());
                        return true;
                    }
                    if (pi.getIslandLocation() != null) {
                        sender.sendMessage(ChatColor.GREEN + "Teleporting to " + split[1] + "'s island.");
                        player.teleport(pi.getIslandLocation());
                        return true;
                    }
                    sender.sendMessage("Error: That player does not have an island!");
                }
            } else if (split[0].equals("refresh") && (sender.hasPermission("usb.admin.refresh"))) {
                final PlayerInfo pi = new PlayerInfo(split[1]);
                if (!pi.getHasIsland()) {
                    sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
                } else {
                    if (pi.getIslandLocation() != null) {
                        uSkyBlock.getInstance().getIslandLogic().reloadIsland(pi.getIslandLocation());
                        return true;
                    }
                    sender.sendMessage("Error: That player does not have an island!");
                }
            } else if (split[0].equals("delete") && (sender.hasPermission("usb.admin.delete"))) {
                final PlayerInfo pi = new PlayerInfo(split[1]);
                if (!pi.getHasIsland()) {
                    sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
                } else {
                    if (pi.getIslandLocation() != null) {
                        sender.sendMessage(ChatColor.YELLOW + "Removing " + split[1] + "'s island.");
                        uSkyBlock.getInstance().deletePlayerIsland(split[1]);
                        return true;
                    }
                    sender.sendMessage("Error: That player does not have an island!");
                }
            } else if (split[0].equals("register") && (sender.hasPermission("usb.admin.register"))) {
            	if (!(sender instanceof Player)) {
                    return false;
                }
            	player = (Player) sender;
                final PlayerInfo pi = new PlayerInfo(split[1]);
                if (pi.getHasIsland()) {
                    uSkyBlock.getInstance().deletePlayerIsland(split[1]);
                }
                if (uSkyBlock.getInstance().devSetPlayerIsland(player, player.getLocation(), split[1])) {
                    sender.sendMessage(ChatColor.GREEN + "Set " + split[1] + "'s island to the bedrock nearest you.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Bedrock not found: unable to set the island!");
                }
            } else if (!split[0].equals("info") || (!sender.hasPermission("usb.mod.party"))) {
                PlayerInfo pi = uSkyBlock.getInstance().getPlayerInfo(split[1]);
                if (split[0].equals("resetallchallenges") && (sender.hasPermission("usb.mod.challenges"))) {
                    if (!pi.getHasIsland()) {
                        sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
                        return true;
                    }
                    pi.resetAllChallenges();
                    pi.save();
                    sender.sendMessage(ChatColor.YELLOW + split[1] + " has had all challenges reset.");
                } else if (split[0].equals("setbiome") && (sender.hasPermission("usb.mod.setbiome"))) {
                    if (!pi.getHasIsland()) {
                        sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
                        return true;
                    }
                    uSkyBlock.getInstance().setBiome(pi.getIslandLocation(), "OCEAN");
                    pi.save();
                    sender.sendMessage(ChatColor.YELLOW + split[1] + " has had their biome changed to OCEAN.");
                }
            }
        } else if (split.length == 3) {
            if (split[0].equals("completechallenge") && (sender.hasPermission("usb.mod.challenges"))) {
                PlayerInfo pi = uSkyBlock.getInstance().getPlayerInfo(split[2]);
                if (!pi.getHasIsland()) {
                    sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
                    return true;
                }
                if (pi.checkChallenge(split[1].toLowerCase()) > 0 || !pi.challengeExists(split[1].toLowerCase())) {
                    sender.sendMessage(ChatColor.RED + "Challenge doesn't exist or is already completed");
                    return true;
                }
                pi.completeChallenge(split[1].toLowerCase());
                pi.save();
                sender.sendMessage(ChatColor.YELLOW + "challange: " + split[1].toLowerCase() + " has been completed for " + split[2]);
            } else if (split[0].equals("resetchallenge") && (sender.hasPermission("usb.mod.challenges"))) {
                PlayerInfo pi = uSkyBlock.getInstance().getPlayerInfo(split[2]);
                if (!pi.getHasIsland()) {
                    sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
                    return true;
                }
                if (pi.checkChallenge(split[1].toLowerCase()) == 0 || !pi.challengeExists(split[1].toLowerCase())) {
                    sender.sendMessage(ChatColor.RED + "Challenge doesn't exist or isn't yet completed");
                    return true;
                }
                pi.resetChallenge(split[1].toLowerCase());
                pi.save();
                sender.sendMessage(ChatColor.YELLOW + "challange: " + split[1].toLowerCase() + " has been reset for " + split[2]);
            } else if (split[0].equals("setbiome") && (sender.hasPermission("usb.mod.setbiome"))) {
                PlayerInfo pi = uSkyBlock.getInstance().getPlayerInfo(split[1]);
                if (!pi.getHasIsland()) {
                    sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
                    return true;
                }
                if (uSkyBlock.getInstance().setBiome(pi.getIslandLocation(), split[2])) {
                    sender.sendMessage(ChatColor.YELLOW + split[1] + " has had their biome changed to " + split[2].toUpperCase() + ".");
                } else {
                    sender.sendMessage(ChatColor.YELLOW + split[1] + " has had their biome changed to OCEAN.");
                }
                pi.save();
            }
        }
        return true;
    }
}
