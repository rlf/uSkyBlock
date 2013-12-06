package us.talabrek.ultimateskyblock.command.island;

import java.util.List;

import org.bukkit.command.CommandSender;

import us.talabrek.ultimateskyblock.ICommand;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class IslandTopCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "top";
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
		return "top";
	}

	@Override
	public String getDescription()
	{
		return "See the top ranked islands";
	}

	@Override
	public boolean canBeConsole()
	{
		return true;
	}

	@Override
	public boolean canBeCommandBlock()
	{
		return true;
	}

	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		uSkyBlock.getInstance().displayTopTen(sender);
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}
