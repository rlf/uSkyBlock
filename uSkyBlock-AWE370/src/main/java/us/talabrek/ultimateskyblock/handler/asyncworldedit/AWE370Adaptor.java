package us.talabrek.ultimateskyblock.handler.asyncworldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacerPlayer;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerManager;
import org.primesoft.asyncworldedit.api.utils.IFuncParamEx;
import org.primesoft.asyncworldedit.api.worldedit.IAsyncEditSessionFactory;
import org.primesoft.asyncworldedit.api.worldedit.ICancelabeEditSession;
import org.primesoft.asyncworldedit.api.worldedit.IThreadSafeEditSession;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.AsyncWorldEditHandler;
import us.talabrek.ultimateskyblock.player.PlayerPerk;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adaptor depending on AWE 3.7.x classes
 */
public class AWE370Adaptor implements AWEAdaptor {
    private static final Logger log = Logger.getLogger(AWE370Adaptor.class.getName());
    static long progressEveryMs = 3000; // 2 seconds
    static double progressEveryPct = 20;
    private static final Set<PlayerJob> pendingJobs = Collections.synchronizedSet(new LinkedHashSet<>());
    private uSkyBlock plugin;

    private static void updateProgress(IPlayerEntry playerEntry, int queuedBlocks, int maxQueuedBlocks) {
        if (maxQueuedBlocks <= 1) {
            return; // Not the "real" number of blocks... just ignore...
        }
        if (playerEntry != null && playerEntry.isUnknown() && playerEntry.getAweMode()) {
            synchronized (pendingJobs) {
                if (queuedBlocks == maxQueuedBlocks) {
                    // Either a fresh job, or a new merge
                    markJobs(maxQueuedBlocks, 0);
                }
                int blocksPlaced = maxQueuedBlocks - queuedBlocks;
                boolean isFirst = true;
                for (Iterator<PlayerJob> it = pendingJobs.iterator(); it.hasNext(); ) {
                    PlayerJob job = it.next();
                    int left = job.progress(blocksPlaced);
                    if (left > 0 && isFirst && pendingJobs.size() > 1) {
                        it.remove();
                        markJobs(blocksPlaced + left, queuedBlocks - left);
                    }
                    isFirst = false;
                }
            }
        }
    }

    private BukkitTask timerTask;

    private static void markJobs(int maxQueuedBlocks, int startOffset) {
        synchronized (pendingJobs) {
            int rest = maxQueuedBlocks;
            for (PlayerJob job : pendingJobs) {
                int missing = job.mark(rest, startOffset);
                rest -= missing;
                startOffset += missing;
            }
        }
    }

    private static IAsyncWorldEdit getAWE() {
        return (IAsyncWorldEdit) Bukkit.getPluginManager().getPlugin("AsyncWorldEdit");
    }

    @Override
    public void onEnable(uSkyBlock plugin) {
        this.plugin = plugin;
        progressEveryMs = plugin.getConfig().getInt("asyncworldedit.progressEveryMs", 3000);
        progressEveryPct = plugin.getConfig().getDouble("asyncworldedit.progressEveryPct", 20);
    }

    @Override
    public void registerCompletion(Player player) {
        PlayerJob newJob = new PlayerJob(player, progressEveryMs, progressEveryPct, plugin);
        pendingJobs.remove(newJob);
        pendingJobs.add(newJob);
    }

    @Override
    public void loadIslandSchematic(final File file, final Location origin, final PlayerPerk playerPerk) {
        final IAsyncWorldEdit awe = getAWE();
        BukkitWorld bukkitWorld = new BukkitWorld(origin.getWorld());
        int maxBlocks = ((bukkitWorld.getMaxY() - bukkitWorld.getMinY()) * Settings.island_protectionRange * Settings.island_protectionRange);
        IPlayerManager pm = awe.getPlayerManager();
        final IPlayerEntry playerEntry = pm.getUnknownPlayer();
        IThreadSafeEditSession tsSession = (IThreadSafeEditSession) createEditSession(bukkitWorld, maxBlocks);
        IFuncParamEx<Integer, ICancelabeEditSession, MaxChangedBlocksException> action = new PasteAction(origin, file);

        String jobName = "loadIslandSchematic";
        if (playerPerk != null) {
            Player player = Bukkit.getPlayer(playerPerk.getPlayerInfo().getUniqueId());
            registerCompletion(player);
            jobName = jobName.concat(":" + playerPerk.getPlayerInfo().getPlayerName());
        }

        awe.getBlockPlacer().performAsAsyncJob(tsSession, playerEntry, jobName, action);
        if (timerTask != null) {
            timerTask.cancel();
        }
        timerTask = plugin.async(new Runnable() {
            int maxSize = -1;
            @Override
            public void run() {
                IBlockPlacerPlayer playerEvents = awe.getBlockPlacer().getPlayerEvents(playerEntry);
                if (playerEvents != null) {
                    int size = playerEvents.getQueue().size();
                    if (maxSize == -1 || size > maxSize) {
                        maxSize = size;
                    }
                    updateProgress(playerEntry, size, maxSize);
                } else {
                    updateProgress(playerEntry, 0, maxSize);
                    timerTask.cancel();
                }
            }
        }, 500, 500);
    }

    public EditSession createEditSession(World bukkitWorld, int maxBlocks) {
        WorldEdit worldEdit = WorldEdit.getInstance();
        IAsyncEditSessionFactory sessionFactory = (IAsyncEditSessionFactory) worldEdit.getEditSessionFactory();
        return (EditSession) sessionFactory.getThreadSafeEditSession(bukkitWorld, maxBlocks, null, getAWE().getPlayerManager().getUnknownPlayer());
    }

    @Override
    public void regenerate(Region region, Runnable onCompletion) {
        AsyncWorldEditHandler.NULL_ADAPTOR.regenerate(region, onCompletion);
    }

    @Override
    public void onDisable(uSkyBlock plugin) {
    }

    private static class PasteAction implements IFuncParamEx<Integer, ICancelabeEditSession, MaxChangedBlocksException> {
        private final Location origin;
        private final File file;

        public PasteAction(Location origin, File file) {
            this.origin = origin;
            this.file = file;
        }

        public Integer execute(ICancelabeEditSession editSession) {
            ClipboardFormat format = ClipboardFormats.findByFile(file);
            if (format == null) {
                log.log(Level.SEVERE, "Unable to find schematic format for file {}", file);
                return null;
            }

            try (InputStream inStream = new FileInputStream(file)) {
                Clipboard clipboard = format.getReader(inStream).read();
                ClipboardHolder holder = new ClipboardHolder(clipboard);
                editSession.enableQueue();

                BlockVector3 to = BlockVector3.at(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
                final Operation operation = holder
                    .createPaste(editSession)
                    .to(to)
                    .ignoreAirBlocks(true)
                    .build();
                Operations.completeBlindly(operation);
                editSession.flushSession();
            } catch (IOException ex) {
                log.log(Level.WARNING, "Error while pasting schematic", ex);
            }
            return 32768;
        }
    }
}
