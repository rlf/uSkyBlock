package us.talabrek.ultimateskyblock.command.island;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.ICommand;
import us.talabrek.ultimateskyblock.UUIDPlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class IslandToggleWarpCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "togglewarp";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"tptoggle", "tw"};
	}

	@Override
	public String getPermission()
	{
		return "usb.extra.addwarp";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label;
	}

	@Override
	public String getDescription()
	{
		return "Enable/disable warping to your island.";
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
		UUIDPlayerInfo info = uSkyBlock.getInstance().getPlayer(((Player) sender).getUniqueId());
		
		if(info == null)
		{
			sender.sendMessage(ChatColor.RED + "You have not started skyblock. Please use " + ChatColor.YELLOW + "/island" + ChatColor.RED + " to begin.");
			return true;
		}
		
		info.toggleWarpActive();
		
		if(info.isWarpActive())
			sender.sendMessage(ChatColor.GREEN + "Your incoming warp is active, players may warp to your island.");
		else
			sender.sendMessage(ChatColor.RED + "Your incoming warp is inactive, players may not warp to your island.");
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}
