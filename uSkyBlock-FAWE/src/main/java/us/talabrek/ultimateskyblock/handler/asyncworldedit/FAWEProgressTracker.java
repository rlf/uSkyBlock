package us.talabrek.ultimateskyblock.handler.asyncworldedit;

import com.boydti.fawe.object.RunnableVal2;
import com.boydti.fawe.util.FaweQueue;
import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import us.talabrek.ultimateskyblock.handler.ActionBarHandler;
import us.talabrek.ultimateskyblock.handler.titlemanager.TitleManagerAdaptor;
import us.talabrek.ultimateskyblock.player.PlayerPerk;

/**
 * Progress tracker for FAWE integration
 */
public class FAWEProgressTracker extends RunnableVal2<FaweQueue.ProgressType, Integer> {
    private final PlayerPerk playerPerk;
    private final int progressEveryMs;
    private final double progressEveryPct;

    private int blocksQueued = 0;
    private int blocksPlaced = 0;

    private double lastProgressPct = 0;
    private long lastProgressTime = 0;

    public FAWEProgressTracker(PlayerPerk playerPerk, int progressEveryMs, double progressEveryPct) {
        this.playerPerk = playerPerk;
        this.progressEveryMs = progressEveryMs;
        this.progressEveryPct = progressEveryPct;
    }

    @Override
    public void run(FaweQueue.ProgressType progressType, Integer amount) {
        switch (progressType) {
            case DISPATCH:
                blocksPlaced = amount;
                break;
            case QUEUE:
                blocksQueued = amount;
                break;
            case DONE:
                blocksPlaced = blocksQueued;
                break;
        }
        send();
    }

    private void send() {
        long t = System.currentTimeMillis();
        double pct = blocksQueued != 0 ? blocksPlaced*1d / blocksQueued : 0;
        if (t >= (lastProgressTime+progressEveryMs) || pct >= (lastProgressPct + progressEveryPct) || pct >= 1) {
            lastProgressPct = pct;
            lastProgressTime = t;
            // TODO: 14/06/2016 - R4zorax: Is this the main server thread?
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerPerk.getPlayerInfo().getUniqueId());
            if (offlinePlayer != null && offlinePlayer.isOnline()) {
                ActionBarHandler.sendActionBar(offlinePlayer.getPlayer(), I18nUtil.tr("\u00a79Creating island...\u00a7e{0,number,###}%", pct));
            }
        }
    }
}