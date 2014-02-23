package us.talabrek.ultimateskyblock.async;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import us.talabrek.ultimateskyblock.Misc;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.VaultHandler;
import us.talabrek.ultimateskyblock.WorldEditHandler;
import us.talabrek.ultimateskyblock.WorldGuardHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class IslandBuilder implements Runnable
{
	private Player mPlayer;
	public IslandBuilder(Player player)
	{
		mPlayer = player;
	}
	@Override
	public void run()
	{
		createIsland();
		//generateIslandBlocks(mLocation.getBlockX(), mLocation.getBlockZ(), mPlayer, mLocation.getWorld());
	}
	
	private void islandExtras(final int x, final int z, final Player player, final World world) {
		int y = Settings.island_height;

		Block blockToChange = world.getBlockAt(x, y + 5, z);
		blockToChange.setTypeId(17);
		blockToChange = world.getBlockAt(x, y + 6, z);
		blockToChange.setTypeId(17);
		blockToChange = world.getBlockAt(x, y + 7, z);
		blockToChange.setTypeId(17);
		y = Settings.island_height + 8;
		for (int x_operate = x - 2; x_operate <= x + 2; x_operate++) {
			for (int z_operate = z - 2; z_operate <= z + 2; z_operate++) {
				blockToChange = world.getBlockAt(x_operate, y, z_operate);
				blockToChange.setTypeId(18);
			}
		}
		blockToChange = world.getBlockAt(x + 2, y, z + 2);
		blockToChange.setTypeId(0);
		blockToChange = world.getBlockAt(x + 2, y, z - 2);
		blockToChange.setTypeId(0);
		blockToChange = world.getBlockAt(x - 2, y, z + 2);
		blockToChange.setTypeId(0);
		blockToChange = world.getBlockAt(x - 2, y, z - 2);
		blockToChange.setTypeId(0);
		blockToChange = world.getBlockAt(x, y, z);
		blockToChange.setTypeId(17);
		y = Settings.island_height + 9;
		for (int x_operate = x - 1; x_operate <= x + 1; x_operate++) {
			for (int z_operate = z - 1; z_operate <= z + 1; z_operate++) {
				blockToChange = world.getBlockAt(x_operate, y, z_operate);
				blockToChange.setTypeId(18);
			}
		}
		blockToChange = world.getBlockAt(x - 2, y, z);
		blockToChange.setTypeId(18);
		blockToChange = world.getBlockAt(x + 2, y, z);
		blockToChange.setTypeId(18);
		blockToChange = world.getBlockAt(x, y, z - 2);
		blockToChange.setTypeId(18);
		blockToChange = world.getBlockAt(x, y, z + 2);
		blockToChange.setTypeId(18);
		blockToChange = world.getBlockAt(x, y, z);
		blockToChange.setTypeId(17);
		y = Settings.island_height + 10;
		blockToChange = world.getBlockAt(x - 1, y, z);
		blockToChange.setTypeId(18);
		blockToChange = world.getBlockAt(x + 1, y, z);
		blockToChange.setTypeId(18);
		blockToChange = world.getBlockAt(x, y, z - 1);
		blockToChange.setTypeId(18);
		blockToChange = world.getBlockAt(x, y, z + 1);
		blockToChange.setTypeId(18);
		blockToChange = world.getBlockAt(x, y, z);
		blockToChange.setTypeId(17);
		blockToChange = world.getBlockAt(x, y + 1, z);
		blockToChange.setTypeId(18);

		blockToChange = world.getBlockAt(x, Settings.island_height + 5, z + 1);
		blockToChange.setTypeId(54);
		final Chest chest = (Chest) blockToChange.getState();
		final Inventory inventory = chest.getInventory();
		inventory.clear();
		inventory.setContents(Settings.island_chestItems);
		if (Settings.island_addExtraItems) {
			for (final String island_extraPermission : Settings.island_extraPermissions) {
				if (VaultHandler.checkPerk(player.getName(), "usb." + island_extraPermission, player.getWorld())) {
					final String[] chestItemString = uSkyBlock.getInstance().getConfig().getString("options.island.extraPermissions." + island_extraPermission).split(" ");
					final ItemStack[] tempChest = new ItemStack[chestItemString.length];
					String[] amountdata = new String[2];
					for (int j = 0; j < chestItemString.length; j++) {
						amountdata = chestItemString[j].split(":");
						tempChest[j] = new ItemStack(Integer.parseInt(amountdata[0]), Integer.parseInt(amountdata[1]));
						inventory.addItem(new ItemStack[] { tempChest[j] });
					}
				}
			}
		}
	}

	private void islandLayer1(final int x, final int z, final Player player, final World world) {
		int y = Settings.island_height;
		y = Settings.island_height + 4;
		for (int x_operate = x - 3; x_operate <= x + 3; x_operate++) {
			for (int z_operate = z - 3; z_operate <= z + 3; z_operate++) {
				final Block blockToChange = world.getBlockAt(x_operate, y, z_operate);
				blockToChange.setTypeId(2);
			}
		}
		Block blockToChange = world.getBlockAt(x - 3, y, z + 3);
		blockToChange.setTypeId(0);
		blockToChange = world.getBlockAt(x - 3, y, z - 3);
		blockToChange.setTypeId(0);
		blockToChange = world.getBlockAt(x + 3, y, z - 3);
		blockToChange.setTypeId(0);
		blockToChange = world.getBlockAt(x + 3, y, z + 3);
		blockToChange.setTypeId(0);
	}

	private void islandLayer2(final int x, final int z, final Player player, final World world) {
		int y = Settings.island_height;
		y = Settings.island_height + 3;
		for (int x_operate = x - 2; x_operate <= x + 2; x_operate++) {
			for (int z_operate = z - 2; z_operate <= z + 2; z_operate++) {
				final Block blockToChange = world.getBlockAt(x_operate, y, z_operate);
				blockToChange.setTypeId(3);
			}
		}
		Block blockToChange = world.getBlockAt(x - 3, y, z);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x + 3, y, z);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x, y, z - 3);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x, y, z + 3);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x, y, z);
		blockToChange.setTypeId(12);
	}

	private void islandLayer3(final int x, final int z, final Player player, final World world) {
		int y = Settings.island_height;
		y = Settings.island_height + 2;
		for (int x_operate = x - 1; x_operate <= x + 1; x_operate++) {
			for (int z_operate = z - 1; z_operate <= z + 1; z_operate++) {
				final Block blockToChange = world.getBlockAt(x_operate, y, z_operate);
				blockToChange.setTypeId(3);
			}
		}
		Block blockToChange = world.getBlockAt(x - 2, y, z);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x + 2, y, z);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x, y, z - 2);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x, y, z + 2);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x, y, z);
		blockToChange.setTypeId(12);
	}

	private void islandLayer4(final int x, final int z, final Player player, final World world) {
		int y = Settings.island_height;
		y = Settings.island_height + 1;
		Block blockToChange = world.getBlockAt(x - 1, y, z);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x + 1, y, z);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x, y, z - 1);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x, y, z + 1);
		blockToChange.setTypeId(3);
		blockToChange = world.getBlockAt(x, y, z);
		blockToChange.setTypeId(12);
	}

	private Location nextIslandLocation(final Location lastIsland) {
		final int x = (int) lastIsland.getX();
		final int z = (int) lastIsland.getZ();
		final Location nextPos = lastIsland;
		if (x < z) {
			if (-1 * x < z) {
				nextPos.setX(nextPos.getX() + Settings.island_distance);
				return nextPos;
			}
			nextPos.setZ(nextPos.getZ() + Settings.island_distance);
			return nextPos;
		}
		if (x > z) {
			if (-1 * x >= z) {
				nextPos.setX(nextPos.getX() - Settings.island_distance);
				return nextPos;
			}
			nextPos.setZ(nextPos.getZ() - Settings.island_distance);
			return nextPos;
		}
		if (x <= 0) {
			nextPos.setZ(nextPos.getZ() + Settings.island_distance);
			return nextPos;
		}
		nextPos.setZ(nextPos.getZ() - Settings.island_distance);
		return nextPos;
	}

	public void oldGenerateIslandBlocks(final int x, final int z, final Player player, final World world) {
		final int y = Settings.island_height;

		for (int x_operate = x; x_operate < x + 3; x_operate++) {
			for (int y_operate = y; y_operate < y + 3; y_operate++) {
				for (int z_operate = z; z_operate < z + 6; z_operate++) {
					final Block blockToChange = world.getBlockAt(x_operate, y_operate, z_operate);
					blockToChange.setTypeId(2);
				}
			}
		}

		for (int x_operate = x + 3; x_operate < x + 6; x_operate++) {
			for (int y_operate = y; y_operate < y + 3; y_operate++) {
				for (int z_operate = z + 3; z_operate < z + 6; z_operate++) {
					final Block blockToChange = world.getBlockAt(x_operate, y_operate, z_operate);
					blockToChange.setTypeId(2);
				}
			}

		}

		for (int x_operate = x + 3; x_operate < x + 7; x_operate++) {
			for (int y_operate = y + 7; y_operate < y + 10; y_operate++) {
				for (int z_operate = z + 3; z_operate < z + 7; z_operate++) {
					final Block blockToChange = world.getBlockAt(x_operate, y_operate, z_operate);
					blockToChange.setTypeId(18);
				}
			}

		}

		for (int y_operate = y + 3; y_operate < y + 9; y_operate++) {
			final Block blockToChange = world.getBlockAt(x + 5, y_operate, z + 5);
			blockToChange.setTypeId(17);
		}

		Block blockToChange = world.getBlockAt(x + 1, y + 3, z + 1);
		blockToChange.setTypeId(54);
		final Chest chest = (Chest) blockToChange.getState();
		final Inventory inventory = chest.getInventory();
		inventory.clear();
		inventory.setContents(Settings.island_chestItems);
		if (Settings.island_addExtraItems) {
			for (final String island_extraPermission : Settings.island_extraPermissions) {
				if (VaultHandler.checkPerk(player.getName(), "usb." + island_extraPermission, player.getWorld())) {
					final String[] chestItemString = uSkyBlock.getInstance().getConfig().getString("options.island.extraPermissions." + island_extraPermission).split(" ");
					final ItemStack[] tempChest = new ItemStack[chestItemString.length];
					String[] amountdata = new String[2];
					for (int j = 0; j < chestItemString.length; j++) {
						amountdata = chestItemString[j].split(":");
						tempChest[j] = new ItemStack(Integer.parseInt(amountdata[0]), Integer.parseInt(amountdata[1]));
						inventory.addItem(new ItemStack[] { tempChest[j] });
					}
				}
			}
		}

		blockToChange = world.getBlockAt(x, y, z);
		blockToChange.setTypeId(7);

		blockToChange = world.getBlockAt(x + 2, y + 1, z + 1);
		blockToChange.setTypeId(12);
		blockToChange = world.getBlockAt(x + 2, y + 1, z + 2);
		blockToChange.setTypeId(12);
		blockToChange = world.getBlockAt(x + 2, y + 1, z + 3);
		blockToChange.setTypeId(12);
	}
	
	public void generateIslandBlocks(final int x, final int z, final Player player, final World world) {
		final int y = Settings.island_height;
		final Block blockToChange = world.getBlockAt(x, y, z);
		blockToChange.setTypeId(7);
		islandLayer1(x, z, player, world);
		islandLayer2(x, z, player, world);
		islandLayer3(x, z, player, world);
		islandLayer4(x, z, player, world);
		islandExtras(x, z, player, world);
	}
	
	private void createIsland() 
	{
		Location last = uSkyBlock.getInstance().getLastIsland();
		last.setY(Settings.island_height);
		try 
		{
			do 
			{
				uSkyBlock.getInstance().removeNextOrphan();

				if (!uSkyBlock.getInstance().hasOrphanedIsland())
					break;
			} while (uSkyBlock.getInstance().islandAtLocation(uSkyBlock.getInstance().checkOrphan()));

			while (uSkyBlock.getInstance().hasOrphanedIsland() && !uSkyBlock.getInstance().checkOrphan().getWorld().getName().equalsIgnoreCase(Settings.general_worldName))
				uSkyBlock.getInstance().removeNextOrphan();
			
			Location next;
			if (uSkyBlock.getInstance().hasOrphanedIsland() && !uSkyBlock.getInstance().islandAtLocation(uSkyBlock.getInstance().checkOrphan())) 
			{
				next = uSkyBlock.getInstance().getOrphanedIsland();
				uSkyBlock.getInstance().saveOrphans();
				uSkyBlock.getInstance().updateOrphans();
			} 
			else 
			{
				next = nextIslandLocation(last);
				uSkyBlock.getInstance().setLastIsland(next);

				while (uSkyBlock.getInstance().islandAtLocation(next))
					next = nextIslandLocation(next);

				uSkyBlock.getInstance().setLastIsland(next);
			}
			boolean hasIslandNow = false;

			if (uSkyBlock.getInstance().getSchemFile().length > 0 && Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) 
			{
				String cSchem = "";
				for (int i = 0; i < uSkyBlock.getInstance().getSchemFile().length; i++) 
				{
					if (!hasIslandNow) 
					{
						if (uSkyBlock.getInstance().getSchemFile()[i].getName().lastIndexOf('.') > 0)
							cSchem = uSkyBlock.getInstance().getSchemFile()[i].getName().substring(0, uSkyBlock.getInstance().getSchemFile()[i].getName().lastIndexOf('.'));
						else
							cSchem = uSkyBlock.getInstance().getSchemFile()[i].getName();

						if (VaultHandler.hasPerm(mPlayer, "usb.schematic." + cSchem)) 
						{
							if (WorldEditHandler.loadIslandSchematic(uSkyBlock.getSkyBlockWorld(), uSkyBlock.getInstance().getSchemFile()[i], next)) 
							{
								setChest(next, mPlayer);
								hasIslandNow = true;
							}
						}
					}
				}
				if (!hasIslandNow) 
				{
					for (int i = 0; i < uSkyBlock.getInstance().getSchemFile().length; i++) 
					{
						if (uSkyBlock.getInstance().getSchemFile()[i].getName().lastIndexOf('.') > 0)
							cSchem = uSkyBlock.getInstance().getSchemFile()[i].getName().substring(0, uSkyBlock.getInstance().getSchemFile()[i].getName().lastIndexOf('.'));
						else
							cSchem = uSkyBlock.getInstance().getSchemFile()[i].getName();
						
						if (cSchem.equalsIgnoreCase(Settings.island_schematicName)) 
						{
							if (WorldEditHandler.loadIslandSchematic(uSkyBlock.getSkyBlockWorld(), uSkyBlock.getInstance().getSchemFile()[i], next)) 
							{
								setChest(next, mPlayer);
								hasIslandNow = true;
							}
						}
					}
				}
			}
			
			if (!hasIslandNow) 
			{
				if (!Settings.island_useOldIslands) 
					generateIslandBlocks(next.getBlockX(), next.getBlockZ(), mPlayer, uSkyBlock.getSkyBlockWorld());
				else
					oldGenerateIslandBlocks(next.getBlockX(), next.getBlockZ(), mPlayer, uSkyBlock.getSkyBlockWorld());
			}
			setNewPlayerIsland(mPlayer, next);

			for(Entity tempent : mPlayer.getNearbyEntities(50.0D, 250.0D, 50.0D))
			{
				if (!(tempent instanceof Player))
					tempent.remove();
			}
			
			if (Settings.island_protectWithWorldGuard && Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) 
			{
				try
				{
					WorldGuardHandler.protectIsland(mPlayer.getName());
				}
				catch(IllegalArgumentException e)
				{
					mPlayer.sendMessage(ChatColor.RED + e.getMessage());
				}
				catch(IllegalStateException e)
				{
					e.printStackTrace();
					mPlayer.sendMessage(ChatColor.RED + "Could not create island. An error with WorldGuard occured. See console for details.");
					Misc.safeTeleport(mPlayer, Bukkit.getWorlds().get(0).getSpawnLocation());
				}
				
			}
		} catch (final Exception ex) 
		{
			mPlayer.sendMessage("Could not create your Island. Pleace contact a server moderator.");
			ex.printStackTrace();
		}
	}
	
	private void setNewPlayerIsland(final Player player, final Location loc) {
		uSkyBlock.getInstance().getPlayer(player.getName()).setHasIsland(true);
		uSkyBlock.getInstance().getPlayer(player.getName()).setIslandLocation(loc);

		player.teleport(getChestSpawnLoc(loc, player));
		uSkyBlock.getInstance().homeSet(player);
		uSkyBlock.getInstance().savePlayer(uSkyBlock.getInstance().getPlayer(player.getName()));
	}
	
	public Location getChestSpawnLoc(final Location loc, final Player player) {
		for (int x = -15; x <= 15; x++) {
			for (int y = -15; y <= 15; y++) {
				for (int z = -15; z <= 15; z++) {
					if (uSkyBlock.getSkyBlockWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z).getTypeId() == 54) {
						if (uSkyBlock.getSkyBlockWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z + 1).getTypeId() == 0 && uSkyBlock.getSkyBlockWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y - 1, loc.getBlockZ() + z + 1).getTypeId() != 0) {
							return new Location(uSkyBlock.getSkyBlockWorld(), loc.getBlockX() + x, loc.getBlockY() + y + 1, loc.getBlockZ() + z + 1);
						}
						if (uSkyBlock.getSkyBlockWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z - 1).getTypeId() == 0 && uSkyBlock.getSkyBlockWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y - 1, loc.getBlockZ() + z - 1).getTypeId() != 0) {
							return new Location(uSkyBlock.getSkyBlockWorld(), loc.getBlockX() + x, loc.getBlockY() + y + 1, loc.getBlockZ() + z + 1);
						}
						if (uSkyBlock.getSkyBlockWorld().getBlockAt(loc.getBlockX() + x + 1, loc.getBlockY() + y, loc.getBlockZ() + z).getTypeId() == 0 && uSkyBlock.getSkyBlockWorld().getBlockAt(loc.getBlockX() + x + 1, loc.getBlockY() + y - 1, loc.getBlockZ() + z).getTypeId() != 0) {
							return new Location(uSkyBlock.getSkyBlockWorld(), loc.getBlockX() + x, loc.getBlockY() + y + 1, loc.getBlockZ() + z + 1);
						}
						if (uSkyBlock.getSkyBlockWorld().getBlockAt(loc.getBlockX() + x - 1, loc.getBlockY() + y, loc.getBlockZ() + z).getTypeId() == 0 && uSkyBlock.getSkyBlockWorld().getBlockAt(loc.getBlockX() + x - 1, loc.getBlockY() + y - 1, loc.getBlockZ() + z).getTypeId() != 0) {
							return new Location(uSkyBlock.getSkyBlockWorld(), loc.getBlockX() + x, loc.getBlockY() + y + 1, loc.getBlockZ() + z + 1);
						}
						return loc;
					}
				}
			}
		}
		return loc;
	}
	
	public void setChest(final Location loc, final Player player) {
		for (int x = -15; x <= 15; x++) {
			for (int y = -15; y <= 15; y++) {
				for (int z = -15; z <= 15; z++) {
					if (uSkyBlock.getSkyBlockWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z).getTypeId() == 54) {
						final Block blockToChange = uSkyBlock.getSkyBlockWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
						final Chest chest = (Chest) blockToChange.getState();
						final Inventory inventory = chest.getInventory();
						inventory.clear();
						inventory.setContents(Settings.island_chestItems);
						if (Settings.island_addExtraItems) {
							for (final String island_extraPermission : Settings.island_extraPermissions) {
								if (VaultHandler.checkPerk(player.getName(), "usb." + island_extraPermission, player.getWorld())) {
									final String[] chestItemString = uSkyBlock.getInstance().getConfig().getString("options.island.extraPermissions." + island_extraPermission).split(" ");
									final ItemStack[] tempChest = new ItemStack[chestItemString.length];
									String[] amountdata = new String[2];
									for (int j = 0; j < chestItemString.length; j++) {
										amountdata = chestItemString[j].split(":");
										tempChest[j] = new ItemStack(Integer.parseInt(amountdata[0]), Integer.parseInt(amountdata[1]));
										inventory.addItem(new ItemStack[] { tempChest[j] });
									}
								}
							}
						}
					}
				}
			}
		}
	}

}
