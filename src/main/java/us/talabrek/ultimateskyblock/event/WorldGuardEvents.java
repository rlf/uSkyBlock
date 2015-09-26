package us.talabrek.ultimateskyblock.event;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * Replacement for the WG ENTRY/EXIT deny flags.
 */
public class WorldGuardEvents implements Listener {
    private final uSkyBlock plugin;

    public WorldGuardEvents(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e == null || e.getTo() == null || e.getPlayer() == null || !plugin.isSkyAssociatedWorld(e.getTo().getWorld())) {
            return;
        }
        String islandNameAt = WorldGuardHandler.getIslandNameAt(e.getTo());
        if (islandNameAt == null) {
            return;
        }
        IslandInfo islandInfo = plugin.getIslandInfo(islandNameAt);
        if (islandInfo == null || islandInfo.getBans().isEmpty()) {
            return;
        }
        if (islandInfo.isBanned(e.getPlayer())) {
            e.setCancelled(true);
            Location l = e.getTo().clone();
            l.subtract(islandInfo.getIslandLocation());
            Vector v = new Vector(l.getX(), l.getY(), l.getZ());
            v.normalize();
            v.multiply(1.5); // Bounce
            e.getPlayer().setVelocity(v);
            plugin.notifyPlayer(e.getPlayer(), tr("\u00a7cBanned:\u00a7e You are banned from this island."));
        }
    }
}
