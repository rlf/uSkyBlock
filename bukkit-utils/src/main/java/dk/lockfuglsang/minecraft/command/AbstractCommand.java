package dk.lockfuglsang.minecraft.command;

import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.command.TabCompleter;

/**
 * Convenience implementation of the Command
 */
public abstract class AbstractCommand implements Command {
    private final String[] aliases;
    private final String permission;
    private final String description;
    private final String usage;
    private final String[] params;
    private CompositeCommand parent;

    public AbstractCommand(String name, String permission, String params, String description, String usage) {
        this.aliases = name.split("\\|");
        this.permission = permission;
        this.description = I18nUtil.tr(description);
        this.usage = usage;
        this.params = params != null && !params.trim().isEmpty() ? params.split(" ") : new String[0];
    }

    public AbstractCommand(String name, String permission, String params, String description) {
        this(name, permission, params, description, null);
    }

    public AbstractCommand(String name, String permission, String description) {
        this(name, permission, null, description, null);
    }

    public AbstractCommand(String name, String description) {
        this(name, null, null, description, null);
    }

    @Override
    public String getName() {
        return aliases[0];
    }

    public String[] getAliases() {
        return aliases;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getUsage() {
        return usage;
    }

    @Override
    public String[] getParams() {
        return params;
    }

    @Override
    public TabCompleter getTabCompleter() {
        return null;
    }

    @Override
    public CompositeCommand getParent() {
        return parent;
    }

    @Override
    public void setParent(CompositeCommand parent) {
        this.parent = parent;
    }

    @Override
    public void accept(CommandVisitor visitor) {
        if (visitor != null) {
            visitor.visit(this);
        }
    }

    /**
     * Convenience method until we can fully rely on everybody running JRE 8.
     * @param args  A list of arguments
     * @param delim Delimiter to join them
     * @return A string containing the arguments concatenated.
     */
    public static String join(String[] args, String delim) {
        String res = "";
        if (args != null && args.length > 0) {
            for (String arg : args) {
                res += (res.isEmpty() ? "" : delim) + arg;
            }
        }
        return res;
    }

    public static String join(String[] args) {
        return join(args, " ");
    }
}
