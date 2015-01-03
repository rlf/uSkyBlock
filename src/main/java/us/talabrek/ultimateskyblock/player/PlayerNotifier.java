package us.talabrek.ultimateskyblock.player;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Notifier that tries to minimize spam.
 */
public class PlayerNotifier {
    private final long maxSpam;
    private final Map<UUID, String> lastMessage = new WeakHashMap<>();
    private final Map<UUID, Long> lastTime = new WeakHashMap<>();

    public PlayerNotifier(FileConfiguration config) {
        maxSpam = config.getInt("general.maxSpam", 3000); // every 3 seconds.
    }

    public synchronized void notifyPlayer(Player player, String message) {
        UUID uuid = player.getUniqueId();
        String lastMsg = lastMessage.get(uuid);
        long now = System.currentTimeMillis();
        Long last = lastTime.get(uuid);
        if (last == null || now >= last + maxSpam || !message.equals(lastMsg)) {
            lastMessage.put(uuid, message);
            lastTime.put(uuid, now);
            player.sendMessage("\u00a7e" + message);
        }
    }

    public synchronized void unloadPlayer(Player player) {
        lastMessage.remove(player.getUniqueId());
        lastTime.remove(player.getUniqueId());
    }
}
