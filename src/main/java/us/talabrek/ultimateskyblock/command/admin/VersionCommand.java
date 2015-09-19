package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * Displays detailed version information.
 */
public class VersionCommand extends AbstractUSBCommand {
    private final uSkyBlock plugin;

    public VersionCommand(uSkyBlock plugin) {
        super("version|v", "usb.admin.version", tr("displays version information"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        sender.sendMessage(plugin.getVersionInfo(true).split("\n"));
        return true;
    }

}
