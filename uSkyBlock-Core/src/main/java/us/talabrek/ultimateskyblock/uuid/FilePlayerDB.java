package us.talabrek.ultimateskyblock.uuid;

import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.UUIDUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PlayerDB backed by a simple yml-uuid2NameFile.
 */
public class FilePlayerDB implements PlayerDB {
    private static final Logger log = Logger.getLogger(FilePlayerDB.class.getName());

    private final File uuid2NameFile;
    private final YmlConfiguration uuid2NameConfig;
    private final uSkyBlock plugin;

    private boolean isShuttingDown = false;
    private volatile BukkitTask saveTask;
    private long saveDelay;

    // These caches should NOT be guavaCaches, we need them alive most of the time
    private final Map<String, UUID> name2uuidCache = new ConcurrentHashMap<>();
    private final Map<UUID, String> uuid2nameCache = new ConcurrentHashMap<>();

    public FilePlayerDB(uSkyBlock plugin) {
        this.plugin = plugin;
        uuid2NameFile = new File(plugin.getDataFolder(), "uuid2name.yml");
        uuid2NameConfig = new YmlConfiguration();
        if (uuid2NameFile.exists()) {
            FileUtil.readConfig(uuid2NameConfig, uuid2NameFile);
        }
        // Save max every 10 seconds
        saveDelay = plugin.getConfig().getInt("playerdb.saveDelay", 10000);
        plugin.async(new Runnable() {
            @Override
            public void run() {
                synchronized (uuid2NameConfig) {
                    Set<String> uuids = uuid2NameConfig.getKeys(false);
                    for (String uuid : uuids) {
                        UUID id = UUIDUtil.fromString(uuid);
                        String name = uuid2NameConfig.getString(uuid + ".name", null);
                        if (name != null && id != null) {
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
    public UUID getUUIDFromName(String name) {
        return getUUIDFromName(name, true);
    }

    @Override
    public UUID getUUIDFromName(String name, boolean lookup) {
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
    public String getName(UUID uuid) {
        if (UNKNOWN_PLAYER_UUID.equals(uuid)) {
            return UNKNOWN_PLAYER_NAME;
        }
        if (uuid2nameCache.containsKey(uuid)) {
            return uuid2nameCache.get(uuid);
        }
        String name = null;
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer != null) {
            updatePlayer(offlinePlayer.getUniqueId(), offlinePlayer.getName(), offlinePlayer.getName());
            name = offlinePlayer.getName();
        }
        if (name != null) {
            uuid2nameCache.put(uuid, name);
        }
        return name;
    }

    @Override
    public String getDisplayName(UUID uuid) {
        String uuidStr = UUIDUtil.asString(uuid);
        synchronized (uuid2NameConfig) {
            return uuid2NameConfig.getString(uuidStr + ".displayName", null);
        }
    }

    @Override
    public String getDisplayName(String playerName) {
        UUID uuid = getUUIDFromName(playerName);
        if (uuid != null) {
            return getDisplayName(uuid);
        }
        return playerName;
    }

    @Override
    public Set<String> getNames(String search) {
        HashSet<String> names = new HashSet<>(uuid2nameCache.values());
        String lowerSearch = search != null ? search.toLowerCase() : null;
        for (Iterator<String> it = names.iterator(); it.hasNext(); ) {
            String name = it.next();
            if (name == null || (search != null && !name.toLowerCase().startsWith(lowerSearch))) {
                it.remove();
            }
        }
        return names;
    }

    @Override
    public void updatePlayer(final UUID id, final String name, final String displayName) {
        addEntry(id, name, displayName);
        if (isShuttingDown) {
            saveToFile();
        } else {
            if (saveTask == null) {
                // Only have one pending save-task at a time
                saveTask = plugin.async(new Runnable() {
                    @Override
                    public void run() {
                        saveToFile();
                    }
                }, saveDelay);
            }
        }
    }

    @Override
    public Player getPlayer(UUID uuid) {
        if (uuid != null) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                updatePlayer(player.getUniqueId(), player.getName(), player.getDisplayName());
            }
            return player;
        }
        return null;
    }

    @Override
    public Player getPlayer(String name) {
        if (name != null) {
            UUID uuid = getUUIDFromName(name);
            if (uuid != null) {
                return getPlayer(uuid);
            }
            Player player = Bukkit.getPlayer(name);
            if (player != null) {
                updatePlayer(player.getUniqueId(), player.getName(), player.getDisplayName());
            }
            return player;
        }
        return null;
    }

    private void saveToFile() {
        try {
            synchronized (uuid2NameConfig) {
                uuid2NameConfig.save(uuid2NameFile);
            }
        } catch (IOException e) {
            log.log(Level.INFO, "Error saving playerdb", e);
        } finally {
            saveTask = null;
        }
    }

    private void addEntry(UUID id, String name, String displayName) {
        String uuid = UUIDUtil.asString(id);
        UUID oldUUID = null;
        if (name != null) {
            oldUUID = name2uuidCache.get(name);
            uuid2nameCache.put(id, name);
            name2uuidCache.put(name, id);
        }
        synchronized (uuid2NameConfig) {
            String oldName = uuid2NameConfig.getString(uuid + ".name", name);
            if (uuid2NameConfig.contains(uuid) && oldName != null && !oldName.equals(name)) {
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
            if (oldUUID != null && !oldUUID.equals(id)) {
                // Cleanup, remove all references to the new name for the old UUID
                uuid2NameConfig.set(UUIDUtil.asString(oldUUID), null);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent e) {
        updatePlayer(e.getPlayer().getUniqueId(), e.getPlayer().getName(), e.getPlayer().getDisplayName());
    }
}
