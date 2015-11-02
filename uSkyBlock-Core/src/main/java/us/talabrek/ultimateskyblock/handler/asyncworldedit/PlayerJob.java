package us.talabrek.ultimateskyblock.handler.asyncworldedit;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.handler.ActionBarHandler;
import us.talabrek.ultimateskyblock.util.I18nUtil;

import java.util.logging.Logger;

/**
 * Internal state of a job
 */
class PlayerJob {
    private static final Logger log = Logger.getLogger(PlayerJob.class.getName());
    private final Player player;
    private final long progressEveryMs;
    private final double progressEveryPct;
    private long lastProgressMs;
    private double percentage;
    private double lastProgressPct;

    /**
     * The number of blocks placed in previous jobs
     */
    private int offset = 0;
    private int placedBlocks;
    private int maxQueuedBlocks;
    private int startOffset;

    PlayerJob(Player player, long progressEveryMs, double progressEveryPct) {
        this.player = player;
        this.progressEveryMs = progressEveryMs;
        this.progressEveryPct = progressEveryPct;
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
        if (blocksPlaced < startOffset) {
            showProgress(I18nUtil.tr("\u00a7eWaiting for our turn \u00a7c{0,number,###}%", 100d * blocksPlaced / startOffset));
            log.finer("waiting: " + this);
            return blocksPlaced;
        }
        this.placedBlocks = Math.min(blocksPlaced-startOffset, (maxQueuedBlocks-offset));
        this.percentage = Math.floor(Math.min((100d * getPlacedBlocks() / maxQueuedBlocks), 100));
        showProgress(I18nUtil.tr("\u00a79Creating island...\u00a7e{0,number,###}%", percentage));
        log.finer("progress: " + this);
        return blocksPlaced-placedBlocks;
    }

    private void showProgress(String message) {
        long t = System.currentTimeMillis();
        if (t > (lastProgressMs + progressEveryMs) || percentage > (lastProgressPct + progressEveryPct)) {
            if (ActionBarHandler.isEnabled()) {
                ActionBarHandler.sendActionBar(player, message);
            } else {
                player.sendMessage(message);
            }
            lastProgressMs = t;
            lastProgressPct = Math.floor(percentage/ progressEveryPct) * progressEveryPct;
        }
    }

    public int mark(int max, int startAt) {
        log.finer("mark(" + max + "," + startAt + ")");
        if ((placedBlocks + offset) == 0) {
            this.startOffset = startAt;
            if (maxQueuedBlocks == 0) {
                this.maxQueuedBlocks = max;
            }
        } else {
            offset += placedBlocks;
            placedBlocks = 0;
        }
        log.finer("mark: " + this);
        return maxQueuedBlocks - (offset + placedBlocks);
    }

    @Override
    public String toString() {
        return "PlayerJob{" +
                "player=" + player +
                ", startOffset=" + startOffset +
                ", offset=" + offset +
                ", placedBlocks=" + placedBlocks +
                ", maxQueuedBlocks=" + maxQueuedBlocks +
                ", percentage=" + percentage +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PlayerJob && ((PlayerJob)o).getPlayer().getUniqueId().equals(player.getUniqueId());
    }

    @Override
    public int hashCode() {
        return player != null ? player.getUniqueId().hashCode() : 0;
    }
}
