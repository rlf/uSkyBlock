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
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.VaultHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class IslandInviteCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "invite";
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
		return label + ChatColor.GREEN + " [<player>]";
	}

	@Override
	public String getDescription()
	{
		return "Invites <player> to join your island.";
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
		if(args.length > 1)
			return false;

		PlayerInfo info = uSkyBlock.getInstance().getPlayer(sender.getName());
		
		if(info == null)
		{
			sender.sendMessage(ChatColor.RED + "You have not started skyblock. Please use " + ChatColor.YELLOW + "/island" + ChatColor.RED + " to begin");
			return true;
		}
		
		int maxSize = Settings.general_maxPartySize;
		
		if (VaultHandler.hasPerm(sender, "usb.extra.partysize"))
			maxSize *= 2;
		
		if(args.length == 0)
		{
			sender.sendMessage(ChatColor.YELLOW + "Use" + ChatColor.WHITE + " /island invite <playername>" + ChatColor.YELLOW + " to invite a player to your island.");
	        if (info.getHasParty())
	        {
	        	if (info.getPartyLeader().equalsIgnoreCase(sender.getName()))
	        	{
	        		if(info.getMembers().size() < maxSize)
	        			sender.sendMessage(ChatColor.GREEN + "You can invite " + (maxSize - info.getMembers().size()) + " more players.");
	        		else
	        			sender.sendMessage(ChatColor.RED + "You can't invite any more players.");
	            }
	        	else
	        		sender.sendMessage(ChatColor.RED + "Only the island's owner can invite!");
	        }
	        else
	        	sender.sendMessage(ChatColor.GREEN + "You can invite " + maxSize + " more players.");
		}
		else
		{
			Player otherPlayer = Bukkit.getPlayer(args[0]);
			
			if(otherPlayer == null)
			{
				sender.sendMessage(ChatColor.RED + "Unknown player " + args[0]);
				return true;
			}
			
			if(otherPlayer.getName().equals(sender.getName()))
			{
				sender.sendMessage(ChatColor.RED + "You cannot invite yourself.");
				return true;
			}
			
			if(info.getMembers().size() >= maxSize)
			{
				sender.sendMessage(ChatColor.RED + "You can't invite any more players.");
				return true;
			}
			
			try
			{
				InviteHandler.invitePlayer(otherPlayer, (Player)sender);
			}
			catch(IllegalArgumentException e)
			{
				sender.sendMessage(ChatColor.RED + e.getMessage());
			}
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
