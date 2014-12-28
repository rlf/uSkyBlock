package us.talabrek.ultimateskyblock.admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.command.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * The purge-command.
 */
public class PurgeCommand extends AbstractUSBCommand {
    private final uSkyBlock plugin;

    public PurgeCommand(uSkyBlock plugin) {
        super("purge", "usb.admin.purge", "time-in-days", "purges all abandoned islands");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(final CommandSender sender, Map<String, Object> data, String... args) {
        if (plugin.isPurgeActive()) {
            sender.sendMessage(ChatColor.RED + "A purge is already running, please wait for it to finish!");
            return true;
        }
        plugin.activatePurge();
        final int time = Integer.parseInt(args[1], 10) * 24;
        sender.sendMessage(ChatColor.YELLOW + "Marking all islands inactive for more than " + args[1] + " days.");
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                final File directoryPlayers = plugin.directoryPlayers;
                long offlineTime = 0L;
                File[] listFiles = directoryPlayers.listFiles();
                final List<String> removeList = new ArrayList<>();
                for (File child : listFiles) {
                    if (Bukkit.getOfflinePlayer(child.getName()) != null && Bukkit.getPlayer(child.getName()) == null) {
                        final OfflinePlayer oplayer = Bukkit.getOfflinePlayer(child.getName());
                        offlineTime = oplayer.getLastPlayed();
                        offlineTime = (System.currentTimeMillis() - offlineTime) / 3600000L;
                        if (offlineTime > time && plugin.hasIsland(oplayer.getName())) {
                            final PlayerInfo pi = new PlayerInfo(oplayer.getName());
                            if (pi.getHasIsland()) {
                                IslandInfo islandInfo = plugin.getIslandInfo(pi);
                                if (!islandInfo.isParty()) {
                                    if (islandInfo.getLevel() < 10) {
                                        removeList.add(child.getName());
                                    }
                                } else {
                                    // TODO: 28/12/2014 - R4zorax: Support purging of party-islands
                                }
                            }
                        }
                    }
                }
                plugin.log(Level.INFO, "Removing " + removeList.size() + " inactive islands.");
                final int[] jobId = new int[1];
                jobId[0] = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (!removeList.isEmpty() && plugin.isPurgeActive()) {
                            String playerName = removeList.remove(0);
                            plugin.deletePlayerIsland(playerName);
                            plugin.log(Level.INFO, "Purge: Removing " + playerName + "'s island");
                        }
                        if (removeList.isEmpty() && plugin.isPurgeActive()) {
                            plugin.log(Level.INFO, "Finished purging marked inactive islands.");
                            if (jobId != null && jobId[0] != 0) {
                                plugin.getServer().getScheduler().cancelTask(jobId[0]);
                            }
                            plugin.deactivatePurge();
                        }
                    }
                }, 0L, 20L);
            }
        });
        return false;
    }
}
