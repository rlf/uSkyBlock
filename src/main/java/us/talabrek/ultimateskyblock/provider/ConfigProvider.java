package us.talabrek.ultimateskyblock.provider;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Inversion of responsibility - allows for reload etc.
 */
public interface ConfigProvider {
    FileConfiguration getConfig();
}
