package us.talabrek.ultimateskyblock.async;

import java.io.File;
import java.util.UUID;

import us.talabrek.ultimateskyblock.UUIDPlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class PartyListBuilder implements Runnable
{
	private final File mDir;
	
	
	public PartyListBuilder()
	{
		mDir = uSkyBlock.getInstance().directoryPlayers;
	}
	@Override
	public void run()
	{
		uSkyBlock.getLog().info("Building a new party list...");
		
		for(File file : mDir.listFiles())
		{
			UUIDPlayerInfo info = uSkyBlock.getInstance().getPlayer(UUID.fromString(file.getName()));
			if(info == null)
				continue;
			
			if (info.getHasParty()) 
			{
				UUIDPlayerInfo leaderInfo;
				if (!info.getPartyLeader().equals(UUID.fromString(file.getName())))
					leaderInfo = uSkyBlock.getInstance().getPlayer(info.getPartyLeader());
				else
					leaderInfo = info;
				
				leaderInfo.getHasParty();

				if (!leaderInfo.getMembers().contains(file.getName()))
					leaderInfo.addMember(UUID.fromString(file.getName()));

				uSkyBlock.getInstance().savePlayer(leaderInfo);
			}
		}
		
		uSkyBlock.getLog().info("Party list completed.");
	}
}
