package us.talabrek.ultimateskyblock.handler.asyncworldedit;

import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import us.talabrek.ultimateskyblock.handler.ActionBarHandler;
import us.talabrek.ultimateskyblock.player.PlayerPerk;

import java.util.ArrayList;
import java.util.List;

/**
 * Consolidated progress-tracker for multiple simultaneous edit-session
 */
public class PlayerProgressTracker {
    private final FAWEAdaptor adaptor;
    private final PlayerPerk perk;
    private final int progressEveryMs;
    private final double progressEveryPct;

    List<FAWEProgressTracker> trackers = new ArrayList<>();

    private long lastProgressTime;
    private double lastProgressPct;

    public PlayerProgressTracker(FAWEAdaptor adaptor, PlayerPerk perk, int progressEveryMs, double progressEveryPct) {
        this.adaptor = adaptor;
        this.perk = perk;
        this.progressEveryMs = progressEveryMs;
        this.progressEveryPct = progressEveryPct;
    }

    void addTracker(FAWEProgressTracker tracker) {
        trackers.add(tracker);
    }

    void removeTracker(FAWEProgressTracker tracker) {
        trackers.remove(tracker);
    }

    boolean isDone() {
        for (FAWEProgressTracker tracker : trackers) {
            if (!tracker.isDone()) {
                return false;
            }
        }
        return true;
    }

    void tick() {
        long t = System.currentTimeMillis();
        long totalChunks = getTotal();
        double pct = totalChunks != 0 ? getCompleted()*100d / totalChunks : 0;
        if (t >= (lastProgressTime+progressEveryMs) || pct >= (lastProgressPct + progressEveryPct)) {
            lastProgressPct = pct;
            lastProgressTime = t;
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(perk.getPlayerInfo().getUniqueId());
            if (offlinePlayer != null && offlinePlayer.isOnline()) {
                ActionBarHandler.sendActionBar(offlinePlayer.getPlayer(), I18nUtil.tr("\u00a79Creating island...\u00a7e{0,number,###}%", pct));
            }
        }
        if (isDone()) {
            adaptor.registerCompletion(Bukkit.getPlayer(perk.getPlayerInfo().getUniqueId()));
        }
    }

    private int getTotal() {
        int total = 0;
        for (FAWEProgressTracker tracker : trackers) {
            total += tracker.getTotal();
        }
        return total;
    }

    private int getCompleted() {
        int completed = 0;
        for (FAWEProgressTracker tracker : trackers) {
            completed += tracker.getCompleted();
        }
        return completed;
    }
}
