package us.talabrek.ultimateskyblock.async;

import java.io.File;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import us.talabrek.ultimateskyblock.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class Purger implements Runnable
{
	private long mEarliest;
	private boolean mNoIsland;
	
	public Purger(long time, boolean includeNoIsland)
	{
		mEarliest = System.currentTimeMillis() - time;
		mNoIsland = includeNoIsland;
	}
	
	@Override
	public void run()
	{
		File directoryPlayers = uSkyBlock.getInstance().directoryPlayers;

		uSkyBlock.getLog().info("Preparing list of islands to purge.");
		
		LinkedList<PlayerInfo> toRemove = new LinkedList<PlayerInfo>();

		for (File child : directoryPlayers.listFiles()) 
		{
			OfflinePlayer player = Bukkit.getOfflinePlayer(child.getName());
			if(player.hasPlayedBefore() && !player.isOnline())
			{
				if (player.getLastPlayed() < mEarliest || (mNoIsland && !uSkyBlock.getInstance().hasIsland(player.getName()))) 
				{
					PlayerInfo pi = uSkyBlock.getInstance().getPlayerNoStore(player.getName());
					if (pi != null && (mNoIsland || pi.getHasIsland())) 
						toRemove.add(pi);
				}
			}
		}
		
		IslandRemover remover = new IslandRemover(toRemove);
		remover.start();
	}

}
