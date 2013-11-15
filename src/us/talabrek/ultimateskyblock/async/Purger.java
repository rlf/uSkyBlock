package us.talabrek.ultimateskyblock.async;

import java.io.File;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import us.talabrek.ultimateskyblock.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class Purger implements Runnable
{
	private int mTime;
	public Purger(int time)
	{
		mTime = time;
	}
	
	@Override
	public void run()
	{
		File directoryPlayers = uSkyBlock.getInstance().directoryPlayers;

		long offlineTime = 0L;
		
		uSkyBlock.getLog().info("Preparing list of islands to purge.");
		
		LinkedList<PlayerInfo> toRemove = new LinkedList<PlayerInfo>();

		for (File child : directoryPlayers.listFiles()) 
		{
			OfflinePlayer player = Bukkit.getOfflinePlayer(child.getName());
			if(player.hasPlayedBefore() && !player.isOnline())
			{
				offlineTime = player.getLastPlayed();
				offlineTime = (System.currentTimeMillis() - offlineTime) / 3600000L;
				if (offlineTime > mTime && uSkyBlock.getInstance().hasIsland(player.getName())) 
				{
					PlayerInfo pi = uSkyBlock.getInstance().getPlayer(player.getName());
					if (pi != null && !pi.getHasParty() && pi.getIslandLevel() < 10) 
						toRemove.add(pi);
				}
			}
		}
		
		IslandRemover remover = new IslandRemover(toRemove);
		remover.start();
	}

}
