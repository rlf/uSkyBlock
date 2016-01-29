package us.talabrek.ultimateskyblock.uuid;

import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.UUIDUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PlayerDB backed by a simple yml-file.
 */
public class FilePlayerDB implements PlayerDB {
    private static final Logger log = Logger.getLogger(FilePlayerDB.class.getName());
    private final YmlConfiguration config;
    private final File file;
    private boolean isShuttingDown = false;

    public FilePlayerDB(File file) {
        this.file = file;
        config = new YmlConfiguration();
        if (file.exists()) {
            FileUtil.readConfig(config, file);
        }
    }

    @Override
    public void shutdown() {
        isShuttingDown = true;
    }

    @Override
    public synchronized UUID getUUIDFromName(String name) {
        Set<String> keys = new HashSet<>(config.getKeys(false));
        for (String uuidStr : keys) {
            String entryName = config.getString(uuidStr + ".name", config.getString(uuidStr, null));
            if (entryName != null && entryName.equals(name)) {
                return UUIDUtil.fromString(uuidStr);
            }
        }
        return null;
    }

    @Override
    public synchronized String getName(UUID uuid) {
        String uuidStr = UUIDUtil.asString(uuid);
        return config.getString(uuidStr + ".name", config.getString(uuidStr, null));
    }

    @Override
    public synchronized String getDisplayName(UUID uuid) {
        String uuidStr = UUIDUtil.asString(uuid);
        return config.getString(uuidStr + ".displayName", null);
    }

    @Override
    public synchronized String getDisplayName(String playerName) {
        UUID uuid = getUUIDFromName(playerName);
        if (uuid != null) {
            return getDisplayName(uuid);
        }
        return playerName;
    }

    @Override
    public synchronized void updatePlayer(Player player) {
        updatePlayerAsync(player.getUniqueId(), player.getName(), player.getDisplayName());
    }

    public void updatePlayerAsync(final UUID id, final String name, final String displayName) {
        if (isShuttingDown) {
            saveToFile(id, name, displayName);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(uSkyBlock.getInstance(), new Runnable() {
                @Override
                public void run() {
                    saveToFile(id, name, displayName);
                }
            });
        }
    }

    private void saveToFile(UUID id, String name, String displayName) {
        try {
            YmlConfiguration copy;
            synchronized (this) {
                String uuid = UUIDUtil.asString(id);
                config.set(uuid + ".name", name);
                config.set(uuid + ".displayName", displayName);
                copy = new YmlConfiguration();
                copy.setDefaults(config);
                copy.options().copyDefaults(true);
                copy.addComments(config.getComments());
            }
            copy.save(file);
        } catch (IOException e) {
            log.log(Level.INFO, "Error saving playerdb", e);
        }
    }
}
