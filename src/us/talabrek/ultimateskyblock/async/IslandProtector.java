package us.talabrek.ultimateskyblock.async;

import java.io.File;
import java.util.UUID;

import com.sk89q.worldguard.protection.managers.RegionManager;

import us.talabrek.ultimateskyblock.UUIDPlayerInfo;
import us.talabrek.ultimateskyblock.WorldGuardHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class IslandProtector implements Runnable
{
	public IslandProtector()
	{
	}
	
	@Override
	public void run()
	{
		File dir = uSkyBlock.getInstance().directoryPlayers;
		
		RegionManager manager = WorldGuardHandler.getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld());
		
		for(File file : dir.listFiles())
		{
			// not completely sure of this
			// todo: get sure
			UUIDPlayerInfo pi = uSkyBlock.getInstance().getPlayer(UUID.fromString(file.getName()));
			
			if(pi == null || !pi.getHasIsland())
				continue;
			
			String regionName = file.getName() + "Island";
			
			if(manager.hasRegion(regionName))
				continue;

			try
			{
				WorldGuardHandler.protectIsland(pi);
			}
			catch (IllegalStateException e)
			{
				e.printStackTrace();
				break;
			}
			catch (IllegalArgumentException e)
			{
				uSkyBlock.getLog().warning("Failed to protect " + file.getName() + "'s island");
			}
		}
	}

}
