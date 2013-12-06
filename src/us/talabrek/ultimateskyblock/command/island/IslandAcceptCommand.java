package us.talabrek.ultimateskyblock.command.island;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.talabrek.ultimateskyblock.ICommand;
import us.talabrek.ultimateskyblock.InviteHandler;

public class IslandAcceptCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "accept";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "usb.party.create";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label;
	}

	@Override
	public String getDescription()
	{
		return "Accepts an invitation.";
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
		
		if(!InviteHandler.hasInvite((Player)sender))
		{
			sender.sendMessage(ChatColor.RED + "You have no invites.");
			return true;
		}
		
		InviteHandler.acceptInvite((Player)sender);
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}
	
}
