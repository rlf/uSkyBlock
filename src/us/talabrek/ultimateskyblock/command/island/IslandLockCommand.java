package us.talabrek.ultimateskyblock.command.island;

import java.util.List;

import org.bukkit.command.CommandSender;

import us.talabrek.ultimateskyblock.ICommand;
import us.talabrek.ultimateskyblock.WorldGuardHandler;

public class IslandLockCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "lock";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "usb.lock";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label;
	}

	@Override
	public String getDescription()
	{
		return "Locks your island so that non party members cannot enter.";
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
		
		WorldGuardHandler.islandLock(sender, sender.getName());
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}
