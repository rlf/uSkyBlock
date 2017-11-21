package us.talabrek.ultimateskyblock.uuid;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Non performing PlayerDB that goes directly to the Bukkit implementation
 */
public class BukkitPlayerDB implements PlayerDB {
    @Override
    public UUID getUUIDFromName(String name) {
        if (UNKNOWN_PLAYER_NAME.equalsIgnoreCase(name)) {
            return UNKNOWN_PLAYER_UUID;
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        return offlinePlayer != null ? offlinePlayer.getUniqueId() : null;
    }

    @Override
    public UUID getUUIDFromName(String name, boolean lookup) {
        return getUUIDFromName(name);
    }

    @Override
    public String getName(UUID uuid) {
        if (UNKNOWN_PLAYER_UUID.equals(uuid)) {
            return UNKNOWN_PLAYER_NAME;
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        return offlinePlayer != null ? offlinePlayer.getName() : null;
    }

    @Override
    public String getDisplayName(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null ? player.getDisplayName() : null;
    }

    @Override
    public String getDisplayName(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        return player != null ? player.getDisplayName() : null;
    }

    @Override
    public Set<String> getNames(String search) {
        Set<String> names = new HashSet<>();
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        for (Player player : onlinePlayers) {
            if (player != null && player.isOnline() && player.getName() != null) {
                names.add(player.getName());
            }
        }
        return names;
    }

    @Override
    public void updatePlayer(UUID uuid, String name, String displayName) {
        // Do nothing
    }

    @Override
    public Player getPlayer(UUID uuid) {
        return Bukkit.getPlayer(uuid);
    }

    @Override
    public Player getPlayer(String name) {
        return Bukkit.getPlayer(name);
    }

    @Override
    public void shutdown() {
        // Do nothing
    }
}
