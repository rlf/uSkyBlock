package us.talabrek.ultimateskyblock.util;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Common file-utilities.
 */
public enum FileUtil {;
    private static final Logger log = Logger.getLogger(FileUtil.class.getName());

    public static void readConfig(FileConfiguration config, File configFile) {
        if (configFile == null || !configFile.exists()) {
            log.log(Level.INFO, "No "  + configFile + " found, it will be created");
            return;
        }
        try (Reader rdr = new InputStreamReader(new FileInputStream(configFile), "UTF-8")) {
            config.load(rdr);
        } catch (InvalidConfigurationException | IOException e) {
            log.log(Level.SEVERE, "Unable to read config file " + configFile, e);
        }
    }

    public static void readConfig(FileConfiguration config, InputStream inputStream) {
        if (inputStream == null) {
            return;
        }
        try (Reader rdr = new InputStreamReader(inputStream, "UTF-8")) {
            config.load(rdr);
        } catch (InvalidConfigurationException | IOException e) {
            log.log(Level.SEVERE, "Unable to read configuration", e);
        }
    }

    public static FilenameFilter createYmlFilenameFilter() {
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name != null && name.endsWith(".yml");
            }
        };
    }

    public static String getBasename(String file) {
        if (file != null && file.lastIndexOf('.') != -1) {
            return file.substring(0, file.lastIndexOf('.'));
        }
        return file;
    }
}
