package us.talabrek.ultimateskyblock.command;

import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class ChallengesCommand implements CommandExecutor {
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] split) {
        if (!(sender instanceof Player)) {
            return false;
        }
        final Player player = sender.getServer().getPlayer(sender.getName());
        if (!Settings.challenges_allowChallenges) {
            return true;
        }
        if (!VaultHandler.checkPerk(player.getName(), "usb.island.challenges", player.getWorld()) && !player.isOp()) {
            player.sendMessage(ChatColor.RED + "You don't have access to this command!");
            return true;
        }
        if (!player.getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
            player.sendMessage(ChatColor.RED + "You can only submit challenges in the skyblock world!");
            return true;
        }
        if (split.length == 0) {
            uSkyBlock.getInstance().getMenuHandler().showMenu(player, "§9Challenge Menu");
            //player.openInventory(uSkyBlock.getInstance().getMenu().displayChallengeGUI(player));
        } else if (split.length == 1) {
            if (split[0].equalsIgnoreCase("help") || split[0].equalsIgnoreCase("complete") || split[0].equalsIgnoreCase("c")) {
                sender.sendMessage(ChatColor.YELLOW + "Use /c <name> to view information about a challenge.");
                sender.sendMessage(ChatColor.YELLOW + "Use /c complete <name> to attempt to complete that challenge.");
                sender.sendMessage(ChatColor.YELLOW + "Challenges will have different colors depending on if they are:");
                sender.sendMessage(Settings.challenges_challengeColor.replace('&', '\u00a7') + "Incomplete " + Settings.challenges_finishedColor.replace('&', '\u00a7') + "Completed(not repeatable) " + Settings.challenges_repeatableColor.replace('&', '\u00a7') + "Completed(repeatable) ");
            } else if (uSkyBlock.getInstance().isRankAvailable(player, uSkyBlock.getInstance().getConfig().getString("options.challenges.challengeList." + split[0].toLowerCase() + ".rankLevel"))) {
                sender.sendMessage(ChatColor.YELLOW + "Challenge Name: " + ChatColor.WHITE + split[0].toLowerCase());
                sender.sendMessage(ChatColor.YELLOW + uSkyBlock.getInstance().getConfig().getString("options.challenges.challengeList." + split[0].toLowerCase() + ".description"));
                if (uSkyBlock.getInstance().getConfig().getString("options.challenges.challengeList." + split[0].toLowerCase() + ".type").equalsIgnoreCase("onPlayer")) {
                    if (uSkyBlock.getInstance().getConfig().getBoolean("options.challenges.challengeList." + split[0].toLowerCase() + ".takeItems")) {
                        sender.sendMessage(ChatColor.RED + "You will lose all required items when you complete this challenge!");
                    }
                } else if (uSkyBlock.getInstance().getConfig().getString("options.challenges.challengeList." + split[0].toLowerCase() + ".type").equalsIgnoreCase("onIsland")) {
                    sender.sendMessage(ChatColor.RED + "All required items must be placed on your island!");
                }
                if (Settings.challenges_ranks.length > 1) {
                    sender.sendMessage(ChatColor.YELLOW + "Rank: " + ChatColor.WHITE + uSkyBlock.getInstance().getConfig().getString("options.challenges.challengeList." + split[0].toLowerCase() + ".rankLevel"));
                }
                if (uSkyBlock.getInstance().getActivePlayers().get(player.getName()).checkChallenge(split[0].toLowerCase()) > 0 && (!uSkyBlock.getInstance().getConfig().getString("options.challenges.challengeList." + split[0].toLowerCase() + ".type").equalsIgnoreCase("onPlayer") || !uSkyBlock.getInstance().getConfig().getBoolean("options.challenges.challengeList." + split[0].toLowerCase() + ".repeatable"))) {
                    sender.sendMessage(ChatColor.RED + "This Challenge is not repeatable!");
                    return true;
                }
                if (Settings.challenges_enableEconomyPlugin && VaultHandler.hasEcon()) {
                    if (uSkyBlock.getInstance().getActivePlayers().get(player.getName()).checkChallenge(split[0].toLowerCase()) > 0) {
                        sender.sendMessage(ChatColor.YELLOW + "Repeat reward(s): " + ChatColor.WHITE + uSkyBlock.getInstance().getConfig().getString("options.challenges.challengeList." + split[0].toLowerCase() + ".repeatRewardText").replace('&', '\u00a7'));
                        player.sendMessage(ChatColor.YELLOW + "Repeat exp reward: " + ChatColor.WHITE + uSkyBlock.getInstance().getConfig().getInt("options.challenges.challengeList." + split[0].toLowerCase() + ".repeatXpReward"));
                        sender.sendMessage(ChatColor.YELLOW + "Repeat currency reward: " + ChatColor.WHITE + uSkyBlock.getInstance().getConfig().getInt("options.challenges.challengeList." + split[0].toLowerCase() + ".repeatCurrencyReward") + " " + VaultHandler.getEcon().currencyNamePlural());
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "Reward(s): " + ChatColor.WHITE + uSkyBlock.getInstance().getConfig().getString("options.challenges.challengeList." + split[0].toLowerCase() + ".rewardText").replace('&', '\u00a7'));
                        player.sendMessage(ChatColor.YELLOW + "Exp reward: " + ChatColor.WHITE + uSkyBlock.getInstance().getConfig().getInt("options.challenges.challengeList." + split[0].toLowerCase() + ".xpReward"));
                        sender.sendMessage(ChatColor.YELLOW + "Currency reward: " + ChatColor.WHITE + uSkyBlock.getInstance().getConfig().getInt("options.challenges.challengeList." + split[0].toLowerCase() + ".currencyReward") + " " + VaultHandler.getEcon().currencyNamePlural());
                    }
                } else if (uSkyBlock.getInstance().getActivePlayers().get(player.getName()).checkChallenge(split[0].toLowerCase()) > 0) {
                    sender.sendMessage(ChatColor.YELLOW + "Repeat reward(s): " + ChatColor.WHITE + uSkyBlock.getInstance().getConfig().getString("options.challenges.challengeList." + split[0].toLowerCase() + ".repeatRewardText").replace('&', '\u00a7'));
                    player.sendMessage(ChatColor.YELLOW + "Repeat exp reward: " + ChatColor.WHITE + uSkyBlock.getInstance().getConfig().getInt("options.challenges.challengeList." + split[0].toLowerCase() + ".repeatXpReward"));
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "Reward(s): " + ChatColor.WHITE + uSkyBlock.getInstance().getConfig().getString("options.challenges.challengeList." + split[0].toLowerCase() + ".rewardText").replace('&', '\u00a7'));
                    player.sendMessage(ChatColor.YELLOW + "Exp reward: " + ChatColor.WHITE + uSkyBlock.getInstance().getConfig().getInt("options.challenges.challengeList." + split[0].toLowerCase() + ".xpReward"));
                }
                sender.sendMessage(ChatColor.YELLOW + "To complete this challenge, use " + ChatColor.WHITE + "/c c " + split[0].toLowerCase());
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid challenge name! Use /c help for more information");
            }
        } else if (split.length == 2 && (split[0].equalsIgnoreCase("complete") || split[0].equalsIgnoreCase("c")) && uSkyBlock.getInstance().checkIfCanCompleteChallenge(player, split[1].toLowerCase())) {
            uSkyBlock.getInstance().giveReward(player, split[1].toLowerCase());
        }
        return true;
    }
}
