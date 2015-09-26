package us.talabrek.ultimateskyblock.handler.asyncworldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplay;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;
import us.talabrek.ultimateskyblock.handler.ActionBarHandler;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.VersionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class AsyncWorldEditAdaptor {
    private static long progressEveryMs = 3000; // 2 seconds
    private static double progressEveryPct = 20;
    private static List<PlayerJob> pendingJobs = Collections.synchronizedList(new ArrayList<PlayerJob>());
    private static IProgressDisplay progressDisplay = new IProgressDisplay() {
        @Override
        public String getName() {
            return "uSkyBlock AWE Progress";
        }

        @Override
        public void disableMessage(PlayerEntry playerEntry) {
            //System.out.println("disableMessage: " + playerEntry.getName());
            if (playerEntry != null && playerEntry.isUnknown() && playerEntry.getMode() && !pendingJobs.isEmpty()) {
                pendingJobs.remove(findNextJobToComplete());
            }
        }

        @Override
        public void setMessage(PlayerEntry playerEntry, int jobsCount,
                               int queuedBlocks, int maxQueuedBlocks, double timeLeft, double placingSpeed, double percentage) {
            // Since AWE intercepts WE, we get UNKNOWN, and the job is simply merged.
            //System.out.println("setMessage: " + playerEntry.getName() + ", jobsCount: " + jobsCount + ", queued: " + queuedBlocks + ", max: " + maxQueuedBlocks + ", pct=" + percentage);
            if (playerEntry != null && playerEntry.isUnknown() && playerEntry.getMode()) {
                synchronized (pendingJobs) {
                    if (queuedBlocks == maxQueuedBlocks) {
                        // Either a fresh job, or a new merge
                        markJobs(maxQueuedBlocks);
                    }
                    int rest = maxQueuedBlocks - queuedBlocks;
                    for (Iterator<PlayerJob> it = pendingJobs.iterator(); it.hasNext(); ) {
                        PlayerJob job = it.next();
                        rest = job.progress(rest);
                        if (rest > 0) {
                            it.remove();
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    };

    private static void markJobs(int maxQueuedBlocks) {
        synchronized (pendingJobs) {
            int rest = maxQueuedBlocks;
            for (PlayerJob job : pendingJobs) {
                rest -= job.mark(rest);
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
            System.out.println("Completed: " + match);
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
        AsyncWorldEditMain awe = getAWE();
        WorldEditPlugin we = WorldEditHandler.getWorldEdit();
        com.sk89q.worldedit.util.eventbus.EventBus eventBus = we.getWorldEdit().getEventBus();
        Actor actor = WorldEditHandler.createActor();
        EditSessionEvent event = new EditSessionEvent(world, actor, maxblocks, EditSession.Stage.BEFORE_HISTORY);
        return new AsyncEditSession(awe, PlayerEntry.UNKNOWN, eventBus, world, maxblocks, null, event);
    }

    private static class PlayerJob {
        private final Player player;
        private long lastProgressMs;
        private double percentage;
        private double lastProgressPct;

        private int offset = 0;
        private int placedBlocks;
        private int maxQueuedBlocks;

        private PlayerJob(Player player) {
            this.player = player;
            lastProgressMs = System.currentTimeMillis();
            lastProgressPct = 0;
            placedBlocks = 0;
            maxQueuedBlocks = 0;
            percentage = 0;
        }

        public double getPercentage() {
            return percentage;
        }

        public int getPlacedBlocks() {
            return offset + placedBlocks;
        }

        public Player getPlayer() {
            return player;
        }

        public int progress(int blocksPlaced) {
            this.placedBlocks = Math.min(blocksPlaced, (maxQueuedBlocks-offset));
            this.percentage = (100d*getPlacedBlocks() / maxQueuedBlocks);
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
            return blocksPlaced-placedBlocks;
        }

        public int mark(int maxQueuedBlocks) {
            if (this.maxQueuedBlocks == 0) {
                this.maxQueuedBlocks = maxQueuedBlocks;
            } else {
                this.offset += placedBlocks;
            }
            return this.maxQueuedBlocks - this.offset;
        }

        @Override
        public String toString() {
            return "PlayerJob{" +
                    "player=" + player +
                    ", percentage=" + percentage +
                    ", offset=" + offset +
                    ", placedBlocks=" + placedBlocks +
                    ", maxQueuedBlocks=" + maxQueuedBlocks +
                    '}';
        }
    }
}
