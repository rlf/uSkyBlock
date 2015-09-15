package us.talabrek.ultimateskyblock.player;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Holds the active players
 */
public class PlayerLogic {
    private static final Logger log = Logger.getLogger(PlayerLogic.class.getName());
    private final LoadingCache<String, PlayerInfo> playerCache;
    private final uSkyBlock plugin;
    private final BukkitTask saveTask;
    public PlayerLogic(uSkyBlock plugin) {
        this.plugin = plugin;
        playerCache = CacheBuilder
                .from(plugin.getConfig().getString("options.advanced.playerCache", "maximumSize=200,expireAfterWrite=15m,expireAfterAccess=10m"))
                .removalListener(new RemovalListener<String, PlayerInfo>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, PlayerInfo> removal) {
                        PlayerInfo playerInfo = removal.getValue();
                        if (playerInfo.isDirty()) {
                            playerInfo.saveToFile();
                        }
                    }
                })
                .build(new CacheLoader<String, PlayerInfo>() {
                           @Override
                           public PlayerInfo load(String s) throws Exception {
                               return loadPlayerData(s);
                           }
                       }
                );
        int every = plugin.getConfig().getInt("options.advanced.player.saveEvery", 20*60*2);
        saveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                saveDirtyToFiles();
            }
        }, every, every);
    }

    private void saveDirtyToFiles() {
        // asMap.values() should NOT touch the cache.
        for (PlayerInfo pi : playerCache.asMap().values()) {
            if (pi.isDirty()) {
                pi.saveToFile();
            }
        }
    }

    public PlayerInfo loadPlayerData(String playerName) {
        return loadPlayerData(Bukkit.getOfflinePlayer(playerName));
    }

    public PlayerInfo loadPlayerData(OfflinePlayer player) {
        return loadPlayerData(player.getUniqueId(), player.getName());
    }

    private PlayerInfo loadPlayerData(UUID playerUUID, String playerName) {
        log.log(Level.FINER, "Loading player data for " + playerName);

        PlayerInfo playerInfo = new PlayerInfo(playerName, playerUUID);

        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            plugin.getPlayerNameChangeManager().checkPlayer(onlinePlayer, playerInfo);

            if (playerInfo.getHasIsland()) {
                WorldGuardHandler.protectIsland(onlinePlayer, playerInfo);
                plugin.getIslandLogic().clearFlatland(onlinePlayer, playerInfo.getIslandLocation(), 400);
                IslandInfo islandInfo = plugin.getIslandInfo(playerInfo);
                if (islandInfo != null) {
                    islandInfo.handleMemberLoggedIn(onlinePlayer);
                }
            }
        }

        return playerInfo;
    }

    public PlayerInfo getPlayerInfo(Player player) {
        return getPlayerInfo(player.getName());
    }

    public PlayerInfo getPlayerInfo(String playerName) {
        // Do not return anything if it is loading.
        try {
            return playerCache.get(playerName);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e); // Escalate - we need it in the server log
        }
    }

    public void loadPlayerDataAsync(final Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                playerCache.refresh(player.getName());
            }
        });
    }

    public void removeActivePlayer(PlayerInfo pi) {
        playerCache.invalidate(pi.getPlayerName());
    }

    public void shutdown() {
        saveTask.cancel();
        playerCache.invalidateAll(); // Doing this on the main thread.
    }
}
