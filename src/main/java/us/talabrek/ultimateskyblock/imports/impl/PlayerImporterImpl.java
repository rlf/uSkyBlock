package us.talabrek.ultimateskyblock.imports.impl;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.imports.USBImporter;
import us.talabrek.ultimateskyblock.imports.wolfwork.WolfWorkUSBImporter;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;

/**
 * Tries all available importers on a file.
 */
public class PlayerImporterImpl {
    private final uSkyBlock plugin;
    private List<USBImporter> importers;

    public PlayerImporterImpl(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    public boolean importPlayer(File playerFile) {
        for (USBImporter importer : getImporters()) {
            if (importer.importPlayer(plugin, playerFile)) {
                return true;
            }
        }
        return false;
    }

    private List<USBImporter> getImporters() {
        if (importers == null) {
            importers = new ArrayList<>();
            importers.add(new WolfWorkUSBImporter());
            ServiceLoader serviceLoader = ServiceLoader.load(USBImporter.class, getClass().getClassLoader());
            for (Iterator<USBImporter> it = serviceLoader.iterator(); it.hasNext(); ) {
                importers.add(it.next());
            }
        }
        return importers;
    }

    public void importUSB(final CommandSender sender) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                doImport(sender);
            }
        });
    }

    private void doImport(CommandSender sender) {
        String msg = "Imported " + importOrphans(plugin, plugin.getDataFolder()) + " orphans";
        sender.sendMessage(ChatColor.YELLOW + msg);
        plugin.log(Level.INFO, msg);
        int countSuccess = 0;
        int countFailed = 0;
        for (File playerFile : plugin.directoryPlayers.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name != null && !name.endsWith(".yml");
            }
        })) {
            if (importPlayer(playerFile)) {
                countSuccess++;
                plugin.log(Level.INFO, "Successfully imported player-file " + playerFile);
                if (!playerFile.delete()) {
                    playerFile.deleteOnExit();
                }
            } else {
                countFailed++;
                plugin.log(Level.WARNING, "Could not import player-file " + playerFile);
            }
        }
        sender.sendMessage(ChatColor.YELLOW + "Converted " + countSuccess + "/" + (countSuccess+countFailed) + " players");
    }

    private int importOrphans(uSkyBlock plugin, File dataFolder) {
        int count = 0;
        for (USBImporter importer : getImporters()) {
            count += importer.importOrphans(plugin, dataFolder);
        }
        return count;
    }
}
