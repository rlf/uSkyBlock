package us.talabrek.ultimateskyblock.handler.placeholder;

import me.clip.deluxechat.placeholders.*;
import me.clip.deluxechat.placeholders.PlaceholderHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.uSkyBlock;

/**
 * DeluxeChatPlaceholder integration
 */
public class DeluxeChatPlaceholderAPI implements PlaceholderAPI {
    private uSkyBlock plugin;
    private PlaceholderReplacer replacer;

    @Override
    public boolean registerPlaceholder(uSkyBlock plugin, PlaceholderReplacer replacer) {
        this.plugin = plugin;
        this.replacer = replacer;
        if (Bukkit.getPluginManager().isPluginEnabled("DeluxeChat")) {
            Hook hook = new Hook();
            me.clip.deluxechat.placeholders.PlaceholderHandler.registerPlaceholderHook(plugin, hook);
            return true;
        }
        return false;
    }

    @Override
    public void unregisterPlaceholder(uSkyBlock plugin, PlaceholderReplacer placeholderReplacer) {
        if (Bukkit.getPluginManager().isPluginEnabled("DeluxeChat")) {
            PlaceholderHandler.unregisterPlaceholderHook(plugin);
        }
    }

    @Override
    public String replacePlaceholders(Player player, String message) {
        return PlaceholderHandler.setPlaceholders(player, message);
    }

    private class Hook extends DeluxePlaceholderHook {
        @Override
        public String onPlaceholderRequest(Player player, String placeholder) {
            String usbPlaceholder = "usb_" + placeholder;
            if (replacer.getPlaceholders().contains(usbPlaceholder)) {
                return replacer.replace(player, player, usbPlaceholder);
            }
            return null;
        }
    }
}
