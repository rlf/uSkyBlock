package dk.lockfuglsang.minecraft.command;

import org.bukkit.command.CommandSender;

/**
 * Static singleton manager for controlling common requirements.
 */
public enum CommandManager {;
    private static RequirementChecker checker;

    public static void registerRequirements(RequirementChecker reqs) {
        checker = reqs;
    }

    public static boolean isRequirementsMet(CommandSender sender, Command command) {
        if (checker != null) {
            return checker.isRequirementsMet(sender, command);
        }
        return true;
    }

    public interface RequirementChecker {
        /**
         * Checks whether the requirements for the command has been met.
         * @param sender A sender to send detailed feedback to.
         * @return <code>true</code> iff the command can proceed.
         */
        boolean isRequirementsMet(CommandSender sender, Command command);
    }
}
