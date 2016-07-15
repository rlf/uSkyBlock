package dk.lockfuglsang.minecraft.command;

import org.bukkit.command.CommandExecutor;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Generates a yml-file following the syntax define in http://wiki.bukkit.org/Plugin_YAML
 * @since 1.8
 */
public class PluginYamlCommandVisitor implements DocumentWriter {
    List<Command> topLevel = new ArrayList<>();
    PermissionNode rootNode;

    PluginYamlCommandVisitor() {
        rootNode = new PermissionNode("");
    }

    public void writeTo(PrintStream out) {
        out.println("commands:");
        for (Command cmd : topLevel) {
            out.println("  " + cmd.getName() + ":");
            out.println("    description: " + cmd.getDescription());
            if (cmd.getAliases().length > 1) {
                String[] copy = new String[cmd.getAliases().length-1];
                System.arraycopy(cmd.getAliases(), 1, copy, 0, copy.length);
                out.println("    aliases: " + Arrays.toString(copy));
            }
            if (cmd.getPermission() != null && !cmd.getPermission().isEmpty()) {
                out.println("    permission: " + cmd.getPermission());
            }
        }
        out.println("permissions:");
        out.println("  #");
        out.println("  # Permission Groups");
        out.println("  # =================");
        for (PermissionNode node : rootNode.getRoots()) {
            if (!node.permission.isEmpty()) {
                out.println("  " + node.permission + ".*:");
                out.println("    children:");
                for (String p : node.getChildPermissions()) {
                    out.println("      " + p + ": true");
                }
                out.println();
            }
        }
        out.println("  #");
        out.println("  # Permission Descriptions");
        out.println("  # =======================");
        for (PermissionNode node : rootNode.getLeafs()) {
            if (!node.permission.isEmpty()) {
                out.println("  " + node.permission + ":");
                String desc = node.getDescription();
                if (desc != null) {
                    out.println("    description: " + desc);
                }
                out.println();
            }
        }
    }

    private static String getCmdDescription(Command cmd) {
        return "/" + getCommandPath(cmd) + " - " + cmd.getDescription();
    }

    private static String getCommandPath(Command cmd) {
        String path = cmd.getParent() != null ? getCommandPath(cmd.getParent()) + " " : "";
        return path + cmd.getName();
    }

    @Override
    public void visit(Command cmd) {
        if (cmd instanceof CommandExecutor) {
            topLevel.add(cmd);
        }
        String permission = getPermission(cmd);
        rootNode.add(permission, cmd);
        for (Map.Entry<String,String> entry : cmd.getFeaturePermissions().entrySet()) {
            rootNode.add(entry.getKey(), entry.getValue());
        }
    }

    private String getPermission(Command cmd) {
        if (cmd.getPermission() == null && cmd.getParent() != null) {
            return getPermission(cmd.getParent());
        } else if (cmd.getPermission() != null) {
            return cmd.getPermission();
        }
        return null;
    }

    static class PermissionNode implements Comparable<PermissionNode> {
        String permission;
        String description; // Used for feature perms
        List<PermissionNode> children = new ArrayList<>();
        List<Command> commands = new ArrayList<>();

        PermissionNode(String permission) {
            this.permission = permission;
        }

        PermissionNode(String permission, Command cmd) {
            this.permission = permission;
            commands.add(cmd);
        }

        void add(String perm, Command cmd) {
            if (permission.equalsIgnoreCase(perm) || perm == null) {
                commands.add(cmd);
            } else if (perm.startsWith(permission)) {
                for (PermissionNode child : children) {
                    if (isSub(child, perm)) {
                        child.add(perm, cmd);
                        return;
                    }
                }
                String[] parts = !permission.isEmpty()
                        ? perm.substring(permission.length() + 1).split("\\.")
                        : perm.split("\\.");
                String parentPerm = !permission.isEmpty()
                        ? permission + "." + parts[0]
                        : parts[0];
                PermissionNode n = new PermissionNode(parentPerm);
                children.add(n);
                for (int i = 1; i < parts.length; i++) {
                    parentPerm += "." + parts[i];
                    PermissionNode newNode = new PermissionNode(parentPerm);
                    n.children.add(newNode);
                    n = newNode;
                }
                n.commands.add(cmd);
            }
        }

        private boolean isSub(PermissionNode child, String perm) {
            return perm.startsWith(child.permission)
                    && (child.permission.equalsIgnoreCase(perm) || (perm.length() > child.permission.length()
                    && perm.charAt(child.permission.length()) == '.')
                    );
        }

        void add(String perm, String description) {
            if (permission.equalsIgnoreCase(perm) || perm == null) {
                if (this.description == null) {
                    this.description = description;
                }
            } else if (perm.startsWith(permission)) {
                for (PermissionNode child : children) {
                    if (isSub(child, perm)) {
                        child.add(perm, description);
                        return;
                    }
                }
                String[] parts = !permission.isEmpty()
                        ? perm.substring(permission.length() + 1).split("\\.")
                        : perm.split("\\.");
                String parentPerm = !permission.isEmpty()
                        ? permission + "." + parts[0]
                        : parts[0];
                PermissionNode n = new PermissionNode(parentPerm);
                children.add(n);
                for (int i = 1; i < parts.length; i++) {
                    parentPerm += "." + parts[i];
                    PermissionNode newNode = new PermissionNode(parentPerm);
                    n.children.add(newNode);
                    n = newNode;
                }
                n.description = description;
            }
        }

        List<String> getChildPermissions() {
            List<String> perms = new ArrayList<>();
            for (PermissionNode child : children) {
                perms.add(child.permission);
                perms.addAll(child.getChildPermissions());
            }
            Collections.sort(perms);
            return perms;
        }

        void addRoots(List<PermissionNode> roots) {
            if (!children.isEmpty()) {
                roots.add(this);
                for (PermissionNode node : children) {
                    node.addRoots(roots);
                }
            }
        }

        List<PermissionNode> getRoots() {
            List<PermissionNode> roots = new ArrayList<>();
            addRoots(roots);
            Collections.sort(roots);
            return roots;
        }

        void addLeafs(List<PermissionNode> leafs) {
            if (children.isEmpty() || !commands.isEmpty()) {
                leafs.add(this);
            }
            for (PermissionNode node : children) {
                node.addLeafs(leafs);
            }
        }

        List<PermissionNode> getLeafs() {
            List<PermissionNode> leafs = new ArrayList<>();
            addLeafs(leafs);
            Collections.sort(leafs);
            return leafs;
        }

        String getDescription() {
            if (description != null) {
                return description;
            }
            if (commands.size() == 1) {
                return "Grants access to " + getCmdDescription(commands.get(0));
            } else if (commands.size() > 1) {
                String desc = "|\r\n";
                desc += "      Grants access to " + getCmdDescription(commands.get(0));
                for (int i = 1; i < commands.size(); i++) {
                    desc += "\r\n";
                    desc += "      " + getCmdDescription(commands.get(i));
                }
                return desc;
            }
            return null;
        }

        @Override
        public int compareTo(PermissionNode o) {
            return permission.compareTo(o.permission);
        }
    }
}
