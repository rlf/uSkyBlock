package us.talabrek.ultimateskyblock.util;

import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.yml.YmlConfiguration;

import java.io.File;

public enum ServerUtil {;
    public static boolean isBungeeEnabled() {
        boolean isBungeeMode = false;
        File spigotYml = new File(".", "spigot.yml");
        if (spigotYml.exists()) {
            YmlConfiguration spigotConfig = new YmlConfiguration();
            FileUtil.readConfig(spigotConfig, spigotYml);
            isBungeeMode = spigotConfig.getBoolean("settings.bungeecord", false);
        }
        return isBungeeMode;
    }
}
