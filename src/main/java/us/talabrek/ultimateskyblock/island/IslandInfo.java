package us.talabrek.ultimateskyblock.island;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.File;

/**
 * Data object for an island
 */
public class IslandInfo {
    private static File directory = new File(".");

    private final FileConfiguration config;

    public IslandInfo(String islandName) {
        config = new YamlConfiguration();
        uSkyBlock.readConfig(config, new File(directory, islandName + ".yml"));
    }

    public static void setDirectory(File dir) {
        directory = dir;
    }
}
