package us.talabrek.ultimateskyblock.command.island;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.talabrek.ultimateskyblock.ICommand;
import us.talabrek.ultimateskyblock.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class IslandSetWarpCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "setwarp";
	}

	@Override
	public String[] getAliases()
	{
		return null;
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
		return "Sets your island's warp location.";
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
		Player player = (Player)sender;
		
		if(!uSkyBlock.isSkyBlockWorld(player.getWorld()) || !uSkyBlock.getInstance().playerIsOnIsland(player))
		{
			sender.sendMessage(ChatColor.RED + "You need to be on your island to set a warp.");
			return true;
		}
		
		PlayerInfo info = uSkyBlock.getInstance().getPlayer(sender.getName());
		
		if(info == null)
		{
			sender.sendMessage(ChatColor.RED + "You have not started skyblock. Please use " + ChatColor.YELLOW + "/island" + ChatColor.RED + " to begin");
			return true;
		}
		
		info.setWarpLocation(player.getLocation());
		info.warpOn();
		
		sender.sendMessage(ChatColor.GREEN + "Your warp has been set at your location. Warping to your island is also enabled.");
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}
