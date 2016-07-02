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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PlayerDB backed by a simple yml-uuid2NameFile.
 */
public class FilePlayerDB implements PlayerDB {
    private static final Logger log = Logger.getLogger(FilePlayerDB.class.getName());

    private final File uuid2NameFile;
    private final YmlConfiguration uuid2NameConfig;

    private boolean isShuttingDown = false;
    private volatile BukkitTask saveTask;
    private long saveDelay;

    // These caches should NOT be guavaCaches, we need them alive most of the time
    private final Map<String, UUID> name2uuidCache = new HashMap<>();
    private final Map<UUID, String> uuid2nameCache = new HashMap<>();

    public FilePlayerDB(File dataFolder) {
        uuid2NameFile = new File(dataFolder, "uuid2name.yml");
        uuid2NameConfig = new YmlConfiguration();
        if (uuid2NameFile.exists()) {
            FileUtil.readConfig(uuid2NameConfig, uuid2NameFile);
        }
        // Save max every 10 seconds
        saveDelay = uSkyBlock.getInstance().getConfig().getInt("playerdb.saveDelay", 20 * 10);
        Bukkit.getScheduler().runTaskAsynchronously(uSkyBlock.getInstance(), new Runnable() {
            @Override
            public void run() {
                Set<String> uuids = uuid2NameConfig.getKeys(false);
                for (String uuid : uuids) {
                    UUID id = UUIDUtil.fromString(uuid);
                    String name = uuid2NameConfig.getString(uuid + ".name", null);
                    uuid2nameCache.put(id, name);
                    name2uuidCache.put(name, id);
                    List<String> akas = uuid2NameConfig.getStringList(uuid + ".aka");
                    for (String aka : akas) {
                        if (!name2uuidCache.containsKey(aka)) {
                            name2uuidCache.put(aka, id);
                        }
                    }
                }
            }
        });
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
        return getUUIDFromName(name, true);
    }

    @Override
    public synchronized UUID getUUIDFromName(String name, boolean lookup) {
        if (name2uuidCache.containsKey(name)) {
            return name2uuidCache.get(name);
        }
        UUID result = null;
        if (lookup) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
            if (offlinePlayer != null) {
                updatePlayer(offlinePlayer.getUniqueId(), offlinePlayer.getName(), offlinePlayer.getName());
                result = offlinePlayer.getUniqueId();
            }
        }
        name2uuidCache.put(name, result);
        return result;
    }

    @Override
    public synchronized String getName(UUID uuid) {
        if (uuid2nameCache.containsKey(uuid)) {
            return uuid2nameCache.get(uuid);
        }
        String uuidStr = UUIDUtil.asString(uuid);
        String name = null;
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer != null) {
            updatePlayer(offlinePlayer.getUniqueId(), offlinePlayer.getName(), offlinePlayer.getName());
            name = offlinePlayer.getName();
            uuid2NameConfig.set(uuidStr, name);
        }
        if (name != null) {
            uuid2nameCache.put(uuid, name);
        }
        return name;
    }

    @Override
    public synchronized String getDisplayName(UUID uuid) {
        String uuidStr = UUIDUtil.asString(uuid);
        return uuid2NameConfig.getString(uuidStr + ".displayName", null);
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
            if (saveTask == null) {
                // Only have one pending save-task at a time
                saveTask = Bukkit.getScheduler().runTaskLaterAsynchronously(uSkyBlock.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        saveToFile();
                    }
                }, saveDelay);
            }
        }
    }

    private void saveToFile() {
        try {
            synchronized (this) {
                uuid2NameConfig.save(uuid2NameFile);
            }
        } catch (IOException e) {
            log.log(Level.INFO, "Error saving playerdb", e);
        } finally {
            saveTask = null;
        }
    }

    private synchronized void addEntry(UUID id, String name, String displayName) {
        String uuid = UUIDUtil.asString(id);
        uuid2nameCache.put(id, name);
        name2uuidCache.put(name, id);
        String oldName = uuid2NameConfig.getString(uuid + ".name", name);
        if (uuid2NameConfig.contains(uuid) && !oldName.equals(name)) {
            List<String> stringList = uuid2NameConfig.getStringList(uuid + ".aka");
            if (!stringList.contains(oldName)) {
                stringList.add(oldName);
                uuid2NameConfig.set(uuid + ".aka", stringList);
                if (!name2uuidCache.containsKey(oldName)) {
                    name2uuidCache.put(oldName, id);
                }
            }
        }
        uuid2NameConfig.set(uuid + ".name", name);
        uuid2NameConfig.set(uuid + ".updated", System.currentTimeMillis());
        if (displayName != null) {
            uuid2NameConfig.set(uuid + ".displayName", displayName);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent e) {
        updatePlayer(e.getPlayer().getUniqueId(), e.getPlayer().getName(), e.getPlayer().getDisplayName());
    }
}
