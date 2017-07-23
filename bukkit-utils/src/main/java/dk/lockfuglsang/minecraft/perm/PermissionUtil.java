package dk.lockfuglsang.minecraft.perm;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 * Ensures even bad SuperPerm plugins works with * perms.
 */
public enum PermissionUtil {;

    /**
     * @deprecated Since 1.11 - use sender#hasPermission instead
     */
    @Deprecated
    public static boolean hasPermission(CommandSender sender, String perm) {
        return sender != null && sender.hasPermission(perm) || sender instanceof ConsoleCommandSender;
    }
}
