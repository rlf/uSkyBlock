package us.talabrek.ultimateskyblock.async;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import us.talabrek.ultimateskyblock.UUIDPlayerInfo;
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
		
		TreeMap<Integer, UUID> topIslands = new TreeMap<Integer, UUID>();
		uSkyBlock.getLog().info("Generating top list");
		
		File playerDir = uSkyBlock.getInstance().directoryPlayers;

		for (File file : playerDir.listFiles()) 
		{
			UUIDPlayerInfo info = uSkyBlock.getInstance().getPlayerNoStore(UUID.fromString(file.getName()));
			
			if(info != null && info.getIslandLevel() > 0 && (!info.getHasParty() || info.getPartyLeader().equals(info.getPlayerUUID())))
				topIslands.put(info.getIslandLevel(), info.getPlayer().getUniqueId());
		}
		
		ArrayList<Entry<UUID, Integer>> shortList = new ArrayList<Entry<UUID, Integer>>(topIslands.size());
		
		for(Entry<Integer, UUID> entry : topIslands.descendingMap().entrySet())
			shortList.add(new AbstractMap.SimpleEntry<UUID, Integer>(entry.getValue(), entry.getKey()));
		
		uSkyBlock.getInstance().setTopIslands(shortList);

		uSkyBlock.getLog().info("Finished generating top list");
	}

}
