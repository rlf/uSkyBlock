package us.talabrek.ultimateskyblock.uuid;

import dk.lockfuglsang.minecraft.file.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.UUIDUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PlayerDB backed by a simple yml-file.
 */
public class FilePlayerDB implements PlayerDB {
    private static final Logger log = Logger.getLogger(FilePlayerDB.class.getName());
    private final YamlConfiguration config;
    private final File file;

    public FilePlayerDB(File file) {
        this.file = file;
        config = new YamlConfiguration();
        if (file.exists()) {
            FileUtil.readConfig(config, file);
        }
    }

    @Override
    public synchronized UUID getUUIDFromName(String name) {
        Set<String> keys = new HashSet<>(config.getKeys(false));
        for (String uuidStr : keys) {
            String entryName = config.getString(uuidStr + ".name", config.getString(uuidStr, null));
            if (entryName != null && entryName.equals(name)) {
                return UUIDUtil.fromString(uuidStr);
            }
        }
        return null;
    }

    @Override
    public synchronized String getName(UUID uuid) {
        String uuidStr = UUIDUtil.asString(uuid);
        return config.getString(uuidStr + ".name", config.getString(uuidStr, null));
    }

    @Override
    public synchronized String getDisplayName(UUID uuid) {
        String uuidStr = UUIDUtil.asString(uuid);
        return config.getString(uuidStr + ".displayName", null);
    }

    @Override
    public synchronized String getDisplayName(String playerName) {
        UUID uuid = getUUIDFromName(playerName);
        if (uuid != null) {
            return getDisplayName(uuid);
        }
        return playerName;
    }

    @Override
    public synchronized void updatePlayer(Player player) throws IOException {
        updatePlayerAsync(player.getUniqueId(), player.getName(), player.getDisplayName());
    }

    @Override
    public void updatePlayerAsync(final UUID id, final String name, final String displayName) {
        Bukkit.getScheduler().runTaskAsynchronously(uSkyBlock.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    String uuid = UUIDUtil.asString(id);
                    config.set(uuid + ".name", name);
                    config.set(uuid + ".displayName", displayName);
                    config.save(file);
                } catch (IOException e) {
                    log.log(Level.INFO, "Error saving playerdb", e);
                }
            }
        });
    }
}
