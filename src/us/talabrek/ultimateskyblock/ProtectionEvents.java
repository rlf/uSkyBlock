package us.talabrek.ultimateskyblock;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;

public class ProtectionEvents implements Listener {
	private Player attacker = null;
	private Player breaker = null;

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerBlockBreak(BlockBreakEvent event) {
		if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
			if (!uSkyBlock.getInstance().locationIsOnIsland(event.getPlayer(), event.getBlock().getLocation())
					&& !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld())
					&& !event.getPlayer().isOp()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerBlockPlace(BlockPlaceEvent event) {
		if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
			if (!uSkyBlock.getInstance().locationIsOnIsland(event.getPlayer(), event.getBlock().getLocation())
					&& !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld())
					&& !event.getPlayer().isOp()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
			if (!uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer()) && !uSkyBlock.getInstance().playerIsInSpawn(event.getPlayer())
					&& !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld())
					&& !event.getPlayer().isOp()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
		if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
			if (!uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer())
					&& !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld())
					&& !event.getPlayer().isOp()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerShearEntity(PlayerShearEntityEvent event) {
		if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
			if (!uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer())
					&& !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld())
					&& !event.getPlayer().isOp()) {
				event.getPlayer().sendMessage(ChatColor.RED + "You can only do that on your island!");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
			if (!uSkyBlock.getInstance().locationIsOnIsland(event.getPlayer(), event.getBlockClicked().getLocation())
					&& !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld())
					&& !event.getPlayer().isOp()) {
				event.getPlayer().sendMessage(ChatColor.RED + "You can only do that on your island!");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
			if (!uSkyBlock.getInstance().locationIsOnIsland(event.getPlayer(), event.getBlockClicked().getLocation())
					&& !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld())
					&& !event.getPlayer().isOp()) {
				event.getPlayer().sendMessage(ChatColor.RED + "You can only do that on your island!");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBreakHanging(HangingBreakByEntityEvent event) {
		if (event.getRemover() instanceof Player) {
			breaker = (Player) event.getRemover();
			if (breaker.getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
				if (!uSkyBlock.getInstance().locationIsOnIsland(breaker, event.getEntity().getLocation())
						&& !VaultHandler.checkPerk(breaker.getName(), "usb.mod.bypassprotection", breaker.getWorld()) && !breaker.isOp()) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerVehicleDamage(VehicleDamageEvent event) {
		if (event.getAttacker() instanceof Player) {
			breaker = (Player) event.getAttacker();
			if (breaker.getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
				if (!uSkyBlock.getInstance().locationIsOnIsland(breaker, event.getVehicle().getLocation())
						&& !VaultHandler.checkPerk(breaker.getName(), "usb.mod.bypassprotection", breaker.getWorld()) && !breaker.isOp()) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerAttack(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			attacker = (Player) event.getDamager();
			if (attacker.getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
				if (!(event.getEntity() instanceof Player)) {
					if (!uSkyBlock.getInstance().playerIsOnIsland(attacker)
							&& !VaultHandler.checkPerk(attacker.getName(), "usb.mod.bypassprotection", attacker.getWorld())
							&& !attacker.isOp()) {
						event.setCancelled(true);
					}
				}
			}
		}
	}
}