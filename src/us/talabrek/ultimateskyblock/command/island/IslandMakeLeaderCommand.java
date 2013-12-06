package us.talabrek.ultimateskyblock.command.island;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.talabrek.ultimateskyblock.ICommand;
import us.talabrek.ultimateskyblock.InviteHandler;
import us.talabrek.ultimateskyblock.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class IslandMakeLeaderCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "makeleader";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"transfer"};
	}

	@Override
	public String getPermission()
	{
		return "usb.party.makeleader";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " " + ChatColor.GOLD + "<player>";
	}

	@Override
	public String getDescription()
	{
		return "Transfer the island to <player>";
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
		if(args.length != 1)
			return false;
		
		PlayerInfo info = uSkyBlock.getInstance().getPlayer(sender.getName());
		
		if(info == null)
		{
			sender.sendMessage(ChatColor.RED + "You have not started skyblock. Please use " + ChatColor.YELLOW + "/island" + ChatColor.RED + " to begin");
			return true;
		}
		
		Player otherPlayer = Bukkit.getPlayer(args[0]);
		
		if(otherPlayer == null)
		{
			sender.sendMessage(ChatColor.RED + "Unknown player " + args[0]);
			return true;
		}

		if(otherPlayer.equals(sender))
		{
			sender.sendMessage(ChatColor.RED + "You cannot transfer to yourself.");
			return true;
		}
		
		PlayerInfo otherInfo = uSkyBlock.getInstance().getOrCreatePlayer(otherPlayer.getName());
		
		if(otherInfo.getHasParty())
		{
			sender.sendMessage(ChatColor.RED + otherPlayer.getName() + " is already part of a party. You cannot transfer the island to them");
			return true;
		}
		
		InviteHandler.transferRequest((Player)sender, otherPlayer);
		sender.sendMessage(ChatColor.GREEN + "You have sent a request to " + otherPlayer.getName() + " to transfer your island to them.");
		
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
