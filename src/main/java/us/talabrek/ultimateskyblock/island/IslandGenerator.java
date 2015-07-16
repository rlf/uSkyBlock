package us.talabrek.ultimateskyblock.island;

import java.io.File;
import java.util.HashSet;
import java.util.logging.Logger;
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
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.ItemStackUtil;

/**
 * The factory for creating islands (actual blocks).
 */
@SuppressWarnings("deprecation")
public class IslandGenerator {
    private static final String CN = IslandGenerator.class.getName();
    private static final Logger log = Logger.getLogger(IslandGenerator.class.getName());
    private final FileConfiguration config;
    private final File[] schemFiles;

    public IslandGenerator(File dataFolder, FileConfiguration config) {
        this.config = config;
        File directorySchematics = new File(dataFolder + File.separator + "schematics");
        if (!directorySchematics.exists()) {
            directorySchematics.mkdir();
        }
        this.schemFiles = directorySchematics.listFiles();
        if (this.schemFiles == null) {
            System.out.print("[uSkyBlock] No schematic file loaded.");
        } else {
            System.out.print("[uSkyBlock] " + this.schemFiles.length + " schematics loaded.");
        }
    }

    public class PlayerIslandCreationData {
        private final PlayerInfo playerInfo;
        private HashSet<String> allowAccessSchematics = new HashSet<String>();
        private HashSet<String> allowedExtraItemPermissions = new HashSet<String>();
        private HashSet<String> allowedBiomePermissions = new HashSet<String>();

        public PlayerIslandCreationData(PlayerInfo playerInfo) {
            this.playerInfo = playerInfo;
        }

        public PlayerInfo getPlayerInfo() {
            return this.playerInfo;
        }

        protected void allowSchematicPermission(String schematic) {
            this.allowAccessSchematics.add(schematic);
        }

        protected boolean hasSchematicPermission(String schematic) {
            return this.allowAccessSchematics.contains(schematic);
        }

        protected void allowExtraItemPermission(String permission) {
            this.allowedExtraItemPermissions.add(permission);
        }

        protected boolean hasExtraItemPermission(String permission) {
            return this.allowedExtraItemPermissions.contains(permission);
        }

        protected void allowBiomePermission(String permission) {
            this.allowedBiomePermissions.add(permission);
        }

        public boolean hasBiomePermission(String permission) {
            return this.allowedBiomePermissions.contains(permission);
        }
    }

    public PlayerIslandCreationData preCreateData(Player player, PlayerInfo playerInfo) {
        PlayerIslandCreationData playerIslandCreationData = new PlayerIslandCreationData(playerInfo);

        for (File schemFile : schemFiles) {
            // First run-through - try to set the island the player has permission for.
            String cSchem = schemFile.getName();
            if (cSchem.lastIndexOf('.') > 0) {
                cSchem = cSchem.substring(0, cSchem.lastIndexOf('.'));
            }
            if (VaultHandler.checkPerk(player.getName(), "usb.schematic." + cSchem, uSkyBlock.skyBlockWorld)) {
                playerIslandCreationData.allowSchematicPermission(cSchem);
            }
        }

        for (String perm : Settings.island_extraPermissions) {
            if (VaultHandler.checkPerk(player.getName(), "usb." + perm, uSkyBlock.skyBlockWorld)) {
                playerIslandCreationData.allowExtraItemPermission(perm);
            }
        }

        for (String biome : uSkyBlock.getInstance().getValidBiomes().keySet()) {
            if (VaultHandler.checkPerk(player.getName(), "usb.biome." + biome, uSkyBlock.skyBlockWorld)) {
                playerIslandCreationData.allowBiomePermission(biome);
            }
        }

        return playerIslandCreationData;
    }

    public void createIsland(uSkyBlock plugin, PlayerIslandCreationData playerIslandCreationData, Location next) {
        log.entering(CN, "createIsland", new Object[]{plugin, playerIslandCreationData.getPlayerInfo().getPlayerName(), next});
        log.fine("creating island for " + playerIslandCreationData.getPlayerInfo().getPlayerName() + " at " + next);
        boolean hasIslandNow = false;
        if (schemFiles.length > 0 && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
            for (File schemFile : schemFiles) {
                // First run-through - try to set the island the player has permission for.
                String cSchem = schemFile.getName();
                if (cSchem.lastIndexOf('.') > 0) {
                    cSchem = cSchem.substring(0, cSchem.lastIndexOf('.'));
                }
                if (playerIslandCreationData.hasSchematicPermission(cSchem)
                        && WorldEditHandler.loadIslandSchematic(uSkyBlock.skyBlockWorld, schemFile, next)) {
                    setChest(next, playerIslandCreationData);
                    hasIslandNow = true;
                    log.fine("chose schematic " + cSchem + " due to permission.");
                    break;
                }
            }
            if (!hasIslandNow) {
                for (File schemFile : schemFiles) {
                    // 2nd Run through, set the default set schematic (if found).
                    String cSchem = schemFile.getName();
                    if (cSchem.lastIndexOf('.') > 0) {
                        cSchem = cSchem.substring(0, cSchem.lastIndexOf('.'));
                    }
                    if (cSchem.equalsIgnoreCase(Settings.island_schematicName)
                            && WorldEditHandler.loadIslandSchematic(uSkyBlock.skyBlockWorld, schemFile, next)) {
                        setChest(next, playerIslandCreationData);
                        hasIslandNow = true;
                        log.fine("chose schematic " + cSchem);
                        break;
                    }
                }
            }
        }
        if (!hasIslandNow) {
            if (!Settings.island_useOldIslands) {
                log.fine("generating a uSkyBlock default island");
                generateIslandBlocks(next.getBlockX(), next.getBlockZ(), playerIslandCreationData, uSkyBlock.skyBlockWorld);
            } else {
                log.fine("generating a skySMP island");
                oldGenerateIslandBlocks(next.getBlockX(), next.getBlockZ(), playerIslandCreationData, uSkyBlock.skyBlockWorld);
            }
            hasIslandNow = true;
        }
        next.setY((double) Settings.island_height);

        log.exiting(CN, "createIsland");
    }

    public void generateIslandBlocks(final int x, final int z, final PlayerIslandCreationData playerIslandCreationData, final World world) {
        final int y = Settings.island_height;
        final Block blockToChange = world.getBlockAt(x, y, z);
        blockToChange.setTypeIdAndData(7, (byte) 0, false);
        this.islandLayer1(x, z, world);
        this.islandLayer2(x, z, world);
        this.islandLayer3(x, z, world);
        this.islandLayer4(x, z, world);
        this.islandExtras(x, z, playerIslandCreationData, world);
    }

    public void oldGenerateIslandBlocks(final int x, final int z, final PlayerIslandCreationData playerIslandCreationData, final World world) {
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
        setChest(chest.getLocation(), playerIslandCreationData);
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

    private void islandExtras(final int x, final int z, final PlayerIslandCreationData playerIslandCreationData, final World world) {
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
        final Chest chest = (Chest) blockToChange.getState();
        setChest(chest.getLocation(), playerIslandCreationData);
    }

    public void setChest(final Location loc, final PlayerIslandCreationData playerIslandCreationData) {
        World world = loc.getWorld();
        for (int dx = 1; dx <= 30; dx++) {
            for (int dy = 1; dy <= 30; dy++) {
                for (int dz = 1; dz <= 30; dz++) {
                    int x = ((dx % 2) == 0) ? dx / 2 : -dx / 2;
                    int y = ((dy % 2) == 0) ? dy / 2 : -dy / 2;
                    int z = ((dz % 2) == 0) ? dz / 2 : -dz / 2;
                    if (world.getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z).getTypeId() == 54) {
                        final Block blockToChange = world.getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
                        final Chest chest = (Chest) blockToChange.getState();
                        final Inventory inventory = chest.getInventory();
                        inventory.clear();
                        inventory.setContents(Settings.island_chestItems);
                        if (Settings.island_addExtraItems) {
                            for (String perm : Settings.island_extraPermissions) {
                                if (playerIslandCreationData.hasExtraItemPermission(perm)) {
                                    String itemString = config.getString("options.island.extraPermissions." + perm);
                                    if (itemString == null || itemString.isEmpty()) {
                                        continue;
                                    }
                                    ItemStack[] itemArray = ItemStackUtil.createItemArray(itemString);
                                    inventory.addItem(itemArray);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
