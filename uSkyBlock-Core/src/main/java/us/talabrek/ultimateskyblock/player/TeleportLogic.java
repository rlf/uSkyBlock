package us.talabrek.ultimateskyblock.player;

import io.papermc.lib.PaperLib;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;
import dk.lockfuglsang.minecraft.util.TimeUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Responsible for teleporting (and cancelling teleporting) of players.
 */
public class TeleportLogic implements Listener {
    private static final Logger log = Logger.getLogger(TeleportLogic.class.getName());

    private final uSkyBlock plugin;
    private final int teleportDelay;
    private final Map<UUID, PendingTeleport> pendingTeleports = new ConcurrentHashMap<>();
    private final double cancelDistance;

    public TeleportLogic(uSkyBlock plugin) {
        this.plugin = plugin;
        teleportDelay = plugin.getConfig().getInt("options.island.islandTeleportDelay", 2);
        cancelDistance = plugin.getConfig().getDouble("options.island.teleportCancelDistance", 0.2);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Teleport the given {@link Player} to his island home.
     * @param player Player to teleport
     * @param force True to override teleport delay, false otherwise.
     */
    public void homeTeleport(@NotNull Player player, boolean force) {
        Validate.notNull(player, "Player cannot be null");

        Location homeLocation = null;
        PlayerInfo playerInfo = plugin.getPlayerLogic().getPlayerInfo(player);

        if (playerInfo != null) {
            homeLocation = plugin.getSafeHomeLocation(playerInfo);
        }

        if (homeLocation == null) {
            player.sendMessage(tr("\u00a74Unable to find a safe home-location on your island!"));
            if (player.isFlying()) {
                player.sendMessage(tr("\u00a7cWARNING: \u00a7eTeleporting you to mid-air."));
                safeTeleport(player, playerInfo.getIslandLocation(), true);
            }
            return;
        }

        plugin.getWorldManager().removeCreatures(homeLocation);
        player.sendMessage(tr("\u00a7aTeleporting you to your island."));
        safeTeleport(player, homeLocation, force);
    }

    /**
     * Teleport the given {@link Player} to the given {@link Location}, loading the {@link org.bukkit.Chunk} before
     * teleporting and with the configured teleport delay if applicable.
     * @param player Player to teleport.
     * @param targetLocation Location to teleport the player to.
     * @param force True to override teleport delay, false otherwise.
     */
    public void safeTeleport(@NotNull Player player, @NotNull Location targetLocation, boolean force) {
        Validate.notNull(player, "Player cannot be null");
        Validate.notNull(targetLocation, "TargetLocation cannot be null");

        log.log(Level.FINER, "safeTeleport " + player + " to " + targetLocation + (force ? " with force" : ""));
        final Location targetLoc = LocationUtil.centerOnBlock(targetLocation.clone());
        if (player.hasPermission("usb.mod.bypassteleport") || (teleportDelay == 0) || force) {
            PaperLib.teleportAsync(player, targetLoc);
        } else {
            player.sendMessage(tr("\u00a7aYou will be teleported in {0} seconds.", teleportDelay));
            BukkitTask tpTask = plugin.sync(() -> {
                pendingTeleports.remove(player.getUniqueId());
                PaperLib.teleportAsync(player, targetLoc);
            }, TimeUtil.secondsAsMillis(teleportDelay));
            pendingTeleports.put(player.getUniqueId(), new PendingTeleport(player.getLocation(), tpTask));
        }
    }

    /**
     * Teleport the given {@link Player} to the spawn in the island world, loading the {@link org.bukkit.Chunk} before
     * teleporting and with the configured teleport delay if applicable.
     * @param player Player to teleport.
     * @param force True to override teleport delay, false otherwise.
     */
    public void spawnTeleport(@NotNull Player player, boolean force) {
        Validate.notNull(player, "Player cannot be null");

        Location spawnLocation = LocationUtil.centerOnBlock(plugin.getWorldManager().getWorld().getSpawnLocation());
        if (player.hasPermission("usb.mod.bypassteleport") || (teleportDelay == 0) || force) {
            if (Settings.extras_sendToSpawn) {
                plugin.execCommand(player, "op:spawn", false);
            } else {
                PaperLib.teleportAsync(player, spawnLocation);
            }
        } else {
            player.sendMessage(tr("\u00a7aYou will be teleported in {0} seconds.", teleportDelay));
            BukkitTask tpTask = plugin.sync(() -> {
                pendingTeleports.remove(player.getUniqueId());
                if (Settings.extras_sendToSpawn) {
                    plugin.execCommand(player, "op:spawn", false);
                } else {
                    PaperLib.teleportAsync(player, spawnLocation);
                }
            }, TimeUtil.secondsAsMillis(teleportDelay));
            pendingTeleports.put(player.getUniqueId(), new PendingTeleport(player.getLocation(), tpTask));
        }
    }

    /**
     * Teleport the given {@link Player} to the warp location for the given {@link PlayerInfo}.
     * @param player Player to teleport.
     * @param playerInfo PlayerInfo to lookup the target warp location.
     * @param force True to override teleport delay, false otherwise.
     */
    public void warpTeleport(@NotNull Player player, @Nullable PlayerInfo playerInfo, boolean force) {
        Validate.notNull(player, "Player cannot be null");

        Location warpLocation = null;
        if (playerInfo == null) {
            player.sendMessage(tr("\u00a74That player does not exist!"));
            return;
        }

        warpLocation = plugin.getSafeWarpLocation(playerInfo);
        if (warpLocation == null) {
            player.sendMessage(tr("\u00a74Unable to warp you to that player''s island!"));
            return;
        }
        player.sendMessage(tr("\u00a7aTeleporting you to {0}''s island.", playerInfo.getDisplayName()));
        safeTeleport(player, warpLocation, force);
    }

    @EventHandler(priority = EventPriority.HIGHEST,  ignoreCancelled = true)
    @SuppressWarnings("unused")
    public void onPlayerMove(PlayerMoveEvent e) {
        PendingTeleport pendingTeleport = pendingTeleports.get(e.getPlayer().getUniqueId());
        if (pendingTeleport != null) {
            pendingTeleport.playerMoved(e.getPlayer());
        }
    }

    private class PendingTeleport {
        private final Location location;
        private final BukkitTask task;

        private PendingTeleport(Location location, BukkitTask task) {
            this.location = location != null ? location.clone() : null;
            this.task = task;
        }

        public Location getLocation() {
            return location;
        }

        public BukkitTask getTask() {
            return task;
        }

        void playerMoved(Player player) {
            Location newLocation = player.getLocation();
            if (location != null && location.getWorld() != null && location.getWorld().equals(newLocation.getWorld())) {
                double distance = location.distance(newLocation);
                if (distance > cancelDistance) {
                    task.cancel();
                    pendingTeleports.remove(player.getUniqueId());
                    player.sendMessage(tr("\u00a77Teleport cancelled"));
                }
            }
        }
    }
}
