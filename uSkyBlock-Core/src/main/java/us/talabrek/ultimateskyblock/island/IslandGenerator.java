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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The factory for creating islands (actual blocks).
 */
@SuppressWarnings("deprecation")
public class IslandGenerator {
    private static final List<String> USB_SCHEMATICS = Arrays.asList(
            "uSkyBlockNether",
            "uSkyBlockDefault",
            "uSkyBlockSkySMP"
    );
    private static final Logger log = Logger.getLogger(IslandGenerator.class.getName());
    private final File[] schemFiles;
    private final File netherSchematic;
    private final File directorySchematics;

    public IslandGenerator(File dataFolder, FileConfiguration config) {
        directorySchematics = new File(dataFolder + File.separator + "schematics");
        if (!directorySchematics.exists()) {
            directorySchematics.mkdir();
        }
        netherSchematic = new File(directorySchematics, config.getString("nether.schematicName", "uSkyBlockNether") + ".schematic");
        for (String schem : USB_SCHEMATICS) {
            File f = new File(directorySchematics, schem + ".schematic");
            if (!f.exists()) {
                try (InputStream inputStream = uSkyBlock.class.getClassLoader().getResourceAsStream("schematics/" + schem + ".schematic")) {
                    FileUtil.copy(inputStream, f);
                } catch (IOException e) {
                    log.log(Level.WARNING, "Unable to load schematic " + schem, e);
                }
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
        Collections.sort(names);
        return names;
    }

    public boolean createIsland(uSkyBlock plugin, final PlayerPerk playerPerk, final Location next, String cSchem) {
        // Hacky, but clear the Orphan info
        next.setYaw(0);
        next.setPitch(0);
        next.setY((double) Settings.island_height);
        File schemFile = new File(directorySchematics, (cSchem != null ? cSchem : "uSkyBlockDefault") + ".schematic");
        if (schemFile.exists() && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
            AsyncWorldEditHandler.loadIslandSchematic(schemFile, next, playerPerk);
            World skyBlockNetherWorld = uSkyBlock.getInstance().getSkyBlockNetherWorld();
            if (skyBlockNetherWorld != null) {
                Location netherHome = new Location(skyBlockNetherWorld, next.getBlockX(), Settings.nether_height, next.getBlockZ());
                AsyncWorldEditHandler.loadIslandSchematic(netherSchematic, netherHome, playerPerk);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean setChest(final Location loc, final PlayerPerk playerPerk) {
        Location chestLocation = LocationUtil.findChestLocation(loc);
        if (chestLocation != null) {
            final Block block = chestLocation.getWorld().getBlockAt(chestLocation);
            if (block != null && block.getType() == Material.CHEST) {
                final Chest chest = (Chest) block.getState();
                final Inventory inventory = chest.getInventory();
                inventory.addItem(Settings.island_chestItems);
                if (Settings.island_addExtraItems) {
                    inventory.addItem(ItemStackUtil.createItemArray(playerPerk.getPerk().getExtraItems()));
                }
                return true;
            }

        }
        return false;
    }
}
