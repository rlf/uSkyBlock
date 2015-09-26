package us.talabrek.ultimateskyblock.handler.asyncworldedit;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.handler.ActionBarHandler;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * Internal state of a job
 */
class PlayerJob {
    private final Player player;
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

    PlayerJob(Player player) {
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
        this.placedBlocks = Math.min(blocksPlaced-startOffset, (maxQueuedBlocks-offset));
        this.percentage = (100d*getPlacedBlocks() / maxQueuedBlocks);
        long t = System.currentTimeMillis();
        if (t > (lastProgressMs + AsyncWorldEditAdaptor.progressEveryMs) || percentage > (lastProgressPct + AsyncWorldEditAdaptor.progressEveryPct)) {
            if (ActionBarHandler.isEnabled()) {
                ActionBarHandler.sendActionBar(player, tr("\u00a79Creating island...\u00a7e{0,number,###}%", percentage));
            } else {
                player.sendMessage(tr("\u00a7cSorry for the delay! \u00a79Your island is now \u00a7e{0,number,##}%\u00a79 done...", percentage));
            }
            lastProgressMs = t;
            lastProgressPct = Math.floor(percentage/ AsyncWorldEditAdaptor.progressEveryPct) * AsyncWorldEditAdaptor.progressEveryPct;
        }
        return blocksPlaced-placedBlocks;
    }

    public int mark(int maxQueuedBlocks, int startOffset) {
        this.startOffset = startOffset;
        if (this.maxQueuedBlocks == 0) {
            this.maxQueuedBlocks = maxQueuedBlocks;
        } else {
            this.offset += placedBlocks;
        }
        return (this.maxQueuedBlocks - this.offset) + startOffset;
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
