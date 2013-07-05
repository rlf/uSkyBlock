package us.talabrek.ultimateskyblock;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChallengesCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		if (!(sender instanceof Player)) { return false; }

		final Player player = sender.getServer().getPlayer(sender.getName());
		if (!Settings.challenges_allowChallenges) { return true; }
		if (!VaultHandler.checkPerk(player.getName(), "usb.island.challenges", player.getWorld()) && !player.isOp()) {
			player.sendMessage(ChatColor.RED + "You don't have access to this command!");
			return true;
		}
		if (!player.getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
			player.sendMessage(ChatColor.RED + "You can only submit challenges in the skyblock world!");
			return true;
		}

		if (split.length == 0) {
			int rankComplete = 0;
			sender.sendMessage(ChatColor.GOLD + Settings.challenges_ranks[0] + ": "
					+ uSkyBlock.getInstance().getChallengesFromRank(player, Settings.challenges_ranks[0]));
			for (int i = 1; i < Settings.challenges_ranks.length; i++) {
				rankComplete = uSkyBlock.getInstance().checkRankCompletion(player, Settings.challenges_ranks[i - 1]);
				if (rankComplete <= 0) {
					sender.sendMessage(ChatColor.GOLD + Settings.challenges_ranks[i] + ": "
							+ uSkyBlock.getInstance().getChallengesFromRank(player, Settings.challenges_ranks[i]));
				} else {
					sender.sendMessage(ChatColor.GOLD + Settings.challenges_ranks[i] + ChatColor.GRAY + ": Complete " + rankComplete
							+ " more " + Settings.challenges_ranks[i - 1] + " challenges to unlock this rank!");
				}
			}
			sender.sendMessage(ChatColor.YELLOW + "Use /c <name> to view information about a challenge.");
			sender.sendMessage(ChatColor.YELLOW + "Use /c complete <name> to attempt to complete that challenge.");
		} else if (split.length == 1) {
			if (split[0].equalsIgnoreCase("help") || split[0].equalsIgnoreCase("complete") || split[0].equalsIgnoreCase("c")) {
				sender.sendMessage(ChatColor.YELLOW + "Use /c <name> to view information about a challenge.");
				sender.sendMessage(ChatColor.YELLOW + "Use /c complete <name> to attempt to complete that challenge.");
				sender.sendMessage(ChatColor.YELLOW + "Challenges will have different colors depending on if they are:");
				sender.sendMessage(Settings.challenges_challengeColor.replace('&', '§') + "Incomplete "
						+ Settings.challenges_finishedColor.replace('&', '§') + "Completed(not repeatable) "
						+ Settings.challenges_repeatableColor.replace('&', '§') + "Completed(repeatable) ");
			} else if (uSkyBlock.getInstance().isRankAvailable(
					player,
					uSkyBlock.getInstance().getConfig()
							.getString("options.challenges.challengeList." + split[0].toLowerCase() + ".rankLevel"))) {
				sender.sendMessage(ChatColor.YELLOW + "Challenge Name: " + ChatColor.WHITE + split[0].toLowerCase());
				sender.sendMessage(ChatColor.YELLOW
						+ uSkyBlock
								.getInstance()
								.getConfig()
								.getString(
										new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
												.append(".description").toString()));
				if (uSkyBlock.getInstance().getConfig().getString("options.challenges.challengeList." + split[0].toLowerCase() + ".type")
						.equalsIgnoreCase("onPlayer")) {
					if (uSkyBlock.getInstance().getConfig()
							.getBoolean("options.challenges.challengeList." + split[0].toLowerCase() + ".takeItems")) {
						sender.sendMessage(ChatColor.RED + "You will lose all required items when you complete this challenge!");
					}
				} else if (uSkyBlock.getInstance().getConfig()
						.getString("options.challenges.challengeList." + split[0].toLowerCase() + ".type").equalsIgnoreCase("onIsland")) {
					sender.sendMessage(ChatColor.RED + "All required items must be placed on your island!");
				}

				if (Settings.challenges_ranks.length > 1) {
					sender.sendMessage(ChatColor.YELLOW
							+ "Rank: "
							+ ChatColor.WHITE
							+ uSkyBlock
									.getInstance()
									.getConfig()
									.getString(
											new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
													.append(".rankLevel").toString()));
				}
				if (uSkyBlock.getInstance().getActivePlayers().get(player.getName()).checkChallenge(split[0].toLowerCase())
						&& (!uSkyBlock.getInstance().getConfig()
								.getString("options.challenges.challengeList." + split[0].toLowerCase() + ".type")
								.equalsIgnoreCase("onPlayer") || !uSkyBlock.getInstance().getConfig()
								.getBoolean("options.challenges.challengeList." + split[0].toLowerCase() + ".repeatable"))) {
					sender.sendMessage(ChatColor.RED + "This Challenge is not repeatable!");
					return true;
				}
				if (Settings.challenges_enableEconomyPlugin && VaultHandler.econ != null) {
					if (uSkyBlock.getInstance().getActivePlayers().get(player.getName()).checkChallenge(split[0].toLowerCase())) {
						sender.sendMessage(ChatColor.YELLOW
								+ "Repeat reward(s): "
								+ ChatColor.WHITE
								+ uSkyBlock
										.getInstance()
										.getConfig()
										.getString(
												new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
														.append(".repeatRewardText").toString()).replace('&', '§'));
						player.sendMessage(ChatColor.YELLOW
								+ "Repeat exp reward: "
								+ ChatColor.WHITE
								+ uSkyBlock
										.getInstance()
										.getConfig()
										.getInt(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
												.append(".repeatXpReward").toString()));
						sender.sendMessage(ChatColor.YELLOW
								+ "Repeat currency reward: "
								+ ChatColor.WHITE
								+ uSkyBlock
										.getInstance()
										.getConfig()
										.getInt(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
												.append(".repeatCurrencyReward").toString()) + " " + VaultHandler.econ.currencyNamePlural());
					} else {
						sender.sendMessage(ChatColor.YELLOW
								+ "Reward(s): "
								+ ChatColor.WHITE
								+ uSkyBlock
										.getInstance()
										.getConfig()
										.getString(
												new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
														.append(".rewardText").toString()).replace('&', '§'));
						player.sendMessage(ChatColor.YELLOW
								+ "Exp reward: "
								+ ChatColor.WHITE
								+ uSkyBlock
										.getInstance()
										.getConfig()
										.getInt(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
												.append(".xpReward").toString()));
						sender.sendMessage(ChatColor.YELLOW
								+ "Currency reward: "
								+ ChatColor.WHITE
								+ uSkyBlock
										.getInstance()
										.getConfig()
										.getInt(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
												.append(".currencyReward").toString()) + " " + VaultHandler.econ.currencyNamePlural());
					}

				} else if (uSkyBlock.getInstance().getActivePlayers().get(player.getName()).checkChallenge(split[0].toLowerCase())) {
					sender.sendMessage(ChatColor.YELLOW
							+ "Repeat reward(s): "
							+ ChatColor.WHITE
							+ uSkyBlock
									.getInstance()
									.getConfig()
									.getString(
											new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
													.append(".repeatRewardText").toString()).replace('&', '§'));
					player.sendMessage(ChatColor.YELLOW
							+ "Repeat exp reward: "
							+ ChatColor.WHITE
							+ uSkyBlock
									.getInstance()
									.getConfig()
									.getInt(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
											.append(".repeatXpReward").toString()));
				} else {
					sender.sendMessage(ChatColor.YELLOW
							+ "Reward(s): "
							+ ChatColor.WHITE
							+ uSkyBlock
									.getInstance()
									.getConfig()
									.getString(
											new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
													.append(".rewardText").toString()).replace('&', '§'));
					player.sendMessage(ChatColor.YELLOW
							+ "Exp reward: "
							+ ChatColor.WHITE
							+ uSkyBlock
									.getInstance()
									.getConfig()
									.getInt(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
											.append(".xpReward").toString()));
				}

				sender.sendMessage(ChatColor.YELLOW + "To complete this challenge, use " + ChatColor.WHITE + "/c c "
						+ split[0].toLowerCase());
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid challenge name! Use /c help for more information");
			}
		} else if (split.length == 2) {
			if (split[0].equalsIgnoreCase("complete") || split[0].equalsIgnoreCase("c")) {
				if (uSkyBlock.getInstance().checkIfCanCompleteChallenge(player, split[1].toLowerCase())) {
					uSkyBlock.getInstance().giveReward(player, split[1].toLowerCase());
				}
			}
		}
		return true;
	}
}