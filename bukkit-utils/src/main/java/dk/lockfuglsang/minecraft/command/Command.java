package dk.lockfuglsang.minecraft.command;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Map;

/**
 * An abstraction for supporting nesting of commands.
 * This is a light-weight version of the BukkitCommand abstraction.
 */
public interface Command {
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
     * Returns aliases for the command.
     * Can be empty, cannot be <code>null</code>.
     */
    String[] getAliases();

    /**
     * Executes the command.
     */
    boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args);

    /**
     * Optional TabCompleter to override the default ones.
     * Can be <code>null</code>
     */
    TabCompleter getTabCompleter();

    /**
     * Returns the parent command (if one such is available).
     * May return <code>null</code>.
     */
    CompositeCommand getParent();

    /**
     * Assigns a parent command.
     * @param parent
     */
    void setParent(CompositeCommand parent);

    /**
     * Visitor pattern.
     * @param visitor A visitor for this node.
     */
    void accept(CommandVisitor visitor);

    /**
     * Returns a map of feature-toggling permissions supporte by this command.
     * @return A map of permission-node as key, and description as value.
     */
    Map<String,String> getFeaturePermissions();

    /**
     * Allows for other than permission checking
     * @param sender     The commandSender requesting permission
     * @param permission The permission
     * @return <code>true</code> if the sender can execute this command
     */
    boolean hasPermission(CommandSender sender, String permission);
}
