package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

/**
 * Displays detailed version information.
 */
public class VersionCommand extends AbstractUSBCommand {
    private final uSkyBlock plugin;

    public VersionCommand(uSkyBlock plugin) {
        super("version|v", "usb.admin", "displays version information");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        sender.sendMessage(plugin.getVersionInfo().split("\n"));
        return true;
    }

}
