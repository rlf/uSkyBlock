package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

/**
 * Reloads the config-files for USB.
 */
public class ReloadCommand extends AbstractUSBCommand {
    public ReloadCommand() {
        super("reload", "usb.admin.reload", "reload configuration from file.");
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        uSkyBlock.getInstance().reloadConfig();
        sender.sendMessage("\u00a7eConfiguration reloaded from file.");
        return true;
    }
}
