package us.talabrek.ultimateskyblock.handler.placeholder;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common PlaceholderAPI for internal placeholders.
 */
public class TextPlaceholder implements PlaceholderAPI {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(?<placeholder>usb_[^}]*)\\}");
    private uSkyBlock plugin;
    private PlaceholderReplacer replacer;

    @Override
    public boolean registerPlaceholder(uSkyBlock plugin, PlaceholderReplacer replacer) {
        this.plugin = plugin;
        this.replacer = replacer;
        return true;
    }

    public String replacePlaceholders(Player player, String message) {
        return replacePlaceholdersInternal(player, message);
    }

    private String replacePlaceholdersInternal(Player player, String message) {
        if (message == null) {
            return null;
        }
        String result = message;
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(message);
        if (matcher.find()) {
            int ix = 0;
            StringBuilder sb = new StringBuilder();
            do {
                sb.append(message.substring(ix, matcher.start()));
                String placeholderString = matcher.group("placeholder");
                if (placeholderString != null && replacer.getPlaceholders().contains(placeholderString)) {
                    String replacement = replacer.replace(null, player, placeholderString);
                    if (replacement != null) {
                        sb.append(replacement);
                    } else {
                        sb.append(message.substring(matcher.start(), matcher.end()));
                    }
                } else {
                    sb.append("{" + placeholderString + "}");
                }
                ix = matcher.end();
            } while (matcher.find());
            if (ix < message.length()) {
                sb.append(message.substring(ix));
            }
            result = sb.toString();
        }
        return result;
    }

    @Override
    public void unregisterPlaceholder(uSkyBlock plugin, PlaceholderReplacer placeholderReplacer) {
        // Not needed, since the plugin will unregister all handlers
    }
}
