package us.talabrek.ultimateskyblock.uuid;

import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;
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
    private BukkitTask saveTask;
    private long saveDelay;

    public FilePlayerDB(File file) {
        this.file = file;
        config = new YmlConfiguration();
        if (file.exists()) {
            FileUtil.readConfig(config, file);
        }
        saveDelay = uSkyBlock.getInstance().getConfig().getInt("playerdb.saveDelay", 20);
    }

    @Override
    public void shutdown() {
        isShuttingDown = true;
        if (saveTask != null) {
            saveTask.cancel();
        }
        saveToFile();
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
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (offlinePlayer != null) {
            updatePlayer(offlinePlayer.getUniqueId(), offlinePlayer.getName(), offlinePlayer.getName());
            return offlinePlayer.getUniqueId();
        }
        return null;
    }

    @Override
    public synchronized String getName(UUID uuid) {
        String uuidStr = UUIDUtil.asString(uuid);
        String name = config.getString(uuidStr + ".name", config.getString(uuidStr, null));
        if (name == null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer != null) {
                updatePlayer(offlinePlayer.getUniqueId(), offlinePlayer.getName(), offlinePlayer.getName());
                return offlinePlayer.getName();
            }
        }
        return name;
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
    public void updatePlayer(final UUID id, final String name, final String displayName) {
        addEntry(id, name, displayName);
        if (isShuttingDown) {
            saveToFile();
        } else {
            if (saveTask != null) {
                saveTask.cancel();
            }
            saveTask = Bukkit.getScheduler().runTaskLaterAsynchronously(uSkyBlock.getInstance(), new Runnable() {
                @Override
                public void run() {
                    saveToFile();
                }
            }, saveDelay);
        }
    }

    private void saveToFile() {
        try {
            synchronized (this) {
                config.save(file);
            }
        } catch (IOException e) {
            log.log(Level.INFO, "Error saving playerdb", e);
        } finally {
            saveTask = null;
        }
    }

    private synchronized void addEntry(UUID id, String name, String displayName) {
        String uuid = UUIDUtil.asString(id);
        config.set(uuid + ".name", name);
        config.set(uuid + ".displayName", displayName);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent e) {
        updatePlayer(e.getPlayer().getUniqueId(), e.getPlayer().getName(), e.getPlayer().getDisplayName());
    }
}
