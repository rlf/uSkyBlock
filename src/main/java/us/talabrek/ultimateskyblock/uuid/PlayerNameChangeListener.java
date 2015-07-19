package us.talabrek.ultimateskyblock.uuid;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The listener that takes action - if a player changes name.
 */
public class PlayerNameChangeListener implements Listener {
    private static final Logger log = Logger.getLogger(PlayerNameChangeListener.class.getName());
    private final uSkyBlock plugin;

    public PlayerNameChangeListener(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerNameChange(AsyncPlayerNameChangedEvent event) {
        if (event.getOldName() != null) {
            renamePlayer(event);
        }
    }

    private void renamePlayer(AsyncPlayerNameChangedEvent e) {
        final String oldName = e.getOldName();
        final Player player = e.getPlayer();
        final long t1 = System.currentTimeMillis();
        // TODO: 19/01/2015 - R4zorax: Block for interactions while converting?

        PlayerInfo playerInfo = e.getPlayerInfo();
        playerInfo.renameFrom(e.getOldName());
        Runnable completion = new Runnable() {
            @Override
            public void run() {
                double t2 = System.currentTimeMillis();
                t2 = (t2 - t1) / 1000d;
                String message = String.format("Renamed player %s to %s in %s seconds.", oldName, player.getName(), t2);
                log.log(Level.INFO, message);
                player.sendMessage(String.format("\u00a79It took \u00a7a%s\u00a79 seconds to rename your uSkyBlock presence from \u00a7a%s\u00a79.", t2, oldName));
            }
        };
        plugin.getIslandLogic().renamePlayer(playerInfo, completion, e);
    }
}
