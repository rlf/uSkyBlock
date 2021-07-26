package us.talabrek.ultimateskyblock.player;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class NotificationManager {
    private final uSkyBlock plugin;
    private final BukkitAudiences audiences;
    private LegacyComponentSerializer legacySerializer;

    public NotificationManager(uSkyBlock plugin) {
        this.plugin = plugin;
        audiences = BukkitAudiences.create(plugin);
    }

    /**
     * Gets a {@link LegacyComponentSerializer} configured for uSkyBlock's (translatable) messages.
     * @return LegacyComponentSerializer configured for uSkyblock's (translatable) messages.
     */
    public @NotNull LegacyComponentSerializer getLegacySerializer() {
        if (legacySerializer == null) {
            legacySerializer = LegacyComponentSerializer.builder().character('\u00a7').build();
        }
        return legacySerializer;
    }

    /**
     * Sends the given {@link String} as message to the {@link Player}'s ActionBar.
     * @param player Player to send the given message to
     * @param message Message to send to the given player
     */
    public void sendActionBar(@NotNull Player player, @NotNull String message) {
        sendActionBar(player, getLegacySerializer().deserialize(message));
    }

    /**
     * Sends the given {@link Component} as message to the {@link Player}'s ActionBar.
     * @param player Player to send the given message to
     * @param component Component to send to the given player
     */
    public void sendActionBar(@NotNull Player player, @NotNull Component component) {
        audiences.player(player).sendActionBar(component);
    }

    public void shutdown() {
        audiences.close();
    }
}
