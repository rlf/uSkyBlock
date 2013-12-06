package us.talabrek.ultimateskyblock.command.island;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.talabrek.ultimateskyblock.ICommand;
import us.talabrek.ultimateskyblock.InviteHandler;
import us.talabrek.ultimateskyblock.InviteHandler.Invite;
import us.talabrek.ultimateskyblock.Misc;
import us.talabrek.ultimateskyblock.PlayerInfo;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.VaultHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class IslandPartyCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "party";
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
		if(sender instanceof Player)
			return label;
		else
			return label + ChatColor.GOLD + " <player>";
	}

	@Override
	public String getDescription()
	{
		return "Views party information.";
	}

	@Override
	public boolean canBeConsole()
	{
		return true;
	}

	@Override
	public boolean canBeCommandBlock()
	{
		return false;
	}

	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		if(sender instanceof Player)
		{
			if(args.length != 0)
				return false;
		}
		else
		{
			if(args.length != 1)
				return false;
		}
		
		PlayerInfo info = null;
		
		if(args.length == 1)
		{
			info = Misc.getPlayerInfo(args[0]);
			
			if(info == null)
			{
				sender.sendMessage("No player by name " + args[0]);
				return true;
			}
		}
		else
		{
			info = uSkyBlock.getInstance().getPlayer(sender.getName());
			
			if(info == null)
			{
				sender.sendMessage(ChatColor.RED + "You have not started skyblock. Please use " + ChatColor.YELLOW + "/island" + ChatColor.RED + " to begin");
				return true;
			}
		}
		
        if (info.getHasParty())
        {
        	if (sender instanceof Player && VaultHandler.hasPerm(sender, "usb.party.create"))
    			sender.sendMessage(ChatColor.WHITE + "/island invite <playername>" + ChatColor.YELLOW + " to invite a player to join your island.");
        	
            sender.sendMessage(ChatColor.WHITE + "/island leave" + ChatColor.YELLOW + " leave your current island and return to spawn");
            if (info.getPartyLeader().equals(sender.getName()))
            {
            	if (VaultHandler.hasPerm(sender, "usb.party.kick"))
            		sender.sendMessage(ChatColor.WHITE + "/island remove <playername>" + ChatColor.YELLOW + " remove <playername> from your island");
            	if (VaultHandler.hasPerm(sender, "usb.party.makeleader"))
            		sender.sendMessage(ChatColor.WHITE + "/island makeleader <playername>" + ChatColor.YELLOW + " give ownership of the island to <playername>");
            	
            	int maxSize = Settings.general_maxPartySize;
            	
            	if (VaultHandler.hasPerm(sender, "usb.extra.partysize"))
            		maxSize *= 2;
            	
            	if (info.getMembers().size() < maxSize)
            		sender.sendMessage(ChatColor.GREEN + "You can invite " + (maxSize - info.getMembers().size()) + " more players.");
                else 
                	sender.sendMessage(ChatColor.RED + "You can't invite any more players.");
            }

            sender.sendMessage(ChatColor.YELLOW + "Listing your island members:");
            PlayerInfo leader = uSkyBlock.getInstance().getPlayerNoStore(info.getPartyLeader());
            sender.sendMessage(ChatColor.WHITE + leader.getMembers().toString());
        } 
        else if (sender instanceof Player && InviteHandler.hasInvite((Player)sender))
        {
        	Invite invite = InviteHandler.getInvite((Player)sender);
        	
        	switch(invite.type)
        	{
        	case JoinIsland:
        		sender.sendMessage(ChatColor.YELLOW + invite.from.getName() + " has invited you to join their island.");
                sender.sendMessage(ChatColor.WHITE + "/island [accept/reject]" + ChatColor.YELLOW + " to accept or reject the invite.");
        		break;
        	case Transfer:
        		sender.sendMessage(ChatColor.YELLOW + invite.from.getName() + " has requested to transfer their island to you.");
                sender.sendMessage(ChatColor.WHITE + "/island [accept/reject]" + ChatColor.YELLOW + " to accept or reject the transfer.");
        		break;
        	}
        }
        else
        {
        	sender.sendMessage(ChatColor.RED + "Nothing to display.");
        }
        
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		if(args.length == 1 && !(sender instanceof Player))
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
