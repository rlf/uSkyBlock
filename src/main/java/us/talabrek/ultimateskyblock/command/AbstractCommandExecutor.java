package us.talabrek.ultimateskyblock.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import us.talabrek.ultimateskyblock.command.completion.AbstractTabCompleter;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
        return super.execute(commandSender, new HashMap<String, Object>(), args);
    }
}
