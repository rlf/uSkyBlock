package us.talabrek.ultimateskyblock.handler.asyncworldedit;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.WorldData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerManager;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplay;
import org.primesoft.asyncworldedit.utils.FuncParamEx;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSessionFactory;
import org.primesoft.asyncworldedit.worldedit.CancelabeEditSession;
import org.primesoft.asyncworldedit.worldedit.ThreadSafeEditSession;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.AsyncWorldEditHandler;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.player.PlayerPerk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adaptor depending on AWE 3.1.1 classes
 */
public class AWE311Adaptor implements AWEAdaptor {
    private static final Logger log = Logger.getLogger(AWE311Adaptor.class.getName());
    static long progressEveryMs = 3000; // 2 seconds
    static double progressEveryPct = 20;
    private static List<PlayerJob> pendingJobs = Collections.synchronizedList(new ArrayList<PlayerJob>());
    /**
     * Apparently, all jobs are merged, when uSkyBlock creates them through WE
     * <pre>
     *       setMessage                                   A      B     C
     * a) Job A added with 10.000 blocks
     *       queued: 10.000, max: 10.000, pct:  0%        0%     -     -
     *       queued:  8.000, max: 10.000, pct: 20%       20%     -     -
     *
     * b) Job B added with 10.000 blocks
     *       queued: 18.000, max 18.000, pct:   0%       20%     0%
     * c)    queued: 15.000, max 18.000, pct:  17%       50%     0%
     *       queued: 11.000, max 18.000, pct:  39%       90%     0%
     *
     * d) Job C added with 5.000 blocks
     *       queued: 16.000, max 16.000, pct:   0%       90%     0%    0%
     * e)    queued: 13.000, max 16.000, pct:  19%      100%    10%    0%
     * f)    queued:  4.000, max 16.000, pct:  75%      100%   100%   20%
     *
     * a) A: max: 10.000
     * b) A: progress: 2.000, B: max: 10.000 -
     * c) A: progress: 5.000
     * d) A: progress: 9.000, C: max:  5.000
     * </pre>
     */
    private static IProgressDisplay progressDisplay = new IProgressDisplay() {
        @Override
        public String getName() {
            return "uSkyBlock AWE v3.1.1 Progress";
        }

        @Override
        public void disableMessage(IPlayerEntry playerEntry) {
            log.finer("disableMessage: " + playerEntry.getName());
            if (playerEntry != null && playerEntry.isConsole() && playerEntry.getAweMode() && !pendingJobs.isEmpty()) {
                PlayerJob nextJobToComplete = findNextJobToComplete();
                log.finer("disable: " + nextJobToComplete);
                pendingJobs.remove(nextJobToComplete);
            }
        }

        @Override
        public void setMessage(IPlayerEntry playerEntry, int jobsCount,
                               int queuedBlocks, int maxQueuedBlocks, double timeLeft, double placingSpeed, double percentage) {
            // Since AWE intercepts WE, we get UNKNOWN, and the job is simply merged.
            log.finer("setMessage: " + playerEntry.getName() + ", jobsCount: " + jobsCount + ", queued: " + queuedBlocks + ", max: " + maxQueuedBlocks + ", pct=" + percentage);
            if (maxQueuedBlocks <= 1) {
                return; // Not the "real" number of blocks... just ignore...
            }
            if (playerEntry != null && playerEntry.isConsole() && playerEntry.getAweMode()) {
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
                            log.finer("remove: " + job);
                            it.remove();
                            markJobs(blocksPlaced + left, queuedBlocks - left);
                        }
                        isFirst = false;
                    }
                }
            }
        }
    };

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

    private static PlayerJob findNextJobToComplete() {
        synchronized (pendingJobs) {
            double complete = 0;
            PlayerJob match = null;
            for (PlayerJob job : pendingJobs) {
                if (job.getPercentage() > complete) {
                    complete = job.getPercentage();
                    match = job;
                }
            }
            log.finer("Completed: " + match);
            return match;
        }
    }

    private static IAsyncWorldEdit getAWE() {
        return (IAsyncWorldEdit) Bukkit.getPluginManager().getPlugin("AsyncWorldEdit");
    }

    @Override
    public void onEnable(Plugin plugin) {
        getAWE().getProgressDisplayManager().registerProgressDisplay(progressDisplay);
        progressEveryMs = plugin.getConfig().getInt("asyncworldedit.progressEveryMs", 3000);
        progressEveryPct = plugin.getConfig().getDouble("asyncworldedit.progressEveryPct", 20);
    }

    @Override
    public void registerCompletion(Player player) {
        pendingJobs.add(new PlayerJob(player, progressEveryMs, progressEveryPct));
    }

    @Override
    public void loadIslandSchematic(final File file, final Location origin, final PlayerPerk playerPerk) {
        IAsyncWorldEdit awe = getAWE();
        BukkitWorld bukkitWorld = new BukkitWorld(origin.getWorld());
        Player player = Bukkit.getPlayer(playerPerk.getPlayerInfo().getUniqueId());
        int maxBlocks = (255 * Settings.island_protectionRange * Settings.island_protectionRange);
        IPlayerManager pm = awe.getPlayerManager();
        IPlayerEntry playerEntry = pm.getConsolePlayer();
        ThreadSafeEditSession tsSession = createEditSession(bukkitWorld, maxBlocks);
        FuncParamEx<Integer, CancelabeEditSession, MaxChangedBlocksException> action = new PasteAction(bukkitWorld, origin, file);
        registerCompletion(player);
        awe.getBlockPlacer().performAsAsyncJob(tsSession, playerEntry, "loadIslandSchematic", action);
    }

    public ThreadSafeEditSession createEditSession(World bukkitWorld, int maxBlocks) {
        WorldEdit worldEdit = WorldEditHandler.getWorldEdit().getWorldEdit();
        AsyncEditSessionFactory sessionFactory = (AsyncEditSessionFactory) worldEdit.getEditSessionFactory();
        return sessionFactory.getThreadSafeEditSession(bukkitWorld, maxBlocks);
    }

    @Override
    public void regenerate(Region region, Runnable onCompletion) {
        AsyncWorldEditHandler.NULL_ADAPTOR.regenerate(region, onCompletion);
    }

    @Override
    public void onDisable(Plugin plugin) {
        getAWE().getProgressDisplayManager().unregisterProgressDisplay(progressDisplay);
    }

    private static class PasteAction implements FuncParamEx<Integer, CancelabeEditSession, MaxChangedBlocksException> {
        private final BukkitWorld bukkitWorld;
        private final Location origin;
        private final File file;

        public PasteAction(BukkitWorld bukkitWorld, Location origin, File file) {
            this.bukkitWorld = bukkitWorld;
            this.origin = origin;
            this.file = file;
        }

        public Integer execute(CancelabeEditSession editSession) throws MaxChangedBlocksException {
            try {
                ClipboardReader reader = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(file));
                WorldData worldData = bukkitWorld.getWorldData();
                Clipboard clipboard = reader.read(worldData);
                ClipboardHolder holder = new ClipboardHolder(clipboard, worldData);
                editSession.enableQueue();
                editSession.setFastMode(true);
                Vector to = new Vector(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
                final Operation operation = holder
                        .createPaste(editSession, worldData)
                        .to(to)
                        .ignoreAirBlocks(true)
                        .build();
                Operations.completeBlindly(operation);
                editSession.flushQueue();
            } catch (IOException e) {
                log.log(Level.WARNING, "Error trying to paste " + file, e);
            }
            return 32768;
        }
    }
}
