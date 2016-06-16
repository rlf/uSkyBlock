package dk.lockfuglsang.minecraft.command;

import org.bukkit.command.CommandExecutor;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Simple visitor for generating plain-text documentation of an Command-hierarchy.
 */
public class PlainTextCommandVisitor implements CommandVisitor {
    private final List<Row> rows = new ArrayList<>();
    public PlainTextCommandVisitor() {
    }

    public void writeTo(PrintStream out) {
        int[] colWidths = new int[3];
        for (Row row : rows) {
            if (row != null) {
                if (row.getCommand().length() > colWidths[0]) {
                    colWidths[0] = row.getCommand().length();
                }
                if (row.getPermission().length() > colWidths[1]) {
                    colWidths[1] = row.getPermission().length();
                }
                if (row.getDescription().length() > colWidths[2]) {
                    colWidths[2] = row.getDescription().length();
                }
            }
        }
        colWidths[0]++; // make room for the '/'
        String rowFormat = "";
        String separator = "";
        for (int i = 0; i < colWidths.length; i++) {
            if (i != 0) {
                rowFormat += " | ";
                separator += "-+-";
            }
            rowFormat += "%-" + colWidths[i] + "s";
            separator += String.format("%" + colWidths[i] + "s", "").replaceAll(" ", "-");
        }
        out.println(String.format(rowFormat, tr("Command"), tr("Permission"), tr("Description")));
        for (Row row : rows) {
            if (row == null) {
                out.println(separator);
            } else {
                out.println(String.format(rowFormat, "/" + row.getCommand(), row.getPermission(), row.getDescription()));
            }
        }
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

    private static class Row {
        private final String command;
        private final String description;
        private final String permission;

        public Row(String command, String description, String permission) {
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
