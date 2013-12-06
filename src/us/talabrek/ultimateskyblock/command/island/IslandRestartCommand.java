package us.talabrek.ultimateskyblock.command.island;

import java.util.List;
import java.util.WeakHashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.talabrek.ultimateskyblock.ICommand;
import us.talabrek.ultimateskyblock.PlayerInfo;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class IslandRestartCommand implements ICommand
{
	private WeakHashMap<Player, Long> mLastUsedCommand = new WeakHashMap<Player, Long>();
	
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
		
		PlayerInfo info = uSkyBlock.getInstance().getPlayer(sender.getName());
		
		if (info.getHasParty()) 
		{
			if (!info.getPartyLeader().equalsIgnoreCase(sender.getName()))
				sender.sendMessage(ChatColor.RED + "Only the owner may restart this island. Leave this island in order to start your own (/island leave).");
			else
				sender.sendMessage(ChatColor.YELLOW + "You must remove all players from your island before you can restart it (/island kick <player>). See a list of players currently part of your island using /island party.");
			return true;
		}
		
		long lastUsed = 0;
		if(mLastUsedCommand.containsKey(sender))
			lastUsed = mLastUsedCommand.get(sender);
		
		if(Settings.general_cooldownRestart == 0 || (System.currentTimeMillis() - lastUsed) > Settings.general_cooldownRestart * 1000)
		{
			mLastUsedCommand.put((Player)sender, System.currentTimeMillis());
			sender.sendMessage(ChatColor.GREEN + "Creating a new island for you.");
			uSkyBlock.getInstance().restartIsland(info);
		}
		else
		{
			long remaining = ((Settings.general_cooldownRestart * 1000) - (System.currentTimeMillis() - lastUsed)) / 1000;
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
