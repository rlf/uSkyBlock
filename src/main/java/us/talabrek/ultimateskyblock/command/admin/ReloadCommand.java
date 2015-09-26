package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * Reloads the config-files for USB.
 */
public class ReloadCommand extends AbstractUSBCommand {
    public ReloadCommand() {
        super("reload", "usb.admin.reload", tr("reload configuration from file."));
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        uSkyBlock.getInstance().reloadConfig();
        uSkyBlock.getInstance().registerEvents();
        sender.sendMessage(tr("\u00a7eConfiguration reloaded from file."));
        return true;
    }
}
