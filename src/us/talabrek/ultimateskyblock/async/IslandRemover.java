package us.talabrek.ultimateskyblock.async;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitTask;

import us.talabrek.ultimateskyblock.PlayerInfo;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.WorldGuardHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class IslandRemover extends QueueTask
{
	private List<PlayerInfo> mIslands;
	private Iterator<PlayerInfo> mNext;
	private BukkitTask mTask;
	
	private int mFailCount = 0;
	
	public IslandRemover(List<PlayerInfo> islands)
	{
		mIslands = islands;
	}
	
	public void start()
	{
		mTask = Bukkit.getScheduler().runTaskTimer(uSkyBlock.getInstance(), this, 0L, 20L);
		mNext = mIslands.iterator();
		uSkyBlock.getLog().info("Starting removal of " + mIslands.size() + " islands");
	}
	
	@Override
	public void run()
	{
		PlayerInfo island = mNext.next();
		
		if(!remove(island))
			++mFailCount;
		
		if(!mNext.hasNext())
		{
			mTask.cancel();
			
			doNext();
			
			if(mFailCount > 0)
				uSkyBlock.getLog().info("Island removal finished. " + mFailCount + " islands failed");
			else
				uSkyBlock.getLog().info("Island removal finished.");
			
			return;
		}
	}
	
	private boolean remove(PlayerInfo island)
	{
		Location center = island.getIslandLocation();
		if(center == null)
			return false;
		
		for (int x = center.getBlockX() - Settings.island_protectionRange / 2; x <= center.getBlockX() + Settings.island_protectionRange / 2; x++) 
		{
			for (int z = center.getBlockZ() - Settings.island_protectionRange / 2; z <= center.getBlockZ() + Settings.island_protectionRange / 2; z++) 
			{
				for (int y = 0; y <= 255; y++) 
				{
					Block block = new Location(center.getWorld(), x, y, z).getBlock();
					if(block.getType() == Material.AIR)
						continue;
					
					if(block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST || 
						block.getType() == Material.DISPENSER || block.getType() == Material.DROPPER || 
						block.getType() == Material.FURNACE || block.getType() == Material.BREWING_STAND)
					{
						BlockState state = block.getState();
						((InventoryHolder)state).getInventory().clear();
					}
					
					block.setType(Material.AIR);
				}
			}
		}
		
		if (Settings.island_protectWithWorldGuard && Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) 
		{
			if (WorldGuardHandler.getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(island.getPlayerName() + "Island"))
				WorldGuardHandler.getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).removeRegion(island.getPlayerName() + "Island");
		}
		
		uSkyBlock.getInstance().addOrphan(island.getIslandLocation());
		island.clearChallenges();
		island.setIslandLocation(null);
		island.setIslandLevel(0);
		island.setIslandExp(0);
		
		uSkyBlock.getLog().info("Removed " + island.getPlayerName() + "'s island");
		
		return true;
	}
}
