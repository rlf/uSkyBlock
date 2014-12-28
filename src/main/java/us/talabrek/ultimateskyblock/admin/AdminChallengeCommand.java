package us.talabrek.ultimateskyblock.admin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import us.talabrek.ultimateskyblock.challenge.ChallengeCompletion;
import us.talabrek.ultimateskyblock.command.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.command.CompositeUSBCommand;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

/**
 * The challenge admin command.
 */
public class AdminChallengeCommand extends CompositeUSBCommand {

    private final uSkyBlock plugin;

    public AdminChallengeCommand(final uSkyBlock plugin, TabCompleter challengeCompleter) {
        super("challenge", "usb.mod.challenges", "player", "Manage challenges for a player");
        this.plugin = plugin;
        add(new ChallengeCommand("complete", null, "completes the challenge for the player") {
            @Override
            protected void doExecute(CommandSender sender, PlayerInfo playerInfo, ChallengeCompletion completion) {
                String challenge = completion.getName();
                if (completion.getTimesCompleted() > 0) {
                    sender.sendMessage(ChatColor.RED + "Challenge has already been completed");
                } else {
                    playerInfo.completeChallenge(challenge);
                    playerInfo.save();
                    sender.sendMessage(ChatColor.YELLOW + "challenge: " + challenge + " has been completed for " + playerInfo.getPlayerName());
                }
            }
        });
        add(new ChallengeCommand("reset", null, "resets the challenge for the player") {
            @Override
            protected void doExecute(CommandSender sender, PlayerInfo pi, ChallengeCompletion completion) {
                String challenge = completion.getName();
                String playerName = pi.getPlayerName();
                if (completion.getTimesCompleted() == 0) {
                    sender.sendMessage(ChatColor.RED + "Challenge has never been completed");
                } else {
                    pi.resetChallenge(challenge);
                    pi.save();
                    sender.sendMessage(ChatColor.YELLOW + "challenge: " + challenge + " has been reset for " + playerName);
                }
            }
        });
        add(new AbstractUSBCommand("resetall", null, "resets all challenges for the player") {
            @Override
            public boolean execute(CommandSender sender, Map<String, Object> data, String... args) {
                PlayerInfo playerInfo = (PlayerInfo) data.get("playerInfo");
                if (playerInfo != null) {
                    playerInfo.resetAllChallenges();
                    playerInfo.save();
                    sender.sendMessage(ChatColor.YELLOW + playerInfo.getPlayerName() + " has had all challenges reset.");
                    return true;
                }
                return false;
            }
        });
        addTab("challenge", challengeCompleter);
    }

    @Override
    public boolean execute(CommandSender sender, Map<String, Object> data, String... args) {
        if (args.length > 0) {
            PlayerInfo playerInfo = plugin.getPlayerInfo(args[0]);
            if (playerInfo != null) {
                data.put("playerInfo", playerInfo);
            }
        }
        return super.execute(sender, data, args);
    }

    private abstract class ChallengeCommand extends AbstractUSBCommand {
        public ChallengeCommand(String name, String permission, String description) {
            super(name, permission, "challenge", description);
        }
        protected abstract void doExecute(CommandSender sender, PlayerInfo playerInfo, ChallengeCompletion challenge);

        @Override
        public boolean execute(CommandSender sender, Map<String, Object> data, String... args) {
            PlayerInfo playerInfo = (PlayerInfo) data.get("playerInfo");
            if (playerInfo != null && args.length > 0) {
                ChallengeCompletion completion = playerInfo.getChallenge(args[0]);
                if (completion != null) {
                    doExecute(sender, playerInfo, completion);
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "No challenge named " + args[0] + " was found!");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "No player named " + data.get("player") + " was found!");
            }
            return false;
        }
    }
}
