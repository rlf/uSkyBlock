package us.talabrek.ultimateskyblock.island;

import dk.lockfuglsang.minecraft.util.ItemStackUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.AsyncWorldEditHandler;
import us.talabrek.ultimateskyblock.player.Perk;
import us.talabrek.ultimateskyblock.player.PlayerPerk;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.FileUtil;
import us.talabrek.ultimateskyblock.util.LocationUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The factory for creating islands (actual blocks).
 */
public class IslandGenerator {
    private static final Logger log = Logger.getLogger(IslandGenerator.class.getName());
    private final File[] schemFiles;
    private final File netherSchematic;
    private final File directorySchematics;

    public IslandGenerator(@NotNull File dataFolder, @NotNull FileConfiguration config) {
        directorySchematics = new File(dataFolder + File.separator + "schematics");
        if (!directorySchematics.exists()) {
            //noinspection ResultOfMethodCallIgnored
            directorySchematics.mkdir();
        }
        copySchematicsFromJar();
        netherSchematic =  getSchematicFile(config.getString("nether.schematicName", "uSkyBlockNether"));

        schemFiles = loadSchematics(config);
        if (schemFiles == null) {
            log.info("[uSkyBlock] No schematic file loaded.");
        } else {
            log.info("[uSkyBlock] " + schemFiles.length + " schematics loaded.");
        }
    }

    /**
     * Gets a {@link List} of available schematic names.
     * @return List of available schematic names.
     */
    public List<String> getSchemeNames() {
        List<String> names = new ArrayList<>();
        for (File f : schemFiles) {
            names.add(FileUtil.getBasename(f));
        }
        Collections.sort(names);
        return names;
    }

    /**
     * Generate an island at the given {@link Location}.
     * @param playerPerk PlayerPerk object for the island owner.
     * @param next Location to generate an island.
     * @param cSchem New island schematic.
     * @return True if the island was generated, false otherwise.
     */
    public boolean createIsland(@NotNull PlayerPerk playerPerk, @NotNull Location next, @Nullable String cSchem) {
        // Hacky, but clear the Orphan info
        next.setYaw(0);
        next.setPitch(0);
        next.setY((double) Settings.island_height);
        File schemFile = getSchematicFile(cSchem != null ? cSchem : "default");
        File netherFile = getSchematicFile(cSchem != null ? cSchem + "Nether" : "uSkyBlockNether");
        if (netherFile == null) {
            netherFile = netherSchematic;
        }
        if (schemFile.exists() && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
            AsyncWorldEditHandler.loadIslandSchematic(schemFile, next, playerPerk);
            World skyBlockNetherWorld = uSkyBlock.getInstance().getWorldManager().getNetherWorld();
            if (skyBlockNetherWorld != null) {
                Location netherHome = new Location(skyBlockNetherWorld, next.getBlockX(), Settings.nether_height, next.getBlockZ());
                AsyncWorldEditHandler.loadIslandSchematic(netherFile, netherHome, playerPerk);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Find the nearest chest at the given {@link Location} and fill the chest with the starter and {@link Perk}
     * based items.
     * @param location Location to search for a chest.
     * @param perk Perk containing extra perk-based items to add.
     * @return True if the chest is found and filled, false otherwise.
     */
    public boolean findAndSetChest(@NotNull Location location, @NotNull Perk perk) {
        Location chestLocation = LocationUtil.findChestLocation(location);
        return setChest(chestLocation, perk);
    }

    /**
     * Fill the {@link Inventory} of the given chest {@link Location} with the starter and {@link Perk} based items.
     * @param chestLocation Location of the chest block.
     * @param perk Perk containing extra perk-based items to add.
     * @return True if the chest is found and filled, false otherwise.
     */
    public boolean setChest(@Nullable Location chestLocation, @NotNull Perk perk) {
        if (chestLocation == null || chestLocation.getWorld() == null) {
            return false;
        }

        final Block block = chestLocation.getWorld().getBlockAt(chestLocation);
        if (block.getType() == Material.CHEST) {
            final Chest chest = (Chest) block.getState();
            final Inventory inventory = chest.getInventory();
            inventory.addItem(Settings.getIslandChestItems().toArray(new ItemStack[0]));
            if (Settings.island_addExtraItems) {
                inventory.addItem(ItemStackUtil.createItemArray(perk.getExtraItems()));
            }
            return true;
        }
        return false;
    }

    /**
     * Copy all schematics in the plugins JAR-file to the plugins data folder.
     */
    private void copySchematicsFromJar() {
        CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            return;
        }

        URL jar = codeSource.getLocation();
        try (ZipInputStream zin = new ZipInputStream(jar.openStream())) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                String prefix = "schematics/";
                if (isSchematicFile(entry, prefix)) {
                    File f = new File(directorySchematics + File.separator + entry.getName().substring(prefix.length()));
                    if (!f.exists()) {
                        try (InputStream inputStream = uSkyBlock.class.getClassLoader().getResourceAsStream(entry.getName())) {
                            FileUtil.copy(inputStream, f);
                        } catch (IOException e) {
                            log.log(Level.WARNING, "Unable to load schematic " + entry.getName(), e);
                        }
                    }
                }
                zin.closeEntry();
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to find schematics in plugin JAR", e);
        }
    }

    private File[] loadSchematics(@NotNull FileConfiguration config) {
        return directorySchematics.listFiles((dir, name) -> {
            String basename = FileUtil.getBasename(name);
            boolean enabled = config.getBoolean("island-schemes." + basename + ".enabled", true);
            return enabled &&
                    name != null &&
                    (name.endsWith(".schematic") || name.endsWith(".schem")) &&
                    !name.startsWith("uSkyBlock") &&
                    !basename.toLowerCase().endsWith("nether");
        });
    }

    private File getSchematicFile(String cSchem) {
        List<String> extensions = Arrays.asList("schematic", "schem");
        return extensions.stream()
                .map(f -> new File(directorySchematics, cSchem + "." + f))
                .filter(f -> f.exists() && f.canRead())
                .findFirst()
                .orElse(null);
    }

    private boolean isSchematicFile(ZipEntry entry, String prefix) {
        return entry.getName().startsWith(prefix);
    }
}
