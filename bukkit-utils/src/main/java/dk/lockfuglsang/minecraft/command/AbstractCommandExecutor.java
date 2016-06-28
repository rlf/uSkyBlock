package dk.lockfuglsang.minecraft.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Command delegator.
 */
public class AbstractCommandExecutor extends CompositeCommand implements CommandExecutor {

    public AbstractCommandExecutor(String name, String permission, String description) {
        super(name, permission, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!CommandManager.isRequirementsMet(sender, this)) {
            tr("\u00a7cCommand is currently disabled!");
            return false;
        }
        dk.lockfuglsang.minecraft.command.Command cmd = this;
        if (!hasAccess(cmd, sender)) {
            if (cmd != null) {
                sender.sendMessage(tr("\u00a7eYou do not have access (\u00a74{0}\u00a7e)", cmd.getPermission()));
            } else {
                sender.sendMessage(tr("\u00a7eInvalid command: {0}", alias));
            }
            showUsage(sender, 1);
        } else {
            return super.execute(sender, alias, new HashMap<String, Object>(), args);
        }
        return true;
    }
}
