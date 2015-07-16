package us.talabrek.ultimateskyblock.player;

import java.util.Map;
import java.util.Queue;
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

    private PlayerInfo loadPlayerInfo(String playerName) {
        final PlayerInfo playerInfo = new PlayerInfo(playerName);
        activePlayers.put(playerName, playerInfo);
        return playerInfo;
    }

    public PlayerInfo getPlayerInfo(final Player player) {
        try {
            locked.add(player.getName());
            PlayerInfo playerInfo = getPlayerInfo(player.getName());
            if (playerInfo != null && player.isOnline()) {
                playerInfo.updatePlayerInfo(player);
            }
            return playerInfo;
        } finally {
            locked.remove(player.getName());
        }
    }

    public PlayerInfo getPlayerInfo(String player) {
        PlayerInfo playerInfo = activePlayers.get(player);
        if (playerInfo == null && !locked.contains(player)) {
            playerInfo = loadPlayerInfo(player);
        }
        return playerInfo;
    }

    public boolean isLocked(Player player) {
        synchronized (locked) {
            return locked.contains(player.getName());
        }
    }

    public PlayerInfo loadPlayerData(final Player player) {
        log.log(Level.INFO, "Loading player data for " + player.getName());
        final PlayerInfo pi = loadPlayerInfo(player.getName());
        plugin.getPlayerNameChangeManager().checkPlayer(player, pi);
        if (pi.getHasIsland()) {
            WorldGuardHandler.protectIsland(player, pi);
            plugin.getIslandLogic().clearFlatland(player, pi.getIslandLocation(), 400);
            IslandInfo islandInfo = plugin.getIslandInfo(pi);
            if (islandInfo != null) {
                islandInfo.handleMemberLoggedIn(player);
            }
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

    public synchronized PlayerInfo renameTo(PlayerInfo playerInfo, String oldName, String newName) {
        try {
            locked.add(oldName);
            locked.add(newName);
            playerInfo = playerInfo.renameTo(newName);
            activePlayers.remove(oldName);
            activePlayers.put(newName, playerInfo);
            return playerInfo;
        } finally {
            locked.remove(newName);
            locked.remove(oldName);
        }
    }

    public void loadPlayerDataAsync(final Player player) {
        locked.add(player.getName());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
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
