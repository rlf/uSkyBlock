package us.talabrek.ultimateskyblock.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;
import us.talabrek.ultimateskyblock.util.TimeUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dk.lockfuglsang.minecraft.perm.PermissionUtil.hasPermission;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Responsible for teleporting (and cancelling teleporting) of players.
 */
public class TeleportLogic {
    private static final Logger log = Logger.getLogger(TeleportLogic.class.getName());

    private final uSkyBlock plugin;
    private final int teleportDelay;
    private final Map<UUID, BukkitTask> pendingTPs = new ConcurrentHashMap<>();

    public TeleportLogic(uSkyBlock plugin) {
        this.plugin = plugin;
        teleportDelay = plugin.getConfig().getInt("options.island.islandTeleportDelay", 2);
    }

    public void safeTeleport(final Player player, final Location homeSweetHome, boolean force) {
        log.log(Level.FINER, "safeTeleport " + player + " to " + homeSweetHome + (force ? " with force" : ""));
        final Location targetLoc = LocationUtil.centerOnBlock(homeSweetHome.clone());
        if (hasPermission(player, "usb.mod.bypassteleport") || (teleportDelay == 0) || force) {
            player.setVelocity(new org.bukkit.util.Vector());
            LocationUtil.loadChunkAt(targetLoc);
            player.teleport(targetLoc);
            player.setVelocity(new org.bukkit.util.Vector());
        } else {
            player.sendMessage(tr("\u00a7aYou will be teleported in {0} seconds.", teleportDelay));
            BukkitTask tpTask = plugin.sync(new Runnable() {
                @Override
                public void run() {
                    pendingTPs.remove(player.getUniqueId());
                    player.setVelocity(new Vector());
                    LocationUtil.loadChunkAt(targetLoc);
                    player.teleport(targetLoc);
                    player.setVelocity(new Vector());
                }
            }, TimeUtil.secondsAsMillis(teleportDelay));
            pendingTPs.put(player.getUniqueId(), tpTask);
        }
    }

    public void spawnTeleport(final Player player, boolean force) {
        int delay = teleportDelay;
        final Location spawnLocation = LocationUtil.centerOnBlock(plugin.getWorld().getSpawnLocation());
        if (hasPermission(player, "usb.mod.bypassteleport") || (delay == 0) || force) {
            if (Settings.extras_sendToSpawn) {
                plugin.execCommand(player, "op:spawn", false);
            } else {
                LocationUtil.loadChunkAt(spawnLocation);
                player.teleport(spawnLocation);
            }
        } else {
            player.sendMessage(tr("\u00a7aYou will be teleported in {0} seconds.", delay));
            BukkitTask tpTask = plugin.sync(new Runnable() {
                @Override
                public void run() {
                    pendingTPs.remove(player.getUniqueId());
                    if (Settings.extras_sendToSpawn) {
                        plugin.execCommand(player, "op:spawn", false);
                    } else {
                        LocationUtil.loadChunkAt(spawnLocation);
                        player.teleport(spawnLocation);
                    }
                }
            }, TimeUtil.secondsAsMillis(delay));
            pendingTPs.put(player.getUniqueId(), tpTask);
        }
    }

    public void cancelTeleport(Player player) {
        BukkitTask task = pendingTPs.remove(player.getUniqueId());
        if (task != null) {
            player.sendMessage(tr("\u00a77Teleport cancelled"));
            task.cancel();
        }
    }
}
