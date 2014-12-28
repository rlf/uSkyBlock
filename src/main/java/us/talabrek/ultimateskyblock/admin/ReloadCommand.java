package us.talabrek.ultimateskyblock.admin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.command.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.uSkyBlock;

/**
 * Reloads the config-files for USB.
 */
public class ReloadCommand extends AbstractUSBCommand {
    public ReloadCommand() {
        super("reload", "usb.admin.reload", "reload configuration from file.");
    }

    @Override
    public boolean execute(CommandSender sender, String... args) {
        uSkyBlock.getInstance().reloadConfig();
        Settings.loadPluginConfig(uSkyBlock.getInstance().getConfig());
        sender.sendMessage(ChatColor.YELLOW + "Configuration reloaded from file.");
        return true;
    }
}
