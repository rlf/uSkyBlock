package us.talabrek.ultimateskyblock.async;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import us.talabrek.ultimateskyblock.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class TopGenerator implements Runnable
{
	public TopGenerator()
	{
	}
	
	@Override
	public void run()
	{
		uSkyBlock.getInstance().setTopIslands(null);
		
		TreeMap<Integer, String> topIslands = new TreeMap<Integer, String>();
		uSkyBlock.getLog().info("Generating top list");
		
		File playerDir = uSkyBlock.getInstance().directoryPlayers;

		for (File file : playerDir.listFiles()) 
		{
			PlayerInfo info = uSkyBlock.getInstance().getPlayerNoStore(file.getName());
			
			if(info != null && info.getIslandLevel() > 0 && (!info.getHasParty() || info.getPartyLeader().equals(info.getPlayerName())))
				topIslands.put(info.getIslandLevel(), info.getPlayerName());
		}
		
		ArrayList<Entry<String, Integer>> shortList = new ArrayList<Entry<String, Integer>>(topIslands.size());
		
		for(Entry<Integer, String> entry : topIslands.descendingMap().entrySet())
			shortList.add(new AbstractMap.SimpleEntry<String, Integer>(entry.getValue(), entry.getKey()));
		
		uSkyBlock.getInstance().setTopIslands(shortList);

		uSkyBlock.getLog().info("Finished generating top list");
	}

}
