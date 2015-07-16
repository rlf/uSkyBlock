package us.talabrek.ultimateskyblock.uuid;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        save(event.getPlayer());
    }

    public void checkPlayer(Player player, PlayerInfo playerInfo) {
        String oldName = playerDB.getName(player.getUniqueId());
        if (oldName == null || !oldName.equalsIgnoreCase(player.getName())) {
            save(player);
            plugin.getServer().getPluginManager().callEvent(new PlayerNameChangedEvent(player, playerInfo, oldName, player.getName()));
        } else {
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
