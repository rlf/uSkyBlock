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
    private final Map<String, USBCommand> commandMap;
    private final Map<String, TabCompleter> tabMap;

    public CompositeUSBCommand(String name, String permission, String description) {
        this.name = name;
        this.permission = permission;
        this.description = description;
        commandMap = new HashMap<>();
        tabMap = new HashMap<>();
    }

    public CompositeUSBCommand add(USBCommand... cmds) {
        for (USBCommand cmd : cmds) {
            commandMap.put(cmd.getName().toLowerCase(), cmd);
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
        return new String[0];
    }

    @Override
    public boolean execute(CommandSender sender, String... args) {
        if (args.length == 0 || (args.length == 1 && args[0].matches("(?iu)h|help"))) {
            showUsage(sender);
        } else if (args.length > 1 && args[0].matches("(?iu)h|help")) {
            showUsage(sender, args[1]);
        } else if (args.length >= 1 && commandMap.containsKey(args[0].toLowerCase())) {
            USBCommand usbCommand = commandMap.get(args[0].toLowerCase());
            String[] subArgs = new String[args.length-1];
            System.arraycopy(args, 1, subArgs, 0, subArgs.length);
            if (!hasAccess(usbCommand, sender)) {
                showUsage(sender);
            } else if (!usbCommand.execute(sender, subArgs)) {
                showUsage(sender, args[0]);
            }
        } else {
            showUsage(sender);
        }
        return true;
    }
    private void showUsage(CommandSender sender) {
        String msg = "\u00a77Usage: \u00a73/" + name + " \u00a7e [command] | help [command]\n";
        ArrayList<String> cmds = new ArrayList<>(commandMap.keySet());
        Collections.sort(cmds);
        for (String key : cmds) {
            USBCommand usbCommand = commandMap.get(key);
            msg += "  \u00a73" + usbCommand.getName() + "\u00a77 " + usbCommand.getDescription() + "\n";
        }
        sender.sendMessage(msg.split("\n"));
    }

    private void showUsage(CommandSender sender, String arg) {
        String cmdName = arg.toLowerCase();
        USBCommand cmd = commandMap.get(cmdName);
        if (cmd != null && hasAccess(cmd, sender)) {
            String msg = "\u00a77Usage: \u00a73/" + name + " \u00a7e";
            msg += cmdName + " \u00a7f";
            msg += cmd.getDescription() + "\n";
            if (cmd.getUsage() != null && !cmd.getUsage().isEmpty()) {
                msg += "\u00a77" + cmd.getUsage();
            }
            sender.sendMessage(msg.split("\n"));
        } else {
            showUsage(sender);
        }
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0 || args.length == 1) {
            return super.onTabComplete(sender, command, alias, args);
        } else if (args.length == 2 && args[0].matches(HELP_PATTERN)) {
            return super.onTabComplete(sender, command, alias, args);
        } else {
            String cmdName = args[0].toLowerCase();
            USBCommand cmd = commandMap.get(cmdName);
            if (cmd != null) {
                String[] subArgs = new String[args.length-1];
                System.arraycopy(args, 1, subArgs, 0, subArgs.length);
                String[] params = cmd.getParams();
                if (params.length >= subArgs.length && tabMap.containsKey(params[subArgs.length-1])) {
                    return tabMap.get(params[subArgs.length-1]).onTabComplete(sender, command, cmd.getName(), subArgs);
                }
            }
        }
        return Collections.emptyList();
    }
}
