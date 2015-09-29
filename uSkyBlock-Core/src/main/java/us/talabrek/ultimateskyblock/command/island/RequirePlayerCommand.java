package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.util.I18nUtil;

import java.util.Map;

/**
 * Common command for all those that require a Player as CommandSender.
 */
public abstract class RequirePlayerCommand extends AbstractUSBCommand {

    public RequirePlayerCommand(String name, String permission, String params, String description) {
        super(name, permission, params, description);
    }

    public RequirePlayerCommand(String name, String permission, String description) {
        super(name, permission, description);
    }

    protected abstract boolean doExecute(String alias, Player player, Map<String, Object> data, String... args);

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(I18nUtil.tr("\u00a74This command can only be executed by a player"));
            return false;
        }
        Player player = (Player) sender;
        return doExecute(alias, player, data, args);
    }
}
