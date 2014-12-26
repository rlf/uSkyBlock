package us.talabrek.ultimateskyblock.event;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
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
}
