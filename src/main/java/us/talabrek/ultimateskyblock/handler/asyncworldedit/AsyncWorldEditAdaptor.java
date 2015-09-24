package us.talabrek.ultimateskyblock.handler.asyncworldedit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplay;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import us.talabrek.ultimateskyblock.handler.ActionBarHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class AsyncWorldEditAdaptor {
    private static long progressEveryMs = 3000; // 2 seconds
    private static double progressEveryPct = 20;
    private static List<PlayerJob> pendingJobs = Collections.synchronizedList(new ArrayList<PlayerJob>());
    private static IProgressDisplay progressDisplay = new IProgressDisplay() {
        @Override
        public String getName() {
            return "uSkyBlock Progress WatchDog";
        }

        @Override
        public void disableMessage(PlayerEntry playerEntry) {
            if (playerEntry.isUnknown() && playerEntry.getMode() && !pendingJobs.isEmpty()) {
                pendingJobs.remove(findNextJobToComplete());
            }
        }

        @Override
        public void setMessage(PlayerEntry playerEntry, int jobsCount,
                               int queuedBlocks, int maxQueuedBlocks, double timeLeft, double placingSpeed, double percentage) {
            if (playerEntry.isUnknown() && playerEntry.getMode()) {
                synchronized (pendingJobs) {
                    if (!pendingJobs.isEmpty()) {
                        PlayerJob peek = findJob(queuedBlocks, maxQueuedBlocks, percentage);
                        if (peek != null) {
                            peek.progress(queuedBlocks, maxQueuedBlocks, percentage);
                        }
                    }
                }
            }
        }
    };

    private static PlayerJob findNextJobToComplete() {
        synchronized (pendingJobs) {
            long blocksShort = Integer.MAX_VALUE;
            PlayerJob match = null;
            for (PlayerJob job : pendingJobs) {
                if (job.getQueuedBlocks() < blocksShort) {
                    blocksShort = job.getQueuedBlocks();
                    match = job;
                }
            }
            return match;
        }
    }

    /**
     * Finds the best matching job on the queue.
     */
    private static PlayerJob findJob(int queuedBlocks, int maxQueuedBlocks, double percentage) {
        synchronized (pendingJobs) {
            for (PlayerJob job : pendingJobs) {
                if (job.getMaxQueuedBlocks() == maxQueuedBlocks && job.getPercentage() < percentage && job.getQueuedBlocks() > queuedBlocks) {
                    // The first on the queue SHOULD be the one that matches... not 100% accurate
                    return job;
                }
            }
            // If we get here, nothing matched, try the new ones...
            for (PlayerJob job : pendingJobs) {
                if (job.getMaxQueuedBlocks() == 0) {
                    // The first on the queue SHOULD be the one that matches... not 100% accurate
                    return job;
                }
            }
            return null;
        }
    }

    public static void onEnable(uSkyBlock plugin) {
        if (isAWE()) {
            IAsyncWorldEdit awe = getAWE();
            awe.getProgressDisplayManager().registerProgressDisplay(progressDisplay);
            progressEveryMs = plugin.getConfig().getInt("asyncworldedit.progressEveryMs", 3000);
            progressEveryPct = plugin.getConfig().getDouble("asyncworldedit.progressEveryPct", 20);
        }
    }

    public static void onDisable(uSkyBlock plugin) {
        if (isAWE()) {
            IAsyncWorldEdit awe = getAWE();
            awe.getProgressDisplayManager().unregisterProgressDisplay(progressDisplay);
        }
    }

    private static IAsyncWorldEdit getAWE() {
        return (IAsyncWorldEdit) Bukkit.getPluginManager().getPlugin("AsyncWorldEdit");
    }

    public static boolean isAWE() {
        return Bukkit.getPluginManager().isPluginEnabled("AsyncWorldEdit");
    }

    public static void registerCompletion(Player player) {
        if (isAWE()) {
            pendingJobs.add(new PlayerJob(player));
        }
    }

    private static class PlayerJob {
        private final Player player;
        private long lastProgressMs;
        private double percentage;
        private double lastProgressPct;
        private int queuedBlocks;
        private int maxQueuedBlocks;

        private PlayerJob(Player player) {
            this.player = player;
            lastProgressMs = System.currentTimeMillis();
            lastProgressPct = 0;
            queuedBlocks = 0;
            maxQueuedBlocks = 0;
            percentage = 0;
        }

        public double getPercentage() {
            return percentage;
        }

        public int getQueuedBlocks() {
            return queuedBlocks;
        }

        public int getMaxQueuedBlocks() {
            return maxQueuedBlocks;
        }

        public Player getPlayer() {
            return player;
        }

        public void progress(int queuedBlocks, int maxQueuedBlocks, double percentage) {
            this.queuedBlocks = queuedBlocks;
            this.maxQueuedBlocks = maxQueuedBlocks;
            this.percentage = percentage;
            long t = System.currentTimeMillis();
            if (t > (lastProgressMs + progressEveryMs) || percentage > (lastProgressPct + progressEveryPct)) {
                if (ActionBarHandler.isEnabled()) {
                    ActionBarHandler.sendActionBar(player, tr("\u00a79Creating island...\u00a7e{0,number,###}%", percentage));
                } else {
                    player.sendMessage(tr("\u00a7cSorry for the delay! \u00a79Your island is now \u00a7e{0,number,##}%\u00a79 done...", percentage));
                }
                lastProgressMs = t;
                lastProgressPct = Math.floor(percentage/progressEveryPct) * progressEveryPct;
            }
        }
    }
}
