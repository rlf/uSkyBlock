package us.talabrek.ultimateskyblock.async;

import java.io.File;

import us.talabrek.ultimateskyblock.PlayerInfo;
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
			PlayerInfo info = uSkyBlock.getInstance().getPlayer(file.getName());
			if(info == null)
				continue;
			
			if (info.getHasParty()) 
			{
				PlayerInfo leaderInfo;
				if (!info.getPartyLeader().equalsIgnoreCase(file.getName())) 
					leaderInfo = uSkyBlock.getInstance().getPlayer(info.getPartyLeader());
				else
					leaderInfo = info;
				
				leaderInfo.getHasParty();

				if (!leaderInfo.getMembers().contains(file.getName()))
					leaderInfo.addMember(file.getName());

				uSkyBlock.getInstance().savePlayer(leaderInfo);
			}
		}
		
		uSkyBlock.getLog().info("Party list completed.");
	}
}
