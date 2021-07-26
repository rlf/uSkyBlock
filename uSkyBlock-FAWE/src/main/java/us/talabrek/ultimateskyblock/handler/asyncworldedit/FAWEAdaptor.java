package us.talabrek.ultimateskyblock.handler.asyncworldedit;

import com.fastasyncworldedit.core.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.player.PlayerPerk;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adaptor depending on FAWE (FastAsyncWorldEdit).
 */
public class FAWEAdaptor implements AWEAdaptor {
    private static final Logger log = Logger.getLogger(FAWEAdaptor.class.getName());
    private uSkyBlock plugin;

    @Override
    public void onEnable(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onDisable(uSkyBlock plugin) {
        this.plugin = null;
    }

    @Override
    public synchronized void registerCompletion(Player player) {
    }

    @Override
    public void loadIslandSchematic(final File file, final Location origin, final PlayerPerk playerPerk) {
        plugin.async(() -> {
            if (file == null || !file.exists() || !file.canRead()) {
                log.log(Level.WARNING, "Unable to load schematic {}", file);
                return;
            }

            ClipboardFormat format = ClipboardFormats.findByFile(file);
            if (format == null) {
                log.log(Level.SEVERE, "Unable to find schematic format for file {}", file);
                return;
            }

            BlockVector3 to = BlockVector3.at(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
            EditSession editSession = getEditSession(playerPerk, origin);
            try {
                format
                    .load(file)
                    .paste(editSession, to, false);
                editSession.flushQueue();
            } catch (IOException ex) {
                log.log(Level.INFO, "Unable to paste schematic " + file, ex);
            }
        });
    }

    private synchronized EditSession getEditSession(PlayerPerk playerPerk, Location origin) {
        return createEditSession(new BukkitWorld(origin.getWorld()), -1);
    }

    public EditSession createEditSession(World bukkitWorld, int maxBlocks) {
        return new EditSessionBuilder(bukkitWorld).fastmode(true).build();
    }

    @Override
    public void regenerate(final Region region, final Runnable onCompletion) {
        // NOTE: Running this asynchronous MIGHT be a bit dangereous! Since pasting could interfere
        plugin.async(() -> {
            try {
                EditSession editSession = createEditSession(region.getWorld(), -1);
                editSession.regenerate(region);
                editSession.flushQueue();
            } finally {
                onCompletion.run();
            }
        });
    }
}
