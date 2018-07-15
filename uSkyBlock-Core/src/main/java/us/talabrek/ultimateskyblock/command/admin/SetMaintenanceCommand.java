package us.talabrek.ultimateskyblock.command.admin;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Enables the user to toggle maintenance mode on and off.
 */
public class SetMaintenanceCommand extends AbstractCommand {
    private final uSkyBlock plugin;

    public SetMaintenanceCommand(uSkyBlock plugin) {
        super("maintenance", "usb.admin.maintenance", "true|false", marktr("toggles maintenance mode"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        if (sender instanceof ConsoleCommandSender) {
            if (args.length == 1 && args[0].matches("(true)|(false)")) {
                boolean maintenanceMode = Boolean.parseBoolean(args[0]);
                plugin.setMaintenanceMode(maintenanceMode);
                if (maintenanceMode) {
                    sender.sendMessage(tr("\u00a7cMAINTENANCE: \u00a7aActivated\u00a7e all uSkyBlock features currently disabled."));
                } else {
                    sender.sendMessage(tr("\u00a7cMAINTENANCE: \u00a74Deactivated\u00a7e all uSkyBlock features back to operational."));
                }
            } else {
                return false;
            }
        } else {
            sender.sendMessage(tr("\u00a7cMaintenance mode can only be changed from console!"));
        }
        return true;
    }
}
