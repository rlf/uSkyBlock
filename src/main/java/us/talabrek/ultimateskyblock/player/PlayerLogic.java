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

    public PlayerInfo loadPlayerData(Player player) {
        return loadPlayerData(player.getUniqueId(), player.getName());
    }

    public PlayerInfo loadPlayerData(UUID playerUUID, String playerName) {
        Preconditions.checkState(!Bukkit.isPrimaryThread(), "This method cannot run in the main server thread!");

        log.log(Level.INFO, "Loading player data for " + playerName);

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
        // Do not return anything if it is converting.
        if (this.locked.contains(playerName)) return null;

        return this.activePlayers.get(playerName);
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
        this.locked.add(player.getName());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    if (player == null || !player.isOnline()) {
                        return;
                    }

                    PlayerInfo loadedInfo = loadPlayerData(player);
                    if (loadedInfo != null && player != null && player.isOnline()) {
                        PlayerLogic.this.activePlayers.put(player.getName(), loadedInfo);
                    }
                } catch (Exception exception) {
                    throw exception;
                } finally {
                    PlayerLogic.this.locked.remove(player.getName());

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
