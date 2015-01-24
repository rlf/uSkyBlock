package us.talabrek.ultimateskyblock.island;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.ItemStackUtil;

import java.io.File;

/**
 * The factory for creating islands (actual blocks).
 */
@SuppressWarnings("deprecation")
public class IslandGenerator {

    private final FileConfiguration config;

    public IslandGenerator(FileConfiguration config) {
        this.config = config;
    }

    public void createIsland(uSkyBlock plugin, Player player, Location next) {
        boolean hasIslandNow = false;
        if (uSkyBlock.getInstance().getSchemFile().length > 0 && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
            for (File schemFile : plugin.getSchemFile()) {
                // First run-through - try to set the island the player has permission for.
                String cSchem = schemFile.getName();
                if (cSchem.lastIndexOf('.') > 0) {
                    cSchem = cSchem.substring(0, cSchem.lastIndexOf('.'));
                }
                if (VaultHandler.checkPerk(player.getName(), "usb.schematic." + cSchem, uSkyBlock.skyBlockWorld)
                        && WorldEditHandler.loadIslandSchematic(player, uSkyBlock.skyBlockWorld, schemFile, next)) {
                    plugin.setChest(next, player);
                    hasIslandNow = true;
                    break;
                }
            }
            if (!hasIslandNow) {
                for (File schemFile : plugin.getSchemFile()) {
                    // 2nd Run through, set the default set schematic (if found).
                    String cSchem = schemFile.getName();
                    if (cSchem.lastIndexOf('.') > 0) {
                        cSchem = cSchem.substring(0, cSchem.lastIndexOf('.'));
                    }
                    if (cSchem.equalsIgnoreCase(Settings.island_schematicName)
                            && WorldEditHandler.loadIslandSchematic(player, uSkyBlock.skyBlockWorld, schemFile, next)) {
                        plugin.setChest(next, player);
                        hasIslandNow = true;
                        break;
                    }
                }
            }
        }
        if (!hasIslandNow) {
            if (!Settings.island_useOldIslands) {
                generateIslandBlocks(next.getBlockX(), next.getBlockZ(), player, uSkyBlock.skyBlockWorld);
            } else {
                oldGenerateIslandBlocks(next.getBlockX(), next.getBlockZ(), player, uSkyBlock.skyBlockWorld);
            }
        }
        next.setY((double) Settings.island_height);
    }

    public void generateIslandBlocks(final int x, final int z, final Player player, final World world) {
        final int y = Settings.island_height;
        final Block blockToChange = world.getBlockAt(x, y, z);
        blockToChange.setTypeId(7);
        this.islandLayer1(x, z, player, world);
        this.islandLayer2(x, z, player, world);
        this.islandLayer3(x, z, player, world);
        this.islandLayer4(x, z, player, world);
        this.islandExtras(x, z, player, world);
    }

    public void oldGenerateIslandBlocks(final int x, final int z, final Player player, final World world) {
        final int y = Settings.island_height;
        for (int x_operate = x; x_operate < x + 3; ++x_operate) {
            for (int y_operate = y; y_operate < y + 3; ++y_operate) {
                for (int z_operate = z; z_operate < z + 6; ++z_operate) {
                    final Block blockToChange = world.getBlockAt(x_operate, y_operate, z_operate);
                    blockToChange.setTypeId(2);
                }
            }
        }
        for (int x_operate = x + 3; x_operate < x + 6; ++x_operate) {
            for (int y_operate = y; y_operate < y + 3; ++y_operate) {
                for (int z_operate = z + 3; z_operate < z + 6; ++z_operate) {
                    final Block blockToChange = world.getBlockAt(x_operate, y_operate, z_operate);
                    blockToChange.setTypeId(2);
                }
            }
        }
        for (int x_operate = x + 3; x_operate < x + 7; ++x_operate) {
            for (int y_operate = y + 7; y_operate < y + 10; ++y_operate) {
                for (int z_operate = z + 3; z_operate < z + 7; ++z_operate) {
                    final Block blockToChange = world.getBlockAt(x_operate, y_operate, z_operate);
                    blockToChange.setTypeId(18);
                }
            }
        }
        for (int y_operate2 = y + 3; y_operate2 < y + 9; ++y_operate2) {
            final Block blockToChange2 = world.getBlockAt(x + 5, y_operate2, z + 5);
            blockToChange2.setTypeId(17);
        }
        Block blockToChange3 = world.getBlockAt(x + 1, y + 3, z + 1);
        blockToChange3.setTypeId(54);
        final Chest chest = (Chest) blockToChange3.getState();
        final Inventory inventory = chest.getInventory();
        inventory.clear();
        inventory.setContents(Settings.island_chestItems);
        if (Settings.island_addExtraItems) {
            for (int i = 0; i < Settings.island_extraPermissions.length; ++i) {
                if (VaultHandler.checkPerk(player.getName(), "usb." + Settings.island_extraPermissions[i], player.getWorld())) {
                    final String[] chestItemString = config.getString("options.island.extraPermissions." + Settings.island_extraPermissions[i]).split(" ");
                    final ItemStack[] tempChest = new ItemStack[chestItemString.length];
                    String[] amountdata;
                    for (int j = 0; j < chestItemString.length; ++j) {
                        amountdata = chestItemString[j].split(":");
                        tempChest[j] = new ItemStack(Integer.parseInt(amountdata[0], 10), Integer.parseInt(amountdata[1], 10));
                        inventory.addItem(new ItemStack[]{tempChest[j]});
                    }
                }
            }
        }
        blockToChange3 = world.getBlockAt(x, y, z);
        blockToChange3.setTypeId(7);
        blockToChange3 = world.getBlockAt(x + 2, y + 1, z + 1);
        blockToChange3.setTypeId(12);
        blockToChange3 = world.getBlockAt(x + 2, y + 1, z + 2);
        blockToChange3.setTypeId(12);
        blockToChange3 = world.getBlockAt(x + 2, y + 1, z + 3);
        blockToChange3.setTypeId(12);
    }

    private void islandLayer1(final int x, final int z, final Player player, final World world) {
        int y = Settings.island_height + 4;
        for (int x_operate = x - 3; x_operate <= x + 3; ++x_operate) {
            for (int z_operate = z - 3; z_operate <= z + 3; ++z_operate) {
                final Block blockToChange = world.getBlockAt(x_operate, y, z_operate);
                blockToChange.setTypeId(2);
            }
        }
        Block blockToChange2 = world.getBlockAt(x - 3, y, z + 3);
        blockToChange2.setTypeId(0);
        blockToChange2 = world.getBlockAt(x - 3, y, z - 3);
        blockToChange2.setTypeId(0);
        blockToChange2 = world.getBlockAt(x + 3, y, z - 3);
        blockToChange2.setTypeId(0);
        blockToChange2 = world.getBlockAt(x + 3, y, z + 3);
        blockToChange2.setTypeId(0);
    }

    private void islandLayer2(final int x, final int z, final Player player, final World world) {
        int y = Settings.island_height + 3;
        for (int x_operate = x - 2; x_operate <= x + 2; ++x_operate) {
            for (int z_operate = z - 2; z_operate <= z + 2; ++z_operate) {
                final Block blockToChange = world.getBlockAt(x_operate, y, z_operate);
                blockToChange.setTypeId(3);
            }
        }
        Block blockToChange2 = world.getBlockAt(x - 3, y, z);
        blockToChange2.setTypeId(3);
        blockToChange2 = world.getBlockAt(x + 3, y, z);
        blockToChange2.setTypeId(3);
        blockToChange2 = world.getBlockAt(x, y, z - 3);
        blockToChange2.setTypeId(3);
        blockToChange2 = world.getBlockAt(x, y, z + 3);
        blockToChange2.setTypeId(3);
        blockToChange2 = world.getBlockAt(x, y, z);
        blockToChange2.setTypeId(12);
    }

    private void islandLayer3(final int x, final int z, final Player player, final World world) {
        int y = Settings.island_height + 2;
        for (int x_operate = x - 1; x_operate <= x + 1; ++x_operate) {
            for (int z_operate = z - 1; z_operate <= z + 1; ++z_operate) {
                final Block blockToChange = world.getBlockAt(x_operate, y, z_operate);
                blockToChange.setTypeId(3);
            }
        }
        Block blockToChange2 = world.getBlockAt(x - 2, y, z);
        blockToChange2.setTypeId(3);
        blockToChange2 = world.getBlockAt(x + 2, y, z);
        blockToChange2.setTypeId(3);
        blockToChange2 = world.getBlockAt(x, y, z - 2);
        blockToChange2.setTypeId(3);
        blockToChange2 = world.getBlockAt(x, y, z + 2);
        blockToChange2.setTypeId(3);
        blockToChange2 = world.getBlockAt(x, y, z);
        blockToChange2.setTypeId(12);
    }

    private void islandLayer4(final int x, final int z, final Player player, final World world) {
        int y = Settings.island_height + 1;
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

    private void islandExtras(final int x, final int z, final Player player, final World world) {
        int y = Settings.island_height;
        Block blockToChange = world.getBlockAt(x, y + 5, z);
        blockToChange.setTypeId(17);
        blockToChange = world.getBlockAt(x, y + 6, z);
        blockToChange.setTypeId(17);
        blockToChange = world.getBlockAt(x, y + 7, z);
        blockToChange.setTypeId(17);
        y = Settings.island_height + 8;
        for (int x_operate = x - 2; x_operate <= x + 2; ++x_operate) {
            for (int z_operate = z - 2; z_operate <= z + 2; ++z_operate) {
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
        for (int x_operate = x - 1; x_operate <= x + 1; ++x_operate) {
            for (int z_operate = z - 1; z_operate <= z + 1; ++z_operate) {
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
        blockToChange.setData((byte) 3);
        final Chest chest = (Chest) blockToChange.getState();
        final Inventory inventory = chest.getInventory();
        inventory.clear();
        inventory.setContents(Settings.island_chestItems);
        if (Settings.island_addExtraItems) {
            for (int i = 0; i < Settings.island_extraPermissions.length; ++i) {
                if (VaultHandler.checkPerk(player.getName(), "usb." + Settings.island_extraPermissions[i], player.getWorld())) {
                    final String chestItemString = config.getString("options.island.extraPermissions." + Settings.island_extraPermissions[i], "");
                    inventory.addItem(ItemStackUtil.createItemArray(chestItemString));
                }
            }
        }
    }

}
