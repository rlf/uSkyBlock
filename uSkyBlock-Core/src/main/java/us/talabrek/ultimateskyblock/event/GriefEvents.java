package us.talabrek.ultimateskyblock.event;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInventoryEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import us.talabrek.ultimateskyblock.uSkyBlock;

/**
 * Handling of mob-related events.
 */
public class GriefEvents implements Listener {

    private final uSkyBlock plugin;
    private final boolean creeperEnabled;
    private final boolean shearingEnabled;
    private final boolean killMonstersEnabled;
    private final boolean killAnimalsEnabled;
    private final boolean tramplingEnabled;

    public GriefEvents(uSkyBlock plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        creeperEnabled = config.getBoolean("options.protection.creepers", true);
        shearingEnabled = config.getBoolean("options.protection.visitors.shearing", true);
        killMonstersEnabled = config.getBoolean("options.protection.visitors.kill-monsters", true);
        killAnimalsEnabled = config.getBoolean("options.protection.visitors.kill-animals", true);
        tramplingEnabled = config.getBoolean("options.protection.visitors.trampling", true);
    }

    @EventHandler
    public void onCreeperExplode(ExplosionPrimeEvent event) {
        if (!creeperEnabled || !plugin.isSkyWorld(event.getEntity().getWorld())) {
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
        if (!shearingEnabled || !plugin.isSkyWorld(player.getWorld())) {
            return; // Not our concern
        }
        if (player.hasPermission("usb.mod.bypassprotection")) {
            return;
        }
        if (!plugin.playerIsOnIsland(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if ((!killAnimalsEnabled && !killMonstersEnabled) || !plugin.isSkyWorld(event.getDamager().getWorld())) {
            return;
        }
        if (!(event.getEntity() instanceof Creature)) {
            return;
        }
        if (event.getDamager() instanceof Player
                && !plugin.playerIsOnIsland((Player)event.getDamager())) {
            if (((Player) event.getDamager()).hasPermission("usb.mod.bypassprotection")) {
                return;
            }
            cancelMobDamage(event);
        } else if (event.getDamager() instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
            if (!(shooter instanceof Player)) {
                return;
            }
            Player player = (Player) shooter;
            if (player.hasPermission("usb.mod.bypassprotection") || plugin.playerIsOnIsland(player)) {
                return;
            }
            cancelMobDamage(event);
        }
    }

    private void cancelMobDamage(EntityDamageByEntityEvent event) {
        if (killAnimalsEnabled && event.getEntity() instanceof Animals) {
            event.setCancelled(true);
        } else if (killMonstersEnabled && event.getEntity() instanceof Monster) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTrampling(PlayerInteractEvent event) {
        if (!tramplingEnabled || !plugin.isSkyWorld(event.getPlayer().getWorld())) {
            return;
        }
        if (event.getAction() == Action.PHYSICAL
                && !isValidTarget(event.getPlayer())
                && event.getBlockFace() == BlockFace.UP
                && event.getMaterial() == Material.SOIL) {
            event.setCancelled(true);
        }
    }

}
