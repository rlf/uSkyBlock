package us.talabrek.ultimateskyblock.async;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;

import us.talabrek.ultimateskyblock.PlayerInfo;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.WorldGuardHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class IslandRemover extends QueueTask
{
	private List<PlayerInfo> mIslands;
	private Iterator<PlayerInfo> mNext;
	private int mFailCount = 0;
	private int mVisitedCount = 0; 
	
	private long mLastUpdate;
	
	public IslandRemover(List<PlayerInfo> islands)
	{
		mIslands = islands;
	}
	
	public void start()
	{
		if(mIslands.isEmpty())
			return;
		
		Bukkit.getScheduler().runTaskLater(uSkyBlock.getInstance(), this, 4L);
		mNext = mIslands.iterator();
		mLastUpdate = System.currentTimeMillis();
		mVisitedCount = 0;
		mFailCount = 0;
		uSkyBlock.getLog().info("Starting removal of " + mIslands.size() + " islands");
	}
	
	@Override
	public void run()
	{
		PlayerInfo island = mNext.next();
		
		++mVisitedCount;
		
		if(System.currentTimeMillis() - mLastUpdate > 2000)
		{
			uSkyBlock.getLog().info(String.format("Removal: %d/%d %.0f%%", mVisitedCount, mIslands.size(), (mVisitedCount / (float)mIslands.size()) * 100));
			mLastUpdate = System.currentTimeMillis();
		}
		
		boolean ok = false;
		boolean removed = false;
		
		if(!island.getHasIsland() && !island.getHasParty())
		{
			ok = uSkyBlock.getInstance().deletePlayerData(island.getPlayerName());
		}
		else
			removed = ok = remove(island);
		
		if(!ok)
			++mFailCount;
		
		if(!mNext.hasNext())
		{
			doNext();
			
			if(mFailCount > 0)
				uSkyBlock.getLog().info("Island removal finished. " + mFailCount + " islands failed");
			else
				uSkyBlock.getLog().info("Island removal finished.");
		}
		else
			Bukkit.getScheduler().runTaskLater(uSkyBlock.getInstance(), this, (removed ? 20L : 4L));
	}
	
	private boolean remove(PlayerInfo island)
	{
		Location center = island.getIslandLocation();
		
		if(island.getHasParty() && island.getPlayerName().equals(island.getPartyLeader()))
			center = island.getPartyIslandLocation();
		
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
		island.setHasIsland(false);
		island.setHomeLocation(null);
		
		uSkyBlock.getLog().info("Removed " + island.getPlayerName() + "'s island");
		
		uSkyBlock.getInstance().removeFromTop(island);
		if(!uSkyBlock.getInstance().deletePlayerData(island.getPlayerName()))
			uSkyBlock.getInstance().savePlayer(island);
		
		return true;
	}
}
