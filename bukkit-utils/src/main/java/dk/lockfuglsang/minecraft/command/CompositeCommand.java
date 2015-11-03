package dk.lockfuglsang.minecraft.command;

import dk.lockfuglsang.minecraft.command.completion.AbstractTabCompleter;
import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command with nested commandMap inside.
 */
public class CompositeCommand extends AbstractTabCompleter implements Command, TabCompleter {

    public static final String HELP_PATTERN = "(?iu)help|\\?";
    private static final int MAX_PER_PAGE = 10;

    private final String name;
    private final String[] aliases;
    private final String permission;
    private final String description;
    private final String[] params;
    private CompositeCommand parent;
    private final Map<String, Command> commandMap;
    private final Map<String, Command> aliasMap;
    private final Map<String, TabCompleter> tabMap;

    public CompositeCommand(String name, String permission, String description) {
        this(name, permission, null, description);
    }

    public CompositeCommand(String name, String permission, String params, String description) {
        this.aliases = name.split("\\|");
        this.name = aliases[0];
        this.permission = permission;
        this.description = description;
        this.params = params != null ? params.split(" ") : new String[0];
        commandMap = new HashMap<>();
        aliasMap = new HashMap<>();
        tabMap = new HashMap<>();
    }

    public CompositeCommand add(Command... cmds) {
        for (Command cmd : cmds) {
            commandMap.put(cmd.getName(), cmd);
            for (String alias : cmd.getAliases()) {
                aliasMap.put(alias, cmd);
            }
            cmd.setParent(this);
        }
        return this;
    }

    public CompositeCommand addTab(String arg, TabCompleter tab) {
        tabMap.put(arg, tab);
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }

    @Override
    public String getPermission() {
        return permission != null && !permission.isEmpty() ? permission : null;
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
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        if (!CommandManager.isRequirementsMet(sender, this)) {
            return false;
        }
        if (args.length == 0 || (args.length == 1 && args[0].matches(HELP_PATTERN))) {
            showUsage(sender, 1);
        } else if (args.length > 1 && args[0].matches(HELP_PATTERN)) {
            showUsage(sender, args[1]);
        } else if (args.length > params.length) {
            String cmdName = args[params.length].toLowerCase();
            Command cmd = aliasMap.get(cmdName);
            String[] subArgs = new String[args.length-1-params.length];
            System.arraycopy(args, 1+params.length, subArgs, 0, subArgs.length);
            int ix = 0;
            for (String p : params) {
                data.put(p, args[ix++]);
            }
            if (!hasAccess(cmd, sender)) {
                if (cmd != null && sender.isOp()) {
                    sender.sendMessage(MessageFormat.format("\u00a7eYou do not have access (\u00a74{0}\u00a7e)", cmd.getPermission()));
                } else {
                    sender.sendMessage("\u00a7eYou do not have access");
                }
                showUsage(sender, args[0]);
            } else if (!cmd.execute(sender, cmdName, data, subArgs)) {
                showUsage(sender, args[0]);
            }
        } else {
            showUsage(sender, 1);
        }
        return true;
    }
    private void showUsage(CommandSender sender, int page) {
        String msg = "\u00a77Usage: " + getShortDescription(sender, this);
        List<String> cmds = new ArrayList<>(commandMap.keySet());
        Collections.sort(cmds);
        int realPage = 0;
        int maxPage = 0;
        if (cmds.size() > MAX_PER_PAGE) {
            msg = msg.substring(0, msg.length()-1); // Remove \n
            maxPage = (int) Math.round(Math.ceil(cmds.size() * 1f / MAX_PER_PAGE));
            realPage = Math.max(1, Math.min(maxPage, page));
            msg += " \u00a77[" + realPage + "/" + maxPage + "]\n";
            cmds = cmds.subList((realPage-1)*MAX_PER_PAGE, Math.min(realPage*MAX_PER_PAGE, cmds.size()));
        }
        for (String key : cmds) {
            Command cmd = commandMap.get(key);
            msg += "  " + getShortDescription(sender, cmd);
        }
        if (realPage > 0 && maxPage > realPage) {
            msg += "\u00a77Use \u00a73/" + getName() + " ? " + (realPage+1) + " \u00a77 to display next page\n";
        } else if (realPage > 0 && maxPage == realPage) {
            msg += "\u00a77Use \u00a73/" + getName() + " ? " + (realPage-1) + " \u00a77 to display previous page\n";
        }
        sender.sendMessage(msg.split("\n"));
    }

    private void showUsage(CommandSender sender, String arg) {
        String cmdName = arg.toLowerCase();
        Command cmd = aliasMap.get(cmdName);
        if (cmd != null && hasAccess(cmd, sender)) {
            String msg = I18nUtil.tr("\u00a77Usage: {0}", name) + " \u00a7e";
            msg += getShortDescription(sender, cmd);
            if (cmd.getUsage() != null && !cmd.getUsage().isEmpty()) {
                msg += "\u00a77" + cmd.getUsage();
            }
            sender.sendMessage(msg.split("\n"));
        } else if (cmdName.matches("[0-9]+")) {
            showUsage(sender, Integer.parseInt(cmdName));
        } else {
            List<String> cmds = filter(aliasMap.keySet(), cmdName);
            if (cmds.isEmpty()) {
                showUsage(sender, 1);
            } else {
                String msg = I18nUtil.tr("\u00a77Usage: {0}", getShortDescription(sender, this));
                Collections.sort(cmds);
                for (String key : cmds) {
                    Command scmd = commandMap.get(key);
                    if (scmd != null) {
                        msg += "  " + getShortDescription(sender, scmd);
                    }
                }
                sender.sendMessage(msg.split("\n"));
            }
        }
    }

    private String getShortDescription(CommandSender sender, Command cmd) {
        String msg = "\u00a73" + cmd.getName();
        String[] aliases = cmd.getAliases();
        if (aliases.length > 1) {
            msg += "\u00a77";
            for (int i = 1; i < aliases.length; i++) {
                msg += " | " + aliases[i];
            }
        }
        msg += "\u00a7a";
        for (String param : cmd.getParams()) {
            if (param.startsWith("?")) {
                msg += " [" + param.substring(1) + "]";
            } else {
                msg += " <" + param + ">";
            }
        }
        if (cmd instanceof CompositeCommand) {
            msg += " [command|help]";
        }
        msg += "\u00a77 - \u00a7e";
        msg += cmd.getDescription();
        if (sender.isOp() && cmd.getPermission() != null) {
            msg += " \u00a7c(" + cmd.getPermission() + ")";
        }
        msg += "\n";
        return msg;
    }

    public boolean hasAccess(Command cmd, CommandSender sender) {
        return cmd != null && (cmd.getPermission() == null || sender.hasPermission(cmd.getPermission()));
    }

    @Override
    protected List<String> getTabList(CommandSender commandSender, String term) {
        ArrayList<String> strings = new ArrayList<>();
        for (Command cmd : commandMap.values()) {
            if (hasAccess(cmd, commandSender)) {
                strings.addAll(Arrays.asList(cmd.getAliases()));
            }
        }
        strings.add("help");
        return strings;
    }

    protected TabCompleter getTabCompleter(Command cmd, int argNum) {
        argNum = argNum >= 0 ? argNum : 0;
        if (cmd.getParams().length > argNum) {
            String paramName = cmd.getParams()[argNum];
            if (paramName != null && paramName.startsWith("?")) {
                paramName = paramName.substring(1);
            }
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
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if (args.length <=  params.length && args.length > 0) {
            TabCompleter tab = getTabCompleter(this, args.length-1);
            if (tab != null && tab != this) {
                return tab.onTabComplete(sender, command, alias, args);
            } else if (tab == this) {
                return getTabList(sender, args[args.length-1]);
            }
        } else if (args.length > params.length+1) { // Sub-commands
            String cmdName = args[params.length].toLowerCase();
            Command cmd = aliasMap.get(cmdName);
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
    public CompositeCommand getParent() {
        return parent;
    }

    @Override
    public void setParent(CompositeCommand parent) {
        this.parent = parent;
    }

    public List<Command> getChildren() {
        ArrayList<Command> list = new ArrayList<>(commandMap.values());
        Collections.sort(list, new CommandComparator());
        return Collections.unmodifiableList(list);
    }

    @Override
    public void accept(CommandVisitor visitor) {
        if (visitor != null) {
            visitor.visit(this);
            for (Command child : getChildren()) {
                child.accept(visitor);
            }
        }
    }
}
