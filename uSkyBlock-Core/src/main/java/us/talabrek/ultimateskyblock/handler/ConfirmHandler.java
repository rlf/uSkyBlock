package us.talabrek.ultimateskyblock.handler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.I18nUtil;
import us.talabrek.ultimateskyblock.util.TimeUtil;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Handles confirms.
 */
public class ConfirmHandler {
    private final Map<UUID, ConfirmCommand> confirmMap = new WeakHashMap<>();
    private final uSkyBlock plugin;
    private final int timeout;

    public ConfirmHandler(uSkyBlock plugin, int timeout) {
        this.plugin = plugin;
        this.timeout = timeout;
    }

    public boolean checkCommand(final Player player, final String cmd) {
        if (!confirmationsActiveFor(cmd)) {
            return true;
        }
        UUID uuid = player.getUniqueId();
        if (confirmMap.containsKey(uuid)) {
            ConfirmCommand confirmCommand = confirmMap.get(uuid);
            if (confirmCommand != null && confirmCommand.isValid(cmd, timeout)) {
                confirmMap.remove(uuid);
                return true;
            }
        }
        confirmMap.put(uuid, new ConfirmCommand(cmd));
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                ConfirmCommand confirmCommand = confirmMap.remove(player.getUniqueId());
                if (confirmCommand != null && player != null && player.isOnline()) {
                    player.sendMessage(I18nUtil.tr("\u00a79{0}\u00a77 timed out", cmd));
                }
            }
        }, TimeUtil.secondsAsTicks(timeout));
        player.sendMessage(I18nUtil.tr("\u00a7eDoing \u00a79{0}\u00a7e is \u00a7cRISKY\u00a7e. Repeat the command within \u00a7a{1}\u00a7e seconds to accept!", cmd, timeout));
        return false;
    }

    private boolean confirmationsActiveFor(String cmd) {
        return plugin.getConfig().getBoolean("options.confirmation." + cmd.replaceAll("[^a-z\\ ]", ""), true);
    }

    private static class ConfirmCommand {
        private final String cmd;
        private final long tstamp;

        private ConfirmCommand(String cmd) {
            this.cmd = cmd;
            this.tstamp = System.currentTimeMillis();
        }

        public boolean isValid(String cmd, long timeout) {
            return this.cmd.equals(cmd) && tstamp >= (System.currentTimeMillis() - TimeUtil.secondsAsMillis(timeout));
        }
    }
}
