package us.talabrek.ultimateskyblock.handler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.TimeUtil;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Responsible for handling various cooldowns on commands.
 */
public class CooldownHandler {
    private final Map<UUID, Map<String,Long>> cooldowns = new WeakHashMap<>();
    private final uSkyBlock plugin;

    public CooldownHandler(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns the number of seconds left on the cooldown.
     * <code>0</code> if it's not on cooldown anymore.
     * @param player The player
     * @param cmd The command to check
     * @return
     */
    public int getCooldown(org.bukkit.entity.Player player, String cmd) {
        if (player.hasPermission("usb.mod.bypasscooldowns") || player.hasPermission("usb.exempt." + cmd + "Cooldown")) {
            return 0;
        }
        Map<String, Long> map = cooldowns.get(player.getUniqueId());
        if (map != null) {
            Long timeout = map.get(cmd);
            long now = System.currentTimeMillis();
            return timeout != null && timeout > now ? TimeUtil.millisAsSeconds(timeout - now) : 0;
        }
        return 0;
    }

    public void resetCooldown(final Player player, final String cmd, int cooldownSecs) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) {
            Map<String, Long> cdMap = new ConcurrentHashMap<>();
            cooldowns.put(uuid, cdMap);
        }
        if (cooldownSecs == 0) {
            cooldowns.get(uuid).remove(cmd);
            return;
        }
        cooldowns.get(uuid).put(cmd, System.currentTimeMillis() + TimeUtil.secondsAsMillis(cooldownSecs));
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Map<String, Long> cmdMap = cooldowns.get(player.getUniqueId());
                if (cmdMap != null) {
                    cmdMap.remove(cmd);
                }
            }
        }, TimeUtil.secondsAsTicks(cooldownSecs));
    }

    public boolean clearCooldown(Player player, String cmd) {
        Map<String, Long> cmdMap = cooldowns.get(player.getUniqueId());
        if (cmdMap != null) {
            return cmdMap.remove(cmd) != null;
        }
        return false;
    }

    public Map<String, Long> getCooldowns(UUID uuid) {
        if (cooldowns.containsKey(uuid)) {
            return cooldowns.get(uuid);
        }
        return Collections.emptyMap();
    }
}
