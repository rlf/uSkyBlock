package us.talabrek.ultimateskyblock.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Holds the active players
 */
public class PlayerLogic {
    private static final Logger log = Logger.getLogger(PlayerLogic.class.getName());
    private final Map<String, PlayerInfo> activePlayers = new ConcurrentHashMap<>();
    private final Set<UUID> locked = Collections.synchronizedSet(new HashSet<UUID>());
    private final uSkyBlock plugin;

    public PlayerLogic(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    private PlayerInfo loadPlayerInfo(String playerName) {
        final PlayerInfo playerInfo = new PlayerInfo(playerName);
        activePlayers.put(playerName, playerInfo);
        return playerInfo;
    }

    public PlayerInfo getPlayerInfo(final Player player) {
        try {
            locked.add(player.getUniqueId());
            PlayerInfo playerInfo = getPlayerInfo(player.getName());
            if (player.isOnline()) {
                playerInfo.updatePlayerInfo(player);
            }
            return playerInfo;
        } finally {
            locked.remove(player.getUniqueId());
        }
    }

    public PlayerInfo getPlayerInfo(String player) {
        PlayerInfo playerInfo = activePlayers.get(player);
        if (playerInfo == null) {
            playerInfo = loadPlayerInfo(player);
        }
        return playerInfo;
    }

    public boolean isLocked(Player player) {
        synchronized (locked) {
            return locked.contains(player.getUniqueId());
        }
    }

    public PlayerInfo loadPlayerData(final Player player) {
        log.log(Level.INFO, "Loading player data for " + player.getName());
        final PlayerInfo pi = loadPlayerInfo(player.getName());
        if (pi.getHasIsland()) {
            WorldGuardHandler.protectIsland(player, pi);
            plugin.getIslandLogic().clearFlatland(player, pi.getIslandLocation(), 400);
        }
        activePlayers.put(player.getName(), pi);
        return pi;
    }

    public boolean isActive(Player player) {
        return player != null && activePlayers.containsKey(player.getName());
    }

    public void removeActivePlayer(Player player) {
        if (player != null && player.getName() != null && activePlayers.containsKey(player.getName())) {
            activePlayers.remove(player.getName());
        }
    }

    public void removeActivePlayer(PlayerInfo player) {
        if (player != null && player.getPlayerName() != null && activePlayers.containsKey(player.getPlayerName())) {
            activePlayers.remove(player.getPlayerName());
        }
    }

    public synchronized PlayerInfo renameTo(String oldName, String name) {
        PlayerInfo playerInfo = getPlayerInfo(oldName);
        playerInfo = playerInfo.renameTo(name);
        activePlayers.remove(oldName);
        activePlayers.put(name, playerInfo);
        return playerInfo;
    }

    public void loadPlayerDataAsync(final Player player) {
        locked.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    final PlayerInfo playerInfo = loadPlayerData(player);
                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (playerInfo != null && !playerInfo.getHasIsland() && !player.isOnGround()) {
                                    uSkyBlock.getInstance().spawnTeleport(player);
                                }
                            } finally {
                                locked.remove(player.getUniqueId());
                            }
                        }
                    });
                } catch (Exception e) {
                    locked.remove(player.getUniqueId());
                    throw e;
                }
            }
        });
    }
}
