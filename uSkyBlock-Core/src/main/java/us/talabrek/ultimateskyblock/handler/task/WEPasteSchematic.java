package us.talabrek.ultimateskyblock.handler.task;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import us.talabrek.ultimateskyblock.handler.AsyncWorldEditHandler;
import us.talabrek.ultimateskyblock.player.PlayerPerk;

import java.io.File;

/**
 * BukkitRunnable - to single it out on the timings page
 */
public class WEPasteSchematic extends BukkitRunnable {
    private final File file;
    private final Location origin;
    private final PlayerPerk playerPerk;

    public WEPasteSchematic(File file, Location origin, PlayerPerk playerPerk) {
        this.file = file;
        this.origin = origin;
        this.playerPerk = playerPerk;
    }

    @Override
    public void run() {
        AsyncWorldEditHandler.getAWEAdaptor().loadIslandSchematic(file, origin, playerPerk);
    }
}
