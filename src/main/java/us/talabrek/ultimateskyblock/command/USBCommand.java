package us.talabrek.ultimateskyblock.command;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Map;

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
    boolean execute(CommandSender sender, Map<String, Object> data, String... args);

    /**
     * Optional TabCompleter to override the default ones.
     * Can be <code>null</code>
     */
    TabCompleter getTabCompleter();

    /**
     * Returns the parent command (if one such is available).
     * May return <code>null</code>.
     */
    CompositeUSBCommand getParent();

    /**
     * Assigns a parent command.
     * @param parent
     */
    void setParent(CompositeUSBCommand parent);
}
