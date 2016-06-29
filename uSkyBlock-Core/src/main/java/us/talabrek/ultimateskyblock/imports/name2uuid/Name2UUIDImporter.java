package us.talabrek.ultimateskyblock.imports.name2uuid;

import dk.lockfuglsang.minecraft.file.FileUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import us.talabrek.ultimateskyblock.imports.USBImporter;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.UUIDUtil;
import us.talabrek.ultimateskyblock.uuid.PlayerDB;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

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
        } else {
            return importIsland(file);
        }
    }

    private Boolean importIsland(File file) {
        log.info("Importing " + file);
        FileConfiguration config = new YamlConfiguration();
        FileUtil.readConfig(config, file);
        if (config.getInt("version", 0) >= 2) {
            log.info("- island already converted, version is " + config.getInt("version"));
            return null;
        }
        if (config.contains("party.leader")) {
            String leaderName = config.getString("party.leader", null);
            config.set("party.leader-uuid", getUUIDString(leaderName));
        }
        ConfigurationSection members = config.getConfigurationSection("party.members");
        if (members != null) {
            for (String member : members.getKeys(false)) {
                ConfigurationSection section = members.getConfigurationSection(member);
                if (!section.contains("name")) {
                    members.set(member, null);
                    String uuid = getUUIDString(member);
                    members.createSection(uuid, section.getValues(true));
                    members.set(uuid + ".name", member);
                } else {
                    log.info("Skipping " + member + ", already has a name");
                }
            }
        }
        List<String> bans = config.getStringList("banned.list");
        List<String> newBans = new ArrayList<>();
        for (String name : bans) {
            newBans.add(getUUIDString(name));
        }
        config.set("banned.list", newBans);
        List<String> trusts = config.getStringList("trust.list");
        List<String> newTrusts = new ArrayList<>();
        for (String name : trusts) {
            newTrusts.add(getUUIDString(name));
        }
        config.set("trust.list", newTrusts);
        config.set("version", 2);
        try {
            config.save(file);
            return true;
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to import " + file, e);
            return false;
        }
    }

    private String getUUIDString(String name) {
        return UUIDUtil.asString(getUUID(name));
    }

    private UUID getUUID(String playerName) {
        return playerDB.getUUIDFromName(playerName);
    }

    private Boolean importPlayer(File file) {
        log.info("Importing " + file);
        String name = FileUtil.getBasename(file);
        UUID uniqueId = playerDB.getUUIDFromName(name);
        if (uniqueId == null) {
            log.info("No UUID found for " + name);
            return false;
        }
        if (uniqueId.toString().equals(name)) {
            log.info("Skipping, the filename is already a UUID");
            return null; // Skipped
        }
        File newConfig = new File(plugin.getDataFolder() + File.separator + "players", uniqueId.toString() + ".yml");
        if (file.renameTo(newConfig)) {
            FileConfiguration config = new YamlConfiguration();
            FileUtil.readConfig(config, newConfig);
            if (config.contains("player.uuid")) {
                log.info("Skipping, player.uuid already present");
                return true;
            }
            config.set("player.name", name);
            config.set("player.uuid", UUIDUtil.asString(uniqueId));
            // TODO: 04/01/2015 - R4zorax: Move the challenges now we're at it...
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

    @Override
    public File[] getFiles() {
        File[] playerFiles = new File(plugin.getDataFolder(), "players").listFiles(YML_FILES);
        File[] islandFiles = new File(plugin.getDataFolder(), "islands").listFiles(YML_FILES);
        File[] files = new File[islandFiles.length + playerFiles.length];
        System.arraycopy(playerFiles, 0, files, 0, playerFiles.length);
        System.arraycopy(islandFiles, 0, files, playerFiles.length, islandFiles.length);
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
