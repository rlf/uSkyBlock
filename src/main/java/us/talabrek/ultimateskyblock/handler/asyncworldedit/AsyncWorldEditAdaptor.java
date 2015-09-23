package us.talabrek.ultimateskyblock.handler.asyncworldedit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplay;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Deque;
import java.util.LinkedList;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class AsyncWorldEditAdaptor {
    private static long progressEveryMs = 3000; // 2 seconds
    private static double progressEveryPct = 20;
    private static Deque<PlayerJob> pendingJobs = new LinkedList<>();
    private static IProgressDisplay progressDisplay = new IProgressDisplay() {
        @Override
        public String getName() {
            return "uSkyBlock Progress WatchDog";
        }

        @Override
        public void disableMessage(PlayerEntry playerEntry) {
            if (playerEntry.isUnknown() && playerEntry.getMode() && !pendingJobs.isEmpty()) {
                PlayerJob job = pendingJobs.pop();
                if (job != null && job.getJob() != null) {
                    Bukkit.getScheduler().runTask(uSkyBlock.getInstance(), job.getJob());
                }
            }
            System.out.println("disableMessage " + asString(playerEntry) + ", pending jobs: " + pendingJobs.size());
        }

        @Override
        public void setMessage(PlayerEntry playerEntry, int jobsCount,
                               int queuedBlocks, int maxQueuedBlocks, double timeLeft, double placingSpeed, double percentage) {
            System.out.println("setMessage " + asString(playerEntry) + ", pending jobs: " + pendingJobs.size() + ", jobsCount: " + jobsCount);
            if (playerEntry.isUnknown() && playerEntry.getMode()) {
                if (!pendingJobs.isEmpty()) {
                    PlayerJob peek = pendingJobs.peek();
                    peek.progress(timeLeft, placingSpeed, percentage);
                }
            }
        }
    };

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

    public static void registerCompletion(Player player, final Runnable runnable) {
        if (isAWE()) {
            pendingJobs.add(new PlayerJob(player, runnable));
        } else {
            Bukkit.getScheduler().runTaskLater(uSkyBlock.getInstance(), runnable, 5);
        }
    }
    private static String asString(PlayerEntry playerEntry) {
        return "" + playerEntry.getName() + ", mode=" + playerEntry.getMode() + ", uuid=" + playerEntry.getUUID() + ", player= " + playerEntry.getPlayer();
    }

    private static class PlayerJob {
        private final Player player;
        private final Runnable job;
        private long lastProgressMs;
        private double lastProgressPct;

        private PlayerJob(Player player, Runnable job) {
            this.player = player;
            this.job = job;
            lastProgressMs = System.currentTimeMillis();
            lastProgressPct = 0;
        }

        public Player getPlayer() {
            return player;
        }

        public Runnable getJob() {
            return job;
        }

        public void progress(double timeLeft, double placingSpeed, double percentage) {
            long t = System.currentTimeMillis();
            if (t > (lastProgressMs + progressEveryMs) || percentage > (lastProgressPct + progressEveryPct)) {
                player.sendMessage(tr("\u00a7cSorry for the delay! \u00a79Your island is now \u00a7e{1,number,##}%\u00a79 done...", timeLeft, percentage));
                lastProgressMs = t;
                lastProgressPct = Math.floor(percentage/progressEveryPct) * progressEveryPct;
            }
        }
    }
}
