package us.talabrek.ultimateskyblock.uuid;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsible for detecting when a player changes name.
 */
public class PlayerNameChangeManager implements Listener {
    private static final Logger log = Logger.getLogger(PlayerNameChangeManager.class.getName());
    private final uSkyBlock plugin;
    private final PlayerDB playerDB;

    public PlayerNameChangeManager(uSkyBlock plugin, PlayerDB playerDB) {
        this.plugin = plugin;
        this.playerDB = playerDB;
    }
    
    public void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            save(player);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        save(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String oldName = playerDB.getName(player.getUniqueId());
        playerDB.updatePlayer(player);
        if (hasNameChanged(player.getUniqueId(), oldName)) {
            if (oldName != null) {
                PlayerInfo playerInfo = plugin.getPlayerInfo(oldName);
                if (playerInfo != null) {
                    plugin.getServer().getPluginManager().callEvent(new AsyncPlayerNameChangedEvent(player, playerInfo, oldName, player.getName()));
                }
            }
        }
    }

    public boolean hasNameChanged(UUID playerUUID, String playerCurrentName) {
        String oldName = playerDB.getName(playerUUID);
        return oldName != null && !oldName.equalsIgnoreCase(playerCurrentName);
    }

    private void save(Player player) {
        if (playerDB == null) {
            return;
        }
        playerDB.updatePlayer(player);
    }
}
