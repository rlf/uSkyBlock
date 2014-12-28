package us.talabrek.ultimateskyblock.command;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/**
 * An abstraction for supporting nesting of commands.
 * This is a light-weight version of the BukkitCommand abstraction.
 */
public interface USBCommand {
    /**
     * Returns the name of the sub-command.
     */
    String getName();

    /**
     * The permission of the command. Can be <code>null</code>.
     */
    String getPermission();

    /**
     * A short description of the sub-command.
     * Used when listing the commands with others.
     */
    String getDescription();

    /**
     * A more verbatim description of the command.
     * Used when <code>/command help</code> is executed.
     */
    String getUsage();

    /**
     * The list of parameters accepted by the command.
     * Can be empty, not <code>null</code>.
     */
    String[] getParams();

    /**
     * Executes the command.
     */
    boolean execute(CommandSender sender, String... args);

}
