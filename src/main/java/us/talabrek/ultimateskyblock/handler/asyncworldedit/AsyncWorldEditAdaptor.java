package us.talabrek.ultimateskyblock.handler.asyncworldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplay;
import org.primesoft.asyncworldedit.injector.classfactory.IJob;
import org.primesoft.asyncworldedit.injector.core.InjectorCore;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.handler.worldedit.ConsolePlayer;
import us.talabrek.ultimateskyblock.player.PlayerPerk;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.VersionUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class AsyncWorldEditAdaptor {
    private static final Logger log = Logger.getLogger(AsyncWorldEditAdaptor.class.getName());

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
            return "uSkyBlock AWE Progress";
        }

        @Override
        public void disableMessage(PlayerEntry playerEntry) {
            log.finer("disableMessage: " + playerEntry.getName());
            if (playerEntry != null && playerEntry.isUnknown() && playerEntry.getMode() && !pendingJobs.isEmpty()) {
                PlayerJob nextJobToComplete = findNextJobToComplete();
                log.finer("disable: " + nextJobToComplete);
                pendingJobs.remove(nextJobToComplete);
            }
        }

        @Override
        public void setMessage(PlayerEntry playerEntry, int jobsCount,
                               int queuedBlocks, int maxQueuedBlocks, double timeLeft, double placingSpeed, double percentage) {
            // Since AWE intercepts WE, we get UNKNOWN, and the job is simply merged.
            log.finer("setMessage: " + playerEntry.getName() + ", jobsCount: " + jobsCount + ", queued: " + queuedBlocks + ", max: " + maxQueuedBlocks + ", pct=" + percentage);
            if (playerEntry != null && playerEntry.isUnknown() && playerEntry.getMode()) {
                synchronized (pendingJobs) {
                    if (queuedBlocks == maxQueuedBlocks) {
                        // Either a fresh job, or a new merge
                        markJobs(maxQueuedBlocks, 0);
                    }
                    int blocksPlaced = maxQueuedBlocks - queuedBlocks;
                    boolean isFirst = true;
                    for (Iterator<PlayerJob> it = pendingJobs.iterator(); it.hasNext(); ) {
                        PlayerJob job = it.next();
                        if (job.progress(blocksPlaced) > 0 && isFirst && pendingJobs.size() > 1) {
                            log.finer("remove: " + job);
                            it.remove();
                            markJobs(maxQueuedBlocks, queuedBlocks - blocksPlaced);
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

    public static void onEnable(uSkyBlock plugin) {
        if (isAWE()) {
            AsyncWorldEditMain awe = getAWE();
            awe.getProgressDisplayManager().registerProgressDisplay(progressDisplay);
            progressEveryMs = plugin.getConfig().getInt("asyncworldedit.progressEveryMs", 3000);
            progressEveryPct = plugin.getConfig().getDouble("asyncworldedit.progressEveryPct", 20);
        }
    }

    public static void onDisable(uSkyBlock plugin) {
        if (isAWE()) {
            AsyncWorldEditMain awe = getAWE();
            awe.getProgressDisplayManager().unregisterProgressDisplay(progressDisplay);
        }
    }

    private static AsyncWorldEditMain getAWE() {
        return (AsyncWorldEditMain) Bukkit.getPluginManager().getPlugin("AsyncWorldEdit");
    }

    public static boolean isAWE() {
        return Bukkit.getPluginManager().isPluginEnabled("AsyncWorldEdit") && VersionUtil.getVersion(getAWE().getDescription().getVersion()).isLT("3.0");
    }

    public static void registerCompletion(Player player) {
        if (isAWE()) {
            pendingJobs.add(new PlayerJob(player));
        }
    }

    public static EditSession createSession(BukkitWorld world, int maxblocks) {
        return WorldEditHandler.getWorldEdit().getWorldEdit().getEditSessionFactory().getEditSession(world, maxblocks);
    }

    public static void loadIslandSchematic(final File file, final Location origin, final PlayerPerk playerPerk) {
        InjectorCore.getInstance().getClassFactory().getJobProcessor().executeJob(ConsolePlayer.getInstance(), new IJob() {
            @Override
            public String getName() {
                return "loadIslandSchematic";
            }

            @Override
            public void execute() {
                WorldEditHandler.loadIslandSchematic(file, origin, playerPerk);
                /*
                log.finer("Trying to load schematic " + file);
                try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
                    BukkitWorld bukkitWorld = new BukkitWorld(origin.getWorld());
                    ClipboardReader reader = ClipboardFormat.SCHEMATIC.getReader(in);

                    WorldData worldData = bukkitWorld.getWorldData();
                    Clipboard clipboard = reader.read(worldData);
                    ClipboardHolder holder = new ClipboardHolder(clipboard, worldData);

                    Player player = Bukkit.getPlayer(playerPerk.getPlayerInfo().getUniqueId());
                    int maxBlocks = (255 * Settings.island_protectionRange * Settings.island_protectionRange);
                    final EditSession editSession = AsyncWorldEditHandler.createEditSession(bukkitWorld, maxBlocks);
                    editSession.enableQueue();
                    editSession.setFastMode(true);
                    Vector to = new Vector(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
                    final Operation operation = holder
                            .createPaste(editSession, worldData)
                            .to(to)
                            .ignoreAirBlocks(false)
                            .build();
                    Operations.completeBlindly(operation);
                    AsyncWorldEditHandler.registerCompletion(player);
                    editSession.flushQueue();
                } catch (IOException e) {
                    uSkyBlock.log(Level.WARNING, "Unable to load schematic " + file, e);
                }
                */
            }
        });
    }
}
