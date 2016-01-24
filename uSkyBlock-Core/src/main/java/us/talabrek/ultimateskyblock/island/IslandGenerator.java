package us.talabrek.ultimateskyblock.island;

import dk.lockfuglsang.minecraft.file.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.AsyncWorldEditHandler;
import us.talabrek.ultimateskyblock.player.PlayerPerk;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.ItemStackUtil;
import us.talabrek.ultimateskyblock.util.LocationUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The factory for creating islands (actual blocks).
 */
@SuppressWarnings("deprecation")
public class IslandGenerator {
    private static final String CN = IslandGenerator.class.getName();
    private static final Logger log = Logger.getLogger(IslandGenerator.class.getName());
    private final File[] schemFiles;
    private final File netherSchematic;

    public IslandGenerator(File dataFolder, FileConfiguration config) {
        File directorySchematics = new File(dataFolder + File.separator + "schematics");
        if (!directorySchematics.exists()) {
            directorySchematics.mkdir();
        }
        netherSchematic = new File(directorySchematics, config.getString("nether.schematicName", "uSkyBlockNether") + ".schematic");
        if (!netherSchematic.exists()) {
            try (InputStream inputStream = uSkyBlock.class.getClassLoader().getResourceAsStream("schematics/uSkyBlockNether.schematic")) {
                FileUtil.copy(inputStream, netherSchematic);
            } catch (IOException e) {
                log.log(Level.WARNING, "Unable to load nether-schematic " + netherSchematic, e);
            }
        }
        this.schemFiles = directorySchematics.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name != null && name.endsWith(".schematic") && !name.startsWith("uSkyBlockNether");
            }
        });
        if (this.schemFiles == null) {
            log.log(Level.INFO, "[uSkyBlock] No schematic file loaded.");
        } else {
            log.log(Level.INFO, "[uSkyBlock] " + this.schemFiles.length + " schematics loaded.");
        }
    }

    public List<String> getSchemeNames() {
        List<String> names = new ArrayList<>();
        for (File f : schemFiles) {
            names.add(FileUtil.getBasename(f));
        }
        return names;
    }

    public void createIsland(uSkyBlock plugin, final PlayerPerk playerPerk, final Location next) {
        log.entering(CN, "createIsland", new Object[]{plugin, playerPerk.getPlayerInfo().getPlayerName(), next});
        log.fine("creating island for " + playerPerk.getPlayerInfo().getPlayerName() + " at " + next);
        // Hacky, but clear the Orphan info
        next.setYaw(0);
        next.setPitch(0);
        boolean hasIslandNow = false;
        String cSchem = "default";
        if (schemFiles.length > 0 && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
            File permFile = null;
            File defaultFile = null;
            for (File schemFile : schemFiles) {
                // First run-through - try to set the island the player has permission for.
                cSchem = FileUtil.getBasename(schemFile);
                if (cSchem.equalsIgnoreCase(Settings.island_schematicName)) {
                    defaultFile = schemFile;
                }
                if (permFile == null && playerPerk.getPerk().getSchematics().contains(cSchem)) {
                    permFile = schemFile;
                }
            }
            if (permFile != null) {
                defaultFile = permFile;
            }
            if (defaultFile != null) {
                log.fine("chose schematic " + defaultFile);
                AsyncWorldEditHandler.loadIslandSchematic(defaultFile, next, playerPerk);
                hasIslandNow = true;
            }
        }
        if (!hasIslandNow) {
            if (!Settings.island_useOldIslands) {
                log.fine("generating a uSkyBlock default island");
                generateIslandBlocks(next.getBlockX(), next.getBlockZ(), playerPerk, uSkyBlock.skyBlockWorld);
            } else {
                log.fine("generating a skySMP island");
                oldGenerateIslandBlocks(next.getBlockX(), next.getBlockZ(), playerPerk, uSkyBlock.skyBlockWorld);
            }
        }
        next.setY((double) Settings.island_height);
        World skyBlockNetherWorld = uSkyBlock.getInstance().getSkyBlockNetherWorld();
        if (skyBlockNetherWorld != null) {
            int netherY = plugin.getConfig().getInt("nether.height", Settings.island_height/2);
            Location netherHome = new Location(skyBlockNetherWorld, next.getBlockX(), netherY, next.getBlockZ());
            AsyncWorldEditHandler.loadIslandSchematic(netherSchematic, netherHome, playerPerk);
        }
        log.exiting(CN, "createIsland");
    }

    public void generateIslandBlocks(final int x, final int z, final PlayerPerk playerPerk, final World world) {
        final int y = Settings.island_height;
        final Block blockToChange = world.getBlockAt(x, y, z);
        blockToChange.setTypeIdAndData(7, (byte) 0, false);
        this.islandLayer1(x, z, world);
        this.islandLayer2(x, z, world);
        this.islandLayer3(x, z, world);
        this.islandLayer4(x, z, world);
        this.islandExtras(x, z, playerPerk, world);
    }

    public void oldGenerateIslandBlocks(final int x, final int z, final PlayerPerk playerPerk, final World world) {
        final int y = Settings.island_height;
        for (int x_operate = x; x_operate < x + 3; ++x_operate) {
            for (int y_operate = y; y_operate < y + 3; ++y_operate) {
                for (int z_operate = z; z_operate < z + 6; ++z_operate) {
                    final Block blockToChange = world.getBlockAt(x_operate, y_operate, z_operate);
                    blockToChange.setTypeIdAndData(2, (byte) 0, false);
                }
            }
        }
        for (int x_operate = x + 3; x_operate < x + 6; ++x_operate) {
            for (int y_operate = y; y_operate < y + 3; ++y_operate) {
                for (int z_operate = z + 3; z_operate < z + 6; ++z_operate) {
                    final Block blockToChange = world.getBlockAt(x_operate, y_operate, z_operate);
                    blockToChange.setTypeIdAndData(2, (byte) 0, false);
                }
            }
        }
        for (int x_operate = x + 3; x_operate < x + 7; ++x_operate) {
            for (int y_operate = y + 7; y_operate < y + 10; ++y_operate) {
                for (int z_operate = z + 3; z_operate < z + 7; ++z_operate) {
                    final Block blockToChange = world.getBlockAt(x_operate, y_operate, z_operate);
                    blockToChange.setTypeIdAndData(18, (byte) 0, false);
                }
            }
        }
        for (int y_operate2 = y + 3; y_operate2 < y + 9; ++y_operate2) {
            final Block blockToChange2 = world.getBlockAt(x + 5, y_operate2, z + 5);
            blockToChange2.setTypeIdAndData(17, (byte) 0, false);
        }
        Block blockToChange3 = world.getBlockAt(x + 1, y + 3, z + 1);
        blockToChange3.setTypeIdAndData(54, (byte) 0, false);
        final Chest chest = (Chest) blockToChange3.getState();
        blockToChange3 = world.getBlockAt(x, y, z);
        blockToChange3.setTypeIdAndData(7, (byte) 0, false);
        blockToChange3 = world.getBlockAt(x + 2, y + 1, z + 1);
        blockToChange3.setTypeIdAndData(12, (byte) 0, false);
        blockToChange3 = world.getBlockAt(x + 2, y + 1, z + 2);
        blockToChange3.setTypeIdAndData(12, (byte) 0, false);
        blockToChange3 = world.getBlockAt(x + 2, y + 1, z + 3);
        blockToChange3.setTypeIdAndData(12, (byte) 0, false);
    }

    private void islandLayer1(final int x, final int z, final World world) {
        int y = Settings.island_height + 4;
        for (int x_operate = x - 3; x_operate <= x + 3; ++x_operate) {
            for (int z_operate = z - 3; z_operate <= z + 3; ++z_operate) {
                final Block blockToChange = world.getBlockAt(x_operate, y, z_operate);
                blockToChange.setTypeIdAndData(2, (byte) 0, false);
            }
        }
        Block blockToChange2 = world.getBlockAt(x - 3, y, z + 3);
        blockToChange2.setTypeIdAndData(0, (byte) 0, false);
        blockToChange2 = world.getBlockAt(x - 3, y, z - 3);
        blockToChange2.setTypeIdAndData(0, (byte) 0, false);
        blockToChange2 = world.getBlockAt(x + 3, y, z - 3);
        blockToChange2.setTypeIdAndData(0, (byte) 0, false);
        blockToChange2 = world.getBlockAt(x + 3, y, z + 3);
        blockToChange2.setTypeIdAndData(0, (byte) 0, false);
    }

    private void islandLayer2(final int x, final int z, final World world) {
        int y = Settings.island_height + 3;
        for (int x_operate = x - 2; x_operate <= x + 2; ++x_operate) {
            for (int z_operate = z - 2; z_operate <= z + 2; ++z_operate) {
                final Block blockToChange = world.getBlockAt(x_operate, y, z_operate);
                blockToChange.setTypeIdAndData(3, (byte) 0, false);
            }
        }
        Block blockToChange2 = world.getBlockAt(x - 3, y, z);
        blockToChange2.setTypeIdAndData(3, (byte) 0, false);
        blockToChange2 = world.getBlockAt(x + 3, y, z);
        blockToChange2.setTypeIdAndData(3, (byte) 0, false);
        blockToChange2 = world.getBlockAt(x, y, z - 3);
        blockToChange2.setTypeIdAndData(3, (byte) 0, false);
        blockToChange2 = world.getBlockAt(x, y, z + 3);
        blockToChange2.setTypeIdAndData(3, (byte) 0, false);
        blockToChange2 = world.getBlockAt(x, y, z);
        blockToChange2.setTypeIdAndData(12, (byte) 0, false);
    }

    private void islandLayer3(final int x, final int z, final World world) {
        int y = Settings.island_height + 2;
        for (int x_operate = x - 1; x_operate <= x + 1; ++x_operate) {
            for (int z_operate = z - 1; z_operate <= z + 1; ++z_operate) {
                final Block blockToChange = world.getBlockAt(x_operate, y, z_operate);
                blockToChange.setTypeIdAndData(3, (byte) 0, false);
            }
        }
        Block blockToChange2 = world.getBlockAt(x - 2, y, z);
        blockToChange2.setTypeIdAndData(3, (byte) 0, false);
        blockToChange2 = world.getBlockAt(x + 2, y, z);
        blockToChange2.setTypeIdAndData(3, (byte) 0, false);
        blockToChange2 = world.getBlockAt(x, y, z - 2);
        blockToChange2.setTypeIdAndData(3, (byte) 0, false);
        blockToChange2 = world.getBlockAt(x, y, z + 2);
        blockToChange2.setTypeIdAndData(3, (byte) 0, false);
        blockToChange2 = world.getBlockAt(x, y, z);
        blockToChange2.setTypeIdAndData(12, (byte) 0, false);
    }

    private void islandLayer4(final int x, final int z, final World world) {
        int y = Settings.island_height + 1;
        Block blockToChange = world.getBlockAt(x - 1, y, z);
        blockToChange.setTypeIdAndData(3, (byte) 0, false);
        blockToChange = world.getBlockAt(x + 1, y, z);
        blockToChange.setTypeIdAndData(3, (byte) 0, false);
        blockToChange = world.getBlockAt(x, y, z - 1);
        blockToChange.setTypeIdAndData(3, (byte) 0, false);
        blockToChange = world.getBlockAt(x, y, z + 1);
        blockToChange.setTypeIdAndData(3, (byte) 0, false);
        blockToChange = world.getBlockAt(x, y, z);
        blockToChange.setTypeIdAndData(12, (byte) 0, false);
    }

    private void islandExtras(final int x, final int z, final PlayerPerk playerPerk, final World world) {
        int y = Settings.island_height;
        Block blockToChange = world.getBlockAt(x, y + 5, z);
        blockToChange.setTypeIdAndData(17, (byte) 0, false);
        blockToChange = world.getBlockAt(x, y + 6, z);
        blockToChange.setTypeIdAndData(17, (byte) 0, false);
        blockToChange = world.getBlockAt(x, y + 7, z);
        blockToChange.setTypeIdAndData(17, (byte) 0, false);
        y = Settings.island_height + 8;
        for (int x_operate = x - 2; x_operate <= x + 2; ++x_operate) {
            for (int z_operate = z - 2; z_operate <= z + 2; ++z_operate) {
                blockToChange = world.getBlockAt(x_operate, y, z_operate);
                blockToChange.setTypeIdAndData(18, (byte) 0, false);
            }
        }
        blockToChange = world.getBlockAt(x + 2, y, z + 2);
        blockToChange.setTypeIdAndData(0, (byte) 0, false);
        blockToChange = world.getBlockAt(x + 2, y, z - 2);
        blockToChange.setTypeIdAndData(0, (byte) 0, false);
        blockToChange = world.getBlockAt(x - 2, y, z + 2);
        blockToChange.setTypeIdAndData(0, (byte) 0, false);
        blockToChange = world.getBlockAt(x - 2, y, z - 2);
        blockToChange.setTypeIdAndData(0, (byte) 0, false);
        blockToChange = world.getBlockAt(x, y, z);
        blockToChange.setTypeIdAndData(17, (byte) 0, false);
        y = Settings.island_height + 9;
        for (int x_operate = x - 1; x_operate <= x + 1; ++x_operate) {
            for (int z_operate = z - 1; z_operate <= z + 1; ++z_operate) {
                blockToChange = world.getBlockAt(x_operate, y, z_operate);
                blockToChange.setTypeIdAndData(18, (byte) 0, false);
            }
        }
        blockToChange = world.getBlockAt(x - 2, y, z);
        blockToChange.setTypeIdAndData(18, (byte) 0, false);
        blockToChange = world.getBlockAt(x + 2, y, z);
        blockToChange.setTypeIdAndData(18, (byte) 0, false);
        blockToChange = world.getBlockAt(x, y, z - 2);
        blockToChange.setTypeIdAndData(18, (byte) 0, false);
        blockToChange = world.getBlockAt(x, y, z + 2);
        blockToChange.setTypeIdAndData(18, (byte) 0, false);
        blockToChange = world.getBlockAt(x, y, z);
        blockToChange.setTypeIdAndData(17, (byte) 0, false);
        y = Settings.island_height + 10;
        blockToChange = world.getBlockAt(x - 1, y, z);
        blockToChange.setTypeIdAndData(18, (byte) 0, false);
        blockToChange = world.getBlockAt(x + 1, y, z);
        blockToChange.setTypeIdAndData(18, (byte) 0, false);
        blockToChange = world.getBlockAt(x, y, z - 1);
        blockToChange.setTypeIdAndData(18, (byte) 0, false);
        blockToChange = world.getBlockAt(x, y, z + 1);
        blockToChange.setTypeIdAndData(18, (byte) 0, false);
        blockToChange = world.getBlockAt(x, y, z);
        blockToChange.setTypeIdAndData(17, (byte) 0, false);
        blockToChange = world.getBlockAt(x, y + 1, z);
        blockToChange.setTypeIdAndData(18, (byte) 0, false);
        blockToChange = world.getBlockAt(x, Settings.island_height + 5, z + 1);
        blockToChange.setTypeIdAndData(54, (byte) 3, false);
    }

    public boolean setChest(final Location loc, final PlayerPerk playerPerk) {
        Location chestLocation = LocationUtil.findChestLocation(loc);
        if (chestLocation != null) {
            final Block block = chestLocation.getWorld().getBlockAt(chestLocation);
            if (block != null && block.getType() == Material.CHEST) {
                final Chest chest = (Chest) block.getState();
                final Inventory inventory = chest.getInventory();
                inventory.clear();
                inventory.setContents(Settings.island_chestItems);
                if (Settings.island_addExtraItems) {
                    inventory.addItem(ItemStackUtil.createItemArray(playerPerk.getPerk().getExtraItems()));
                }
                return true;
            }

        }
        return false;
    }
}
