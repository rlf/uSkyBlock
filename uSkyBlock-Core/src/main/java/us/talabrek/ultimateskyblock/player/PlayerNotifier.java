package us.talabrek.ultimateskyblock.player;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Notifier that tries to minimize spam.
 */
public class PlayerNotifier {
    private final long maxSpam;
    private final LoadingCache<UUID, NotifyMessage> cache = CacheBuilder
            .newBuilder()
            .expireAfterAccess(10, TimeUnit.SECONDS)
            .maximumSize(500)
            .build(
                    new CacheLoader<UUID, NotifyMessage>() {
                        @Override
                        public NotifyMessage load(UUID uuid) throws Exception {
                            return new NotifyMessage(null, 0);
                        }
                    }
            );

    public PlayerNotifier(FileConfiguration config) {
        maxSpam = config.getInt("general.maxSpam", 3000); // every 3 seconds.
    }

    public synchronized void notifyPlayer(Player player, String message) {
        UUID uuid = player.getUniqueId();
        try {
            NotifyMessage last = cache.get(uuid);
            long now = System.currentTimeMillis();
            if (now >= last.getTime() + maxSpam || !message.equals(last.getMessage())) {
                cache.put(uuid, new NotifyMessage(message, now));
                player.sendMessage("\u00a7e" + message);
            }
        } catch (ExecutionException e) {
            // Just ignore - we don't care that much
        }
    }

    private static class NotifyMessage {
        private final String message;
        private final long time;

        private NotifyMessage(String message, long time) {
            this.message = message;
            this.time = time;
        }

        public String getMessage() {
            return message;
        }

        public long getTime() {
            return time;
        }
    }
}
