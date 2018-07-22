package us.talabrek.ultimateskyblock.imports;

import dk.lockfuglsang.minecraft.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.imports.challenges.ConfigPre13Importer;
import us.talabrek.ultimateskyblock.imports.fixuuidleader.UUIDLeaderImporter;
import us.talabrek.ultimateskyblock.imports.name2uuid.Name2UUIDImporter;
import us.talabrek.ultimateskyblock.imports.update.USBUpdateImporter;
import us.talabrek.ultimateskyblock.imports.wolfwork.WolfWorkUSBImporter;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.ProgressTracker;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;
import static us.talabrek.ultimateskyblock.util.LogUtil.log;

/**
 * Delegates and batches the import.
 */
public class USBImporterExecutor {
    private final uSkyBlock plugin;
    private final ProgressTracker progressTracker;
    private List<USBImporter> importers;
    private volatile long tStart;
    private volatile int countSuccess;
    private volatile int countSkip;
    private volatile int countFailed;

    public USBImporterExecutor(uSkyBlock plugin) {
        this.plugin = plugin;
        double progressEveryPct = plugin.getConfig().getDouble("importer.progressEveryPct", 10);
        long progressEveryMs = plugin.getConfig().getLong("importer.progressEveryMs", 10000);
        progressTracker = new ProgressTracker(Bukkit.getConsoleSender(), marktr("\u00a7eProgress: {0,number,##}% ({1}/{2} - success:{3}, failed:{4}, skipped:{5}) ~ {6}"), progressEveryPct, progressEveryMs);
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
            importers.add(new UUIDLeaderImporter());
            importers.add(new USBUpdateImporter());
            importers.add(new Name2UUIDImporter());
            importers.add(new ConfigPre13Importer());
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
            sender.sendMessage(tr("\u00a74No importer named \u00a7e{0}\u00a74 found", name));
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
        tStart = System.currentTimeMillis();
        importer.init(plugin);
        countSuccess = 0;
        countFailed = 0;
        countSkip = 0;
        final File[] files = importer.getFiles();
        log(Level.INFO, "Importing " + files.length + " files");
        if (files.length > 0) {
            doImport(sender, importer, files);
        } else {
            complete(sender, importer);
        }
    }

    private void doImport(final CommandSender sender, final USBImporter importer, final File[] files) {
        try {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                try {
                    Boolean status = importer.importFile(file);
                    if (status == null) {
                        countSkip++;
                        log(Level.FINE, "Successfully skipped file " + file);
                    } else if (status) {
                        countSuccess++;
                        log(Level.FINE, "Successfully imported file " + file);
                    } else {
                        countFailed++;
                        log(Level.WARNING, "Could not import file " + file);
                    }
                } catch (Throwable t) {
                    countFailed++;
                    log(Level.WARNING, "Could not import file " + file, t);
                }
                progressTracker.progressUpdate(countSuccess + countFailed + countSkip, files.length,
                        countSuccess, countFailed, countSkip,
                        TimeUtil.millisAsString(System.currentTimeMillis()-tStart));
            }
        } finally {
            complete(sender, importer);
        }
    }

    private void complete(CommandSender sender, USBImporter importer) {
        importer.completed(countSuccess, countFailed, countSkip);
        sender.sendMessage(tr("\u00a7eConverted {0}/{1} files in {2}",
                countSuccess, (countSuccess + countFailed), TimeUtil.millisAsString(System.currentTimeMillis() - tStart)));
        plugin.getConfig().set("importer." + importer.getName() + ".imported", true);
        plugin.saveConfig();
    }
}
