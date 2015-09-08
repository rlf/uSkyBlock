package us.talabrek.ultimateskyblock.player;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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

    public PlayerInfo loadPlayerData(String playerName) {
        //Preconditions.checkState(!Bukkit.isPrimaryThread(), "This method cannot run in the main server thread!");
        return loadPlayerData(Bukkit.getOfflinePlayer(playerName));
    }

    public PlayerInfo loadPlayerData(OfflinePlayer player) {
        return loadPlayerData(player.getUniqueId(), player.getName());
    }

    public PlayerInfo loadPlayerData(UUID playerUUID, String playerName) {
        return loadPlayerData(playerUUID, playerName, false);
    }
    
    private PlayerInfo loadPlayerData(UUID playerUUID, String playerName, boolean skipIsLockedCheck) {
        //Preconditions.checkState(!Bukkit.isPrimaryThread(), "This method cannot run in the main server thread!");

        // Do not return anything if it is loading.
        // Obey by the skipIsLockedCheck to make sure players joining get loaded.
        if (!skipIsLockedCheck && isLocked(playerName)) return null;

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
        // Do not return anything if it is loading.
        if (isLocked(playerName)) return null;
        // TODO: 30/08/2015 - R4zorax: We have a lot of issues reg. offline players here, and performance if we load it...
        if (activePlayers.containsKey(playerName)) {
            return activePlayers.get(playerName);
        }
        // Note: We do not put it in the cache on purpose - that is reserved for online players
        return loadPlayerData(playerName);
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
        final String playerName = player.getName();
        
        this.locked.add(playerName);
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    if (player != null && player.isOnline()) {
                        PlayerInfo loadedInfo = loadPlayerData(player.getUniqueId(), player.getName(), true);
                        if (loadedInfo != null && player != null && player.isOnline()) {
                            PlayerLogic.this.activePlayers.put(playerName, loadedInfo);
                        }
                    }
                } catch (Exception exception) {
                    throw exception;
                } finally {
                    PlayerLogic.this.locked.remove(playerName);

                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            if (player != null && player.isOnline()) {
                                PlayerInfo playerInfo = plugin.getPlayerInfo(player);
                                if (isUnsafe(playerInfo, player)) {
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

    private boolean isUnsafe(PlayerInfo playerInfo, Player player) {
        return (!playerInfo.getHasIsland() && player.getLocation().getWorld().getName().equalsIgnoreCase(uSkyBlock.skyBlockWorld.getName())) || uSkyBlock.getInstance().isSafeLocation(player.getLocation());
    }
}
