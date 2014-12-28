package us.talabrek.ultimateskyblock.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import us.talabrek.ultimateskyblock.command.completion.AbstractTabCompleter;

import java.util.*;

/**
 * Command with nested commandMap inside.
 */
public class CompositeUSBCommand extends AbstractTabCompleter implements USBCommand, TabCompleter {

    public static final String HELP_PATTERN = "(?iu)h|help";

    private final String name;
    private final String permission;
    private final String description;
    private final String[] params;
    private CompositeUSBCommand parent;
    private final Map<String, USBCommand> commandMap;
    private final Map<String, TabCompleter> tabMap;

    public CompositeUSBCommand(String name, String permission, String description) {
        this(name, permission, null, description);
    }

    public CompositeUSBCommand(String name, String permission, String params, String description) {
        this.name = name;
        this.permission = permission;
        this.description = description;
        this.params = params != null ? params.split(" ") : new String[0];
        commandMap = new HashMap<>();
        tabMap = new HashMap<>();
    }

    public CompositeUSBCommand add(USBCommand... cmds) {
        for (USBCommand cmd : cmds) {
            commandMap.put(cmd.getName().toLowerCase(), cmd);
            cmd.setParent(this);
        }
        return this;
    }

    public CompositeUSBCommand addTab(String arg, TabCompleter tab) {
        tabMap.put(arg, tab);
        return this;
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
        return null;
    }

    @Override
    public String[] getParams() {
        return params;
    }

    @Override
    public boolean execute(CommandSender sender, Map<String,Object> data, String... args) {
        if (args.length == 0 || (args.length == 1 && args[0].matches("(?iu)h|help"))) {
            showUsage(sender);
        } else if (args.length > 1 && args[0].matches("(?iu)h|help")) {
            showUsage(sender, args[1]);
        } else if (args.length > params.length) {
            String cmdName = args[params.length].toLowerCase();
            USBCommand cmd = commandMap.get(cmdName);
            String[] subArgs = new String[args.length-1-params.length];
            System.arraycopy(args, 1+params.length, subArgs, 0, subArgs.length);
            int ix = 0;
            for (String p : params) {
                data.put(p, args[ix++]);
            }
            if (!hasAccess(cmd, sender)) {
                showUsage(sender);
            } else if (!cmd.execute(sender, data, subArgs)) {
                showUsage(sender, args[0]);
            }
        } else {
            showUsage(sender);
        }
        return true;
    }
    private void showUsage(CommandSender sender) {
        String msg = "\u00a77Usage: " + getShortDescription(this);
        ArrayList<String> cmds = new ArrayList<>(commandMap.keySet());
        Collections.sort(cmds);
        for (String key : cmds) {
            USBCommand cmd = commandMap.get(key);
            msg += "  " + getShortDescription(cmd);
        }
        sender.sendMessage(msg.split("\n"));
    }

    private void showUsage(CommandSender sender, String arg) {
        String cmdName = arg.toLowerCase();
        USBCommand cmd = commandMap.get(cmdName);
        if (cmd != null && hasAccess(cmd, sender)) {
            String msg = "\u00a77Usage: \u00a73/" + name + " \u00a7e";
            msg += getShortDescription(cmd);
            if (cmd.getUsage() != null && !cmd.getUsage().isEmpty()) {
                msg += "\u00a77" + cmd.getUsage();
            }
            sender.sendMessage(msg.split("\n"));
        } else {
            showUsage(sender);
        }
    }

    private String getShortDescription(USBCommand cmd) {
        String msg = "\u00a73" + cmd.getName() + "\u00a7a";
        for (String param : cmd.getParams()) {
            msg += " <" + param + ">";
        }
        if (cmd instanceof CompositeUSBCommand) {
            msg += " [command|help]";
        }
        msg += "\u00a77 - ";
        msg += cmd.getDescription() + "\n";
        return msg;
    }

    public boolean hasAccess(USBCommand cmd, CommandSender sender) {
        return cmd != null && (cmd.getPermission() == null || sender.hasPermission(cmd.getPermission()));
    }

    @Override
    protected List<String> getTabList(CommandSender commandSender, String term) {
        ArrayList<String> strings = new ArrayList<>();
        for (USBCommand cmd : commandMap.values()) {
            if (hasAccess(cmd, commandSender)) {
                strings.add(cmd.getName());
            }
        }
        strings.add("help");
        return strings;
    }

    protected TabCompleter getTabCompleter(USBCommand cmd, int argNum) {
        argNum = argNum >= 0 ? argNum : 0;
        if (cmd.getParams().length > argNum) {
            String paramName = cmd.getParams()[argNum];
            if (tabMap.containsKey(paramName)) {
                return tabMap.get(paramName);
            } else if (getParent() != null) {
                TabCompleter tab = getParent().getTabCompleter(cmd, argNum);
                if (tab != null) {
                    return tab;
                }
            }
        }
        if (cmd.getTabCompleter() != null) {
            return cmd.getTabCompleter();
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length <=  params.length && args.length > 0) {
            TabCompleter tab = getTabCompleter(this, args.length-1);
            if (tab != null && tab != this) {
                return tab.onTabComplete(sender, command, alias, args);
            } else if (tab == this) {
                return getTabList(sender, args[args.length-1]);
            }
        } else if (args.length > params.length+1) { // Sub-commands
            String cmdName = args[params.length].toLowerCase();
            USBCommand cmd = commandMap.get(cmdName);
            if (cmd != null && (args.length - params.length) > 1) { // Go deeper
                String[] subArgs = new String[args.length-1-params.length];
                System.arraycopy(args, 1+params.length, subArgs, 0, subArgs.length);
                TabCompleter tab = getTabCompleter(cmd, subArgs.length - 1);
                if (tab != null) {
                    return tab.onTabComplete(sender, command, alias, subArgs);
                }
            } else {
                return super.onTabComplete(sender, command, alias, args);
            }
        } else {
            return super.onTabComplete(sender, command, alias, args);
        }
        return Collections.emptyList();
    }

    @Override
    public TabCompleter getTabCompleter() {
        return this;
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
