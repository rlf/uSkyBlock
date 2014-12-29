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
    private volatile int countSuccess;
    private volatile int countFailed;

    public PlayerImporterImpl(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    public List<String> getImporterNames() {
        List<String> result = new ArrayList<>();
        for (USBImporter importer : getImporters()) {
            result.add(importer.getName());
        }
        return result;
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

    public USBImporter getImporter(String name) {
        for (USBImporter importer : getImporters()) {
            if (name.equalsIgnoreCase(importer.getName())) {
                return importer;
            }
        }
        return null;
    }

    public void importUSB(final CommandSender sender, String name) {
        if (name == null || sender == null) {
            throw new IllegalArgumentException("sender and name must be non-null");
        }
        final USBImporter importer = getImporter(name);
        if (importer == null) {
            sender.sendMessage("\u00a74No importer named \u00a7e" + name + "\u00a74 found");
            return;
        }
        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                doImport(sender, importer);
            }
        });
    }

    private void doImport(CommandSender sender, USBImporter importer) {
        String msg = "Imported " + importer.importOrphans(plugin, plugin.getDataFolder()) + " orphans";
        sender.sendMessage("\u00a7e" + msg);
        plugin.log(Level.INFO, msg);
        countSuccess = 0;
        countFailed = 0;
        final File[] files = plugin.directoryPlayers.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name != null && !name.endsWith(".yml");
            }
        });
        int chunkSize = plugin.getConfig().getInt("general.import.maxChunk", 100);
        int delay = plugin.getConfig().getInt("general.import.delay", 15);
        plugin.log(Level.INFO, "Importing " + files.length + " players in chunks of " + chunkSize);
        if (files.length > 0) {
            doImport(sender, importer, files, 0, chunkSize, delay);
        }
    }

    private void doImport(final CommandSender sender, final USBImporter importer, final File[] files, final int offset, final int chunkSize, final int delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                int count = 0;
                int failed = 0;
                for (int i = offset; i < files.length && i < offset+chunkSize; i++) {
                    File playerFile = files[i];
                    if (importer.importPlayer(plugin, playerFile)) {
                        count++;
                        plugin.log(Level.FINE, "Successfully imported player-file " + playerFile);
                        if (!playerFile.delete()) {
                            playerFile.deleteOnExit();
                        }
                    } else {
                        failed++;
                        plugin.log(Level.WARNING, "Could not import player-file " + playerFile);
                    }
                }
                countSuccess += count;
                countFailed += failed;
                float progress = 100f*(countSuccess+countFailed)/files.length;
                sender.sendMessage(String.format("\u00a7eProgress: %02f%% (%d/%d - success:%d, failed:%d)", progress, countFailed+countSuccess, files.length, countSuccess, countFailed));
                if (offset+chunkSize < files.length) {
                    doImport(sender, importer, files, offset + chunkSize, chunkSize, delay);
                } else {
                    sender.sendMessage("\u00a7eConverted " + countSuccess + "/" + (countSuccess + countFailed) + " players");
                }
            }
        }, delay);
    }
}
