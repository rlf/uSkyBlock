package us.talabrek.ultimateskyblock.player;

import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * Holds the active players
 */
public class PlayerLogic {
    private static final Logger log = Logger.getLogger(PlayerLogic.class.getName());
    private final Map<String, PlayerInfo> activePlayers = new ConcurrentHashMap<>();
    private final Queue<String> locked = new ConcurrentLinkedQueue<String>();
    private final uSkyBlock plugin;

    public PlayerLogic(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    private PlayerInfo loadPlayerInfo(String playerName, UUID playerUUID) {
        final PlayerInfo playerInfo = new PlayerInfo(playerName, playerUUID);
        activePlayers.put(playerName, playerInfo);
        return playerInfo;
    }

    public PlayerInfo loadPlayerData(Player player) {
        return loadPlayerData(player.getUniqueId(), player.getName());
    }

    public PlayerInfo loadPlayerData(UUID playerUUID, String playerName) {
        Preconditions.checkState(!Bukkit.isPrimaryThread(), "This method cannot run in the main server thread!");

        log.log(Level.INFO, "Loading player data for " + playerName);

        PlayerInfo pi = loadPlayerInfo(playerName, playerUUID);
        
        // If the online player is null, then it is just a temporary load for commands such as RegisterIslandToPlayer.
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            plugin.getPlayerNameChangeManager().checkPlayer(onlinePlayer, pi);

            if (pi.getHasIsland()) {
                WorldGuardHandler.protectIsland(onlinePlayer, pi);
                plugin.getIslandLogic().clearFlatland(onlinePlayer, pi.getIslandLocation(), 400);
                IslandInfo islandInfo = plugin.getIslandInfo(pi);
                if (islandInfo != null) {
                    islandInfo.handleMemberLoggedIn(onlinePlayer);
                }
            }

            activePlayers.put(playerName, pi);
        }
        
        return pi;
    }

    public PlayerInfo getPlayerInfo(Player player) {
        return getPlayerInfo(player.getName());
    }

    public PlayerInfo getPlayerInfo(String playerName) {
        return activePlayers.get(playerName);
    }

    public boolean isLocked(Player player) {
        return isLocked(player.getName());
    }

    public boolean isLocked(String playerName) {
        synchronized (locked) {
            return locked.contains(playerName);
        }
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

    public void loadPlayerDataAsync(final Player player) {
        locked.add(player.getName());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    if (player == null || !player.isOnline()) {
                        return;
                    }
                    loadPlayerData(player);
                } catch (Exception e) {
                    throw e;
                } finally {
                    locked.remove(player.getName());

                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            if (player != null && player.isOnline()) {
                                PlayerInfo playerInfo = plugin.getPlayerInfo(player);
                                if (!playerInfo.getHasIsland() && player.getLocation().getWorld().getName().equalsIgnoreCase(uSkyBlock.skyBlockWorld.getName())) {
                                    plugin.spawnTeleport(player, true);
                                    player.sendMessage(tr("\u00a7cIt seems you logged out in the sky world, however you are not a member of an island. Teleporting you to spawn."));
                                }
                            }
                        }
                    });
                }
            }
        });
    }
}
