package us.talabrek.ultimateskyblock.uuid;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            checkPlayer(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        checkPlayer(player);
    }

    private void checkPlayer(Player player) {
        String oldName = playerDB.getName(player.getUniqueId());
        if (oldName == null || !oldName.equals(player.getName())) {
            try {
                playerDB.setName(player.getUniqueId(), player.getName());
            } catch (IOException e) {
                log.log(Level.SEVERE, "Error saving player-name in database.", e);
            }
            plugin.getServer().getPluginManager().callEvent(new PlayerNameChangedEvent(player, oldName));
        }
    }
}
