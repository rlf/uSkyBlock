package us.talabrek.ultimateskyblock.handler.asyncworldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.World;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.player.PlayerPerk;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adaptor depending on FAWE (FastAsyncWorldEdit).
 */
public class FAWEAdaptor implements AWEAdaptor {
    private static final Logger log = Logger.getLogger(FAWEAdaptor.class.getName());
    private uSkyBlock plugin;
    private int progressEveryMs;
    private double progressEveryPct;
    private final Map<UUID, PlayerProgressTracker> activeSessions = new ConcurrentHashMap<>();

    @Override
    public void onEnable(Plugin plugin) {
        this.plugin = (uSkyBlock) plugin;
        progressEveryMs = plugin.getConfig().getInt("asyncworldedit.progressEveryMs", 3000);
        progressEveryPct = plugin.getConfig().getDouble("asyncworldedit.progressEveryPct", 20);
        log.finer("- FAWE debugging: Location of WorldEdit EditSession: " + EditSession.class.getResource('/' + EditSession.class.getName().replace('.', '/') + ".class"));
    }

    @Override
    public void onDisable(Plugin plugin) {
        this.plugin = null;
    }

    @Override
    public synchronized void registerCompletion(Player player) {
        if (player != null) {
            activeSessions.remove(player.getUniqueId());
        }
    }

    @Override
    public void loadIslandSchematic(final File file, final Location origin, final PlayerPerk playerPerk) {
        plugin.async(new Runnable() {
            @Override
            public void run() {
                boolean noAir = false;
                boolean entities = true;
                Vector to = new Vector(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
                EditSession editSession = getEditSession(playerPerk, origin);
                try {
                    SchematicFormat.getFormat(file)
                            .load(file)
                            .paste(editSession, to, noAir, entities);
                    editSession.flushQueue();
                } catch (MaxChangedBlocksException | IOException | DataException e) {
                    log.log(Level.INFO, "Unable to paste schematic " + file, e);
                }
            }
        });
    }

    private synchronized EditSession getEditSession(PlayerPerk playerPerk, Location origin) {
        EditSession editSession = createEditSession(BukkitUtil.getLocalWorld(origin.getWorld()), -1);
        attachProgressTracker(editSession, playerPerk);
        return editSession;
    }

    private void attachProgressTracker(EditSession editSession, PlayerPerk playerPerk) {
        try {
            PlayerProgressTracker parentTracker = getPlayerTracker(playerPerk);
            editSession.getQueue().setProgressTracker(new FAWEProgressTracker(parentTracker));
        } catch (Throwable e) {
            log.finest("Warning: Incompatible version of FAWE, no progress-tracking (" + e + ")");
            activeSessions.remove(playerPerk.getPlayerInfo().getUniqueId());
        }
    }

    private synchronized PlayerProgressTracker getPlayerTracker(PlayerPerk playerPerk) {
        if (!activeSessions.containsKey(playerPerk.getPlayerInfo().getUniqueId())) {
            PlayerProgressTracker tracker = new PlayerProgressTracker(this, playerPerk, progressEveryMs, progressEveryPct);
            activeSessions.put(playerPerk.getPlayerInfo().getUniqueId(), tracker);
        }
        return activeSessions.get(playerPerk.getPlayerInfo().getUniqueId());
    }

    public EditSession createEditSession(World bukkitWorld, int maxBlocks) {
        return WorldEdit.getInstance().getEditSessionFactory().getEditSession(bukkitWorld, maxBlocks);
    }

    @Override
    public void regenerate(final Region region, final Runnable onCompletion) {
        // NOTE: Running this asynchronous MIGHT be a bit dangereous! Since pasting could interfere
        plugin.async(new Runnable() {
            @Override
            public void run() {
                try {
                    EditSession editSession = createEditSession(region.getWorld(), -1);
                    editSession.getWorld().regenerate(region, editSession);
                    editSession.flushQueue();
                } finally {
                    onCompletion.run();
                }
            }
        });
    }
}