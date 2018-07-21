package us.talabrek.ultimateskyblock.imports.levelconfig;

import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import us.talabrek.ultimateskyblock.imports.USBImporter;
import us.talabrek.ultimateskyblock.island.level.BlockLevelConfigMap;
import us.talabrek.ultimateskyblock.island.level.yml.LegacyLevelConfigYmlReader;
import us.talabrek.ultimateskyblock.island.level.yml.LevelConfigYmlWriter;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class LevelConfigImporter implements USBImporter {
    private uSkyBlock plugin;

    @Override
    public String getName() {
        return "levelConfig";
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
        try {
            config.save(new File(file.getParentFile(), file.getName() + ".org"));
        } catch (IOException e) {
            LogUtil.log(Level.WARNING, "Unable to save backup of " + file, e);
        }
        int version = config.getInt("version", 0);
        if (version < 100) {
            BlockLevelConfigMap configMap = new LegacyLevelConfigYmlReader().readLevelConfig(config);
            config.set("version", null);
            config.set("blockValues", null);
            config.set("blockLimits", null);
            config.set("diminishingReturns", null);
            config.set("negativeReturns", null);
            if (config.getBoolean("general.useDiminishingReturns", false)) {
                int defaultScale = config.getInt("general.defaultScale", 0);
                if (defaultScale > 0) {
                    config.set("general.diminishingReturns", defaultScale);
                }
            }
            config.set("general.useDiminishingReturns", null);
            config.set("general.defaultScale", null);
            config = new LevelConfigYmlWriter().writeToConfig(config, configMap);
            config.set("version", 100);
            try {
                config.save(file);
            } catch (IOException e) {
                LogUtil.log(Level.WARNING, "Unable to import " + file, e);
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public File[] getFiles() {
        return new File[]{
                new File(plugin.getDataFolder(), "levelConfig.yml")
        };
    }

    @Override
    public void completed(int success, int failed, int skipped) {
        plugin.setMaintenanceMode(false);
    }
}
