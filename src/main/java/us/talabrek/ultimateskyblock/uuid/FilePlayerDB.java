package us.talabrek.ultimateskyblock.uuid;

import org.bukkit.configuration.file.YamlConfiguration;
import us.talabrek.ultimateskyblock.util.UUIDUtil;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static us.talabrek.ultimateskyblock.util.FileUtil.readConfig;

/**
 * PlayerDB backed by a simple yml-file.
 */
public class FilePlayerDB implements PlayerDB {
    private final YamlConfiguration config;
    private final File file;

    public FilePlayerDB(File file) {
        this.file = file;
        config = new YamlConfiguration();
        if (file.exists()) {
            readConfig(config, file);
        }
    }

    @Override
    public String getName(UUID uuid) {
        return config.getString(UUIDUtil.asString(uuid), null);
    }

    @Override
    public void setName(UUID uuid, String name) throws IOException {
        config.set(UUIDUtil.asString(uuid), name);
        config.save(file);
    }
}
