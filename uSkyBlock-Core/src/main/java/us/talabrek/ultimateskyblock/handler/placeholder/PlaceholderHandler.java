package us.talabrek.ultimateskyblock.handler.placeholder;

import dk.lockfuglsang.minecraft.file.FileUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PlaceholderHandler {
    private static final String[] ADAPTORS = {
            ChatPlaceholder.class.getName(),
            ServerCommandPlaceholder.class.getName(),
            "us.talabrek.ultimateskyblock.handler.placeholder.MVdWPlaceholderAPI",
            "us.talabrek.ultimateskyblock.handler.placeholder.DeluxeChatPlaceholderAPI"
    };

    private static PlaceholderAPI.PlaceholderReplacer replacer;
    private static final List<PlaceholderAPI> apis = new ArrayList<>();

    public static void register(uSkyBlock plugin) {
        PlaceholderAPI.PlaceholderReplacer placeholderReplacer = getReplacer(plugin);
        for (String className : ADAPTORS) {
            String baseName = FileUtil.getExtension(className);
            if (plugin.getConfig().getBoolean("placeholder." + baseName.toLowerCase(), false)) {
                try {
                    Class<?> aClass = Class.forName(className);
                    Object o = aClass.newInstance();
                    if (o instanceof PlaceholderAPI) {
                        PlaceholderAPI api = (PlaceholderAPI) o;
                        if (api.registerPlaceholder(plugin, placeholderReplacer)) {
                            plugin.getLogger().info("uSkyBlock hooked into " + baseName);
                            apis.add(api);
                        } else {
                            plugin.getLogger().info("uSkyBlock failed to hook into " + baseName);
                        }
                    }
                } catch (Throwable e) {
                    // Ignore
                    plugin.getLogger().info("uSkyBlock failed to hook into " + baseName);
                }
            }
        }
    }

    public static void unregister(uSkyBlock plugin) {
        PlaceholderAPI.PlaceholderReplacer placeholderReplacer = getReplacer(plugin);
        for (Iterator<PlaceholderAPI> it = apis.iterator(); it.hasNext(); ) {
            it.next().unregisterPlaceholder(plugin, placeholderReplacer);
            it.remove();
        }
        replacer = null;
    }

    public static String replacePlaceholders(Player player, String message) {
        if (message == null) {
            return null;
        }
        String msg = message;
        for (PlaceholderAPI api : apis) {
            msg = api.replacePlaceholders(player, msg);
        }
        return msg;
    }

    private static PlaceholderAPI.PlaceholderReplacer getReplacer(uSkyBlock plugin) {
        if (replacer == null) {
            replacer = new PlaceholderReplacerImpl(plugin);
        }
        return replacer;
    }
}
