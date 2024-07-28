package us.talabrek.ultimateskyblock.util;

import dk.lockfuglsang.minecraft.file.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;

public enum ServerUtil {;
    private static final UUID ONLINE_UUID = UUIDUtil.fromString("97e8584c-438c-43cf-8b58-4e56c52398ed");
    private static final String ONLINE_NAME = "R4zorax";
    private static OfflinePlayer offlinePlayer;

    public static boolean isBungeeEnabled() {
        boolean isBungeeMode = false;
        File spigotYml = new File(".", "spigot.yml");
        if (spigotYml.exists()) {
            FileConfiguration spigotConfig = new YamlConfiguration();
            FileUtil.readConfig(spigotConfig, spigotYml);
            isBungeeMode = spigotConfig.getBoolean("settings.bungeecord", false);
        }
        return isBungeeMode;
    }

    public static boolean isOnlineMode() {
        return Bukkit.getServer().getOnlineMode() ||
                (getOfflinePlayer() != null && getOfflinePlayer().getUniqueId().equals(ONLINE_UUID));
    }

    private static OfflinePlayer getOfflinePlayer() {
        if (offlinePlayer == null) {
            try {
                //noinspection deprecation
                offlinePlayer = Bukkit.getOfflinePlayer(ONLINE_NAME);
            } catch (Throwable ignored) {
                // Ignored
            }
        }
        return offlinePlayer;
    }

    public static void init(final JavaPlugin plugin) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                getOfflinePlayer(); // Just trigger caching
            }
        });
    }
}
