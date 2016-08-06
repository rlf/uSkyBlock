package us.talabrek.ultimateskyblock.util;

import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
            YmlConfiguration spigotConfig = new YmlConfiguration();
            FileUtil.readConfig(spigotConfig, spigotYml);
            isBungeeMode = spigotConfig.getBoolean("settings.bungeecord", false);
        }
        return isBungeeMode;
    }

    public static boolean isOnlineMode() {
        return Bukkit.getServer().getOnlineMode() || getOfflinePlayer().getUniqueId().equals(ONLINE_UUID);
    }

    private static OfflinePlayer getOfflinePlayer() {
        if (offlinePlayer == null) {
            offlinePlayer = Bukkit.getOfflinePlayer(ONLINE_NAME);
        }
        return offlinePlayer;
    }

    public static void init(final JavaPlugin plugin) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                OfflinePlayer player = plugin.getServer().getOfflinePlayer(ONLINE_NAME);
                if (player != null) {
                    offlinePlayer = player;
                }
            }
        });
    }
}
