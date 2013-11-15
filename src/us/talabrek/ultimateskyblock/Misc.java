package us.talabrek.ultimateskyblock;

import org.bukkit.Location;
import org.bukkit.Material;
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
		
		for(int x = loc.getBlockX() - horRange; x < loc.getBlockX() + horRange; ++x)
		{
			for(int z = loc.getBlockZ() - horRange; z < loc.getBlockZ() + horRange; ++z)
			{
				for(int y = 0; y < loc.getWorld().getMaxHeight(); ++y)
				{
					Location l = new Location(loc.getWorld(), x, y, z);
					double dist = loc.distanceSquared(l);
					
					if(dist < closestDist && isSafeLocation(l))
					{
						closest = l;
						closestDist = dist;
					}
				}
			}
		}
		
		if(closest == null)
			return false;
		
		return player.teleport(closest);
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
}
