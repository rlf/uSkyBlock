package us.talabrek.ultimateskyblock.command.island;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.ICommand;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.UUIDPlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class IslandRestartCommand implements ICommand
{
	private HashMap<UUID, Long> mCooldownEnd = new HashMap<UUID, Long>();
	private HashMap<UUID, Integer> mRestartCount = new HashMap<UUID, Integer>();
	
	@Override
	public String getName()
	{
		return "restart";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"reset"};
	}

	@Override
	public String getPermission()
	{
		return null;
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label;
	}

	@Override
	public String getDescription()
	{
		return "Restarts your island and resets all challenges.";
	}

	@Override
	public boolean canBeConsole()
	{
		return false;
	}

	@Override
	public boolean canBeCommandBlock()
	{
		return false;
	}

	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		if(args.length != 0)
			return false;

		UUIDPlayerInfo info = uSkyBlock.getInstance().getPlayer(((Player)sender).getUniqueId());
		
		if (info.getHasParty()) 
		{
			if (!info.getPartyLeader().equals(((Player) sender).getUniqueId()))
				sender.sendMessage(ChatColor.RED + "Only the owner may restart this island. Leave this island in order to start your own (/island leave).");
			else
				sender.sendMessage(ChatColor.YELLOW + "You must remove all players from your island before you can restart it (/island kick <player>). See a list of players currently part of your island using /island party.");
			return true;
		}
		
		long endTime = 0;
		int restartCount = 0;
		if(mCooldownEnd.containsKey(((Player) sender).getUniqueId()))
		{
			endTime = mCooldownEnd.get(((Player) sender).getUniqueId());
			restartCount = mRestartCount.get(((Player) sender).getUniqueId());
		}
		
		if(Settings.general_cooldownRestart == 0 || endTime < System.currentTimeMillis())
		{
			++restartCount;
			endTime = System.currentTimeMillis() + (Settings.general_cooldownRestart * 1000) + (long)((Settings.general_cooldownRestart * 1000) * (restartCount - 1) * (restartCount - 1) * 0.4f);
			mCooldownEnd.put(((Player) sender).getUniqueId(), endTime);
			mRestartCount.put(((Player) sender).getUniqueId(), restartCount);
			sender.sendMessage(ChatColor.GREEN + "Creating a new island for you.");
			uSkyBlock.getInstance().restartIsland(info);
		}
		else
		{
			long remaining = (endTime - System.currentTimeMillis()) / 1000;
			sender.sendMessage(ChatColor.RED + "You must wait " + remaining + " seconds before you can restart again.");
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}
