package us.talabrek.ultimateskyblock.island;

import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.util.ItemStackUtil;
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
import us.talabrek.ultimateskyblock.player.Perk;
import us.talabrek.ultimateskyblock.player.PlayerPerk;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The factory for creating islands (actual blocks).
 */
@SuppressWarnings("deprecation")
public class IslandGenerator {
    private static final Logger log = Logger.getLogger(IslandGenerator.class.getName());
    private final File[] schemFiles;
    private final File netherSchematic;
    private final File directorySchematics;

    public IslandGenerator(File dataFolder, final FileConfiguration config) {
        directorySchematics = new File(dataFolder + File.separator + "schematics");
        if (!directorySchematics.exists()) {
            directorySchematics.mkdir();
        }
        CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
        if (codeSource != null) {
            URL jar = codeSource.getLocation();
            try (ZipInputStream zin = new ZipInputStream(jar.openStream())) {
                ZipEntry entry = null;
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
                log.log(Level.WARNING, "Unable to find schematics in jar", e);
            }
        }
        netherSchematic =  getSchematicFile(config.getString("nether.schematicName", "uSkyBlockNether"));
        this.schemFiles = directorySchematics.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String basename = FileUtil.getBasename(name);
                boolean enabled = config.getBoolean("island-schemes." + basename + ".enabled", true);
                return enabled && name != null && name.endsWith(".schematic") && !name.startsWith("uSkyBlock") && !basename.toLowerCase().endsWith("nether");
            }
        });
        if (this.schemFiles == null) {
            log.log(Level.INFO, "[uSkyBlock] No schematic file loaded.");
        } else {
            log.log(Level.INFO, "[uSkyBlock] " + this.schemFiles.length + " schematics loaded.");
        }
    }

    private boolean isSchematicFile(ZipEntry entry, String prefix) {
        return entry.getName().startsWith(prefix);
    }

    public List<String> getSchemeNames() {
        List<String> names = new ArrayList<>();
        for (File f : schemFiles) {
            names.add(FileUtil.getBasename(f));
        }
        Collections.sort(names);
        return names;
    }

    public boolean createIsland(final PlayerPerk playerPerk, final Location next, String cSchem) {
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
            World skyBlockNetherWorld = uSkyBlock.getInstance().getSkyBlockNetherWorld();
            if (skyBlockNetherWorld != null) {
                Location netherHome = new Location(skyBlockNetherWorld, next.getBlockX(), Settings.nether_height, next.getBlockZ());
                AsyncWorldEditHandler.loadIslandSchematic(netherFile, netherHome, playerPerk);
            }
            return true;
        } else {
            return false;
        }
    }

    private File getSchematicFile(String cSchem) {
        List<String> extensions = ClipboardFormats.getAll().stream().flatMap(f -> f.getFileExtensions().stream()).distinct().collect(Collectors.toList());
        return extensions.stream().map(f -> new File(directorySchematics, cSchem + "." + f)).filter(f -> f.exists() && f.canRead())
                .findFirst().orElse(null);
    }

    public boolean findAndSetChest(final Location loc, final Perk perk) {
        Location chestLocation = LocationUtil.findChestLocation(loc);
        return setChest(chestLocation, perk);
    }

    public boolean setChest(Location chestLocation, Perk perk) {
        if (chestLocation != null) {
            final Block block = chestLocation.getWorld().getBlockAt(chestLocation);
            if (block != null && block.getType() == Material.CHEST) {
                final Chest chest = (Chest) block.getState();
                final Inventory inventory = chest.getInventory();
                inventory.addItem(Settings.island_chestItems);
                if (Settings.island_addExtraItems) {
                    inventory.addItem(ItemStackUtil.createItemArray(perk.getExtraItems()));
                }
                return true;
            }
        }
        return false;
    }
}
