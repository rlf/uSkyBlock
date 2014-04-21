package us.talabrek.ultimateskyblock;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerJoin implements Listener {
	private Player hungerman = null;

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerFoodChange(final FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			hungerman = (Player) event.getEntity();
			if (hungerman.getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
				if (hungerman.getFoodLevel() > event.getFoodLevel()) {
					if (uSkyBlock.getInstance().playerIsOnIsland(hungerman)) {
						if (VaultHandler.checkPerk(hungerman, "usb.extra.hunger", hungerman.getWorld())) {
							event.setCancelled(true);
						}
					}
				}
			}
		}
	}

	@SuppressWarnings( "deprecation" )
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(final PlayerInteractEvent event) 
	{
		if (Settings.extras_obsidianToLava && uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer())) 
		{
			if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) 
					&& event.getPlayer().getItemInHand().getType() == Material.BUCKET
					&& event.getClickedBlock().getType() == Material.OBSIDIAN
					&& event.getClickedBlock().getRelative(event.getBlockFace()).getType() == Material.AIR) 
			{
				event.getPlayer().sendMessage(ChatColor.YELLOW + "Changing your obsidian back into lava. Be careful!");
				event.getClickedBlock().setType(Material.AIR);
				event.getPlayer().getInventory().removeItem(new ItemStack[] { new ItemStack(Material.BUCKET, 1) });
				event.getPlayer().getInventory().addItem(new ItemStack[] { new ItemStack(Material.LAVA_BUCKET, 1) });
				event.getPlayer().updateInventory();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(final PlayerJoinEvent event) 
	{
		if(!uSkyBlock.isSkyBlockWorld(event.getPlayer().getWorld()))
			return;
		
		uSkyBlock.getInstance().onEnterSkyBlock(event.getPlayer().getUniqueId());
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerChangeWorld(PlayerChangedWorldEvent event)
	{
		if(!uSkyBlock.isSkyBlockWorld(event.getPlayer().getWorld()))
			uSkyBlock.getInstance().onLeaveSkyBlock(event.getPlayer().getUniqueId());
		else
			uSkyBlock.getInstance().onEnterSkyBlock(event.getPlayer().getUniqueId());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(final PlayerQuitEvent event) 
	{
		uSkyBlock.getInstance().onLeaveSkyBlock(event.getPlayer().getUniqueId());
	}
}