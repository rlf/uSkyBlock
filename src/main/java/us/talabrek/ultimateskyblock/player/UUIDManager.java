package us.talabrek.ultimateskyblock.player;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.UUIDUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Responsible for detecting name-changes, and migrating the .yml files when it happens.
 */
public class UUIDManager {
    private final uSkyBlock plugin;
    private final FileConfiguration config;
    private final File configFile;

    Map<UUID, String> uuid2nameMap = new HashMap<>();

    public UUIDManager(uSkyBlock plugin) {
        this.plugin = plugin;
        config = new YamlConfiguration();
        configFile = new File(plugin.getDataFolder(), "uuids.yml");
        readMapFromFile(config.getRoot());
    }

    private void readMapFromFile(ConfigurationSection section) {
        for (String uuid : section.getKeys(false)) {
            uuid2nameMap.put(UUIDUtil.fromString(uuid), section.getString(uuid));
        }
    }

    public void updatePlayer(Player player) {
        String oldName = uuid2nameMap.get(player.getUniqueId());
        if (oldName != null && !oldName.equals(player.getName())) {
            renamePlayer(oldName, player);
        }
    }

    private void renamePlayer(final String oldName, Player player) {
        uuid2nameMap.put(player.getUniqueId(), player.getName()); // So lookups will work.
        plugin.removeActivePlayer(oldName);
        // Rename file
        File playerFile = new File(plugin.directoryPlayers, oldName + ".yml");
        File newPlayerFile = new File(plugin.directoryPlayers, player.getUniqueId() + ".yml");
        if (playerFile.renameTo(newPlayerFile)) {
            IslandInfo islandInfo = plugin.getIslandInfo(player);
            if (islandInfo != null) {
                //islandInfo.renamePlayer(oldName, player);
            }
        }
    }
}
