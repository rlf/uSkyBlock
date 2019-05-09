package us.talabrek.ultimateskyblock.uuid;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Purely memory based PlayerDB (no persisting).
 */
public class MemoryPlayerDB implements PlayerDB {
    private final LoadingCache<String, OfflinePlayer> nameCache;
    private final LoadingCache<UUID, OfflinePlayer> uuidCache;
    private static final OfflinePlayer NULL_PLAYER = NullPlayer.INSTANCE;

    public MemoryPlayerDB(FileConfiguration config) {
        nameCache = CacheBuilder
                .from(config.getString("options.advanced.playerdb.nameCache", "maximumSize=1500,expireAfterWrite=30m,expireAfterAccess=15m"))
                .build(new CacheLoader<String, OfflinePlayer>() {
                    @Override
                    public OfflinePlayer load(String name) throws Exception {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
                        if (offlinePlayer != null) {
                            uuidCache.put(offlinePlayer.getUniqueId(), offlinePlayer);
                            return offlinePlayer;
                        }
                        return NULL_PLAYER;
                    }
                });
        uuidCache = CacheBuilder
                .from(config.getString("options.advanced.playerdb.uuidCache", "maximumSize=1500,expireAfterWrite=30m,expireAfterAccess=15m"))
                .build(new CacheLoader<UUID, OfflinePlayer>() {
                    @Override
                    public OfflinePlayer load(UUID uuid) throws Exception {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                        if (offlinePlayer != null) {
                            nameCache.put(offlinePlayer.getName(), offlinePlayer);
                            return offlinePlayer;
                        }
                        return NULL_PLAYER;
                    }
                });
    }

    @Override
    public UUID getUUIDFromName(String name) {
        return getUUIDFromName(name, true);
    }

    @Override
    public UUID getUUIDFromName(String name, boolean lookup) {
        OfflinePlayer offlinePlayer = getOfflinePlayer(name, lookup);
        return offlinePlayer != null ? offlinePlayer.getUniqueId() : null;
    }

    private OfflinePlayer getOfflinePlayer(String name, boolean lookup) {
        OfflinePlayer offlinePlayer;
        try {
            if (!lookup) {
                offlinePlayer = nameCache.getIfPresent(name);
            } else {
                offlinePlayer = nameCache.get(name);
            }
        } catch (Exception e) {
            offlinePlayer = null;
        }
        return offlinePlayer;
    }

    @Override
    public String getName(UUID uuid) {
        OfflinePlayer offlinePlayer = getOfflinePlayer(uuid);
        return offlinePlayer != null ? offlinePlayer.getName() : null;
    }

    @Override
    public OfflinePlayer getOfflinePlayer(UUID uuid) {
        OfflinePlayer offlinePlayer;
        try {
            offlinePlayer = uuidCache.get(uuid);
        } catch (Exception e) {
            offlinePlayer = null;
        }
        return offlinePlayer;
    }

    @Override
    public String getDisplayName(UUID uuid) {
        OfflinePlayer offlinePlayer = getOfflinePlayer(uuid);
        return offlinePlayer != null && offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null
                ? offlinePlayer.getPlayer().getDisplayName()
                : null;
    }

    @Override
    public String getDisplayName(String playerName) {
        OfflinePlayer offlinePlayer = getOfflinePlayer(playerName, true);
        return offlinePlayer != null && offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null
                ? offlinePlayer.getPlayer().getDisplayName()
                : null;
    }

    @Override
    public Set<String> getNames(String search) {
        Set<String> names = new HashSet<>(nameCache.asMap().keySet());
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
    public void updatePlayer(UUID uuid, String name, String displayName) {
        // Not entirely what we want... and not performant at all...
        nameCache.refresh(name);
        uuidCache.refresh(uuid);
    }

    @Override
    public Player getPlayer(UUID uuid) {
        OfflinePlayer offlinePlayer = getOfflinePlayer(uuid);
        return offlinePlayer != null ? offlinePlayer.getPlayer() : null;
    }

    @Override
    public Player getPlayer(String name) {
        OfflinePlayer offlinePlayer = getOfflinePlayer(name, true);
        return offlinePlayer != null ? offlinePlayer.getPlayer() : null;
    }

    @Override
    public void shutdown() {
        nameCache.cleanUp();
        uuidCache.cleanUp();
    }
}