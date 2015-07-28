package us.talabrek.ultimateskyblock.uuid;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.player.PlayerInfo;

/**
 * Responsible for detecting when a player changes name.
 */
public class PlayerNameChangeManager implements Listener {
    private static final Logger log = Logger.getLogger(PlayerNameChangeManager.class.getName());
    private final Plugin plugin;
    private final PlayerDB playerDB;

    public PlayerNameChangeManager(Plugin plugin, PlayerDB playerDB) {
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

    public boolean hasNameChanged(UUID playerUUID, String playerCurrentName) {
        String oldName = playerDB.getName(playerUUID);
        return oldName != null && !oldName.equalsIgnoreCase(playerCurrentName);
    }

    public void checkPlayer(Player player, PlayerInfo playerInfo) {
        Preconditions.checkState(!Bukkit.isPrimaryThread(), "This method cannot be called in the main thread!");
        Preconditions.checkNotNull(player, "Player cannot be null!");
        Preconditions.checkState(player.isOnline(), "Player must be online!");
        Preconditions.checkNotNull(playerInfo, "Player info cannot be null!");
        
        String oldName = playerDB.getName(player.getUniqueId());
        if (hasNameChanged(player.getUniqueId(), player.getName())) {
            try {
                plugin.getServer().getPluginManager().callEvent(new AsyncPlayerNameChangedEvent(player, playerInfo, oldName, player.getName()));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            save(player);
        }
    }

    private void save(Player player) {
        if (playerDB == null) {
            return;
        }
        try {
            playerDB.updatePlayer(player);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error saving player in database.", e);
        }
    }
}
