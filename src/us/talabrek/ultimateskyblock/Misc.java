package us.talabrek.ultimateskyblock;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class Misc
{
	public static boolean safeTeleport(Player player, Location loc)
	{
		int horRange = 30;
		
		double closestDist = Double.MAX_VALUE;
		Location closest = null;
		
		for(int y = 0; y < loc.getWorld().getMaxHeight(); ++y)
		{
			for(int x = loc.getBlockX() - horRange; x < loc.getBlockX() + horRange; ++x)
			{
				for(int z = loc.getBlockZ() - horRange; z < loc.getBlockZ() + horRange; ++z)
				{
					for(int i = 0; i < 2; ++i)
					{
						int yy = loc.getBlockY();
						
						if(i == 0)
						{
							yy -= y;
							if(yy < 0)
								continue;
						}
						else
						{
							yy += y;
							if(yy >= loc.getWorld().getMaxHeight())
								continue;
						}
	
						Location l = new Location(loc.getWorld(), x, yy, z);
						double dist = loc.distanceSquared(l);
						
						if(dist < closestDist && isSafeLocation(l))
						{
							closest = l;
							closestDist = dist;
						}
					}
				}
			}
			
			if(y*y > closestDist)
				break;
		}
		
		if(closest == null)
			return false;
		
		closest.setPitch(loc.getPitch());
		closest.setYaw(loc.getYaw());
		
		return player.teleport(closest.add(0.5, 0, 0.5));
	}
	
	public static boolean isSafeLocation(Location loc)
	{
		Block feet = loc.getBlock();
		Block ground = feet.getRelative(BlockFace.DOWN);
		Block head = feet.getRelative(BlockFace.UP);
		
		return (isSafe(feet) && isSafe(head) && (head.getType() != Material.WATER && head.getType() != Material.STATIONARY_WATER) && ground.getType().isSolid());
	}
	
	private static boolean isSafe(Block block)
	{
		switch(block.getType())
		{
		case AIR:
		case SUGAR_CANE_BLOCK:
		case WATER:
		case STATIONARY_WATER:
		case LONG_GRASS:
		case CROPS:
		case CARROT:
		case POTATO:
		case RED_MUSHROOM:
		case RED_ROSE:
		case BROWN_MUSHROOM:
		case YELLOW_FLOWER:
		case DEAD_BUSH:
		case SIGN_POST:
		case WALL_SIGN:
			return true;
		default:
			return false;
		}
	}
	
	public static long parseDateDiff(String dateDiff)
	{
		if(dateDiff == null)
			return 0;
		
		Pattern dateDiffPattern = Pattern.compile("^\\s*(\\-|\\+)?\\s*(?:([0-9]+)y)?\\s*(?:([0-9]+)mo)?\\s*(?:([0-9]+)w)?\\s*(?:([0-9]+)d)?\\s*(?:([0-9]+)h)?\\s*(?:([0-9]+)m)?\\s*(?:([0-9]+)s)?\\s*$");
		dateDiff = dateDiff.toLowerCase();
		
		Matcher m = dateDiffPattern.matcher(dateDiff);
		
		if(m.matches())
		{
			int years,months,weeks,days,hours,minutes,seconds;
			boolean negative;
			
			if(m.group(1) != null)
				negative = (m.group(1).compareTo("-") == 0);
			else
				negative = false;

			if(m.group(2) != null)
				years = Integer.parseInt(m.group(2));
			else
				years = 0;
			
			if(m.group(3) != null)
				months = Integer.parseInt(m.group(3));
			else
				months = 0;
			
			if(m.group(4) != null)
				weeks = Integer.parseInt(m.group(4));
			else
				weeks = 0;
			
			if(m.group(5) != null)
				days = Integer.parseInt(m.group(5));
			else
				days = 0;
			
			if(m.group(6) != null)
				hours = Integer.parseInt(m.group(6));
			else
				hours = 0;
			
			if(m.group(7) != null)
				minutes = Integer.parseInt(m.group(7));
			else
				minutes = 0;
			
			if(m.group(8) != null)
				seconds = Integer.parseInt(m.group(8));
			else
				seconds = 0;
			
			// Now calculate the time
			long time = 0;
			time += seconds * 1000L;
			time += minutes * 60000L;
			time += hours * 3600000L;
			time += days * 72000000L;
			time += weeks * 504000000L;
			time += months * 2191500000L;
			time += years * 26298000000L;
			
			if(negative)
				time *= -1;
			
			return time;
		}
		
		return 0;
	}
	
	public static String dateDiffToString(long dateDiff, boolean shortFormat)
	{
		String result = "";
		if(dateDiff < 0)
		{
			result += "-";
			dateDiff *= -1;
		}
		
		int years,months,weeks,days,hours,minutes,seconds;
		if(dateDiff >= 26298000000L)
		{
			years = (int)(dateDiff / 26298000000L);
			dateDiff -= years * 26298000000L;
			if(shortFormat)
				result += years + "y";
			else
				result += years + " Year" + (years != 1 ? "s " : " ");
		}
		
		if(dateDiff >= 2191500000L)
		{
			months = (int)(dateDiff / 2191500000L);
			dateDiff -= months * 2191500000L;
			if(shortFormat)
				result += months + "mo";
			else
				result += months + " Month" + (months != 1 ? "s " : " ");
		}
		else
			months = 0;
		
		if(dateDiff >= 504000000L)
		{
			weeks = (int)(dateDiff / 504000000L);
			dateDiff -= weeks * 504000000L;
			if(shortFormat)
				result += weeks + "w";
			else
				result += weeks + " Week" + (weeks != 1 ? "s " : " ");
		}
		else
			weeks = 0;
		
		if(dateDiff >= 72000000L)
		{
			days = (int)(dateDiff / 72000000L);
			dateDiff -= days * 72000000L;
			if(shortFormat)
				result += days + "d";
			else
				result += days + " Day" + (days != 1 ? "s " : " ");
		}
		else
			days = 0;
		
		if(dateDiff >= 3600000L)
		{
			hours = (int)(dateDiff / 3600000L);
			dateDiff -= hours * 3600000L;
			if(shortFormat)
				result += hours + "h";
			else
				result += hours + " Hour" + (hours != 1 ? "s " : " ");
		}
		else
			hours = 0;
		
		if(dateDiff >= 60000L)
		{
			minutes = (int)(dateDiff / 60000L);
			dateDiff -= minutes * 60000L;
			if(shortFormat)
				result += minutes + "m";
			else
				result += minutes + " Minute" + (minutes != 1 ? "s " : " ");
		}
		else
			minutes = 0;
		
		if(dateDiff >= 1000L) // Dont to less than 1 second
		{
			seconds = (int)(dateDiff / 1000L);
			if(shortFormat)
				result += seconds + "s";
			else
				result += seconds + " Second" + (seconds != 1 ? "s " : " ");
		}
		else
			seconds = 0;
		
		return result;
	}
	
	public static PlayerInfo getPlayerInfo(String name)
	{
		OfflinePlayer player = Bukkit.getOfflinePlayer(name);
		if(!player.hasPlayedBefore())
		{
			player = Bukkit.getPlayer(name);
			if(player == null)
				return null;
		}
		
		return uSkyBlock.getInstance().getPlayerNoStore(player.getName());
	}
}
