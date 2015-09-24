package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.I18nUtil;

import java.util.Locale;
import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * Supports setting the language.
 */
public class LanguageCommand extends AbstractUSBCommand {
    private final uSkyBlock plugin;

    public LanguageCommand(uSkyBlock plugin) {
        super("lang|l", "usb.admin.lang", "language", tr("changes the language of the plugin, and reloads"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        if (args.length == 1) {
            Locale loc = I18nUtil.getLocale(args[0]);
            Settings.locale = loc;
            I18nUtil.clearCache();
            plugin.getConfig().set("language", args[0]);
            plugin.saveConfig();
            plugin.reloadConfig();
            if (I18nUtil.getLocale().equals(I18nUtil.getI18n().getLocale())) {
                sender.sendMessage(tr("\u00a7aSuccessfully changed language to \u00a7e{0}", loc));
            } else {
                sender.sendMessage(tr("\u00a7cFailed to change language to \u00a7e{0}", loc));
            }
            return true;
        }
        return false;
    }
}
