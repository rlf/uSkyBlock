package us.talabrek.ultimateskyblock;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.potion.Potion;

public class ProtectionEvents implements Listener {
	private Player breaker = null;

	@EventHandler(priority = EventPriority.NORMAL)
	public void onHorseLead(final PlayerInteractEntityEvent event) {
		if (event.getRightClicked().getType() == EntityType.HORSE || event.getRightClicked().getType() == EntityType.ITEM_FRAME) {
			final Player player = event.getPlayer();
			if (player.getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
				if (!uSkyBlock.getInstance().locationIsOnIsland(player, event.getRightClicked().getLocation()) && !uSkyBlock.getInstance().playerIsInSpawn(event.getPlayer()) && !VaultHandler.checkPerk(player.getName(), "usb.mod.bypassprotection", player.getWorld()) && !player.isOp()) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerAttack(final EntityDamageByEntityEvent event) {
		if (!event.getEntity().getWorld().getName().equalsIgnoreCase(Settings.general_worldName))
			return;

		if (!(event.getDamager() instanceof Player) && !(event.getDamager() instanceof Projectile))
			return;
		
		if (event.getEntity() instanceof Player)
			return;

		Player damager = null;

		if (event.getDamager() instanceof Projectile) {

			Projectile projectile = (Projectile) event.getDamager();

			if (projectile.getShooter() instanceof Player)
				damager = (Player) projectile.getShooter();
		} else if (event.getDamager() instanceof Player)
			damager = (Player) event.getDamager();

		if (damager == null)
			return;
		
		if (damager.hasPermission("usb.mod.bypassprotection"))
			return;

		if (!uSkyBlock.getInstance().playerIsOnIsland(damager))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBedEnter(final PlayerBedEnterEvent event) {
		if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
			if (!uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer()) && !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld()) && !event.getPlayer().isOp()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerBlockBreak(final BlockBreakEvent event) {
		if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
			if (!uSkyBlock.getInstance().locationIsOnIsland(event.getPlayer(), event.getBlock().getLocation()) && !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld()) && !event.getPlayer().isOp()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerBlockPlace(final BlockPlaceEvent event) {
		if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
			if (!uSkyBlock.getInstance().locationIsOnIsland(event.getPlayer(), event.getBlock().getLocation()) && !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld()) && !event.getPlayer().isOp()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBreakHanging(final HangingBreakByEntityEvent event) {
		if (event.getRemover() instanceof Player) {
			breaker = (Player) event.getRemover();
			if (breaker.getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
				if (!uSkyBlock.getInstance().locationIsOnIsland(breaker, event.getEntity().getLocation()) && !VaultHandler.checkPerk(breaker.getName(), "usb.mod.bypassprotection", breaker.getWorld()) && !breaker.isOp()) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event) {
		if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
			if (!uSkyBlock.getInstance().locationIsOnIsland(event.getPlayer(), event.getBlockClicked().getLocation()) && !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld()) && !event.getPlayer().isOp()) {
				event.getPlayer().sendMessage(ChatColor.RED + "You can only do that on your island!");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBucketFill(final PlayerBucketFillEvent event) {
		if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
			if (!uSkyBlock.getInstance().locationIsOnIsland(event.getPlayer(), event.getBlockClicked().getLocation()) && !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld()) && !event.getPlayer().isOp()) {
				event.getPlayer().sendMessage(ChatColor.RED + "You can only do that on your island!");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(final PlayerInteractEvent event) {
		if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
			if (!uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer()) && !uSkyBlock.getInstance().playerIsInSpawn(event.getPlayer()) && !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld()) && !event.getPlayer().isOp()) {
				if (event.getMaterial() == Material.ENDER_PEARL) {
					event.setCancelled(true);
					return;
				}

				if (event.getClickedBlock() != null && !event.getClickedBlock().getType().isEdible()) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerShearEntity(final PlayerShearEntityEvent event) {
		if (event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
			if (!uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer()) && !VaultHandler.checkPerk(event.getPlayer().getName(), "usb.mod.bypassprotection", event.getPlayer().getWorld()) && !event.getPlayer().isOp()) {
				event.getPlayer().sendMessage(ChatColor.RED + "You can only do that on your island!");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerVehicleDamage(final VehicleDamageEvent event) {
		if (event.getAttacker() instanceof Player) {
			breaker = (Player) event.getAttacker();
			if (breaker.getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
				if (!uSkyBlock.getInstance().locationIsOnIsland(breaker, event.getVehicle().getLocation()) && !VaultHandler.checkPerk(breaker.getName(), "usb.mod.bypassprotection", breaker.getWorld()) && !breaker.isOp()) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerEnterVehicle(VehicleEnterEvent event) {
		if (!(event.getEntered() instanceof Player))
			return;

		Player player = (Player) event.getEntered();

		if (player.hasPermission("usb.mod.bypassprotection"))
			return;

		if (!event.getEntered().getWorld().getName().equalsIgnoreCase(Settings.general_worldName))
			return;

		if (!uSkyBlock.getInstance().locationIsOnIsland(player, event.getVehicle().getLocation())) {
			player.sendMessage(ChatColor.RED + "You can only do that on your island!");
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPotionThrow(PlayerInteractEvent event) {
		if (event.getPlayer().hasPermission("usb.mod.bypassprotection"))
			return;

		if (!event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName))
			return;

		if (uSkyBlock.getInstance().locationIsOnIsland(event.getPlayer(), event.getPlayer().getLocation()))
			return;

		if (event.getMaterial() == Material.POTION && event.getItem().getDurability() != 0) {

			Potion potion = null;

			try {
				potion = Potion.fromItemStack(event.getItem());
			} catch (IllegalArgumentException e) {
				return;
			}

			if (potion == null)
				return;

			if (potion.isSplash()) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "Splash potions are disabled when not on your island!");
			}
		}
	}
}