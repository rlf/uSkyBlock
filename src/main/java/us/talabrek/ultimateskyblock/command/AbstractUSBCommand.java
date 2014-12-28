package us.talabrek.ultimateskyblock.command;

import org.bukkit.command.TabCompleter;

/**
 * Convenience implementation of the USBCommand
 */
public abstract class AbstractUSBCommand implements USBCommand {
    private final String name;
    private final String permission;
    private final String description;
    private final String usage;
    private final String[] params;
    private CompositeUSBCommand parent;

    public AbstractUSBCommand(String name, String permission, String params, String description, String usage) {
        this.name = name;
        this.permission = permission;
        this.description = description;
        this.usage = usage;
        this.params = params != null && !params.trim().isEmpty() ? params.split(" ") : new String[0];
    }

    public AbstractUSBCommand(String name, String permission, String params, String description) {
        this(name, permission, params, description, null);
    }

    public AbstractUSBCommand(String name, String permission, String description) {
        this(name, permission, null, description, null);
    }

    public AbstractUSBCommand(String name, String description) {
        this(name, null, null, description, null);
    }

    @Override
    public String getName() {
        return name;
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
    public CompositeUSBCommand getParent() {
        return parent;
    }

    @Override
    public void setParent(CompositeUSBCommand parent) {
        this.parent = parent;
    }
}
