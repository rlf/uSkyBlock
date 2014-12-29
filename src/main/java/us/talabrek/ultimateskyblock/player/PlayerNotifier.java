package us.talabrek.ultimateskyblock.player;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Notifier that tries to minimize spam.
 */
public class PlayerNotifier {
    private final long maxSpam;
    private final Map<UUID, String> lastMessage = new HashMap<>();
    private final Map<UUID, Long> lastTime = new HashMap<>();

    public PlayerNotifier(FileConfiguration config) {
        maxSpam = config.getInt("general.maxSpam", 5000); // every 5 seconds.
    }

    public synchronized void notifyPlayer(Player player, String message) {
        UUID uuid = player.getUniqueId();
        String lastMsg = lastMessage.get(uuid);
        if (lastMsg != null && !lastMsg.equals(message)) {
            long now = System.currentTimeMillis();
            long last = this.lastTime.get(uuid);
            if (now >= last + maxSpam) {
                lastMessage.put(uuid, message);
                lastTime.put(uuid, now);
                player.sendMessage("\u00a7e" + message);
            }
        }
    }

    public synchronized void unloadPlayer(Player player) {
        lastMessage.remove(player.getUniqueId());
        lastTime.remove(player.getUniqueId());
    }
}
