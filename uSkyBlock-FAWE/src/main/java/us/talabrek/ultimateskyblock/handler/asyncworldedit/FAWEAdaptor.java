package us.talabrek.ultimateskyblock.handler.asyncworldedit;

import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.player.PlayerPerk;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LogUtil;

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
    public void onEnable(Plugin plugin) {
        this.plugin = (uSkyBlock) plugin;
        log.finer("- FAWE debugging: Location of WorldEdit EditSession: " + EditSession.class.getResource('/' + EditSession.class.getName().replace('.', '/') + ".class"));
    }

    @Override
    public void onDisable(Plugin plugin) {
        this.plugin = null;
    }

    @Override
    public synchronized void registerCompletion(Player player) {
    }

    @Override
    public void loadIslandSchematic(final File file, final Location origin, final PlayerPerk playerPerk) {
        plugin.async(() -> {
            log.finer("Trying to load schematic " + file);
            if (file == null || !file.exists() || !file.canRead()) {
                LogUtil.log(Level.WARNING, "Unable to load schematic " + file);
            }
            boolean noAir = false;
            Vector to = new Vector(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
            EditSession editSession = getEditSession(playerPerk, origin);
            try {
                ClipboardFormat
                        .findByFile(file)
                        .load(file)
                        .paste(editSession, to, noAir);
                editSession.flushQueue();
            } catch (IOException e) {
                log.log(Level.INFO, "Unable to paste schematic " + file, e);
            }
        });
    }

    private synchronized EditSession getEditSession(PlayerPerk playerPerk, Location origin) {
        EditSession editSession = createEditSession(new BukkitWorld(origin.getWorld()), -1);
        return editSession;
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
