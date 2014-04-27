package us.talabrek.ultimateskyblock.command.island;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.talabrek.ultimateskyblock.ICommand;
import us.talabrek.ultimateskyblock.Misc;
import us.talabrek.ultimateskyblock.UUIDPlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class IslandWarpCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "warp";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"tp", "w", "warpo", "tpo"};
	}

	@Override
	public String getPermission()
	{
		return "usb.island.warp";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + ChatColor.GREEN + " [<player>]";
	}

	@Override
	public String getDescription()
	{
		return "Warps to <player>'s island.";
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
		boolean override = (label.equalsIgnoreCase("warpo") || label.equalsIgnoreCase("tpo"));
		
		if(override && !sender.hasPermission("usb.island.warp.override"))
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
			return true;
		}
		
		if(args.length == 0 && !override)
		{
			UUIDPlayerInfo info = uSkyBlock.getInstance().getPlayer(((Player) sender).getUniqueId());
			
			if(info == null || (!info.getHasIsland() && !info.getHasParty()))
			{
				sender.sendMessage(ChatColor.RED + "You have not started skyblock. Please use " + ChatColor.YELLOW + "/island" + ChatColor.RED + " to begin");
				return true;
			}
			
			if(info.isWarpActive())
				sender.sendMessage(ChatColor.GREEN + "Your incoming warp is active, players may warp to your island.");
			else
				sender.sendMessage(ChatColor.RED + "Your incoming warp is inactive, players may not warp to your island.");
			
			sender.sendMessage("You can set your incoming warp location with " + ChatColor.YELLOW + "/island setwarp");
			sender.sendMessage("Toggle your warp on/off using " + ChatColor.YELLOW + "/island togglewarp");
		}
		else if(args.length == 1)
		{
			UUIDPlayerInfo info = Misc.getPlayerInfo(args[0]);
			
			if(info == null || (!info.getHasIsland() && !info.getHasParty()))
			{
				sender.sendMessage(ChatColor.RED + "Unknown player, or player does not have an island.");
				return true;
			}
			
			if(info.isWarpActive() || override)
			{
				if(override || !info.isBanned(((Player) sender).getUniqueId()))
				{
					if(!info.teleportWarp((Player)sender))
						sender.sendMessage(ChatColor.RED + "There is no safe location to put you, please contact a MOD or ADMIN");
				}
				else
					sender.sendMessage(ChatColor.RED + info.getPlayer().getName() + " has banned you from warping to their island.");
			}
			else
				sender.sendMessage(ChatColor.RED + info.getPlayer().getName() + " has warping disabled.");
		}
		else
			return false;

		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		if(args.length == 1)
		{
			ArrayList<String> players = new ArrayList<String>();
			for(Player player : Bukkit.getOnlinePlayers())
			{
				if(player.getName().toLowerCase().startsWith(args[0]))
				{
					if(sender instanceof Player && ((Player)sender).canSee(player))
						players.add(player.getName());
				}
			}
			
			return players;
		}
		
		return null;
	}

}
