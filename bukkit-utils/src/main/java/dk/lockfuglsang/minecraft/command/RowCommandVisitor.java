package dk.lockfuglsang.minecraft.command;

import org.bukkit.command.CommandExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * A visitor that simply gathers the complete command-hierarchy into a list of rows.
 * @since 1.8
 */
class RowCommandVisitor implements CommandVisitor {
    private final List<Row> rows = new ArrayList<>();

    public List<Row> getRows() {
        return rows;
    }

    @Override
    public void visit(Command cmd) {
        if (cmd instanceof CommandExecutor) {
            rows.add(null); // separator
        }
        String commandPath = getCommandPath(cmd);
        String alias = getAliases(cmd);
        if (!alias.isEmpty()) {
            String shortCmd = getShortestCmd(cmd);
            int ix = commandPath.lastIndexOf(" " + shortCmd) + 1;
            commandPath = commandPath.substring(0, ix) + cmd.getName() + alias + commandPath.substring(ix+shortCmd.length());
        }
        rows.add(new Row(commandPath, cmd.getDescription(), cmd.getPermission()));
        List<String> extraPerms = new ArrayList<>(cmd.getFeaturePermissions().keySet());
        Collections.sort(extraPerms);
        for (String p : extraPerms) {
            rows.add(new Row(null, cmd.getFeaturePermissions().get(p), p));
        }
    }

    private String getCommandPath(Command cmd) {
        String path = cmd.getParent() != null ? getCommandPath(cmd.getParent()) + " " : "";
        return path + getShortestCmd(cmd) + getParams(cmd);
    }

    private String getShortestCmd(Command cmd) {
        String cmdName = cmd.getName();
        for (String alias : cmd.getAliases()) {
            if (alias.length() < cmdName.length()) {
                cmdName = alias;
            }
        }
        return cmdName;
    }

    private String getAliases(Command cmd) {
        String aliases = "";
        for (int i = 1; i < cmd.getAliases().length; i++) {
            aliases += "|" + cmd.getAliases()[i];
        }
        return aliases;
    }

    private String getParams(Command cmd) {
        String msg = "";
        for (String param : cmd.getParams()) {
            if (param.startsWith("?")) {
                msg += tr(" [{0}]", param.substring(1));
            } else {
                msg += tr(" <{0}>", param);
            }
        }
        return msg;
    }

    static class Row {
        private final String command;
        private final String description;
        private final String permission;

        Row(String command, String description, String permission) {
            this.command = command;
            this.description = description;
            this.permission = permission;
        }

        public String getCommand() {
            return command != null ? command : "";
        }

        public String getDescription() {
            return description != null ? description : "";
        }

        public String getPermission() {
            return permission != null ? permission : "";
        }

    }
}
