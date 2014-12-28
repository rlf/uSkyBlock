package us.talabrek.ultimateskyblock.admin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import us.talabrek.ultimateskyblock.challenge.ChallengeCompletion;
import us.talabrek.ultimateskyblock.command.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.command.completion.AbstractTabCompleter;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Arrays;
import java.util.List;

/**
 * The challenge admin command.
 */
public class AdminChallengeCommand extends AbstractUSBCommand implements TabCompleter {

    public AdminChallengeCommand() {
        super("challenge", "usb.mod.challenges",
                "", "\u00a7a<player> \u00a77- Manage challenges for a player",
            "  \u00a7bcomplete <challenge> \u00a77- completes the challenge for the player\n"
            +"  \u00a7breset <challenge> \u00a77- resets the challenge for the player\n"
            +"  \u00a7bresetall \u00a77- resets all challenges for the player");
    }

    @Override
    public boolean execute(CommandSender sender, String... args) {
        String playerName = args.length > 0 ? args[0] : "";
        String cmd = args.length > 1 ? args[1] : "";
        String challenge = args.length > 2 ? args[2] : "";
        if (playerName.isEmpty()) {
            return false;
        }
        PlayerInfo pi = uSkyBlock.getInstance().getPlayerInfo(playerName);
        if (pi == null || !pi.getHasIsland()) {
            sender.sendMessage(ChatColor.RED + "Error: Invalid Player (check spelling)");
            return false;
        }
        if (cmd.matches("(?iu)complete|reset")) {
            if (challenge.isEmpty()) {
                return false;
            }
            ChallengeCompletion completion = pi.getChallenge(challenge);
            if (completion == null) {
                sender.sendMessage(ChatColor.RED + "Invalid challenge '" + challenge + "'");
                return false;
            }
            if (cmd.equalsIgnoreCase("complete")) {
                if (completion.getTimesCompleted() > 0) {
                    sender.sendMessage(ChatColor.RED + "Challenge has already been completed");
                } else {
                    pi.completeChallenge(challenge);
                    pi.save();
                    sender.sendMessage(ChatColor.YELLOW + "challenge: " + challenge + " has been completed for " + playerName);
                }
            } else { // reset
                if (completion.getTimesCompleted() == 0) {
                    sender.sendMessage(ChatColor.RED + "Challenge has never been completed");
                } else {
                    pi.resetChallenge(challenge);
                    pi.save();
                    sender.sendMessage(ChatColor.YELLOW + "challenge: " + challenge + " has been reset for " + playerName);
                }
            }
            return true;
        } else if (cmd.matches("(?iu)resetall")) {
            pi.resetAllChallenges();
            pi.save();
            sender.sendMessage(ChatColor.YELLOW + playerName + " has had all challenges reset.");
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String alias, String[] args) {
        String subCmd = args.length > 1 ? args[1] : "";
        if (args.length <= 1) {
            return playerTabCompleter.onTabComplete(commandSender, command, alias, args);
        } else if (args.length == 2) {
            return AbstractTabCompleter.filter(Arrays.asList("complete", "reset", "resetall"), subCmd);
        } else if (args.length > 2 && subCmd.matches("(?iu)reset|complete")) {
            return challengeCompleter.onTabComplete(commandSender, command, alias, args);
        }
        return null;
    }
}
