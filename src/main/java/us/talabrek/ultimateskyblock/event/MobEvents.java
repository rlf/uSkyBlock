package us.talabrek.ultimateskyblock.event;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import us.talabrek.ultimateskyblock.uSkyBlock;

/**
 * Handling of mob-related events.
 */
public class MobEvents implements Listener {

    private final uSkyBlock plugin;

    public MobEvents(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCreeperExplode(ExplosionPrimeEvent event) {
        if (!plugin.isSkyWorld(event.getEntity().getWorld())) {
            return;
        }
        if (event.getEntity() instanceof Creeper
            && !isValidTarget(((Creeper)event.getEntity()).getTarget()))
        {
            event.setCancelled(true);
        } else if (event.getEntity() instanceof TNTPrimed
            && !isValidTarget(((TNTPrimed) event.getEntity()).getSource())) {
            event.setCancelled(true);
        }
    }

    /**
     * Valid targets are players belonging to the island.
     */
    private boolean isValidTarget(Entity target) {
        return target instanceof Player && plugin.playerIsOnIsland((Player)target);
    }

    @EventHandler
    public void onShearEvent(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isSkyWorld(player.getWorld())) {
            return; // Not our concern
        }
        if (!plugin.playerIsOnIsland(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!plugin.isSkyWorld(event.getDamager().getWorld())) {
            return;
        }
        if (event.getDamager() instanceof Player
                && event.getEntity() instanceof Creature
                && !plugin.playerIsOnIsland((Player)event.getDamager())) {
            event.setCancelled(true);
        }
    }
}
