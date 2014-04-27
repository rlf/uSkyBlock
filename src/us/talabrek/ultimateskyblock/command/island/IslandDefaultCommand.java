package us.talabrek.ultimateskyblock.command.island;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.talabrek.ultimateskyblock.ICommand;
import us.talabrek.ultimateskyblock.UUIDPlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.async.IslandBuilder;

public class IslandDefaultCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "";
	}

	@Override
	public String[] getAliases()
	{
		return null;
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
		return "Start your island, or teleport back to one you have.";
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
		UUIDPlayerInfo pi = uSkyBlock.getInstance().getOrCreatePlayer(((Player)sender).getUniqueId());
		if (pi == null) 
		{
			sender.sendMessage(ChatColor.RED + "Error: Couldn't read your player data!");
			return true;
		}
		
		if(!pi.getHasIsland() && !pi.getHasParty())
		{
			sender.sendMessage(ChatColor.GREEN + "Creating a new skyblock island for you.");
			Bukkit.getScheduler().runTask(uSkyBlock.getInstance(), new IslandBuilder((Player)sender));
		}
		else
		{
			if(pi.teleportHome((Player)sender))
				sender.sendMessage(ChatColor.GREEN + "Teleporting you to your island. (/island help for more info)");
			else
				sender.sendMessage(ChatColor.RED + "There is no safe location to put you. Teleporting anyway.");
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}
