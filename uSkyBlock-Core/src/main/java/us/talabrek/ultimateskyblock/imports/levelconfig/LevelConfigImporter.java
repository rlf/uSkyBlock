package us.talabrek.ultimateskyblock.imports.levelconfig;

import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import us.talabrek.ultimateskyblock.imports.USBImporter;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.File;

public class LevelConfigImporter implements USBImporter {
    private uSkyBlock plugin;

    @Override
    public String getName() {
        return "levelConfig.yml";
    }

    @Override
    public void init(uSkyBlock plugin) {
        this.plugin = plugin;
        plugin.setMaintenanceMode(true);
    }

    @Override
    public Boolean importFile(File file) {
        // Preserves comments
        YmlConfiguration config = new YmlConfiguration();
        FileUtil.readConfig(config, file);
        if (config.getInt("version") < 100) {
            // Do conversion
        }
        return null;
    }

    @Override
    public File[] getFiles() {
        return new File[] {
                new File(plugin.getDataFolder(), "levelConfig.yml")
        };
    }

    @Override
    public void completed(int success, int failed, int skipped) {
        plugin.setMaintenanceMode(false);
    }
}
