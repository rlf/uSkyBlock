package us.talabrek.ultimateskyblock.imports.name2uuid;

import dk.lockfuglsang.minecraft.file.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.imports.USBImporter;
import us.talabrek.ultimateskyblock.mojang.MojangAPI;
import us.talabrek.ultimateskyblock.mojang.NameUUIDConsumer;
import us.talabrek.ultimateskyblock.mojang.ProgressCallback;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.ProgressTracker;
import dk.lockfuglsang.minecraft.util.TimeUtil;
import us.talabrek.ultimateskyblock.util.UUIDUtil;
import us.talabrek.ultimateskyblock.uuid.PlayerDB;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Converts from name-based files to UUID based.
 */
public class Name2UUIDImporter implements USBImporter {

    private Logger log;

    public static final FilenameFilter YML_FILES = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            // Don't include UUID converted files
            return name != null && name.endsWith(".yml") && !name.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}.yml");
        }
    };

    private uSkyBlock plugin;
    private FileHandler handler;
    private PlayerDB playerDB;
    private File[] playerFiles;
    private File playerErrorFolder;
    private File islandErrorFolder;
    private Set<String> invalidNames = new HashSet<>();

    @Override
    public String getName() {
        return "name2uuid";
    }

    @Override
    public void init(uSkyBlock plugin) {
        this.plugin = plugin;
        playerDB = plugin.getPlayerDB();
        plugin.setMaintenanceMode(true);
        log = plugin.getLogger();
        try {
            if (handler == null) {
                handler = new FileHandler(plugin.getDataFolder() + File.separator + "name2uuid-report.log", true);
                handler.setFormatter(new SingleLineFormatter());
            } else {
                log.removeHandler(handler);
            }
            log.addHandler(handler);
            log.setUseParentHandlers(false);
        } catch (IOException e) {
            log.severe("Unable to create file-logging to a report.log file");
        }
        log.info("===================================");
        log.info("=== Running name2uuid importer");
        log.info("===================================");
    }

    @Override
    public Boolean importFile(File file) {
        if (file.getParentFile().getName().equalsIgnoreCase("players")) {
            return importPlayer(file);
        } else if (file.getParentFile().getName().equalsIgnoreCase("islands")) {
            return importIsland(file);
        } else if (file.getName().equalsIgnoreCase("uuid2name.yml")) {
            return importPlayerDB();
        }
        return null;
    }

    private Boolean importPlayerDB() {
        final long tStart = System.currentTimeMillis();
        double progressEveryPct = plugin.getConfig().getDouble("importer.progressEveryPct", 10);
        long progressEveryMs = plugin.getConfig().getLong("importer.progressEveryMs", 30000);

        List<String> names = new ArrayList<>();
        for (File f : playerFiles) {
            names.add(FileUtil.getBasename(f));
        }
        final PlayerDB playerDB = plugin.getPlayerDB();
        Bukkit.getConsoleSender().sendMessage(tr("\u00a77PlayerDB: Filtering {0} players from uuid2name.yml", names.size()));
        ProgressTracker dbtracker = new ProgressTracker(Bukkit.getConsoleSender(),
                marktr("\u00a77 - {0,number,##}% ({1}/{2}) ~ {3}"),
                progressEveryPct, progressEveryMs);
        int cnt = 0;
        int total = names.size();
        for (Iterator<String> it = names.iterator(); it.hasNext(); ) {
            String name = it.next();
            if (playerDB.getUUIDFromName(name, false) != null) {
                cnt++;
                it.remove();
            }
            if (cnt % 20 == 0) {
                dbtracker.progressUpdate(cnt, total, TimeUtil.millisAsString(System.currentTimeMillis() - tStart));
            }
        }
        Bukkit.getConsoleSender().sendMessage(tr("\u00a77PlayerDB: Filtered {0} names", cnt));

        final ProgressTracker tracker = new ProgressTracker(Bukkit.getConsoleSender(),
                marktr("\u00a77 - MojangAPI:{4}: {0,number,##}% ({1}/{2}, failed:{3} ~ {5,number,##}%), {6}"),
                progressEveryPct, progressEveryMs);
        MojangAPI mojangAPI = new MojangAPI();
        Bukkit.getConsoleSender().sendMessage(tr("\u00a77MojangAPI: Trying to fetch {0} players from Mojang", names.size()));

        // This call blocks - good thing we are in Async mode
        mojangAPI.fetchUUIDs(names, new NameUUIDConsumer() {
            @Override
            public void success(Map<String, UUID> names) {
                for (Map.Entry<String, UUID> entry : names.entrySet()) {
                    playerDB.updatePlayer(entry.getValue(), entry.getKey(), null);
                }
            }

            @Override
            public void renamed(String oldName, String newName, UUID id) {
                playerDB.updatePlayer(id, oldName, null);
                playerDB.updatePlayer(id, newName, null);
            }

            @Override
            public void unknown(List<String> unknownNames) {
                invalidNames.addAll(unknownNames);
            }
        }, new ProgressCallback() {
            @Override
            public void progress(int progress, int failed, int total, String message) {
                tracker.progressUpdate(progress, total, failed, message,
                        100f * failed / (progress > 0 ? progress : 1), TimeUtil.millisAsString(System.currentTimeMillis() - tStart));
            }

            @Override
            public void complete(boolean success) {
                Bukkit.getConsoleSender().sendMessage(tr("\u00a77 - MojangAPI:\u00a7aCOMPLETED: {0}", success ? tr("SUCCESS") : tr("FAILED")));
            }

            @Override
            public void error(String message) {
                Bukkit.getConsoleSender().sendMessage(tr("\u00a77 - MojangAPI:\u00a7cERROR: {0}", message));
            }
        });
        return true;
    }

    private Boolean importPlayer(File file) {
        log.info("Importing " + file);
        String name = FileUtil.getBasename(file);
        FileConfiguration config = new YamlConfiguration();
        FileUtil.readConfig(config, file);
        UUID uniqueId;
        if (invalidNames.contains(name)) {
            uniqueId = UUIDUtil.fromString(config.getString("player.uuid", null));
            if (uniqueId != null) {
                invalidNames.remove(name);
                playerDB.updatePlayer(uniqueId, name, null);
            }
        } else {
            uniqueId = playerDB.getUUIDFromName(name);
        }
        if (uniqueId == null) {
            log.info("No UUID found for " + name);
            file.renameTo(new File(playerErrorFolder, file.getName()));
            return false;
        }
        File newConfig = new File(plugin.getDataFolder() + File.separator + "players", uniqueId.toString() + ".yml");
        if (file.renameTo(newConfig)) {
            FileUtil.readConfig(config, newConfig);
            config.set("player.name", name);
            config.set("player.uuid", UUIDUtil.asString(uniqueId));
            try {
                config.save(newConfig);
                if (!newConfig.getName().equals(file.getName())) {
                    if (file.exists() && !file.delete()) {
                        file.deleteOnExit();
                    }
                }
                return true;
            } catch (IOException e) {
                log.log(Level.SEVERE, "Failed!", e);
                return false;
            }
        } else if (newConfig.exists()) {
            log.info("Unable to move " + file + " to " + newConfig + " since it already exists!");
            file.renameTo(new File(newConfig.getParent(), newConfig.getName() + ".old"));
        }
        return false;
    }

    private Boolean importIsland(File file) {
        log.info("Importing " + file);
        FileConfiguration config = new YamlConfiguration();
        FileUtil.readConfig(config, file);
        if (config.getInt("version", 0) >= 3) {
            log.info("- island already converted, version is " + config.getInt("version"));
            return null;
        }
        if (config.contains("party.leader")) {
            String leaderName = config.getString("party.leader", null);
            if (invalidNames.contains(leaderName)) {
                log.info("Island leader had no UUID, removing island " + file);
                file.renameTo(new File(islandErrorFolder, file.getName()));
                String islandName = FileUtil.getBasename(file);
                plugin.getOrphanLogic().addOrphan(islandName);
                WorldGuardHandler.removeIslandRegion(islandName);
                return false;
            }
            config.set("party.leader-uuid", getUUIDString(leaderName));
        }
        ConfigurationSection members = config.getConfigurationSection("party.members");
        if (members != null) {
            for (String member : members.getKeys(false)) {
                ConfigurationSection section = members.getConfigurationSection(member);
                members.set(member, null);
                String uuid = getUUIDString(member);
                if (uuid != null) {
                    members.createSection(uuid, section.getValues(true));
                    members.set(uuid + ".name", member);
                }
            }
        }
        List<String> bans = config.getStringList("banned.list");
        List<String> newBans = new ArrayList<>();
        for (String name : bans) {
            String uuid = getUUIDString(name);
            if (uuid != null) {
                newBans.add(uuid);
            }
        }
        config.set("banned.list", newBans);
        List<String> trusts = config.getStringList("trust.list");
        List<String> newTrusts = new ArrayList<>();
        for (String name : trusts) {
            String uuid = getUUIDString(name);
            if (uuid != null) {
                newTrusts.add(uuid);
            }
        }
        config.set("trust.list", newTrusts);
        config.set("version", 3);
        try {
            config.save(file);
            return true;
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to import " + file, e);
            return false;
        }
    }

    @Override
    public File[] getFiles() {
        File playerFolder = new File(plugin.getDataFolder(), "players");
        playerErrorFolder = new File(playerFolder, "errors");
        if (!playerErrorFolder.exists()) {
            playerErrorFolder.mkdirs();
        }
        playerFiles = playerFolder.listFiles(YML_FILES);
        File islandFolder = new File(plugin.getDataFolder(), "islands");
        islandErrorFolder = new File(islandFolder, "errors");
        if (!islandErrorFolder.exists()) {
            islandErrorFolder.mkdirs();
        }
        File[] islandFiles = islandFolder.listFiles(YML_FILES);
        File[] files = new File[islandFiles.length + playerFiles.length + 1];
        files[0] = new File(plugin.getDataFolder(), "uuid2name.yml");
        System.arraycopy(playerFiles, 0, files, 1, playerFiles.length);
        System.arraycopy(islandFiles, 0, files, playerFiles.length + 1, islandFiles.length);
        return files;
    }

    @Override
    public void completed(int success, int failed, int skipped) {
        if (handler != null) {
            handler.close();
            log.removeHandler(handler);
            log.setUseParentHandlers(true);
        }
        plugin.setMaintenanceMode(false);
    }

    private String getUUIDString(String name) {
        return invalidNames.contains(name) ? null : UUIDUtil.asString(getUUID(name));
    }

    private UUID getUUID(String playerName) {
        return playerDB.getUUIDFromName(playerName);
    }

    public static class SingleLineFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            try {
                return String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %2$5s : %3$s\n",
                        new Date(record.getMillis()),
                        record.getLevel().getName(),
                        record.getMessage());
            } catch (IllegalArgumentException e) {
                return String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %2$s : %3$s\n",
                        new Date(record.getMillis()),
                        record.getLevel().getName(),
                        MessageFormat.format(record.getMessage(), record.getParameters()));
            }
        }
    }
}
