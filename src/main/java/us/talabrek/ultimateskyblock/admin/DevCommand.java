package us.talabrek.ultimateskyblock.admin;

import org.bukkit.command.*;
import org.bukkit.entity.*;

import java.io.*;
import java.util.*;

import org.bukkit.*;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class DevCommand implements CommandExecutor, TabCompleter {
    private static final List<String> commands = Arrays.asList("protectall",
            "purge", "buildpartylist", "info");

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] split) {
        if (!uSkyBlock.getInstance().isRequirementsMet(sender, true)) {
            return true;
        }
        final Player player;
        if (split.length == 0) {
            if (sender.hasPermission("usb.mod.protectall") || sender.hasPermission("usb.admin.delete") || sender.hasPermission("usb.admin.remove") || sender.hasPermission("usb.admin.register")) {
            	sender.sendMessage("\u00a77Usage: /" + label + " <command>");
                if (sender.hasPermission("usb.admin.purge")) {
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " purge [TimeInDays]:" + ChatColor.WHITE + " delete inactive islands older than [TimeInDays].");
                }
                if (sender.hasPermission("usb.mod.party")) {
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " buildpartylist:" + ChatColor.WHITE + " build a new party list (use this if parties are broken).");
                }
                if (sender.hasPermission("usb.mod.party")) {
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " info <player>:" + ChatColor.WHITE + " check the party information for the given player.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            }
        } else if (split.length == 1) {
            if (split[0].equals("purge") && (sender.hasPermission("usb.admin.purge"))) {
                if (uSkyBlock.getInstance().isPurgeActive()) {
                    sender.sendMessage(ChatColor.RED + "A purge is already running, please wait for it to finish!");
                    return true;
                }
                sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " purge [TimeInDays]");
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
            } else {
                sender.sendMessage(ChatColor.RED + "No valid usb commands found!");
            }
        }
        return true;
    }

    private List<String> filter(List<String> list, String prefix) {
        List<String> result = new ArrayList<>();
        for (String item : list) {
            if (item.startsWith(prefix)) {
                result.add(item);
            }
        }
        Collections.sort(result);
        return result;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length == 0 || args.length == 1) {
            return filter(commands, args.length == 1 ? args[0] : "");
        } else if (args.length == 2) {
            String arg = args[1];
            if (args[0].equalsIgnoreCase("import")) {
                return filter(uSkyBlock.getInstance().getPlayerImporter().getImporterNames(), arg);
            }
        }
        return null;
    }
}
