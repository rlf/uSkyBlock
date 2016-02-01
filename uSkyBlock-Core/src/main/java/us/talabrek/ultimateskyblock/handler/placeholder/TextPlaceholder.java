package us.talabrek.ultimateskyblock.handler.placeholder;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by R4zorax on 01/02/2016.
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

    protected String replacePlaceholders(Player player, String message) {
        String result = null;
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(message);
        if (matcher.find()) {
            int ix = 0;
            StringBuilder sb = new StringBuilder();
            do {
                sb.append(message.substring(ix, matcher.start()));
                String replacement = replacer.replace(null, player, matcher.group("placeholder"));
                if (replacement != null) {
                    sb.append(replacement);
                } else {
                    sb.append(message.substring(matcher.start(), matcher.end()));
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
}
