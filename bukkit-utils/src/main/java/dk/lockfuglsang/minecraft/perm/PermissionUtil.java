package dk.lockfuglsang.minecraft.perm;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 * Ensures even bad SuperPerm plugins works with * perms.
 */
public enum PermissionUtil {;
    public static boolean hasPermission(CommandSender sender, String perm) {
        // This is ONLY needed, because some shitty perm-systems don't understand the .* perm.
        if (sender.isOp() || sender instanceof ConsoleCommandSender || sender.hasPermission(perm)) {
            return true;
        } else if (sender.hasPermission("-" + perm)) {
            return false;
        }
        String p = perm;
        if (perm.endsWith(".*")) {
            p = perm.substring(0, perm.length() - 2);
        }
        if (p.contains(".")) {
            return hasPermission(sender, p.substring(0, p.lastIndexOf(".")) + ".*");
        }
        return false;
    }
}
