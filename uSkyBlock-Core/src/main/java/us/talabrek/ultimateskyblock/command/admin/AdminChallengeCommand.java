package us.talabrek.ultimateskyblock.command.admin;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.minecraft.command.CompositeCommand;
import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import us.talabrek.ultimateskyblock.challenge.Challenge;
import us.talabrek.ultimateskyblock.challenge.ChallengeCompletion;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import dk.lockfuglsang.minecraft.util.FormatUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The challenge admin command.
 */
public class AdminChallengeCommand extends CompositeCommand {

    private final uSkyBlock plugin;

    public AdminChallengeCommand(final uSkyBlock plugin, TabCompleter challengeCompleter) {
        super("challenge|ch", "usb.mod.challenges", "player", I18nUtil.tr("Manage challenges for a player"));
        this.plugin = plugin;
        add(new ChallengeCommand("complete", null, "completes the challenge for the player") {
            @Override
            protected void doExecute(CommandSender sender, PlayerInfo playerInfo, ChallengeCompletion completion) {
                String challenge = completion.getName();
                completeChallenge(sender, playerInfo, challenge);
            }
        });
        add(new ChallengeCommand("reset", null, I18nUtil.tr("resets the challenge for the player")) {
            @Override
            protected void doExecute(CommandSender sender, PlayerInfo pi, ChallengeCompletion completion) {
                String challenge = completion.getName();
                String playerName = pi.getPlayerName();
                if (completion.getTimesCompleted() == 0) {
                    sender.sendMessage(I18nUtil.tr("\u00a74Challenge has never been completed"));
                } else {
                    pi.resetChallenge(challenge);
                    pi.save();
                    sender.sendMessage(I18nUtil.tr("\u00a7echallenge: {0} has been reset for {1}", challenge, playerName));
                }
            }
        });
        add(new AbstractCommand("resetall", null, I18nUtil.tr("resets all challenges for the player")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                PlayerInfo playerInfo = (PlayerInfo) data.get("playerInfo");
                if (playerInfo != null) {
                    playerInfo.resetAllChallenges();
                    playerInfo.save();
                    sender.sendMessage(I18nUtil.tr("\u00a7e{0} has had all challenges reset.", playerInfo.getPlayerName()));
                    return true;
                }
                return false;
            }
        });
        add(new RankCommand("rank", null, I18nUtil.tr("complete all challenges in the rank")) {
            @Override
            protected void doExecute(CommandSender sender, PlayerInfo playerInfo, String rankName, List<Challenge> challenges) {
                for (Challenge c : challenges) {
                    String challengeName = c.getName();
                    completeChallenge(sender, playerInfo, challengeName);
                }
            }
        });
        addTab("challenge", challengeCompleter);
    }

    private void completeChallenge(CommandSender sender, PlayerInfo playerInfo, String challengeName) {
        ChallengeCompletion completion = playerInfo.getChallenge(challengeName);
        if (completion.getTimesCompleted() > 0) {
            sender.sendMessage(I18nUtil.tr("\u00a74Challenge {0} has already been completed", challengeName));
        } else {
            playerInfo.completeChallenge(challengeName, true);
            playerInfo.save();
            sender.sendMessage(I18nUtil.tr("\u00a7eChallenge {0} has been completed for {1}", challengeName, playerInfo.getPlayerName()));
        }
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        if (args.length > 0) {
            PlayerInfo playerInfo = plugin.getPlayerInfo(args[0]);
            if (playerInfo != null) {
                data.put("playerInfo", playerInfo);
            }
        }
        return super.execute(sender, alias, data, args);
    }

    private abstract class ChallengeCommand extends AbstractCommand {
        public ChallengeCommand(String name, String permission, String description) {
            super(name, permission, "challenge", description);
        }
        protected abstract void doExecute(CommandSender sender, PlayerInfo playerInfo, ChallengeCompletion challenge);

        @Override
        public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
            PlayerInfo playerInfo = (PlayerInfo) data.get("playerInfo");
            if (playerInfo != null && args.length > 0) {
                ChallengeCompletion completion = playerInfo.getChallenge(args[0]);
                if (completion != null) {
                    doExecute(sender, playerInfo, completion);
                    return true;
                } else {
                    sender.sendMessage(I18nUtil.tr("\u00a74No challenge named {0} was found!", args[0]));
                }
            } else {
                sender.sendMessage(I18nUtil.tr("\u00a74No player named {0} was found!", data.get("player")));
            }
            return false;
        }
    }

    private abstract class RankCommand extends AbstractCommand {
        public RankCommand(String name, String permission, String description) {
            super(name, permission, "rank", description);
        }
        protected abstract void doExecute(CommandSender sender, PlayerInfo playerInfo, String rankName, List<Challenge> challenge);

        @Override
        public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
            PlayerInfo playerInfo = (PlayerInfo) data.get("playerInfo");
            if (playerInfo != null && args.length > 0) {
                String rankName = FormatUtil.join(Arrays.asList(args), " ");
                List<Challenge> challenges = plugin.getChallengeLogic().getChallengesForRank(rankName);
                if (challenges == null || challenges.isEmpty()) {
                    sender.sendMessage(I18nUtil.tr("\u00a74No rank named {0} was found!", rankName));
                } else {
                    doExecute(sender, playerInfo, rankName, challenges);
                    return true;
                }
            } else {
                sender.sendMessage(I18nUtil.tr("\u00a74No player named {0} was found!", data.get("player")));
            }
            return false;
        }
    }
}
