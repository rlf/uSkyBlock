package dk.lockfuglsang.minecraft;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.talabrek.ultimateskyblock.api.event.uSkyBlockEvent;
import us.talabrek.ultimateskyblock.api.event.uSkyBlockScoreChangedEvent;
import us.talabrek.ultimateskyblock.api.model.IslandScore;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listener for catching uSkyBlock Events
 */
public class uSkyBlockListener implements Listener {
    private static final Logger log = Logger.getLogger(uSkyBlockListener.class.getName());

    @EventHandler
    public void uSkyBlockEvent(uSkyBlockEvent e) {
        log.log(Level.INFO, "Received uSkyBlockEvent: " + e);
        if (e.getAPI() != null && e.getAPI().isEnabled()) {
            // Access API
        }
    }

    @EventHandler
    public void uSkyBlockScoreChangedEvent(uSkyBlockScoreChangedEvent e) {
        log.log(Level.INFO, "Received Score-Change Event: " + e);
        IslandScore score = e.getScore();
        if (score != null) {
            // Do some magic here
        }
    }
}
