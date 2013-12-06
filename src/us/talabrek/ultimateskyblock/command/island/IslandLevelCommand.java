package us.talabrek.ultimateskyblock.command.island;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.talabrek.ultimateskyblock.ICommand;
import us.talabrek.ultimateskyblock.Misc;
import us.talabrek.ultimateskyblock.PlayerInfo;
import us.talabrek.ultimateskyblock.VaultHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class IslandLevelCommand implements ICommand
{
	private HashSet<CommandSender> mBlockedSenders = new HashSet<CommandSender>();
	
	@Override
	public String getName()
	{
		return "level";
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
		if(sender instanceof Player)
			return label + ChatColor.GREEN + " [<player>]";
		else
			return label + ChatColor.GOLD + " <player>";
	}

	@Override
	public String getDescription()
	{
		return "Checks the level of an island";
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
	public boolean onCommand( final CommandSender sender, String label, String[] args )
	{
		if(args.length > 1)
			return false;
		
		if(args.length == 1 && !VaultHandler.hasPerm(sender, "usb.island.info.others"))
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
			return true;
		}
		
		if(!(sender instanceof Player) && args.length != 1)
			return false;
		
		if(mBlockedSenders.contains(sender))
		{
			sender.sendMessage(ChatColor.RED + "You cannot use this command at this time.");
			return true;
		}
		
		PlayerInfo info = null;
		
		if(args.length == 1)
			info = Misc.getPlayerInfo(args[0]);
		else
			info = uSkyBlock.getInstance().getPlayer(sender.getName());
		
		if(info == null)
			sender.sendMessage(ChatColor.RED + "Unknown player: " + args[0]);
		else
		{
			if (info.getHasParty() || info.getHasIsland())
			{
				mBlockedSenders.add(sender);
				final PlayerInfo fInfo = info;
				info.recalculateLevel(new Runnable()
				{
					@Override
					public void run()
					{
						sender.sendMessage(ChatColor.YELLOW + "Information about " + fInfo.getPlayerName() + "'s Island:");
						sender.sendMessage(ChatColor.GREEN + " Level: " + ChatColor.YELLOW + fInfo.getIslandLevel());
						mBlockedSenders.remove(sender);
					}
				});
			}
			else
				sender.sendMessage(ChatColor.RED + info.getPlayerName() + " does not have an island to rank.");
		}
		
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
				if(player.getName().toLowerCase().startsWith(args[0].toLowerCase()))
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
