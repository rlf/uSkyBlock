package us.talabrek.ultimateskyblock.util;

import org.bukkit.command.CommandSender;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * General progress tracker using throttling
 */
public class ProgressTracker {
    private double progressEveryPct;
    private long progressEveryMs;
    private final String format;
    private final CommandSender sender;

    private long lastProgressTime;
    private float lastProgressPct;

    public ProgressTracker(CommandSender sender, String format, double progressEveryPct, long progressEveryMs) {
        this.progressEveryPct = progressEveryPct;
        this.progressEveryMs = progressEveryMs;
        this.format = format;
        this.sender = sender;
    }

    public void progressUpdate(long progress, long total, Object... args) {
        long now = System.currentTimeMillis();
        float pct = 100f * progress / (total > 0 ? total : 1);
        if (now > (lastProgressTime + progressEveryMs) || pct > (lastProgressPct + progressEveryPct)) {
            lastProgressPct = pct;
            lastProgressTime = now;
            Object[] newArgs = new Object[args.length + 3];
            newArgs[0] = pct;
            newArgs[1] = progress;
            newArgs[2] = total;
            System.arraycopy(args, 0, newArgs, 3, args.length);
            sender.sendMessage(tr(format, newArgs));
        }
    }
}
