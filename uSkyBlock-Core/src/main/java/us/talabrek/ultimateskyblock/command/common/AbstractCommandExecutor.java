package us.talabrek.ultimateskyblock.command.common;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

/**
 * Command delegator.
 */
// TODO: 27/12/2014 - R4zorax: Add pagination to help
public class AbstractCommandExecutor extends CompositeUSBCommand implements CommandExecutor {

    public AbstractCommandExecutor(String name, String permission, String description) {
        super(name, permission, description);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] args) {
        return super.execute(commandSender, alias, new HashMap<String, Object>(), args);
    }
}
