package us.talabrek.ultimateskyblock.uuid;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

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

    private void renamePlayer(final AsyncPlayerNameChangedEvent event) {
        String oldName = event.getOldName();
        Player player = event.getPlayer();
        long t1 = System.currentTimeMillis();
        // TODO: 19/01/2015 - R4zorax: Block for interactions while converting?

        PlayerInfo playerInfo = event.getPlayerInfo();

        // Rename the player config files first, but DO NOT load them to prevent it from thinking they were kicked from the island.
        playerInfo.renameFrom(event.getOldName());

        // Rename their island data.
        plugin.getIslandLogic().renamePlayer(playerInfo, event);

        // Handle island removal checks after.
        // This fixes the player island from thinking they were kicked.
        playerInfo.postRename();
        
        double t2 = System.currentTimeMillis();
        t2 = (t2 - t1) / 1000d;
        String message = String.format("Renamed player %s to %s in %s seconds.", oldName, player.getName(), t2);
        log.log(Level.INFO, message);
        player.sendMessage(String.format("\u00a79It took \u00a7a%s\u00a79 seconds to rename your uSkyBlock presence from \u00a7a%s\u00a79.", t2, oldName));
    }
}
