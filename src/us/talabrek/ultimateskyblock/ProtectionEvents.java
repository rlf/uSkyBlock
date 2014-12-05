package us.talabrek.ultimateskyblock;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.*;
import org.bukkit.event.player.*;
import org.bukkit.event.hanging.*;
import org.bukkit.event.vehicle.*;
import org.bukkit.event.entity.*;

public class ProtectionEvents implements Listener
{
    private Player attacker;
    private Player breaker;
    
    public ProtectionEvents() {
        super();
        this.attacker = null;
        this.breaker = null;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerBlockBreak(final BlockBreakEvent event) {
        if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName) && !uSkyBlock.getInstance().locationIsOnIsland(event.getPlayer(), event.getBlock().getLocation()) && !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld()) && !event.getPlayer().isOp()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerBlockPlace(final BlockPlaceEvent event) {
        if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName) && !uSkyBlock.getInstance().locationIsOnIsland(event.getPlayer(), event.getBlock().getLocation()) && !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld()) && !event.getPlayer().isOp()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName) && !uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer()) && !uSkyBlock.getInstance().playerIsInSpawn(event.getPlayer()) && !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld()) && !event.getPlayer().isOp()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerBedEnter(final PlayerBedEnterEvent event) {
        if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName) && !uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer()) && !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld()) && !event.getPlayer().isOp()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerShearEntity(final PlayerShearEntityEvent event) {
        if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName) && !uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer()) && !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld()) && !event.getPlayer().isOp()) {
            event.getPlayer().sendMessage(ChatColor.RED + "You can only do that on your island!");
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
        if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName) && !uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer()) && !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld()) && !event.getPlayer().isOp()) {
            event.getPlayer().sendMessage(ChatColor.RED + "You can only do that on your island!");
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerBucketFill(final PlayerBucketFillEvent event) {
        if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName) && !uSkyBlock.getInstance().locationIsOnIsland(event.getPlayer(), event.getBlockClicked().getLocation()) && !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld()) && !event.getPlayer().isOp()) {
            event.getPlayer().sendMessage(ChatColor.RED + "You can only do that on your island!");
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event) {
        if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName) && !uSkyBlock.getInstance().locationIsOnIsland(event.getPlayer(), event.getBlockClicked().getLocation()) && !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld()) && !event.getPlayer().isOp()) {
            event.getPlayer().sendMessage(ChatColor.RED + "You can only do that on your island!");
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerBreakHanging(final HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player) {
            this.breaker = (Player)event.getRemover();
            if (this.breaker.getWorld().getName().equalsIgnoreCase(Settings.general_worldName) && !uSkyBlock.getInstance().locationIsOnIsland(this.breaker, event.getEntity().getLocation()) && !VaultHandler.checkPerk(this.breaker.getName(), "usb.mod.bypassprotection", this.breaker.getWorld()) && !this.breaker.isOp()) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerVehicleDamage(final VehicleDamageEvent event) {
        if (event.getAttacker() instanceof Player) {
            this.breaker = (Player)event.getAttacker();
            if (this.breaker.getWorld().getName().equalsIgnoreCase(Settings.general_worldName) && !uSkyBlock.getInstance().locationIsOnIsland(this.breaker, event.getVehicle().getLocation()) && !VaultHandler.checkPerk(this.breaker.getName(), "usb.mod.bypassprotection", this.breaker.getWorld()) && !this.breaker.isOp()) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerAttack(final EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            this.attacker = (Player)event.getDamager();
            if (this.attacker.getWorld().getName().equalsIgnoreCase(Settings.general_worldName) && !(event.getEntity() instanceof Player) && !uSkyBlock.getInstance().playerIsOnIsland(this.attacker) && !VaultHandler.checkPerk(this.attacker.getName(), "usb.mod.bypassprotection", this.attacker.getWorld()) && !this.attacker.isOp()) {
                event.setCancelled(true);
            }
        }
    }
}
