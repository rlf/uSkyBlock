package us.talabrek.ultimateskyblock.imports.name2uuid;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import us.talabrek.ultimateskyblock.imports.USBImporter;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.UUIDUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Converts from name-based files to UUID based.
 */
public class Name2UUIDImporter implements USBImporter {

    private Logger log;

    public static final FilenameFilter YML_FILES = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name != null && name.endsWith(".yml");
        }
    };

    private final Map<String, UUID> name2uuid = new HashMap<>();
    private uSkyBlock plugin;
    private FileHandler handler;

    @Override
    public String getName() {
        return "name2uuid";
    }

    @Override
    public boolean importFile(uSkyBlock plugin, File file) {
        if (file.getParentFile().getName().equalsIgnoreCase("players")) {
            return importPlayer(plugin, file);
        } else {
            return importIsland(plugin, file);
        }
    }

    private boolean importIsland(uSkyBlock plugin, File file) {
        log.info("Importing " + file);
        FileConfiguration config = new YamlConfiguration();
        plugin.readConfig(config, file);
        if (config.contains("party.leader") && !config.contains("party.leader.name")) {
            String leaderName = config.getString("party.leader");
            ConfigurationSection leaderSection = config.createSection("party.leader");
            leaderSection.set("name", leaderName);
            leaderSection.set("uuid", UUIDUtil.asString(getUUID(leaderName)));
        }
        ConfigurationSection members = config.getConfigurationSection("party.members");
        if (members != null) {
            for (String member : members.getKeys(false)) {
                ConfigurationSection section = members.getConfigurationSection(member);
                if (!section.contains("name")) {
                    members.set(member, null);
                    String uuid = UUIDUtil.asString(getUUID(member));
                    members.createSection(uuid, section.getValues(true));
                    members.set(uuid + ".name", member);
                } else {
                    log.info("Skipping " + member + ", already has a name");
                }
            }
        }
        try {
            config.save(file);
            return true;
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to import " + file, e);
            return false;
        }
    }

    private UUID getUUID(String name) {
        if (!name2uuid.containsKey(name)) {
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(name);
            if (player != null) {
                name2uuid.put(name, player.getUniqueId());
            }
        }
        return name2uuid.get(name);
    }

    private boolean importPlayer(uSkyBlock plugin, File file) {
        log.info("Importing " + file);
        String name = file.getName().substring(0, file.getName().lastIndexOf("."));
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(name);
        if (player == null) {
            // TODO: 04/01/2015 - R4zorax: Some report-logging to a .txt file would be nice
            return false;
        }
        UUID uniqueId = player.getUniqueId();
        name2uuid.put(player.getName(), uniqueId);
        if (uniqueId.toString().equals(name)) {
            log.info("Skipping, the filename is already a UUID");
            return true; // Skipped
        }
        File newConfig = new File(plugin.getDataFolder() + File.separator + "players", uniqueId.toString() + ".yml");
        if (file.renameTo(newConfig)) {
            FileConfiguration config = new YamlConfiguration();
            plugin.readConfig(config, newConfig);
            if (config.contains("player.uuid")) {
                log.info("Skipping, player.uuid already present");
                return true;
            }
            config.set("player.name", player.getName());
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
        }
        return false;
    }

    @Override
    public int importOrphans(uSkyBlock plugin, File configFolder) {
        return 0;
    }

    @Override
    public File[] getFiles(uSkyBlock plugin) {
        log = plugin.getLogger();
        try {
            if (handler == null) {
                handler = new FileHandler(plugin.getDataFolder() + File.separator + "name2uuid-report.log", true);
                handler.setFormatter(new SimpleFormatter());
            } else {
                log.removeHandler(handler);
            }
            log.addHandler(handler);
            log.setUseParentHandlers(false);
        } catch (IOException e) {
            log.severe("Unable to create file-logging to a report.log file");
        }
        name2uuid.clear();
        this.plugin = plugin;
        File[] playerFiles = new File(plugin.getDataFolder(), "players").listFiles(YML_FILES);
        File[] islandFiles = new File(plugin.getDataFolder(), "islands").listFiles(YML_FILES);
        File[] files = new File[islandFiles.length + playerFiles.length];
        System.arraycopy(playerFiles, 0, files, 0, playerFiles.length);
        System.arraycopy(islandFiles, 0, files, playerFiles.length, islandFiles.length);
        return files;
    }

    @Override
    public void completed(uSkyBlock plugin, int success, int failed) {
        if (handler != null) {
            handler.close();
        }
    }
}
